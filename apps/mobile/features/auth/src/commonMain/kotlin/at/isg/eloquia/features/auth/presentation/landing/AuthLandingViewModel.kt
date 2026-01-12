package at.isg.eloquia.features.auth.presentation.landing

import at.isg.eloquia.core.domain.auth.model.AuthResult
import at.isg.eloquia.core.domain.auth.model.toUserMessage
import at.isg.eloquia.core.domain.auth.usecase.PatientLoginUseCase
import at.isg.eloquia.core.domain.auth.usecase.PatientRegisterUseCase
import at.isg.eloquia.core.domain.logging.AppLog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode {
    SignIn,
    Register,
}

data class AuthLandingForm(
    val mode: AuthMode = AuthMode.SignIn,
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
)

data class AuthLandingFieldErrors(
    val displayName: String? = null,
    val email: String? = null,
    val password: String? = null,
)

sealed interface AuthLandingState {
    val form: AuthLandingForm
    val errorMessage: String?

    val fieldErrors: AuthLandingFieldErrors

    val displayNameError: String?
        get() = fieldErrors.displayName

    val emailError: String?
        get() = fieldErrors.email

    val passwordError: String?
        get() = fieldErrors.password

    data class Editing(
        override val form: AuthLandingForm,
        override val errorMessage: String? = null,
        override val fieldErrors: AuthLandingFieldErrors = AuthLandingFieldErrors(),
    ) : AuthLandingState

    data class Submitting(
        override val form: AuthLandingForm,
    ) : AuthLandingState {
        override val errorMessage: String? = null

        override val fieldErrors: AuthLandingFieldErrors = AuthLandingFieldErrors()
    }

    data object Success : AuthLandingState {
        override val form: AuthLandingForm = AuthLandingForm()
        override val errorMessage: String? = null

        override val fieldErrors: AuthLandingFieldErrors = AuthLandingFieldErrors()
    }
}

class AuthLandingViewModel(
    private val patientLogin: PatientLoginUseCase,
    private val patientRegister: PatientRegisterUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<AuthLandingState>(AuthLandingState.Editing(AuthLandingForm()))
    val state: StateFlow<AuthLandingState> = _state

    private companion object {
        const val TAG = "Auth"
    }

    fun updateEmail(email: String) {
        _state.update { it.withForm { copy(email = email) }.clearFieldError(email = true).clearError() }
    }

    fun updatePassword(password: String) {
        _state.update { it.withForm { copy(password = password) }.clearFieldError(password = true).clearError() }
    }

    fun updateDisplayName(displayName: String) {
        _state.update { it.withForm { copy(displayName = displayName) }.clearFieldError(displayName = true).clearError() }
    }

    fun toggleMode() {
        _state.update {
            val next = when (it.form.mode) {
                AuthMode.SignIn -> AuthMode.Register
                AuthMode.Register -> AuthMode.SignIn
            }
            AppLog.i(TAG, "Switch mode ${it.form.mode} -> $next")
            AuthLandingState.Editing(form = it.form.copy(mode = next), errorMessage = null, fieldErrors = AuthLandingFieldErrors())
        }
    }

    fun submit() {
        val form = _state.value.form

        AppLog.i(TAG, "Submit mode=${form.mode} email='${form.email.trim()}' displayNameLen=${form.displayName.trim().length}")

        val fieldErrors = when (form.mode) {
            AuthMode.SignIn -> validateSignIn(form)
            AuthMode.Register -> validateRegister(form)
        }
        if (fieldErrors != null) {
            AppLog.w(TAG, "Validation failed: $fieldErrors")
            _state.value = AuthLandingState.Editing(form = form, errorMessage = null, fieldErrors = fieldErrors)
            return
        }

        _state.value = AuthLandingState.Submitting(form)

        viewModelScope.launch {
            val result = when (form.mode) {
                AuthMode.SignIn -> patientLogin(email = form.email.trim(), password = form.password)
                AuthMode.Register -> patientRegister(
                    email = form.email.trim(),
                    displayName = form.displayName.trim(),
                    password = form.password,
                )
            }

            when (result) {
                is AuthResult.Success -> {
                    AppLog.i(TAG, "Auth success patientId=${result.value.id}")
                    _state.value = AuthLandingState.Success
                }

                is AuthResult.Failure -> {
                    val msg = result.error.toUserMessage()
                    AppLog.w(TAG, "Auth failure: $msg")
                    _state.value = AuthLandingState.Editing(form = form, errorMessage = msg, fieldErrors = AuthLandingFieldErrors())
                }
            }
        }
    }
}

private fun validateSignIn(form: AuthLandingForm): AuthLandingFieldErrors? {
    val emailError = if (form.email.isBlank()) "Enter your email" else null
    val passwordError = if (form.password.isBlank()) "Enter your password" else null

    return if (emailError != null || passwordError != null) {
        AuthLandingFieldErrors(email = emailError, password = passwordError)
    } else {
        null
    }
}

private fun validateRegister(form: AuthLandingForm): AuthLandingFieldErrors? {
    val displayNameError = if (form.displayName.isBlank()) "Enter your display name" else null
    val emailError = if (form.email.isBlank()) "Enter your email" else null
    val passwordError = when {
        form.password.isBlank() -> "Enter your password"
        form.password.length < 8 -> "Password must be at least 8 characters"
        else -> null
    }

    return if (displayNameError != null || emailError != null || passwordError != null) {
        AuthLandingFieldErrors(displayName = displayNameError, email = emailError, password = passwordError)
    } else {
        null
    }
}

private inline fun AuthLandingState.withForm(transform: AuthLandingForm.() -> AuthLandingForm): AuthLandingState =
    when (this) {
        is AuthLandingState.Editing -> copy(form = form.transform())
        is AuthLandingState.Submitting -> copy(form = form.transform())
        AuthLandingState.Success -> this
    }

private fun AuthLandingState.clearError(): AuthLandingState =
    when (this) {
        is AuthLandingState.Editing -> copy(errorMessage = null)
        is AuthLandingState.Submitting -> this
        AuthLandingState.Success -> this
    }

private fun AuthLandingState.clearFieldError(
    displayName: Boolean = false,
    email: Boolean = false,
    password: Boolean = false,
): AuthLandingState =
    when (this) {
        is AuthLandingState.Editing -> copy(
            fieldErrors = fieldErrors.copy(
                displayName = if (displayName) null else fieldErrors.displayName,
                email = if (email) null else fieldErrors.email,
                password = if (password) null else fieldErrors.password,
            ),
        )

        is AuthLandingState.Submitting -> this
        AuthLandingState.Success -> this
    }

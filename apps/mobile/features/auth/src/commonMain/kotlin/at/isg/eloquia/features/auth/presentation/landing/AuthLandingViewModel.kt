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

sealed interface AuthLandingState {
    val form: AuthLandingForm
    val errorMessage: String?

    data class Editing(
        override val form: AuthLandingForm,
        override val errorMessage: String? = null,
    ) : AuthLandingState

    data class Submitting(
        override val form: AuthLandingForm,
    ) : AuthLandingState {
        override val errorMessage: String? = null
    }

    data object Success : AuthLandingState {
        override val form: AuthLandingForm = AuthLandingForm()
        override val errorMessage: String? = null
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
        _state.update { it.withForm { copy(email = email) }.clearError() }
    }

    fun updatePassword(password: String) {
        _state.update { it.withForm { copy(password = password) }.clearError() }
    }

    fun updateDisplayName(displayName: String) {
        _state.update { it.withForm { copy(displayName = displayName) }.clearError() }
    }

    fun toggleMode() {
        _state.update {
            val next = when (it.form.mode) {
                AuthMode.SignIn -> AuthMode.Register
                AuthMode.Register -> AuthMode.SignIn
            }
            AppLog.i(TAG, "Switch mode ${it.form.mode} -> $next")
            AuthLandingState.Editing(form = it.form.copy(mode = next), errorMessage = null)
        }
    }

    fun submit() {
        val form = _state.value.form

        AppLog.i(TAG, "Submit mode=${form.mode} email='${form.email.trim()}' displayNameLen=${form.displayName.trim().length}")

        val localError = when (form.mode) {
            AuthMode.SignIn -> validateSignIn(form)
            AuthMode.Register -> validateRegister(form)
        }
        if (localError != null) {
            AppLog.w(TAG, "Validation failed: $localError")
            _state.value = AuthLandingState.Editing(form = form, errorMessage = localError)
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
                    _state.value = AuthLandingState.Editing(form = form, errorMessage = msg)
                }
            }
        }
    }
}

private fun validateSignIn(form: AuthLandingForm): String? {
    if (form.email.isBlank()) return "Enter your email"
    if (form.password.isBlank()) return "Enter your password"
    return null
}

private fun validateRegister(form: AuthLandingForm): String? {
    if (form.displayName.isBlank()) return "Enter your display name"
    if (form.email.isBlank()) return "Enter your email"
    if (form.password.isBlank()) return "Enter your password"
    if (form.password.length < 8) return "Password must be at least 8 characters"
    return null
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

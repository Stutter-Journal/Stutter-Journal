package at.isg.eloquia.features.auth.presentation.link

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.isg.eloquia.core.domain.auth.model.AuthResult
import at.isg.eloquia.core.domain.auth.model.toUserMessage
import at.isg.eloquia.core.domain.auth.usecase.RequestLinkUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LinkRequestForm(
    val patientCode: String = "",
    val email: String = "",
)

sealed interface LinkRequestState {
    data class Editing(
        val form: LinkRequestForm,
        val errorMessage: String? = null,
    ) : LinkRequestState

    data class Submitting(val form: LinkRequestForm) : LinkRequestState

    data object Success : LinkRequestState
}

class LinkRequestViewModel(
    private val requestLinkUseCase: RequestLinkUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<LinkRequestState>(LinkRequestState.Editing(LinkRequestForm()))
    val state: StateFlow<LinkRequestState> = _state

    fun updatePatientCode(value: String) {
        _state.update { it.withForm { copy(patientCode = value.trim()) }.clearError() }
    }

    fun updateEmail(value: String) {
        _state.update { it.withForm { copy(email = value) }.clearError() }
    }

    fun submit() {
        val form = _state.value.formOrNull() ?: return
        val code = form.patientCode.trim()
        val email = form.email.trim()

        if (code.isBlank()) {
            _state.value = LinkRequestState.Editing(form = form.copy(patientCode = code), errorMessage = "Enter your code")
            return
        }
        if (email.isBlank() || !email.contains('@')) {
            _state.value = LinkRequestState.Editing(form = form.copy(email = email), errorMessage = "Enter a valid email")
            return
        }

        _state.value = LinkRequestState.Submitting(form)
        viewModelScope.launch {
            when (val result = requestLinkUseCase(patientCode = code, email = email)) {
                is AuthResult.Success -> _state.value = LinkRequestState.Success
                is AuthResult.Failure -> _state.value = LinkRequestState.Editing(form = form, errorMessage = result.error.toUserMessage())
            }
        }
    }
}

private inline fun LinkRequestState.withForm(transform: LinkRequestForm.() -> LinkRequestForm): LinkRequestState = when (this) {
    is LinkRequestState.Editing -> copy(form = form.transform())
    is LinkRequestState.Submitting -> copy(form = form.transform())
    LinkRequestState.Success -> this
}

private fun LinkRequestState.formOrNull(): LinkRequestForm? = when (this) {
    is LinkRequestState.Editing -> form
    is LinkRequestState.Submitting -> form
    LinkRequestState.Success -> null
}

private fun LinkRequestState.clearError(): LinkRequestState = when (this) {
    is LinkRequestState.Editing -> copy(errorMessage = null)
    else -> this
}

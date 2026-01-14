package at.isg.eloquia.kmpapp.presentation.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.isg.eloquia.core.domain.auth.model.AuthResult
import at.isg.eloquia.core.domain.auth.model.toUserMessage
import at.isg.eloquia.core.domain.auth.usecase.RedeemPairingCodeUseCase
import at.isg.eloquia.core.domain.auth.usecase.RevokeLinksUseCase
import at.isg.eloquia.kmpapp.presentation.states.AddConnectionState
import at.isg.eloquia.kmpapp.presentation.states.clearError
import at.isg.eloquia.kmpapp.presentation.states.formOrNull
import at.isg.eloquia.kmpapp.presentation.states.withForm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddConnectionForm(
    val code: String = "",
)

class AddConnectionViewModel(
    private val redeemPairingCode: RedeemPairingCodeUseCase,
    private val revokeLinks: RevokeLinksUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<AddConnectionState>(AddConnectionState.Editing(AddConnectionForm()))
    val state: StateFlow<AddConnectionState> = _state

    fun updateCode(value: String) {
        val normalized = value.filter(Char::isDigit).take(6)
        _state.update { it.withForm { copy(code = normalized) }.clearError() }
    }

    fun submit(codeOverride: String? = null) {
        val current = _state.value
        val form = current.formOrNull() ?: return

        val code = (codeOverride ?: form.code).trim().filter(Char::isDigit).take(6)
        if (code.length != 6) {
            _state.value = AddConnectionState.Editing(form = form.copy(code = code), errorMessage = "Enter a 6-digit code")
            return
        }

        _state.value = AddConnectionState.Submitting(form.copy(code = code))
        viewModelScope.launch {
            when (val result = redeemPairingCode(code)) {
                is AuthResult.Success -> _state.value = AddConnectionState.Success
                is AuthResult.Failure -> _state.value = AddConnectionState.Editing(form = form.copy(code = code), errorMessage = result.error.toUserMessage())
            }
        }
    }

    fun revoke() {
        val form = _state.value.formOrNull() ?: AddConnectionForm(code = "")
        _state.value = AddConnectionState.Revoking(form)
        viewModelScope.launch {
            when (val result = revokeLinks()) {
                is AuthResult.Success -> _state.value = AddConnectionState.Disconnected(revokedCount = result.value.revokedCount)
                is AuthResult.Failure -> _state.value = AddConnectionState.Editing(form = form, errorMessage = result.error.toUserMessage())
            }
        }
    }

    fun reset() {
        _state.value = AddConnectionState.Editing(AddConnectionForm())
    }
}

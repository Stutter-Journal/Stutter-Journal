package at.isg.eloquia.kmpapp.presentation.states

import at.isg.eloquia.kmpapp.presentation.components.AddConnectionForm

sealed interface AddConnectionState {
    data class Editing(
        val form: AddConnectionForm,
        val errorMessage: String? = null,
    ) : AddConnectionState

    data class Submitting(val form: AddConnectionForm) : AddConnectionState

    data class Revoking(val form: AddConnectionForm) : AddConnectionState

    data object Success : AddConnectionState

    data class Disconnected(val revokedCount: Int) : AddConnectionState
}

internal inline fun AddConnectionState.withForm(transform: AddConnectionForm.() -> AddConnectionForm): AddConnectionState = when (this) {
    is AddConnectionState.Editing -> copy(form = form.transform())
    is AddConnectionState.Submitting -> copy(form = form.transform())
    is AddConnectionState.Revoking -> copy(form = form.transform())
    AddConnectionState.Success -> this
    is AddConnectionState.Disconnected -> this
}

internal fun AddConnectionState.formOrNull(): AddConnectionForm? = when (this) {
    is AddConnectionState.Editing -> form
    is AddConnectionState.Submitting -> form
    is AddConnectionState.Revoking -> form
    AddConnectionState.Success -> null
    is AddConnectionState.Disconnected -> null
}

internal fun AddConnectionState.clearError(): AddConnectionState = when (this) {
    is AddConnectionState.Editing -> copy(errorMessage = null)
    else -> this
}

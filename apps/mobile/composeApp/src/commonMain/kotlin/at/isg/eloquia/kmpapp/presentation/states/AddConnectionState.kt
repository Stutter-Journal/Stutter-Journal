package at.isg.eloquia.kmpapp.presentation.states

import at.isg.eloquia.kmpapp.presentation.components.AddConnectionForm

sealed interface AddConnectionState {
    data class Editing(
        val form: AddConnectionForm,
        val errorMessage: String? = null,
    ) : AddConnectionState

    data class Submitting(val form: AddConnectionForm) : AddConnectionState

    data object Success : AddConnectionState
}

internal inline fun AddConnectionState.withForm(transform: AddConnectionForm.() -> AddConnectionForm): AddConnectionState =
    when (this) {
        is AddConnectionState.Editing -> copy(form = form.transform())
        is AddConnectionState.Submitting -> copy(form = form.transform())
        AddConnectionState.Success -> this
    }

internal fun AddConnectionState.formOrNull(): AddConnectionForm? = when (this) {
    is AddConnectionState.Editing -> form
    is AddConnectionState.Submitting -> form
    AddConnectionState.Success -> null
}

internal fun AddConnectionState.clearError(): AddConnectionState = when (this) {
    is AddConnectionState.Editing -> copy(errorMessage = null)
    else -> this
}

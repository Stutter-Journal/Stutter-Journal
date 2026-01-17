package at.isg.eloquia.features.therapist.presentation.state

import at.isg.eloquia.features.therapist.presentation.model.TherapistUi

sealed interface MyTherapistUiState {
    data object Loading : MyTherapistUiState
    data class Success(val therapist: TherapistUi) : MyTherapistUiState
    data class Error(val error: TherapistError) : MyTherapistUiState
}

sealed interface TherapistError {
    data object NoTherapistAssigned : TherapistError
    data object Unauthorized : TherapistError
    data object NetworkError : TherapistError
    data object Timeout : TherapistError
    data class Unknown(val message: String) : TherapistError
}

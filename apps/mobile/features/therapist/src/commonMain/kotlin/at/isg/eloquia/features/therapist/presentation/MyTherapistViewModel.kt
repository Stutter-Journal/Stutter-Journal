package at.isg.eloquia.features.therapist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.isg.eloquia.features.therapist.domain.exceptions.OfflineException
import at.isg.eloquia.features.therapist.domain.exceptions.TherapistNotFoundException
import at.isg.eloquia.features.therapist.domain.exceptions.TimeoutException
import at.isg.eloquia.features.therapist.domain.exceptions.UnauthorizedException
import at.isg.eloquia.features.therapist.domain.usecase.GetMyTherapistUseCase
import at.isg.eloquia.features.therapist.presentation.mapper.toUi
import at.isg.eloquia.features.therapist.presentation.state.MyTherapistUiState
import at.isg.eloquia.features.therapist.presentation.state.TherapistError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MyTherapistViewModel(
    private val getMyTherapistUseCase: GetMyTherapistUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyTherapistUiState>(MyTherapistUiState.Loading)
    val uiState: StateFlow<MyTherapistUiState> = _uiState.asStateFlow()

    init {
        loadTherapist()
    }

    fun loadTherapist() {
        viewModelScope.launch {
            _uiState.value = MyTherapistUiState.Loading

            getMyTherapistUseCase().catch { exception ->
                _uiState.value = MyTherapistUiState.Error(
                    error = exception.toTherapistError(),
                )
            }.collect { result ->
                _uiState.value = result.fold(onSuccess = { therapist ->
                    MyTherapistUiState.Success(therapist.toUi())
                }, onFailure = { exception ->
                    MyTherapistUiState.Error(exception.toTherapistError())
                })
            }
        }
    }

    private fun Throwable.toTherapistError(): TherapistError = when (this) {
        is TherapistNotFoundException -> TherapistError.NoTherapistAssigned
        is UnauthorizedException -> TherapistError.Unauthorized
        is OfflineException -> TherapistError.NetworkError
        is TimeoutException -> TherapistError.Timeout
        else -> TherapistError.Unknown(message ?: "An unknown error occurred")
    }
}

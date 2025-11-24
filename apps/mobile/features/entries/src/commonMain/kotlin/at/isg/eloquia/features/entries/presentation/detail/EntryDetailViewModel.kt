package at.isg.eloquia.features.entries.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.isg.eloquia.core.domain.entries.model.JournalEntry
import at.isg.eloquia.core.domain.entries.usecase.DeleteJournalEntryUseCase
import at.isg.eloquia.core.domain.entries.usecase.ObserveJournalEntriesUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EntryDetailUiState(
    val isLoading: Boolean = true,
    val entry: JournalEntry? = null,
    val errorMessage: String? = null,
)

sealed interface EntryDetailEvent {
    data object EntryDeleted : EntryDetailEvent
}

class EntryDetailViewModel(
    private val entryId: String,
    observeJournalEntriesUseCase: ObserveJournalEntriesUseCase,
    private val deleteJournalEntryUseCase: DeleteJournalEntryUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(EntryDetailUiState())
    val state: StateFlow<EntryDetailUiState> = _state

    private val _events = MutableSharedFlow<EntryDetailEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            observeJournalEntriesUseCase().collect { entries ->
                val entry = entries.firstOrNull { it.id == entryId }
                _state.update {
                    if (entry == null) {
                        it.copy(
                            isLoading = false,
                            entry = null,
                            errorMessage = "Entry not found",
                        )
                    } else {
                        EntryDetailUiState(isLoading = false, entry = entry)
                    }
                }
            }
        }
    }

    fun deleteEntry() {
        val entry = _state.value.entry ?: return
        viewModelScope.launch {
            runCatching { deleteJournalEntryUseCase(entry.id) }.onSuccess {
                _events.emit(EntryDetailEvent.EntryDeleted)
            }.onFailure { throwable ->
                _state.update {
                    it.copy(errorMessage = throwable.message ?: "Unable to delete entry")
                }
            }
        }
    }
}

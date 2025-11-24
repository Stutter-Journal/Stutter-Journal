package at.isg.eloquia.features.entries.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.isg.eloquia.core.domain.entries.usecase.CreateJournalEntryRequest
import at.isg.eloquia.core.domain.entries.usecase.CreateJournalEntryUseCase
import at.isg.eloquia.core.domain.entries.usecase.DeleteJournalEntryUseCase
import at.isg.eloquia.core.domain.entries.usecase.ObserveJournalEntriesUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock.System

class EntriesListViewModel(
    observeEntriesUseCase: ObserveJournalEntriesUseCase,
    private val createJournalEntryUseCase: CreateJournalEntryUseCase,
    private val deleteJournalEntryUseCase: DeleteJournalEntryUseCase,
) : ViewModel() {

    val state: StateFlow<EntriesListState> =
        observeEntriesUseCase().map<_, EntriesListState> { entries ->
            EntriesListState.Content(entries)
        }.catch { emit(EntriesListState.Error(it.message ?: "Unable to load entries")) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EntriesListState.Loading,
        )

    private val submissionErrors = MutableStateFlow<String?>(null)
    val lastSubmissionError: StateFlow<String?> = submissionErrors
    private val _events = MutableSharedFlow<EntriesListEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun createQuickEntry(title: String, content: String) {
        if (title.isBlank() && content.isBlank()) {
            submissionErrors.value = "Entry cannot be empty"
            return
        }
        viewModelScope.launch {
            runCatching {
                createJournalEntryUseCase(
                    CreateJournalEntryRequest(
                        title = title.ifBlank { "Untitled Entry" },
                        content = content,
                    ),
                )
            }.onFailure { throwable ->
                submissionErrors.value = throwable.message ?: "Unable to create entry"
            }
        }
    }

    fun createQuickEntry() {
        val timestamp = System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val display = timestamp.date.toString()
        createQuickEntry(title = "Entry $display", content = "Captured at ${timestamp.time}")
    }

    fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            runCatching { deleteJournalEntryUseCase(entryId) }.onSuccess {
                _events.emit(EntriesListEvent.EntryDeleted(entryId))
            }.onFailure { throwable ->
                _events.emit(
                    EntriesListEvent.DeletionFailed(
                        entryId = entryId,
                        reason = throwable.message ?: "Unable to delete entry",
                    ),
                )
            }
        }
    }
}

sealed interface EntriesListEvent {
    data class EntryDeleted(val entryId: String) : EntriesListEvent
    data class DeletionFailed(val entryId: String, val reason: String) : EntriesListEvent
}

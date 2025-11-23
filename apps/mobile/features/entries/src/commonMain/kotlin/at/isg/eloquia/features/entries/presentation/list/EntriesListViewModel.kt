package at.isg.eloquia.features.entries.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.isg.eloquia.core.domain.entries.usecase.CreateJournalEntryRequest
import at.isg.eloquia.core.domain.entries.usecase.CreateJournalEntryUseCase
import at.isg.eloquia.core.domain.entries.usecase.ObserveJournalEntriesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class EntriesListViewModel(
    observeEntriesUseCase: ObserveJournalEntriesUseCase,
    private val createJournalEntryUseCase: CreateJournalEntryUseCase,
) : ViewModel() {

    val state: StateFlow<EntriesListState> = observeEntriesUseCase()
        .map<_, EntriesListState> { entries -> EntriesListState.Content(entries) }
        .catch { emit(EntriesListState.Error(it.message ?: "Unable to load entries")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EntriesListState.Loading,
        )

    private val submissionErrors = MutableStateFlow<String?>(null)
    val lastSubmissionError: StateFlow<String?> = submissionErrors

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
        val timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val display = timestamp.date.toString()
        createQuickEntry(title = "Entry $display", content = "Captured at ${timestamp.time}")
    }
}

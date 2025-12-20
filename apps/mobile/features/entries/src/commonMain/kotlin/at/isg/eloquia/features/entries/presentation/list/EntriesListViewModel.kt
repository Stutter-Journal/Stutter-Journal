package at.isg.eloquia.features.entries.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.isg.eloquia.core.domain.entries.model.JournalEntry
import at.isg.eloquia.core.domain.entries.usecase.CreateJournalEntryRequest
import at.isg.eloquia.core.domain.entries.usecase.CreateJournalEntryUseCase
import at.isg.eloquia.core.domain.entries.usecase.DeleteJournalEntryUseCase
import at.isg.eloquia.core.domain.entries.usecase.ObserveJournalEntriesUseCase
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class EntriesListViewModel(
    observeEntriesUseCase: ObserveJournalEntriesUseCase,
    private val createJournalEntryUseCase: CreateJournalEntryUseCase,
    private val deleteJournalEntryUseCase: DeleteJournalEntryUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val grouping = MutableStateFlow(EntriesGroupBy.Date)
    private val filters = MutableStateFlow(EntriesFilterState())

    val state: StateFlow<EntriesListState> =
        observeEntriesUseCase().combine(filters) { entries, activeFilters ->
            entries to activeFilters
        }.combine(grouping) { (entries, activeFilters), groupBy ->
            val enriched = entries.sortedByDescending { it.createdAt }.map { entry ->
                EnrichedEntry(entry = entry, labels = entry.extractLabels())
            }

            val availableLabels = enriched
                .flatMap { it.labels }
                .distinctBy { it.id }
                .sortedWith(entryLabelComparator)
                .toImmutableList()

            val filtered = enriched.filter { it.matches(activeFilters, clock) }

            val grouped = filtered.groupInto(groupBy)

            EntriesListState.Content(
                groups = grouped.toImmutableList(),
                filters = activeFilters,
                grouping = groupBy,
                availableLabels = availableLabels,
                totalCount = filtered.size,
            ) as EntriesListState
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
        val timestamp = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
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

    fun setGroupBy(groupBy: EntriesGroupBy) {
        grouping.value = groupBy
    }

    fun setDateFilter(preset: DateFilterPreset) {
        filters.update { it.copy(datePreset = preset) }
    }

    fun toggleLabelFilter(label: EntryLabel) {
        filters.update { current ->
            val updated = if (label in current.selectedLabels) {
                current.selectedLabels - label
            } else {
                current.selectedLabels + label
            }
            current.copy(selectedLabels = updated)
        }
    }

    fun clearFilters() {
        filters.value = EntriesFilterState()
    }
}

sealed interface EntriesListEvent {
    data class EntryDeleted(val entryId: String) : EntriesListEvent
    data class DeletionFailed(val entryId: String, val reason: String) : EntriesListEvent
}

private data class EnrichedEntry(
    val entry: JournalEntry,
    val labels: List<EntryLabel>,
)

private val unlabeledGroupLabel = EntryLabel(value = "Unlabeled", category = EntryLabelCategory.Other)

private fun EnrichedEntry.matches(filters: EntriesFilterState, clock: Clock): Boolean {
    val today = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val matchesDate = filters.datePreset.matches(entry.createdAt.date, today)
    val matchesLabels = filters.selectedLabels.all { selected -> labels.any { it.id == selected.id } }
    return matchesDate && matchesLabels
}

private fun DateFilterPreset.matches(entryDate: LocalDate, today: LocalDate): Boolean = when (this) {
    DateFilterPreset.Anytime -> true
    DateFilterPreset.Today -> entryDate == today
    DateFilterPreset.Last7Days -> entryDate >= today.minus(DatePeriod(days = 7))
    DateFilterPreset.Last30Days -> entryDate >= today.minus(DatePeriod(days = 30))
}

private fun List<EnrichedEntry>.groupInto(groupBy: EntriesGroupBy): List<EntryGroupUiModel> {
    if (isEmpty()) return emptyList()

    return when (groupBy) {
        EntriesGroupBy.None -> listOf(
            EntryGroupUiModel(
                id = "all",
                title = "All entries",
                entries = map { it.entry }.toImmutableList(),
            ),
        )

        EntriesGroupBy.Date -> groupBy { it.entry.createdAt.date }
            .toSortedMap(compareByDescending { it })
            .map { (date, bucket) ->
                EntryGroupUiModel(
                    id = date.toString(),
                    title = date.toString(),
                    entries = bucket.sortedByDescending { it.entry.createdAt }.map { it.entry }.toImmutableList(),
                )
            }

        EntriesGroupBy.Label -> {
            val groupedByLabel = linkedMapOf<EntryLabel, MutableList<JournalEntry>>()
            for (item in this) {
                if (item.labels.isEmpty()) {
                    groupedByLabel.getOrPut(unlabeledGroupLabel) { mutableListOf() }.add(item.entry)
                } else {
                    item.labels.forEach { label ->
                        groupedByLabel.getOrPut(label) { mutableListOf() }.add(item.entry)
                    }
                }
            }
            groupedByLabel.entries
                .sortedWith(labelEntryComparator)
                .map { (label, bucket) ->
                    EntryGroupUiModel(
                        id = label.id,
                        title = label.chipLabel,
                        entries = bucket.distinctBy { it.id }.sortedByDescending { it.createdAt }.toImmutableList(),
                    )
                }
        }
    }
}

package at.isg.eloquia.features.entries.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.isg.eloquia.core.domain.entries.model.JournalEntry
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock.System

class EntriesListViewModel(
    observeEntriesUseCase: ObserveJournalEntriesUseCase,
    private val createJournalEntryUseCase: CreateJournalEntryUseCase,
    private val deleteJournalEntryUseCase: DeleteJournalEntryUseCase,
) : ViewModel() {

    private val filters = MutableStateFlow(EntriesFilters())

    val state: StateFlow<EntriesListState> =
        combine(observeEntriesUseCase(), filters) { entries, filters ->
            buildContent(entries = entries, filters = filters)
        }.map { it as EntriesListState }
            .catch { emit(EntriesListState.Error(it.message ?: "Unable to load entries")) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = EntriesListState.Loading,
            )

    private val submissionErrors = MutableStateFlow<String?>(null)
    val lastSubmissionError: StateFlow<String?> = submissionErrors
    private val _events = MutableSharedFlow<EntriesListEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun updateQuery(query: String) {
        filters.update { it.copy(query = query) }
    }

    fun updateDateRange(start: LocalDate?, endInclusive: LocalDate?) {
        filters.update { it.copy(dateRange = EntriesDateRange(start = start, endInclusive = endInclusive)) }
    }

    fun clearDateRange() {
        filters.update { it.copy(dateRange = EntriesDateRange()) }
    }

    fun setSortOrder(sortOrder: EntriesSortOrder) {
        filters.update { it.copy(sortOrder = sortOrder) }
    }

    fun setGroupByDay(enabled: Boolean) {
        filters.update { it.copy(groupByDay = enabled) }
    }

    fun toggleSituation(label: String) {
        filters.update { current ->
            val updated = current.selectedSituations.toMutableSet()
            if (!updated.add(label)) updated.remove(label)
            current.copy(selectedSituations = updated)
        }
    }

    fun toggleTechnique(label: String) {
        filters.update { current ->
            val updated = current.selectedTechniques.toMutableSet()
            if (!updated.add(label)) updated.remove(label)
            current.copy(selectedTechniques = updated)
        }
    }

    fun clearSelections() {
        filters.update { it.copy(selectedSituations = emptySet(), selectedTechniques = emptySet()) }
    }

    fun resetFilters() {
        filters.value = EntriesFilters()
    }

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

    private fun buildContent(entries: List<JournalEntry>, filters: EntriesFilters): EntriesListState.Content {
        val availableSituations =
            entries.asSequence().flatMap { entry ->
                entry.tags.asSequence().mapNotNull { tag -> tagValue(tag, TAG_TRIGGER_PREFIX) }
            }.toSet().toList().sorted()

        val availableTechniques =
            entries.asSequence().flatMap { entry ->
                entry.tags.asSequence().mapNotNull { tag -> tagValue(tag, TAG_METHOD_PREFIX) }
            }.toSet().toList().sorted()

        val filtered = entries
            .asSequence()
            .filter { entry -> matchesQuery(entry = entry, query = filters.query) }
            .filter { entry -> matchesDateRange(entry = entry, dateRange = filters.dateRange) }
            .filter { entry -> matchesSituations(entry = entry, selected = filters.selectedSituations) }
            .filter { entry -> matchesTechniques(entry = entry, selected = filters.selectedTechniques) }
            .toList()

        val sorted = when (filters.sortOrder) {
            EntriesSortOrder.DateDesc ->
                filtered.sortedWith(
                    compareByDescending<JournalEntry> { entryDate(it) }
                        .thenByDescending { it.createdAt },
                )

            EntriesSortOrder.DateAsc ->
                filtered.sortedWith(
                    compareBy<JournalEntry> { entryDate(it) }
                        .thenBy { it.createdAt },
                )

            EntriesSortOrder.IntensityDesc ->
                filtered.sortedWith(
                    intensityComparator(descending = true)
                        .thenByDescending { entryDate(it) }
                        .thenByDescending { it.createdAt },
                )

            EntriesSortOrder.IntensityAsc ->
                filtered.sortedWith(
                    intensityComparator(descending = false)
                        .thenByDescending { entryDate(it) }
                        .thenByDescending { it.createdAt },
                )
        }

        val groupedEntries = groupByDayPreservingOrder(sorted)

        return EntriesListState.Content(
            entries = sorted,
            groupedEntries = groupedEntries,
            filters = filters,
            availableSituations = availableSituations,
            availableTechniques = availableTechniques,
        )
    }

    private fun matchesQuery(entry: JournalEntry, query: String): Boolean {
        val normalized = query.trim().lowercase()
        if (normalized.isBlank()) return true
        return entry.title.lowercase().contains(normalized) ||
            entry.content.lowercase().contains(normalized) ||
            entry.tags.any { it.lowercase().contains(normalized) }
    }

    private fun matchesDateRange(entry: JournalEntry, dateRange: EntriesDateRange): Boolean {
        if (!dateRange.isActive) return true
        val date = entryDate(entry)
        val startOk = dateRange.start?.let { date >= it } ?: true
        val endOk = dateRange.endInclusive?.let { date <= it } ?: true
        return startOk && endOk
    }

    private fun matchesSituations(entry: JournalEntry, selected: Set<String>): Boolean {
        if (selected.isEmpty()) return true
        val triggers = entry.tags.mapNotNull { tagValue(it, TAG_TRIGGER_PREFIX) }
        return triggers.any { it in selected }
    }

    private fun matchesTechniques(entry: JournalEntry, selected: Set<String>): Boolean {
        if (selected.isEmpty()) return true
        val methods = entry.tags.mapNotNull { tagValue(it, TAG_METHOD_PREFIX) }
        return methods.any { it in selected }
    }

    private fun groupByDayPreservingOrder(entries: List<JournalEntry>): List<Pair<LocalDate, List<JournalEntry>>> {
        val result = mutableListOf<Pair<LocalDate, MutableList<JournalEntry>>>()
        for (entry in entries) {
            val date = entryDate(entry)
            val last = result.lastOrNull()
            if (last != null && last.first == date) {
                last.second.add(entry)
            } else {
                result.add(date to mutableListOf(entry))
            }
        }
        return result.map { (date, list) -> date to list.toList() }
    }

    private fun entryDate(entry: JournalEntry): LocalDate {
        val tagDate = entry.tags.firstNotNullOfOrNull { tag -> parseDateTag(tag) }
        return tagDate ?: entry.createdAt.date
    }

    private fun entryIntensity(entry: JournalEntry): Int? {
        val raw = entry.tags.firstNotNullOfOrNull { tag -> tagValue(tag, TAG_INTENSITY_PREFIX) } ?: return null
        return raw.toIntOrNull()
    }

    private fun intensityComparator(descending: Boolean): Comparator<JournalEntry> {
        return Comparator { a, b ->
            val ia = entryIntensity(a)
            val ib = entryIntensity(b)
            when {
                ia == null && ib == null -> 0
                ia == null -> 1
                ib == null -> -1
                else -> if (descending) ib.compareTo(ia) else ia.compareTo(ib)
            }
        }
    }

    private fun tagValue(tag: String, prefix: String): String? {
        if (!tag.startsWith(prefix)) return null
        return tag.removePrefix(prefix).trim().takeIf { it.isNotEmpty() }
    }

    private fun parseDateTag(tag: String): LocalDate? {
        val raw = tagValue(tag, TAG_DATE_PREFIX) ?: return null
        return runCatching { LocalDate.parse(raw) }.getOrNull()
    }

    private companion object {
        private const val TAG_DATE_PREFIX = "date:"
        private const val TAG_INTENSITY_PREFIX = "intensity:"
        private const val TAG_TRIGGER_PREFIX = "trigger:"
        private const val TAG_METHOD_PREFIX = "method:"
    }
}

sealed interface EntriesListEvent {
    data class EntryDeleted(val entryId: String) : EntriesListEvent
    data class DeletionFailed(val entryId: String, val reason: String) : EntriesListEvent
}

package at.isg.eloquia.features.entries.presentation.list

import at.isg.eloquia.core.domain.entries.model.JournalEntry
import kotlinx.datetime.LocalDate

/**
 * UI state for the entries list screen
 */
sealed interface EntriesListState {
    data object Loading : EntriesListState

    data class Content(
        val entries: List<JournalEntry>,
        val groupedEntries: List<Pair<LocalDate, List<JournalEntry>>>,
        val filters: EntriesFilters,
        val availableSituations: List<String>,
        val availableTechniques: List<String>,
    ) : EntriesListState

    data class Error(val message: String) : EntriesListState
}

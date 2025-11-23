package at.isg.eloquia.features.entries.presentation.list

import at.isg.eloquia.features.entries.domain.model.JournalEntry

/**
 * UI state for the entries list screen
 */
sealed interface EntriesListState {
    data object Loading : EntriesListState
    data class Content(val entries: List<JournalEntry>) : EntriesListState
    data class Error(val message: String) : EntriesListState
}

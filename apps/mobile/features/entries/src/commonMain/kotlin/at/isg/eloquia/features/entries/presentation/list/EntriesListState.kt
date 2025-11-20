package at.isg.eloquia.features.entries.presentation.list

import at.isg.eloquia.features.entries.domain.model.JournalEntry

/**
 * UI state for the entries list screen
 */
data class EntriesListState(
    val entries: List<JournalEntry> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

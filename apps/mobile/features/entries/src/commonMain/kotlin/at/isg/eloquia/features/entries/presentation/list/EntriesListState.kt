package at.isg.eloquia.features.entries.presentation.list

import at.isg.eloquia.core.domain.entries.model.JournalEntry

/**
 * UI state for the entries list screen
 */
sealed interface EntriesListState {
    data object Loading : EntriesListState

    data class Content(
        val groups: List<EntryGroupUiModel>,
        val filters: EntriesFilterState,
        val grouping: EntriesGroupBy,
        val availableLabels: List<EntryLabel>,
        val totalCount: Int,
    ) : EntriesListState

    data class Error(val message: String) : EntriesListState
}

enum class EntriesGroupBy { None, Date, Label }

enum class DateFilterPreset(val label: String, val daysBack: Int?) {
    Anytime(label = "Any time", daysBack = null), Today(label = "Today", daysBack = 0), Last7Days(
        label = "Last 7 days",
        daysBack = 7
    ),
    Last30Days(label = "Last 30 days", daysBack = 30),
}

data class EntriesFilterState(
    val datePreset: DateFilterPreset = DateFilterPreset.Anytime,
    val selectedLabels: Set<EntryLabel> = setOf<EntryLabel>(),
) {
    val hasActiveFilters: Boolean
        get() = datePreset != DateFilterPreset.Anytime || selectedLabels.isNotEmpty()
}

data class EntryGroupUiModel(
    val id: String,
    val title: String,
    val entries: List<JournalEntry>,
)

enum class EntryLabelCategory(val displayName: String) {
    Method("Method"), StutterForm("Form"), Trigger("Trigger"), Other("Label"),
}

data class EntryLabel(
    val value: String,
    val category: EntryLabelCategory,
) {
    val id: String = "${category.name}:${value.lowercase()}"
    val chipLabel: String = "${category.displayName}: $value"
}

package at.isg.eloquia.features.entries.presentation.list

import kotlinx.datetime.LocalDate

data class EntriesFilters(
    val query: String = "",
    val dateRange: EntriesDateRange = EntriesDateRange(),
    val selectedSituations: Set<String> = emptySet(),
    val selectedTechniques: Set<String> = emptySet(),
    val sortOrder: EntriesSortOrder = EntriesSortOrder.DateDesc,
    val groupByDay: Boolean = false,
)

data class EntriesDateRange(
    val start: LocalDate? = null,
    val endInclusive: LocalDate? = null,
) {
    val isActive: Boolean get() = start != null || endInclusive != null
}

enum class EntriesSortOrder {
    DateDesc,
    DateAsc,
    IntensityDesc,
    IntensityAsc,
}

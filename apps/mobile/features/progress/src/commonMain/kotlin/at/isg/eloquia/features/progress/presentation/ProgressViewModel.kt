package at.isg.eloquia.features.progress.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.isg.eloquia.core.domain.entries.model.JournalEntry
import at.isg.eloquia.core.domain.entries.usecase.ObserveJournalEntriesUseCase
import at.isg.eloquia.features.progress.presentation.model.CategoryFrequency
import at.isg.eloquia.features.progress.presentation.model.ComparisonData
import at.isg.eloquia.features.progress.presentation.model.ComparisonDataPoint
import at.isg.eloquia.features.progress.presentation.model.FrequencyData
import at.isg.eloquia.features.progress.presentation.model.IntensityDataPoint
import at.isg.eloquia.features.progress.presentation.model.ProgressUiState
import at.isg.eloquia.features.progress.presentation.model.SelectedTimeRange
import at.isg.eloquia.features.progress.presentation.model.TimeRange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class ProgressViewModel(
    observeEntriesUseCase: ObserveJournalEntriesUseCase,
) : ViewModel() {

    private val _showEmptyDays = MutableStateFlow(true)
    val showEmptyDays: StateFlow<Boolean> = _showEmptyDays

    private val _timeRange = MutableStateFlow(TimeRange.MONTH)
    val timeRange: StateFlow<TimeRange> = _timeRange

    private val _comparisonMode = MutableStateFlow(false)
    val comparisonMode: StateFlow<Boolean> = _comparisonMode

    private val _selectedSituations = MutableStateFlow<Set<String>>(emptySet())
    val selectedSituations: StateFlow<Set<String>> = _selectedSituations

    private val _selectedTechniques = MutableStateFlow<Set<String>>(emptySet())
    val selectedTechniques: StateFlow<Set<String>> = _selectedTechniques

    // Separate derived flow for availability data (depends on entries & timeRange)
    private val availabilityData: StateFlow<AvailabilityData> = combine(
        observeEntriesUseCase(),
        _timeRange,
    ) { entries, range ->
        // Parse intensity and date from tags
        val dataPoints = entries.mapNotNull { entry ->
            val intensity = entry.tags
                .firstNotNullOfOrNull { tag ->
                    if (tag.startsWith("intensity:", ignoreCase = true)) {
                        tag.substringAfter(":").trim().toIntOrNull()
                    } else null
                }
            
            val date = entry.tags
                .firstNotNullOfOrNull { tag ->
                    if (tag.startsWith("date:", ignoreCase = true)) {
                        try {
                            LocalDate.parse(tag.substringAfter(":").trim())
                        } catch (_: Exception) {
                            null
                        }
                    } else null
                } ?: entry.createdAt.date
            
            intensity?.let {
                IntensityDataPoint(date = date, intensity = it.toFloat(), entryId = entry.id)
            }
        }
        
        // Group by date and calculate daily average
        val dailyAverages = dataPoints
            .groupBy { it.date }
            .map { (date, points) ->
                IntensityDataPoint(
                    date = date,
                    intensity = points.map { it.intensity }.average().toFloat(),
                    entryId = null,
                )
            }
            .sortedBy { it.date }
        
        // Determine rolling window boundaries
        val today = currentLocalDate()
        val startDate = when {
            range.days != null -> today.minus(DatePeriod(days = range.days - 1))
            range.months != null -> today.minus(DatePeriod(months = range.months))
            dailyAverages.isNotEmpty() -> dailyAverages.first().date
            else -> null
        }

        if (startDate == null) {
            AvailabilityData.Empty
        } else {
            val endDate = when (range) {
                TimeRange.MAX -> dailyAverages.lastOrNull()?.date ?: today
                else -> today
            }

            val selectedTimeRange = SelectedTimeRange(startDate = startDate, endDate = endDate)
            val filteredData = dailyAverages.filter { it.date in startDate..endDate }
            val finalData = if (filteredData.isNotEmpty()) {
                fillMissingDays(filteredData, startDate = startDate, endDate = endDate)
            } else emptyList()
            
            val frequencyData = computeFrequencyData(entries, selectedTimeRange)
            val availableSituations = extractAvailableCategories(entries, "trigger:", selectedTimeRange)
            val availableTechniques = extractAvailableCategories(entries, "method:", selectedTimeRange)

            AvailabilityData.Success(
                dataPoints = finalData,
                selectedTimeRange = selectedTimeRange,
                frequencyData = frequencyData,
                availableSituations = availableSituations,
                availableTechniques = availableTechniques,
                entries = entries,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AvailabilityData.Empty,
    )

    // Derived flow for comparison data (depends on availabilityData & selections)
    val state: StateFlow<ProgressUiState> = combine(
        availabilityData,
        _selectedSituations,
        _selectedTechniques,
    ) { availability, selectedSituations, selectedTechniques ->
        if (availability !is AvailabilityData.Success) {
            return@combine ProgressUiState.Empty
        }

        val comparisonData = computeComparisonData(
            availability.entries,
            availability.selectedTimeRange,
            selectedSituations,
            selectedTechniques
        )

        ProgressUiState.Success(
            dataPoints = availability.dataPoints,
            selectedTimeRange = availability.selectedTimeRange,
            frequencyData = availability.frequencyData,
            comparisonData = comparisonData,
            availableSituations = availability.availableSituations,
            availableTechniques = availability.availableTechniques,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProgressUiState.Loading,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProgressUiState.Loading,
    )

    fun toggleShowEmptyDays() {
        _showEmptyDays.value = !_showEmptyDays.value
    }

    fun setTimeRange(range: TimeRange) {
        _timeRange.value = range
    }

    fun toggleComparisonMode() {
        _comparisonMode.value = !_comparisonMode.value
    }

    fun toggleSituation(situation: String) {
        _selectedSituations.value = if (situation in _selectedSituations.value) {
            _selectedSituations.value - situation
        } else {
            _selectedSituations.value + situation
        }
    }

    fun toggleTechnique(technique: String) {
        _selectedTechniques.value = if (technique in _selectedTechniques.value) {
            _selectedTechniques.value - technique
        } else {
            _selectedTechniques.value + technique
        }
    }

    private fun currentLocalDate(): LocalDate {
        return Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    }

    private fun fillMissingDays(
        dataPoints: List<IntensityDataPoint>,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ): List<IntensityDataPoint> {
        if (dataPoints.isEmpty()) return emptyList()
        
        val sortedPoints = dataPoints.sortedBy { it.date }
        val firstDate = startDate ?: sortedPoints.first().date
        val lastDate = endDate ?: sortedPoints.last().date
        
        val pointsMap = sortedPoints.associateBy { it.date }
        val result = mutableListOf<IntensityDataPoint>()
        
        var currentDate = firstDate
        while (currentDate <= lastDate) {
            result.add(
                pointsMap[currentDate] ?: IntensityDataPoint(
                    date = currentDate,
                    intensity = 0f,
                    entryId = null,
                )
            )
            currentDate = currentDate.plus(DatePeriod(days = 1))
        }
        
        return result
    }

    /**
     * Computes frequency aggregations for categories within the selected time range.
     * This logic is completely separate from the time-series progress chart logic.
     */
    private fun computeFrequencyData(
        entries: List<JournalEntry>,
        selectedTimeRange: SelectedTimeRange,
    ): FrequencyData {
        // Filter entries within the selected time range
        val entriesInRange = entries.filter { entry ->
            val entryDate = entry.tags
                .firstNotNullOfOrNull { tag ->
                    if (tag.startsWith("date:", ignoreCase = true)) {
                        try {
                            LocalDate.parse(tag.substringAfter(":").trim())
                        } catch (_: Exception) {
                            null
                        }
                    } else null
                } ?: entry.createdAt.date
            
            entryDate in selectedTimeRange.startDate..selectedTimeRange.endDate
        }
        
        // Aggregate triggers
        val triggerCounts = mutableMapOf<String, Int>()
        entriesInRange.forEach { entry ->
            entry.tags
                .filter { it.startsWith("trigger:", ignoreCase = true) }
                .forEach { tag ->
                    val trigger = tag.substringAfter(":").trim()
                    triggerCounts[trigger] = (triggerCounts[trigger] ?: 0) + 1
                }
        }
        
        // Aggregate techniques (methods)
        val techniqueCounts = mutableMapOf<String, Int>()
        entriesInRange.forEach { entry ->
            entry.tags
                .filter { it.startsWith("method:", ignoreCase = true) }
                .forEach { tag ->
                    val technique = tag.substringAfter(":").trim()
                    techniqueCounts[technique] = (techniqueCounts[technique] ?: 0) + 1
                }
        }
        
        // Aggregate stutter forms
        val stutterFormCounts = mutableMapOf<String, Int>()
        entriesInRange.forEach { entry ->
            entry.tags
                .filter { it.startsWith("stutterform:", ignoreCase = true) }
                .forEach { tag ->
                    val form = tag.substringAfter(":").trim()
                    stutterFormCounts[form] = (stutterFormCounts[form] ?: 0) + 1
                }
        }
        
        // Convert to sorted lists (descending by frequency)
        val triggers = triggerCounts.entries
            .map { CategoryFrequency(it.key, it.value) }
            .sortedByDescending { it.count }
        
        val techniques = techniqueCounts.entries
            .map { CategoryFrequency(it.key, it.value) }
            .sortedByDescending { it.count }
        
        val stutterForms = stutterFormCounts.entries
            .map { CategoryFrequency(it.key, it.value) }
            .sortedByDescending { it.count }
        
        return FrequencyData(
            triggers = triggers,
            techniques = techniques,
            stutterForms = stutterForms,
        )
    }

    /**
     * Extracts all available categories of a given type from entries in the selected time range.
     */
    private fun extractAvailableCategories(
        entries: List<JournalEntry>,
        prefix: String,
        selectedTimeRange: SelectedTimeRange,
    ): List<String> {
        val entriesInRange = entries.filter { entry ->
            val entryDate = entry.tags
                .firstNotNullOfOrNull { tag ->
                    if (tag.startsWith("date:", ignoreCase = true)) {
                        try {
                            LocalDate.parse(tag.substringAfter(":").trim())
                        } catch (_: Exception) {
                            null
                        }
                    } else null
                } ?: entry.createdAt.date
            
            entryDate in selectedTimeRange.startDate..selectedTimeRange.endDate
        }
        
        return entriesInRange
            .flatMap { entry ->
                entry.tags.filter { it.startsWith(prefix, ignoreCase = true) }
                    .map { it.substringAfter(":").trim() }
            }
            .distinct()
            .sorted()
    }

    /**
     * Computes comparison data showing average intensity per situation and technique.
     */
    private fun computeComparisonData(
        entries: List<JournalEntry>,
        selectedTimeRange: SelectedTimeRange,
        selectedSituations: Set<String>,
        selectedTechniques: Set<String>,
    ): ComparisonData {
        // Filter entries within the selected time range
        val entriesInRange = entries.filter { entry ->
            val entryDate = entry.tags
                .firstNotNullOfOrNull { tag ->
                    if (tag.startsWith("date:", ignoreCase = true)) {
                        try {
                            LocalDate.parse(tag.substringAfter(":").trim())
                        } catch (_: Exception) {
                            null
                        }
                    } else null
                } ?: entry.createdAt.date
            
            entryDate in selectedTimeRange.startDate..selectedTimeRange.endDate
        }
        
        // Compute situation comparison data
        val situationData = if (selectedSituations.isEmpty()) {
            emptyList()
        } else {
            selectedSituations.mapNotNull { situation ->
                val matchingEntries = entriesInRange.filter { entry ->
                    entry.tags.any { tag ->
                        tag.startsWith("trigger:", ignoreCase = true) &&
                        tag.substringAfter(":").trim() == situation
                    }
                }
                
                val intensities = matchingEntries.mapNotNull { entry ->
                    entry.tags
                        .firstNotNullOfOrNull { tag ->
                            if (tag.startsWith("intensity:", ignoreCase = true)) {
                                tag.substringAfter(":").trim().toFloatOrNull()
                            } else null
                        }
                }
                
                if (intensities.isNotEmpty()) {
                    ComparisonDataPoint(
                        category = situation,
                        averageIntensity = intensities.average().toFloat(),
                        count = intensities.size,
                    )
                } else null
            }.sortedByDescending { it.averageIntensity }
        }
        
        // Compute technique comparison data
        val techniqueData = if (selectedTechniques.isEmpty()) {
            emptyList()
        } else {
            selectedTechniques.mapNotNull { technique ->
                val matchingEntries = entriesInRange.filter { entry ->
                    entry.tags.any { tag ->
                        tag.startsWith("method:", ignoreCase = true) &&
                        tag.substringAfter(":").trim() == technique
                    }
                }
                
                val intensities = matchingEntries.mapNotNull { entry ->
                    entry.tags
                        .firstNotNullOfOrNull { tag ->
                            if (tag.startsWith("intensity:", ignoreCase = true)) {
                                tag.substringAfter(":").trim().toFloatOrNull()
                            } else null
                        }
                }
                
                if (intensities.isNotEmpty()) {
                    ComparisonDataPoint(
                        category = technique,
                        averageIntensity = intensities.average().toFloat(),
                        count = intensities.size,
                    )
                } else null
            }.sortedByDescending { it.averageIntensity }
        }
        
        return ComparisonData(
            situationData = situationData,
            techniqueData = techniqueData,
        )
    }

}

/**
 * Internal sealed class to hold availability data that is computed based on
 * entries and time range. This is used to avoid redundant recomputation when
 * only the selection (situations/techniques) changes.
 */
private sealed class AvailabilityData {
    object Empty : AvailabilityData()

    data class Success(
        val dataPoints: List<IntensityDataPoint>,
        val selectedTimeRange: SelectedTimeRange,
        val frequencyData: FrequencyData,
        val availableSituations: List<String>,
        val availableTechniques: List<String>,
        val entries: List<JournalEntry>,
    ) : AvailabilityData()
}

package at.isg.eloquia.features.progress.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.isg.eloquia.core.domain.entries.model.JournalEntry
import at.isg.eloquia.core.domain.entries.usecase.ObserveJournalEntriesUseCase
import at.isg.eloquia.features.progress.presentation.model.CategoryFrequency
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

    val state: StateFlow<ProgressUiState> = combine(
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
                        } catch (e: Exception) {
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

        if (startDate == null) return@combine ProgressUiState.Empty

        val endDate = when (range) {
            TimeRange.MAX -> dailyAverages.lastOrNull()?.date ?: today
            else -> today
        }

        // Create SelectedTimeRange (single source of truth)
        val selectedTimeRange = SelectedTimeRange(startDate = startDate, endDate = endDate)

        // Keep daily granularity; no aggregation
        val filteredData = dailyAverages.filter { it.date in startDate..endDate }

        // Always fill the full time window to keep a continuous time axis
        val finalData = if (filteredData.isNotEmpty()) {
            fillMissingDays(filteredData, startDate = startDate, endDate = endDate)
        } else emptyList()
        
        // Compute frequency data for the selected time range
        val frequencyData = computeFrequencyData(entries, selectedTimeRange)
        
        // Return UI state
        if (finalData.isEmpty()) {
            ProgressUiState.Empty
        } else {
            ProgressUiState.Success(
                dataPoints = finalData,
                selectedTimeRange = selectedTimeRange,
                frequencyData = frequencyData,
            )
        }
    }.stateIn(
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
                        } catch (e: Exception) {
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

}

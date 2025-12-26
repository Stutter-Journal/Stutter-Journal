package at.isg.eloquia.features.progress.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.isg.eloquia.core.domain.entries.usecase.ObserveJournalEntriesUseCase
import at.isg.eloquia.features.progress.presentation.model.IntensityDataPoint
import at.isg.eloquia.features.progress.presentation.model.ProgressUiState
import at.isg.eloquia.features.progress.presentation.model.TimeRange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

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
        _showEmptyDays
    ) { entries, range, showEmpty ->
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
                        runCatching { LocalDate.parse(tag.substringAfter(":").trim()) }.getOrNull()
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
                    entryId = "",
                )
            }
            .sortedBy { it.date }
        
        // Filter by time range
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val cutoffDate = range.days?.let { days -> today.minus(DatePeriod(days = days)) }
        val filteredData = if (cutoffDate != null) {
            println("Filtering: range=${range.label}, today=$today, cutoffDate=$cutoffDate, before=${dailyAverages.size}")
            val filtered = dailyAverages.filter { it.date >= cutoffDate }
            println("Filtering: after=${filtered.size}")
            filtered
        } else {
            println("Filtering: No filter (MAX), showing all ${dailyAverages.size}")
            dailyAverages
        }
        
        // Fill missing days if needed
        val finalData = if (showEmpty && filteredData.isNotEmpty()) {
            val startDate = cutoffDate ?: filteredData.first().date
            val endDate = if (cutoffDate != null) today else filteredData.last().date
            fillMissingDays(filteredData, startDate = startDate, endDate = endDate)
        } else filteredData
        
        // Return UI state
        if (finalData.isEmpty()) ProgressUiState.Empty else ProgressUiState.Success(finalData)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProgressUiState.Loading,
    )

    fun toggleShowEmptyDays() {
        _showEmptyDays.value = !_showEmptyDays.value
    }

    fun setTimeRange(range: TimeRange) {
        println("ProgressViewModel: Setting time range to ${range.label}")
        _timeRange.value = range
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
                    entryId = "",
                )
            )
            currentDate = currentDate.plus(DatePeriod(days = 1))
        }
        
        return result
    }
}

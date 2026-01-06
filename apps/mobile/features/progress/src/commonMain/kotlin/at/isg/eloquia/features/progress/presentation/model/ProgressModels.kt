package at.isg.eloquia.features.progress.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

@Immutable
data class IntensityDataPoint(
    val date: LocalDate,
    val intensity: Float,
    val entryId: String?,
)

enum class TimeRange(val label: String, val days: Int?, val months: Int?) {
    WEEK("Week", 7, null),
    MONTH("Month", 30, null),
    YEAR("Year", null, 12),
    MAX("Max", null, null)
}

/**
 * Represents the selected time range for both progress and frequency charts.
 * This is the single source of truth for time-based filtering.
 */
@Immutable
data class SelectedTimeRange(
    val startDate: LocalDate,
    val endDate: LocalDate,
)

/**
 * Represents a single category frequency data point.
 */
@Immutable
data class CategoryFrequency(
    val category: String,
    val count: Int,
)

/**
 * Container for all frequency data grouped by category type.
 */
@Immutable
data class FrequencyData(
    val triggers: List<CategoryFrequency>,
    val techniques: List<CategoryFrequency>,
    val stutterForms: List<CategoryFrequency>,
)

sealed interface ProgressUiState {
    data object Loading : ProgressUiState
    data object Empty : ProgressUiState
    data class Success(
        val dataPoints: List<IntensityDataPoint>,
        val selectedTimeRange: SelectedTimeRange,
        val frequencyData: FrequencyData,
    ) : ProgressUiState
}

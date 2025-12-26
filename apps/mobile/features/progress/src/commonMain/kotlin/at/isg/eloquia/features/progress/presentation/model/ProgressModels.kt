package at.isg.eloquia.features.progress.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

@Immutable
data class IntensityDataPoint(
    val date: LocalDate,
    val intensity: Float,
    val entryId: String,
)

enum class TimeRange(val label: String, val days: Int?) {
    WEEK("Week", 7),
    MONTH("Month", 30),
    YEAR("Year", 365),
    MAX("Max", null)
}

sealed interface ProgressUiState {
    data object Loading : ProgressUiState
    data object Empty : ProgressUiState
    data class Success(val dataPoints: List<IntensityDataPoint>) : ProgressUiState
}

package at.isg.eloquia.features.progress.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import at.isg.eloquia.features.progress.presentation.model.IntensityDataPoint
import at.isg.eloquia.features.progress.presentation.model.ProgressUiState
import at.isg.eloquia.features.progress.presentation.model.TimeRange
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.max
import kotlinx.datetime.LocalDate

@Composable
fun ProgressScreen(
    modifier: Modifier = Modifier,
    viewModel: ProgressViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val showEmptyDays by viewModel.showEmptyDays.collectAsState()
    val timeRange by viewModel.timeRange.collectAsState()

    ProgressScreenContent(
        state = state,
        showEmptyDays = showEmptyDays,
        timeRange = timeRange,
        onToggleEmptyDays = viewModel::toggleShowEmptyDays,
        onTimeRangeChange = viewModel::setTimeRange,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgressScreenContent(
    state: ProgressUiState,
    showEmptyDays: Boolean,
    timeRange: TimeRange,
    onToggleEmptyDays: () -> Unit,
    onTimeRangeChange: (TimeRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Trends & Progress") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        when (state) {
            is ProgressUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is ProgressUiState.Empty -> {
                EmptyProgressState(modifier = Modifier.fillMaxSize().padding(padding))
            }

            is ProgressUiState.Success -> {
                ProgressContent(
                    dataPoints = state.dataPoints,
                    showEmptyDays = showEmptyDays,
                    timeRange = timeRange,
                    onToggleEmptyDays = onToggleEmptyDays,
                    onTimeRangeChange = onTimeRangeChange,
                    modifier = Modifier.fillMaxSize().padding(padding),
                )
            }
        }
    }
}

@Composable
private fun ProgressContent(
    dataPoints: List<IntensityDataPoint>,
    showEmptyDays: Boolean,
    timeRange: TimeRange,
    onToggleEmptyDays: () -> Unit,
    onTimeRangeChange: (TimeRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            TimeRangeSelector(
                selectedRange = timeRange,
                onRangeSelected = onTimeRangeChange,
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Show days without entries",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Switch(
                    checked = showEmptyDays,
                    onCheckedChange = { onToggleEmptyDays() },
                )
            }
        }

        item {
            IntensityLineChart(
                dataPoints = dataPoints,
                timeRange = timeRange,
                modifier = Modifier.fillMaxWidth().height(280.dp),
            )
        }

        item {
            StatisticsCards(dataPoints = dataPoints)
        }
    }
}

@Composable
private fun TimeRangeSelector(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TimeRange.entries.forEach { range ->
            FilterChip(
                selected = range == selectedRange,
                onClick = { onRangeSelected(range) },
                label = { Text(range.label) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun IntensityLineChart(
    dataPoints: List<IntensityDataPoint>,
    timeRange: TimeRange,
    modifier: Modifier = Modifier,
) {
    if (dataPoints.isEmpty()) return
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Timeline,
                        contentDescription = null,
                        tint = primaryColor,
                    )
                    Text(
                        text = "Daily Average Intensity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                
                val entriesWithData = dataPoints.count { it.intensity > 0 }
                if (entriesWithData > 0) {
                    Text(
                        text = "$entriesWithData entries",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurface.copy(alpha = 0.6f),
                    )
                }
            }

            // Custom Canvas-based line chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(vertical = 16.dp)
            ) {
                val width = size.width
                val height = size.height
                val padding = 40f
                
                val chartWidth = width - padding * 2
                val chartHeight = height - padding * 2
                
                // Find max intensity for scaling
                val maxIntensity = dataPoints.maxOfOrNull { it.intensity } ?: 10f
                val minIntensity = 0f
                val intensityRange = max(maxIntensity - minIntensity, 1f)
                
                // Draw grid lines
                for (i in 0..5) {
                    val y = padding + (chartHeight / 5f) * i
                    drawLine(
                        color = surfaceVariant,
                        start = Offset(padding, y),
                        end = Offset(width - padding, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }
                
                // Draw axes
                drawLine(
                    color = onSurface.copy(alpha = 0.3f),
                    start = Offset(padding, padding),
                    end = Offset(padding, height - padding),
                    strokeWidth = 2f
                )
                drawLine(
                    color = onSurface.copy(alpha = 0.3f),
                    start = Offset(padding, height - padding),
                    end = Offset(width - padding, height - padding),
                    strokeWidth = 2f
                )
                
                // Draw line chart
                val path = Path()
                val stepX = if (dataPoints.size > 1) {
                    chartWidth / (dataPoints.size - 1)
                } else {
                    chartWidth / 2f // Center single point
                }
                
                dataPoints.forEachIndexed { index, point ->
                    val x = padding + stepX * index
                    val normalizedIntensity = (point.intensity - minIntensity) / intensityRange
                    val y = height - padding - (normalizedIntensity * chartHeight)
                    
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
                
                // Draw the line (only if there are multiple points)
                if (dataPoints.size > 1) {
                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(
                            width = 4f,
                            cap = StrokeCap.Round
                        )
                    )
                }
                
                // Draw points
                dataPoints.forEachIndexed { index, point ->
                    val x = padding + stepX * index
                    val normalizedIntensity = (point.intensity - minIntensity) / intensityRange
                    val y = height - padding - (normalizedIntensity * chartHeight)
                    
                    drawCircle(
                        color = primaryColor,
                        radius = 6f,
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3f,
                        center = Offset(x, y)
                    )
                }
            }
            
            // Date range and period info
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Date labels adapted to the selected range
                val axisLabels = buildAxisLabels(dataPoints, timeRange)
                if (axisLabels.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        axisLabels.forEach { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = onSurface.copy(alpha = 0.6f),
                            )
                        }
                    }
                }
                
                // Intensity scale indicator
                Text(
                    text = "Intensity: 0 (low) to 10 (high)",
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}

private val monthNames = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

private fun buildAxisLabels(
    dataPoints: List<IntensityDataPoint>,
    timeRange: TimeRange,
): List<String> {
    if (dataPoints.isEmpty()) return emptyList()

    return when (timeRange) {
        TimeRange.WEEK -> dataPoints.map { formatWeekLabel(it.date) }
        TimeRange.MONTH -> dataPoints.withIndex()
            .filter { (index, _) -> index % 4 == 0 || index == dataPoints.lastIndex }
            .map { formatMonthDayLabel(it.value.date) }
        TimeRange.YEAR -> buildYearLabels(dataPoints)
        TimeRange.MAX -> buildMaxLabels(dataPoints)
    }
}

private fun buildYearLabels(dataPoints: List<IntensityDataPoint>): List<String> {
    val labels = mutableListOf<String>()
    val firstMonthIndex = dataPoints.first().date.year * 12 + dataPoints.first().date.month.ordinal

    dataPoints.forEachIndexed { index, point ->
        val monthIndex = point.date.year * 12 + point.date.month.ordinal
        val include = (monthIndex - firstMonthIndex) % 3 == 0
        if (include || index == dataPoints.lastIndex) {
            val text = formatMonthYearLabel(point.date)
            if (labels.lastOrNull() != text) labels.add(text)
        }
    }

    return labels
}

private fun buildMaxLabels(dataPoints: List<IntensityDataPoint>): List<String> {
    val labels = mutableListOf<String>()
    labels.add(formatMonthDayLabel(dataPoints.first().date))

    if (dataPoints.size > 2) {
        labels.add(formatMonthDayLabel(dataPoints[dataPoints.size / 2].date))
    }

    if (dataPoints.size > 1) {
        val lastLabel = formatMonthDayLabel(dataPoints.last().date)
        if (labels.lastOrNull() != lastLabel) labels.add(lastLabel)
    }

    return labels
}

private fun formatWeekLabel(date: LocalDate): String {
    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val dayIndex = date.dayOfWeek.ordinal
    val dayName = dayNames.getOrElse(dayIndex) { date.dayOfWeek.name.take(3) }
    return "$dayName ${date.dayOfMonth}"
}

private fun formatMonthDayLabel(date: LocalDate): String {
    return "${monthNames[date.month.ordinal]} ${date.dayOfMonth}"
}

private fun formatMonthYearLabel(date: LocalDate): String {
    val yearShort = (date.year % 100).toString().padStart(2, '0')
    return "${monthNames[date.month.ordinal]} '$yearShort"
}

@Composable
private fun StatisticsCards(
    dataPoints: List<IntensityDataPoint>,
    modifier: Modifier = Modifier,
) {
    val nonZeroPoints = dataPoints.filter { it.intensity > 0 }
    
    if (nonZeroPoints.isEmpty()) return
    
    val intensities = nonZeroPoints.map { it.intensity }
    val average = intensities.average().toFloat()
    val min = intensities.minOrNull() ?: 0f
    val max = intensities.maxOrNull() ?: 0f
    
    val trend = if (nonZeroPoints.size >= 2) {
        val firstHalf = nonZeroPoints.take(nonZeroPoints.size / 2).map { it.intensity }.average()
        val secondHalf = nonZeroPoints.takeLast(nonZeroPoints.size / 2).map { it.intensity }.average()
        secondHalf - firstHalf
    } else {
        0.0
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                title = "Average",
                value = String.format("%.1f", average),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                title = "Min",
                value = String.format("%.1f", min),
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                title = "Max",
                value = String.format("%.1f", max),
                modifier = Modifier.weight(1f),
            )
            TrendCard(
                trend = trend.toFloat(),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun TrendCard(
    trend: Float,
    modifier: Modifier = Modifier,
) {
    val (icon, color, text) = when {
        trend > 0.5 -> Triple(
            Icons.AutoMirrored.Filled.TrendingUp,
            MaterialTheme.colorScheme.error,
            "Rising"
        )
        trend < -0.5 -> Triple(
            Icons.AutoMirrored.Filled.TrendingDown,
            MaterialTheme.colorScheme.tertiary,
            "Falling"
        )
        else -> Triple(
            Icons.Default.Info,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Stable"
        )
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Trend",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = color,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun EmptyProgressState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Timeline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "No progress data yet",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Add entries with intensity values to see trends",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

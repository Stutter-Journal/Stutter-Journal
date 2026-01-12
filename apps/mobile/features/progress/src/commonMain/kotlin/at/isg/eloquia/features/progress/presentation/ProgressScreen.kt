package at.isg.eloquia.features.progress.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.sp
import at.isg.eloquia.features.progress.presentation.model.IntensityDataPoint
import at.isg.eloquia.features.progress.presentation.model.ProgressUiState
import at.isg.eloquia.features.progress.presentation.model.TimeRange
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.min
import kotlin.math.round

@Composable
fun ProgressScreen(
    modifier: Modifier = Modifier,
    viewModel: ProgressViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val showEmptyDays by viewModel.showEmptyDays.collectAsState()
    val timeRange by viewModel.timeRange.collectAsState()
    val comparisonMode by viewModel.comparisonMode.collectAsState()
    val selectedSituations by viewModel.selectedSituations.collectAsState()
    val selectedTechniques by viewModel.selectedTechniques.collectAsState()

    ProgressScreenContent(
        state = state,
        showEmptyDays = showEmptyDays,
        timeRange = timeRange,
        comparisonMode = comparisonMode,
        selectedSituations = selectedSituations,
        selectedTechniques = selectedTechniques,
        onToggleEmptyDays = viewModel::toggleShowEmptyDays,
        onTimeRangeChange = viewModel::setTimeRange,
        onToggleComparisonMode = viewModel::toggleComparisonMode,
        onToggleSituation = viewModel::toggleSituation,
        onToggleTechnique = viewModel::toggleTechnique,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgressScreenContent(
    state: ProgressUiState,
    showEmptyDays: Boolean,
    timeRange: TimeRange,
    comparisonMode: Boolean,
    selectedSituations: Set<String>,
    selectedTechniques: Set<String>,
    onToggleEmptyDays: () -> Unit,
    onTimeRangeChange: (TimeRange) -> Unit,
    onToggleComparisonMode: () -> Unit,
    onToggleSituation: (String) -> Unit,
    onToggleTechnique: (String) -> Unit,
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
                    selectedTimeRange = state.selectedTimeRange,
                    frequencyData = state.frequencyData,
                    comparisonData = state.comparisonData,
                    availableSituations = state.availableSituations,
                    availableTechniques = state.availableTechniques,
                    showEmptyDays = showEmptyDays,
                    timeRange = timeRange,
                    comparisonMode = comparisonMode,
                    selectedSituations = selectedSituations,
                    selectedTechniques = selectedTechniques,
                    onToggleEmptyDays = onToggleEmptyDays,
                    onTimeRangeChange = onTimeRangeChange,
                    onToggleComparisonMode = onToggleComparisonMode,
                    onToggleSituation = onToggleSituation,
                    onToggleTechnique = onToggleTechnique,
                    modifier = Modifier.fillMaxSize().padding(padding),
                )
            }
        }
    }
}

@Composable
private fun ProgressContent(
    dataPoints: List<IntensityDataPoint>,
    selectedTimeRange: at.isg.eloquia.features.progress.presentation.model.SelectedTimeRange,
    frequencyData: at.isg.eloquia.features.progress.presentation.model.FrequencyData,
    comparisonData: at.isg.eloquia.features.progress.presentation.model.ComparisonData,
    availableSituations: List<String>,
    availableTechniques: List<String>,
    showEmptyDays: Boolean,
    timeRange: TimeRange,
    comparisonMode: Boolean,
    selectedSituations: Set<String>,
    selectedTechniques: Set<String>,
    onToggleEmptyDays: () -> Unit,
    onTimeRangeChange: (TimeRange) -> Unit,
    onToggleComparisonMode: () -> Unit,
    onToggleSituation: (String) -> Unit,
    onToggleTechnique: (String) -> Unit,
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
                showEmptyDays = showEmptyDays,
                modifier = Modifier.fillMaxWidth().height(280.dp),
            )
        }

        item {
            StatisticsCards(dataPoints = dataPoints)
        }

        // Mode toggle and content
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (comparisonMode) "Comparison Mode" else "Category Frequencies",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Switch(
                    checked = comparisonMode,
                    onCheckedChange = { onToggleComparisonMode() },
                )
            }
        }

        item {
            Text(
                text = "Data from ${selectedTimeRange.startDate} to ${selectedTimeRange.endDate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (comparisonMode) {
            // Comparison mode UI
            item {
                ComparisonModeContent(
                    comparisonData = comparisonData,
                    availableSituations = availableSituations,
                    availableTechniques = availableTechniques,
                    selectedSituations = selectedSituations,
                    selectedTechniques = selectedTechniques,
                    onToggleSituation = onToggleSituation,
                    onToggleTechnique = onToggleTechnique,
                )
            }
        } else {
            // Frequency charts
            item {
                FrequencyBarChart(
                    title = "Triggers",
                    data = frequencyData.triggers,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                FrequencyBarChart(
                    title = "Techniques",
                    data = frequencyData.techniques,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                FrequencyBarChart(
                    title = "Stutter Forms",
                    data = frequencyData.stutterForms,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
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
    showEmptyDays: Boolean,
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

            // Chart with proper axes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
            ) {
                // Y-axis labels (0-10 scale)
                Column(
                    modifier = Modifier.width(28.dp).height(220.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End,
                ) {
                    for (i in 10 downTo 0 step 2) {
                        Text(
                            text = i.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurface.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Chart canvas
                Column(modifier = Modifier.weight(1f)) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        val width = size.width
                        val height = size.height
                        val paddingTop = 8f
                        val paddingBottom = 8f
                        val paddingRight = 8f
                        
                        val chartWidth = width - paddingRight
                        val chartHeight = height - paddingTop - paddingBottom
                        
                        // Y-axis: Always 0 to 10
                        val minY = 0f
                        val maxY = 10f
                        val yRange = maxY - minY
                        
                        // Draw horizontal grid lines (for Y-axis values 0, 2, 4, 6, 8, 10)
                        for (i in 0..5) {
                            val yValue = i * 2f
                            val y = paddingTop + chartHeight - ((yValue - minY) / yRange * chartHeight)
                            drawLine(
                                color = surfaceVariant,
                                start = Offset(0f, y),
                                end = Offset(chartWidth, y),
                                strokeWidth = 1f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                            )
                        }
                        
                        // Calculate X positions for all data points
                        val singlePointX = chartWidth / 2f
                        val stepX = if (dataPoints.size > 1) {
                            chartWidth / (dataPoints.size - 1)
                        } else {
                            singlePointX
                        }
                        
                        // Draw vertical grid lines for ALL data points (all ticks visible)
                        dataPoints.forEachIndexed { index, _ ->
                            val x = if (dataPoints.size > 1) {
                                stepX * index
                            } else {
                                singlePointX
                            }
                            drawLine(
                                color = surfaceVariant.copy(alpha = 0.3f),
                                start = Offset(x, paddingTop),
                                end = Offset(x, height - paddingBottom),
                                strokeWidth = 1f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
                            )
                        }
                        
                        // Draw line chart path
                        val path = Path()
                        dataPoints.forEachIndexed { index, point ->
                            // val hasValue = point.intensity > 0f

                            val x = if (dataPoints.size > 1) {
                                stepX * index
                            } else {
                                singlePointX
                            }
                            val normalizedIntensity = (min(point.intensity, maxY) - minY) / yRange
                            val y = paddingTop + chartHeight - (normalizedIntensity * chartHeight)
                            
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
                                    width = 3f,
                                    cap = StrokeCap.Round
                                )
                            )
                        }
                        
                        // Draw data points
                        dataPoints.forEachIndexed { index, point ->
                            val hasValue = point.intensity > 0f
                            val x = if (dataPoints.size > 1) {
                                stepX * index
                            } else {
                                singlePointX
                            }
                            val normalizedIntensity = (min(point.intensity, maxY) - minY) / yRange
                            val y = paddingTop + chartHeight - (normalizedIntensity * chartHeight)
                            
                            // Respect toggle: draw only real data when hiding empty days
                            if (showEmptyDays || hasValue) {
                                drawCircle(
                                    color = primaryColor,
                                    radius = 5f,
                                    center = Offset(x, y)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.5f,
                                    center = Offset(x, y)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
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
                value = oneDecimal(average),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                title = "Min",
                value = oneDecimal(min),
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                title = "Max",
                value = oneDecimal(max),
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

private fun oneDecimal(value: Float): String {
    val rounded = round(value * 10f) / 10f
    return if (rounded % 1f == 0f) "${rounded.toInt()}.0" else rounded.toString()
}

/**
 * Minimum absolute trend value required to classify a trend as clearly
 * "Rising" or "Falling". Trends in the range [-TREND_THRESHOLD, TREND_THRESHOLD]
 * are treated as "Stable" to avoid marking small fluctuations as significant.
 */
private const val TREND_THRESHOLD = 0.5f

@Composable
private fun TrendCard(
    trend: Float,
    modifier: Modifier = Modifier,
) {
    val (icon, color, text) = when {
        trend > TREND_THRESHOLD -> Triple(
            Icons.AutoMirrored.Filled.TrendingUp,
            MaterialTheme.colorScheme.error,
            "Rising"
        )
        trend < -TREND_THRESHOLD -> Triple(
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

/**
 * Displays a horizontal bar chart for category frequency data.
 * This is completely separate from the time-series progress chart.
 * It shows counts/frequency, not temporal data.
 */
@Composable
private fun FrequencyBarChart(
    title: String,
    data: List<at.isg.eloquia.features.progress.presentation.model.CategoryFrequency>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) {
        return
    }

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
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            val maxCount = data.maxOfOrNull { it.count } ?: 1

            data.forEach { item ->
                FrequencyBarItem(
                    category = item.category,
                    count = item.count,
                    maxCount = maxCount,
                    color = color,
                )
            }
        }
    }
}

@Composable
private fun FrequencyBarItem(
    category: String,
    count: Int,
    maxCount: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }

        // Horizontal bar
        val fraction = if (maxCount > 0) count.toFloat() / maxCount.toFloat() else 0f
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small,
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .background(
                        color = color,
                        shape = MaterialTheme.shapes.small,
                    )
            )
        }
    }
}

@Composable
private fun ComparisonModeContent(
    comparisonData: at.isg.eloquia.features.progress.presentation.model.ComparisonData,
    availableSituations: List<String>,
    availableTechniques: List<String>,
    selectedSituations: Set<String>,
    selectedTechniques: Set<String>,
    onToggleSituation: (String) -> Unit,
    onToggleTechnique: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        CategoryComparisonCard(
            title = "Situations / Triggers",
            categories = availableSituations,
            selectedCategories = selectedSituations,
            onToggleCategory = onToggleSituation,
            emptyMessage = "No situations available in this time range",
            selectMessage = "Select situations to compare:",
            chartTitle = "Average Intensity by Situation",
            chartData = comparisonData.situationData,
            chartColor = MaterialTheme.colorScheme.error,
        )

        CategoryComparisonCard(
            title = "Techniques / Methods",
            categories = availableTechniques,
            selectedCategories = selectedTechniques,
            onToggleCategory = onToggleTechnique,
            emptyMessage = "No techniques available in this time range",
            selectMessage = "Select techniques to compare:",
            chartTitle = "Average Intensity by Technique",
            chartData = comparisonData.techniqueData,
            chartColor = MaterialTheme.colorScheme.tertiary,
        )
    }
}

@Composable
private fun CategoryComparisonCard(
    title: String,
    categories: List<String>,
    selectedCategories: Set<String>,
    onToggleCategory: (String) -> Unit,
    emptyMessage: String,
    selectMessage: String,
    chartTitle: String,
    chartData: List<at.isg.eloquia.features.progress.presentation.model.ComparisonDataPoint>,
    chartColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            if (categories.isEmpty()) {
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = selectMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                CategorySelectionGrid(
                    categories = categories,
                    selectedCategories = selectedCategories,
                    onToggleCategory = onToggleCategory,
                )

                if (selectedCategories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ComparisonChart(
                        title = chartTitle,
                        data = chartData,
                        color = chartColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySelectionGrid(
    categories: List<String>,
    selectedCategories: Set<String>,
    onToggleCategory: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Create rows with 2 items each
        categories.chunked(2).forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowCategories.forEach { category ->
                    FilterChip(
                        selected = category in selectedCategories,
                        onClick = { onToggleCategory(category) },
                        label = { Text(category) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Add spacer if row has only one item
                if (rowCategories.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ComparisonChart(
    title: String,
    data: List<at.isg.eloquia.features.progress.presentation.model.ComparisonDataPoint>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "No data available for selected categories",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
        )

        val maxIntensity = 10f // Fixed scale 0-10

        data.forEach { item ->
            ComparisonBarItem(
                category = item.category,
                averageIntensity = item.averageIntensity,
                count = item.count,
                maxIntensity = maxIntensity,
                color = color,
            )
        }
    }
}

@Composable
private fun ComparisonBarItem(
    category: String,
    averageIntensity: Float,
    count: Int,
    maxIntensity: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "$count ${if (count == 1) "entry" else "entries"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = run {
                    val rounded = kotlin.math.round(averageIntensity * 10) / 10
                    rounded.toString()
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }

        // Horizontal bar
        val fraction = if (maxIntensity > 0) averageIntensity / maxIntensity else 0f
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small,
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(
                        color = color,
                        shape = MaterialTheme.shapes.small,
                    )
            )
        }
    }
}

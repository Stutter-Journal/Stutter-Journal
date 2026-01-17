@file:Suppress("D")

package at.isg.eloquia.features.entries.presentation.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import at.isg.eloquia.core.domain.entries.model.JournalEntry
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Instant

@Composable
fun EntriesListScreen(
    viewModel: EntriesListViewModel = koinViewModel(),
    onEntryClick: (JournalEntry) -> Unit = {},
    onCreateEntry: () -> Unit = viewModel::createQuickEntry,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val deleteEntryAction = remember(viewModel) {
        { entry: JournalEntry -> viewModel.deleteEntry(entry.id) }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is EntriesListEvent.DeletionFailed -> snackbarHostState.showSnackbar(event.reason)
                is EntriesListEvent.EntryDeleted -> Unit
            }
        }
    }

    EntriesListScreenContent(
        state = state,
        onEntryClick = onEntryClick,
        onDeleteEntry = deleteEntryAction,
        onCreateEntry = onCreateEntry,
        onQueryChange = viewModel::updateQuery,
        onToggleSituation = viewModel::toggleSituation,
        onToggleTechnique = viewModel::toggleTechnique,
        onDateRangeChange = viewModel::updateDateRange,
        onClearDateRange = viewModel::clearDateRange,
        onSortOrderChange = viewModel::setSortOrder,
        onGroupByDayChange = viewModel::setGroupByDay,
        onResetFilters = viewModel::resetFilters,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EntriesListScreenContent(
    state: EntriesListState,
    onEntryClick: (JournalEntry) -> Unit,
    onDeleteEntry: (JournalEntry) -> Unit,
    onCreateEntry: () -> Unit,
    onQueryChange: (String) -> Unit,
    onToggleSituation: (String) -> Unit,
    onToggleTechnique: (String) -> Unit,
    onDateRangeChange: (LocalDate?, LocalDate?) -> Unit,
    onClearDateRange: () -> Unit,
    onSortOrderChange: (EntriesSortOrder) -> Unit,
    onGroupByDayChange: (Boolean) -> Unit,
    onResetFilters: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    var entryPendingDeletion by remember { mutableStateOf<JournalEntry?>(null) }
    var isFilterSheetVisible by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val contentState = state as? EntriesListState.Content
    val filters = contentState?.filters ?: EntriesFilters()

    val fabVisible by remember(listState) {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 || !listState.canScrollForward
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(text = "Entries") },
                    actions = {
                        IconButton(
                            onClick = { if (contentState != null) isFilterSheetVisible = true },
                            enabled = contentState != null,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FilterAlt,
                                contentDescription = "Filters",
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                )

                OutlinedTextField(
                    value = filters.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true,
                    placeholder = { Text("Search entries") },
                )
            }
        },
        floatingActionButton = {
            EntriesFabMenu(
                onCreateEntry = onCreateEntry,
                fabVisible = fabVisible,
            )
        },
    ) { padding ->
        EntriesContent(
            state = state,
            onEntryClick = onEntryClick,
            onDeleteRequest = { entryPendingDeletion = it },
            listState = listState,
            modifier = Modifier.padding(padding),
        )
    }

    if (isFilterSheetVisible && contentState != null) {
        EntriesFilterBottomSheet(
            content = contentState,
            onDismiss = { isFilterSheetVisible = false },
            onToggleSituation = onToggleSituation,
            onToggleTechnique = onToggleTechnique,
            onDateRangeChange = onDateRangeChange,
            onClearDateRange = onClearDateRange,
            onSortOrderChange = onSortOrderChange,
            onGroupByDayChange = onGroupByDayChange,
            onResetFilters = onResetFilters,
        )
    }

    EntryDeleteConfirmationDialog(
        entryTitle = entryPendingDeletion?.title,
        onConfirm = {
            entryPendingDeletion?.let(onDeleteEntry)
            entryPendingDeletion = null
        },
        onDismiss = { entryPendingDeletion = null },
    )
}

@Composable
private fun EntriesContent(
    state: EntriesListState,
    onEntryClick: (JournalEntry) -> Unit,
    onDeleteRequest: (JournalEntry) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (state) {
            is EntriesListState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            is EntriesListState.Content -> {
                if (state.entries.isEmpty()) {
                    EmptyEntriesState(modifier = Modifier.fillMaxSize())
                } else {
                    EntriesList(
                        entries = state.entries,
                        groupedEntries = state.groupedEntries,
                        groupByDay = state.filters.groupByDay,
                        onEntryClick = onEntryClick,
                        onDeleteRequest = onDeleteRequest,
                        listState = listState,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            is EntriesListState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
private fun EntriesList(
    entries: List<JournalEntry>,
    groupedEntries: List<Pair<LocalDate, List<JournalEntry>>>,
    groupByDay: Boolean,
    onEntryClick: (JournalEntry) -> Unit,
    onDeleteRequest: (JournalEntry) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (groupByDay) {
            groupedEntries.forEach { (date, dayEntries) ->
                item(key = "header_$date") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                text = date.toString(),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }

                items(dayEntries, key = { it.id }) { entry ->
                    JournalEntryItem(
                        entry = entry,
                        onOpen = { onEntryClick(entry) },
                        onDelete = { onDeleteRequest(entry) },
                    )
                }
            }
        } else {
            items(entries, key = { it.id }) { entry ->
                JournalEntryItem(
                    entry = entry,
                    onOpen = { onEntryClick(entry) },
                    onDelete = { onDeleteRequest(entry) },
                )
            }
        }
    }
}

@Composable
private fun JournalEntryItem(
    entry: JournalEntry,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val details = remember(entry) { entry.toDisplayDetails() }
    var expanded by rememberSaveable(entry.id) { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        onClick = onOpen,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                EntryHeaderRow(
                    title = details.title.ifBlank { "Journal entry" },
                    subtitle = "${details.dateLabel} • ${details.timeLabel}",
                    onDelete = onDelete,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EntryDescriptionGroup(description = details.description)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                EntryIntensityCompact(intensity = details.intensity)
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Hide" else "Details")
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    EntryTagsSection(
                        tags = entry.tags,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EntryHeaderRow(
    title: String,
    subtitle: String,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(
            onClick = onDelete,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete entry")
        }
    }
}

@Composable
private fun EntryDescriptionGroup(description: String) {
    Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun EntryIntensityCompact(intensity: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Timeline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Intensity $intensity/10",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EntryTagsSection(
    tags: List<String>,
    modifier: Modifier = Modifier,
) {
    val visibleTags = tags
        .asSequence()
        .filterNot { it.startsWith("date:", ignoreCase = true) }
        .filterNot { it.startsWith("intensity:", ignoreCase = true) }
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toList()

    if (visibleTags.isEmpty()) {
        Text(
            text = "No tags",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        return
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Tags",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            visibleTags.forEach { tag ->
                Surface(
                    color = Color.Transparent,
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun EntryDeleteConfirmationDialog(
    entryTitle: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (entryTitle == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete entry?") },
        text = {
            Text(
                text = "\"${entryTitle.ifBlank { "Untitled entry" }}\" will be removed permanently.",
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

private fun LocalDateTime.formatTimeLabel(): String {
    val time = this.time
    val hour = time.hour.toString().padStart(2, '0')
    val minute = time.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EntriesFabMenu(
    onCreateEntry: () -> Unit,
    fabVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    EntriesBackHandler(enabled = fabMenuExpanded) { fabMenuExpanded = false }

    val items = remember {
        listOf(
            Icons.AutoMirrored.Filled.Message to "New entry",
        )
    }

    FloatingActionButtonMenu(
        modifier = modifier,
        expanded = fabMenuExpanded,
        button = {
            TooltipBox(
                positionProvider =
                TooltipDefaults.rememberTooltipPositionProvider(
                    if (fabMenuExpanded) TooltipAnchorPosition.Start else TooltipAnchorPosition.Above,
                ),
                tooltip = { PlainTooltip { Text("Toggle menu") } },
                state = rememberTooltipState(),
            ) {
                ToggleFloatingActionButton(
                    modifier =
                    Modifier
                        .semantics {
                            traversalIndex = -1f
                            stateDescription = if (fabMenuExpanded) "Expanded" else "Collapsed"
                            contentDescription = "Toggle menu"
                        }
                        .animateFloatingActionButton(
                            visible = fabVisible || fabMenuExpanded,
                            alignment = Alignment.BottomEnd,
                        )
                        .focusRequester(focusRequester),
                    checked = fabMenuExpanded,
                    onCheckedChange = { fabMenuExpanded = !fabMenuExpanded },
                ) {
                    val imageVector by remember {
                        derivedStateOf {
                            if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                        }
                    }
                    Icon(
                        painter = rememberVectorPainter(imageVector),
                        contentDescription = null,
                        modifier = Modifier.animateIcon({ checkedProgress }),
                    )
                }
            }
        },
    ) {
        items.forEachIndexed { index, item ->
            FloatingActionButtonMenuItem(
                modifier =
                Modifier
                    .semantics {
                        isTraversalGroup = true
                        if (index == items.size - 1) {
                            customActions =
                                listOf(
                                    CustomAccessibilityAction(
                                        label = "Close menu",
                                        action = {
                                            fabMenuExpanded = false
                                            true
                                        },
                                    ),
                                )
                        }
                    }
                    .then(
                        if (index == 0) {
                            Modifier.onKeyEvent {
                                if (
                                    it.type == KeyEventType.KeyDown &&
                                    (it.key == Key.DirectionUp || (it.isShiftPressed && it.key == Key.Tab))
                                ) {
                                    focusRequester.requestFocus()
                                    return@onKeyEvent true
                                }
                                false
                            }
                        } else {
                            Modifier
                        },
                    ),
                onClick = {
                    // Only the first action maps to app behavior for now.
                    if (index == 0) onCreateEntry()
                    fabMenuExpanded = false
                },
                icon = { Icon(item.first, contentDescription = null) },
                text = { Text(text = item.second) },
            )
        }
    }
}

private data class EntryDisplayDetails(
    val title: String,
    val description: String,
    val dateLabel: String,
    val timeLabel: String,
    val intensity: Int,
)

private fun JournalEntry.toDisplayDetails(): EntryDisplayDetails {
    val description = content.ifBlank { title }.ifBlank { "No notes yet" }
    val intensity = tags.firstNotNullOfOrNull(::parseIntensityTag) ?: DEFAULT_INTENSITY
    val date = tags.firstNotNullOfOrNull(::parseDateTag) ?: createdAt.date
    return EntryDisplayDetails(
        title = title,
        description = description,
        dateLabel = date.toString(),
        timeLabel = createdAt.formatTimeLabel(),
        intensity = intensity,
    )
}

private fun parseDateTag(tag: String): LocalDate? = tag.takeIf { it.startsWith("date:", ignoreCase = true) }
    ?.substringAfter(":")
    ?.trim()
    ?.let { raw -> runCatching { LocalDate.parse(raw) }.getOrNull() }

private fun parseIntensityTag(tag: String): Int? = when {
    tag.startsWith("intensity:", ignoreCase = true) -> tag.substringAfter(":").trim().toIntOrNull()
    tag.startsWith("Intensity", ignoreCase = true) -> tag.substringAfterLast(' ').trim()
        .toIntOrNull()

    else -> null
}

private const val DEFAULT_INTENSITY = 5

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
)
@Composable
private fun EntriesFilterBottomSheet(
    content: EntriesListState.Content,
    onDismiss: () -> Unit,
    onToggleSituation: (String) -> Unit,
    onToggleTechnique: (String) -> Unit,
    onDateRangeChange: (LocalDate?, LocalDate?) -> Unit,
    onClearDateRange: () -> Unit,
    onSortOrderChange: (EntriesSortOrder) -> Unit,
    onGroupByDayChange: (Boolean) -> Unit,
    onResetFilters: () -> Unit,
) {
    var isDateRangeDialogVisible by remember { mutableStateOf(false) }
    val timeZone = remember { TimeZone.currentSystemDefault() }

    val datePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = content.filters.dateRange.start?.toEpochMillisAtStart(
            timeZone,
        ),
        initialSelectedEndDateMillis = content.filters.dateRange.endInclusive?.toEpochMillisAtStart(
            timeZone,
        ),
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Filters", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onResetFilters) {
                    Text("Reset")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Date range", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = content.filters.dateRange.toLabel(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { isDateRangeDialogVisible = true }) { Text("Pick") }
                    if (content.filters.dateRange.isActive) {
                        TextButton(onClick = onClearDateRange) { Text("Clear") }
                    }
                }
            }

            HorizontalDivider()

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Sort", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    EntriesSortOrder.entries.forEach { option ->
                        FilterChip(
                            selected = option == content.filters.sortOrder,
                            onClick = { onSortOrderChange(option) },
                            label = { Text(option.toLabel()) },
                            leadingIcon = if (option == content.filters.sortOrder) {
                                {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            } else {
                                null
                            },
                            colors = FilterChipDefaults.filterChipColors(),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Group by day", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "Show date headers",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = content.filters.groupByDay,
                    onCheckedChange = onGroupByDayChange,
                )
            }

            if (content.availableSituations.isNotEmpty()) {
                HorizontalDivider()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Situations", style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        content.availableSituations.forEach { label ->
                            val selected = label in content.filters.selectedSituations
                            FilterChip(
                                selected = selected,
                                onClick = { onToggleSituation(label) },
                                label = { Text(label) },
                                leadingIcon = if (selected) {
                                    {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                } else {
                                    null
                                },
                            )
                        }
                    }
                }
            }

            if (content.availableTechniques.isNotEmpty()) {
                HorizontalDivider()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Techniques", style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        content.availableTechniques.forEach { label ->
                            val selected = label in content.filters.selectedTechniques
                            FilterChip(
                                selected = selected,
                                onClick = { onToggleTechnique(label) },
                                label = { Text(label) },
                                leadingIcon = if (selected) {
                                    {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                } else {
                                    null
                                },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (isDateRangeDialogVisible) {
        DatePickerDialog(
            onDismissRequest = { isDateRangeDialogVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val start =
                            datePickerState.selectedStartDateMillis?.toLocalDateInSystemZone(
                                timeZone,
                            )
                        val end =
                            datePickerState.selectedEndDateMillis?.toLocalDateInSystemZone(timeZone)
                        onDateRangeChange(start, end)
                        isDateRangeDialogVisible = false
                    },
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDateRangeDialogVisible = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DateRangePicker(state = datePickerState)
        }
    }
}

private fun EntriesSortOrder.toLabel(): String = when (this) {
    EntriesSortOrder.DateDesc -> "Date (newest)"
    EntriesSortOrder.DateAsc -> "Date (oldest)"
    EntriesSortOrder.IntensityDesc -> "Intensity (high)"
    EntriesSortOrder.IntensityAsc -> "Intensity (low)"
}

private fun EntriesDateRange.toLabel(): String {
    if (!isActive) return "Any time"
    val startLabel = start?.toString() ?: "…"
    val endLabel = endInclusive?.toString() ?: "…"
    return "$startLabel → $endLabel"
}

private fun LocalDate.toEpochMillisAtStart(timeZone: TimeZone = TimeZone.currentSystemDefault()): Long = atStartOfDayIn(timeZone).toEpochMilliseconds()

private fun Long.toLocalDateInSystemZone(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate = Instant.fromEpochMilliseconds(this).toLocalDateTime(timeZone).date

@Composable
private fun EmptyEntriesState(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = EntriesIcons.calendarToday,
            contentDescription = "No entries",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No entries yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start tracking your progress by creating your first journal entry",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

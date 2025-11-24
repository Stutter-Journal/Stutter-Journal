package at.isg.eloquia.features.entries.presentation.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import at.isg.eloquia.core.domain.entries.model.JournalEntry
import kotlinx.datetime.LocalDateTime
import org.koin.compose.viewmodel.koinViewModel

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
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    var entryPendingDeletion by remember { mutableStateOf<JournalEntry?>(null) }
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            EntriesFabMenu(onCreateEntry = onCreateEntry)
        },
    ) { padding ->
        EntriesContent(
            state = state,
            onEntryClick = onEntryClick,
            onDeleteRequest = { entryPendingDeletion = it },
            modifier = Modifier.padding(padding),
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
                        onEntryClick = onEntryClick,
                        onDeleteRequest = onDeleteRequest,
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
    onEntryClick: (JournalEntry) -> Unit,
    onDeleteRequest: (JournalEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(entries) { entry ->
            val details = entry.toDisplayDetails()
            JournalEntryItem(
                details = details,
                onOpen = { onEntryClick(entry) },
                onDelete = { onDeleteRequest(entry) },
            )
        }
    }
}

@Composable
private fun JournalEntryItem(
    details: EntryDisplayDetails,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        onClick = onOpen,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EntryHeaderRow(title = details.title.ifBlank { "Journal entry" }, onDelete = onDelete)
            EntryDateTimeGroup(dateLabel = details.dateLabel, timeLabel = details.timeLabel)
            EntryDescriptionGroup(description = details.description)
            EntryIntensityGroup(intensity = details.intensity)
        }
    }
}

@Composable
private fun EntryHeaderRow(title: String, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        IconButton(
            onClick = onDelete,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
        ) {
            Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete entry")
        }
    }
}

@Composable
private fun EntryDateTimeGroup(dateLabel: String, timeLabel: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconLabel(icon = Icons.Outlined.CalendarMonth, text = dateLabel)
        IconLabel(icon = Icons.Outlined.Schedule, text = timeLabel)
    }
}

@Composable
private fun EntryDescriptionGroup(description: String) {
    Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 4,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun EntryIntensityGroup(intensity: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                imageVector = Icons.Outlined.Timeline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Intensity",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "$intensity / 10",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        LinearProgressIndicator(
            progress = { intensity.coerceIn(1, 10) / 10f },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun IconLabel(icon: ImageVector, text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
    modifier: Modifier = Modifier,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = true,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        ),
        exit = scaleOut(),
        modifier = modifier,
    ) {
        FloatingActionButtonMenu(
            expanded = isMenuExpanded,
            button = {
                ToggleFloatingActionButton(
                    checked = isMenuExpanded,
                    onCheckedChange = { isMenuExpanded = it },
                ) {
                    val imageVector by remember(isMenuExpanded) {
                        derivedStateOf {
                            if (isMenuExpanded) Icons.Default.Close else Icons.Default.Add
                        }
                    }
                    Icon(
                        imageVector = imageVector,
                        contentDescription = if (isMenuExpanded) "Close Menu" else "Create Entry",
                        modifier = Modifier.size(24.dp),
                    )
                }
            },
        ) {
            FloatingActionButtonMenuItem(
                onClick = {
                    onCreateEntry()
                    isMenuExpanded = false
                },
                icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                text = { Text(text = "New Entry") },
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
    return EntryDisplayDetails(
        title = title,
        description = description,
        dateLabel = createdAt.date.toString(),
        timeLabel = createdAt.formatTimeLabel(),
        intensity = intensity,
    )
}

private fun parseIntensityTag(tag: String): Int? = when {
    tag.startsWith("intensity:", ignoreCase = true) -> tag.substringAfter(":").trim().toIntOrNull()
    tag.startsWith("Intensity", ignoreCase = true) -> tag.substringAfterLast(' ').trim().toIntOrNull()
    else -> null
}

private const val DEFAULT_INTENSITY = 5

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

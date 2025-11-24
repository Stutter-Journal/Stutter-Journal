package at.isg.eloquia.features.entries.presentation.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
    onEditEntry: (JournalEntry) -> Unit = onEntryClick,
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
        onEditEntry = onEditEntry,
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
    onEditEntry: (JournalEntry) -> Unit,
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
            onEditEntry = onEditEntry,
            onDeleteRequest = { entryPendingDeletion = it },
            modifier = Modifier.padding(padding),
        )
    }

    EntryDeleteConfirmationDialog(
        entryTitle = entryPendingDeletion?.title,
        onConfirm = {
            entryPendingDeletion?.let(onDeleteEntry)
        },
        onDismiss = { },
    )
}

@Composable
private fun EntriesContent(
    state: EntriesListState,
    onEntryClick: (JournalEntry) -> Unit,
    onEditEntry: (JournalEntry) -> Unit,
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
                        onEditEntry = onEditEntry,
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
    onEditEntry: (JournalEntry) -> Unit,
    onDeleteRequest: (JournalEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(entries) { entry ->
            JournalEntryItem(
                entry = entry,
                onClick = { onEntryClick(entry) },
                onEdit = { onEditEntry(entry) },
                onDelete = { onDeleteRequest(entry) },
            )
        }
    }
}

@Composable
private fun JournalEntryItem(
    entry: JournalEntry,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = entry.title.ifBlank { "Untitled entry" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = entry.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                if (entry.tags.isNotEmpty()) {
                    Text(
                        text = entry.tags.joinToString(separator = " · ", limit = 3),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = entry.createdAt.date.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = entry.createdAt.formatTimeLabel(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onEdit) {
                        Text("Edit")
                    }
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text("Delete")
                    }
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
                    onCheckedChange = { },
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
                },
                icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                text = { Text(text = "New Entry") },
            )
        }
    }
}

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

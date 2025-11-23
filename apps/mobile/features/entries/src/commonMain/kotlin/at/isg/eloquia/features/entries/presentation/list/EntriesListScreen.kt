package at.isg.eloquia.features.entries.presentation.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import at.isg.eloquia.features.entries.domain.model.JournalEntry

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EntriesListScreenContent(
    state: EntriesListState,
    onEntryClick: (JournalEntry) -> Unit,
    onCreateEntry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            EntriesFabMenu(onCreateEntry = onCreateEntry)
        },
    ) { padding ->
        EntriesContent(
            state = state,
            onEntryClick = onEntryClick,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
private fun EntriesContent(
    state: EntriesListState,
    onEntryClick: (JournalEntry) -> Unit,
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
                    // TODO: Show list of entries
                    EmptyEntriesState(modifier = Modifier.fillMaxSize())
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
                    isMenuExpanded = false
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

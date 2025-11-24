package at.isg.eloquia.features.entries.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.isg.eloquia.core.domain.entries.model.JournalEntry
import kotlinx.datetime.LocalTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun EntryDetailScreen(
//    entryId: String,
//    onBack: () -> Unit,
//    onEdit: (String) -> Unit,
//    modifier: Modifier = Modifier,
//    viewModel: EntryDetailViewModel = koinViewModel(parameters = { parametersOf(entryId) }),
//) {
//    val state by viewModel.state.collectAsState()
//    val entry = state.entry
//    var showDeleteDialog by remember { mutableStateOf(false) }
//
//    LaunchedEffect(Unit) {
//        viewModel.events.collect { event ->
//            when (event) {
//                EntryDetailEvent.EntryDeleted -> onBack()
//            }
//        }
//    }
//
//    Scaffold(
//        modifier = modifier,
//        topBar = {
//            TopAppBar(
//                title = { Text("Entry Details") },
//                navigationIcon = {
//                    IconButton(onClick = onBack) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
//                    }
//                },
//                actions = {
//                    val entryAvailable = entry != null
//                    IconButton(
//                        onClick = { entry?.let { onEdit(it.id) } }, enabled = entryAvailable
//                    ) {
//                        Icon(Icons.Default.Edit, contentDescription = "Edit entry")
//                    }
//                    IconButton(
//                        onClick = { if (entryAvailable) showDeleteDialog = true },
//                        enabled = entryAvailable
//                    ) {
//                        Icon(Icons.Default.Delete, contentDescription = "Delete entry")
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
//            )
//        },
//    ) { padding ->
//        when {
//            state.isLoading -> {
//                Column(
//                    modifier = Modifier.fillMaxSize().padding(padding),
//                    verticalArrangement = Arrangement.Center,
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                ) {
//                    CircularProgressIndicator()
//                }
//            }
//
//            entry == null -> {
//                Column(
//                    modifier = Modifier.fillMaxSize().padding(padding),
//                    verticalArrangement = Arrangement.Center,
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                ) {
//                    Text(
//                        text = state.errorMessage ?: "Entry not found",
//                        color = MaterialTheme.colorScheme.error
//                    )
//                    Spacer(modifier = Modifier.height(12.dp))
//                    Button(onClick = onBack) {
//                        Text("Go back")
//                    }
//                }
//            }
//
//            else -> {
//                EntryDetailContent(
//                    entry = entry,
//                    modifier = Modifier.padding(padding),
//                )
//            }
//        }
//    }
//
//    if (showDeleteDialog) {
//        AlertDialog(
//            onDismissRequest = { showDeleteDialog = false },
//            title = { Text("Delete this entry?") },
//            text = { Text("This action cannot be undone. Are you sure?") },
//            confirmButton = {
//                Button(onClick = {
//                    showDeleteDialog = false
//                    viewModel.deleteEntry()
//                }) {
//                    Text("Delete")
//                }
//            },
//            dismissButton = {
//                Button(onClick = { showDeleteDialog = false }) {
//                    Text("Cancel")
//                }
//            },
//        )
//    }
//}
//
//@Composable
//private fun EntryDetailContent(entry: JournalEntry, modifier: Modifier = Modifier) {
//    Surface(modifier = modifier.fillMaxSize()) {
//        Column(
//            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp),
//        ) {
//            Text(text = entry.title, style = MaterialTheme.typography.headlineSmall)
//            Text(
//                text = "Created ${entry.createdAt.date} at ${entry.createdAt.time.dropSeconds()}",
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//            )
//            Text(text = entry.content, style = MaterialTheme.typography.bodyLarge)
//
//            if (entry.tags.isNotEmpty()) {
//                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                    Text("Tags", style = MaterialTheme.typography.titleMedium)
//                    LazyRow(
//                        horizontalArrangement = Arrangement.spacedBy(8.dp),
//                        contentPadding = PaddingValues(vertical = 4.dp),
//                    ) {
//                        items(entry.tags) { tag ->
//                            Surface(
//                                color = MaterialTheme.colorScheme.secondaryContainer,
//                                shape = MaterialTheme.shapes.small,
//                            ) {
//                                Text(
//                                    text = tag,
//                                    modifier = Modifier.padding(
//                                        horizontal = 12.dp, vertical = 6.dp
//                                    ),
//                                    style = MaterialTheme.typography.labelLarge,
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//private fun LocalTime.dropSeconds(): String = "%02d:%02d".format(hour, minute)

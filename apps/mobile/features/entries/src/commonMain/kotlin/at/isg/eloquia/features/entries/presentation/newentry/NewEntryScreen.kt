package at.isg.eloquia.features.entries.presentation.newentry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

@Immutable
data class MultiSelectOption(
    val id: String,
    val label: String,
)

@Stable
data class NewEntryUiState(
    val date: LocalDate = LocalDate(1970, 1, 1),
    val intensity: Int = 5,
    val intensityRange: IntRange = 1..10,
    val triggers: List<MultiSelectOption> = emptyList(),
    val selectedTriggerIds: Set<String> = emptySet(),
    val customTrigger: String = "",
    val methods: List<MultiSelectOption> = emptyList(),
    val selectedMethodIds: Set<String> = emptySet(),
    val customMethod: String = "",
    val notes: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    val dateDisplay: String get() = date.toString()
    val canSave: Boolean
        get() = notes.isNotBlank() || selectedTriggerIds.isNotEmpty() || selectedMethodIds.isNotEmpty() || customTrigger.isNotBlank() || customMethod.isNotBlank()
}

@Immutable
data class NewEntryCallbacks(
    val onClose: () -> Unit,
    val onEditDate: () -> Unit,
    val onIntensityChange: (Int) -> Unit,
    val onToggleTrigger: (String) -> Unit,
    val onCustomTriggerChange: (String) -> Unit,
    val onToggleMethod: (String) -> Unit,
    val onCustomMethodChange: (String) -> Unit,
    val onNotesChange: (String) -> Unit,
    val onCancel: () -> Unit,
    val onSave: () -> Unit,
)

@Composable
fun NewEntryScreen(
    onClose: () -> Unit,
    onEntrySaved: () -> Unit,
    modifier: Modifier = Modifier,
    onEditDate: (currentDate: LocalDate, onDateSelected: (LocalDate) -> Unit) -> Unit = { _, _ -> },
    viewModel: NewEntryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                NewEntryEvent.EntrySaved -> onEntrySaved()
            }
        }
    }

    NewEntryScreenContent(
        state = state,
        callbacks = NewEntryCallbacks(
            onClose = onClose,
            onEditDate = { onEditDate(state.date, viewModel::setEntryDate) },
            onIntensityChange = viewModel::setIntensity,
            onToggleTrigger = viewModel::toggleTrigger,
            onCustomTriggerChange = viewModel::updateCustomTrigger,
            onToggleMethod = viewModel::toggleMethod,
            onCustomMethodChange = viewModel::updateCustomMethod,
            onNotesChange = viewModel::updateNotes,
            onCancel = {
                viewModel.resetForm()
                onClose()
            },
            onSave = viewModel::saveEntry,
        ),
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEntryScreenContent(
    state: NewEntryUiState,
    callbacks: NewEntryCallbacks,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "New Journal Entry",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "Capture today’s progress",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = callbacks.onClose) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                SectionCard(title = "Date", icon = Icons.Outlined.CalendarMonth) {
                    FormRow(
                        title = state.dateDisplay,
                        subtitle = "Tap to change",
                        onClick = callbacks.onEditDate,
                    )
                }
            }

            item {
                SectionCard(title = "Stutter Intensity") {
                    IntensitySlider(
                        value = state.intensity,
                        range = state.intensityRange,
                        onValueChange = callbacks.onIntensityChange,
                    )
                }
            }

            item {
                SectionCard(title = "Possible Triggers") {
                    MultiSelectSection(
                        options = state.triggers,
                        selectedIds = state.selectedTriggerIds,
                        onToggle = callbacks.onToggleTrigger,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.customTrigger,
                        onValueChange = callbacks.onCustomTriggerChange,
                        placeholder = { Text("Other trigger…") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            }

            item {
                SectionCard(title = "Therapy Methods Used") {
                    MultiSelectSection(
                        options = state.methods,
                        selectedIds = state.selectedMethodIds,
                        onToggle = callbacks.onToggleMethod,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.customMethod,
                        onValueChange = callbacks.onCustomMethodChange,
                        placeholder = { Text("Other method…") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            }

            item {
                SectionCard(title = "Additional Notes", icon = Icons.Outlined.NoteAlt) {
                    OutlinedTextField(
                        value = state.notes,
                        onValueChange = callbacks.onNotesChange,
                        placeholder = { Text("How did you feel? What worked? Any observations…") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                    )
                }
            }

            if (state.errorMessage != null) {
                item {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TextButton(
                        onClick = callbacks.onCancel,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = callbacks.onSave,
                        enabled = state.canSave && !state.isSaving,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background,
                        ),
                    ) {
                        Text(if (state.isSaving) "Saving…" else "Save Entry")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    content: @Composable () -> Unit,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    VerticalDivider(
                        modifier = Modifier.height(20.dp).padding(horizontal = 8.dp),
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun FormRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AssistChip(
            onClick = onClick,
            label = { Text("Edit") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )
    }
}

@Composable
private fun IntensitySlider(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Mild",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = range.count() - 2,
        )
        Text(
            text = "Severe",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            range.forEach { step ->
                Text(
                    text = step.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (step == value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MultiSelectSection(
    options: List<MultiSelectOption>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Checkbox(
                    checked = option.id in selectedIds,
                    onCheckedChange = { onToggle(option.id) },
                )
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

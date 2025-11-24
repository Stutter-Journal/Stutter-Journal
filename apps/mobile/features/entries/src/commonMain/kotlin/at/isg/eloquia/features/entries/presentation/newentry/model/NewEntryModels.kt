package at.isg.eloquia.features.entries.presentation.newentry.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.datetime.LocalDate

@Immutable
data class MultiSelectOption(
    val id: String,
    val label: String,
)

@Stable
sealed interface NewEntryUiState {
    val entryId: String?
    val date: LocalDate
    val intensity: Int
    val intensityRange: IntRange
    val triggers: List<MultiSelectOption>
    val selectedTriggerIds: Set<String>
    val customTrigger: String
    val methods: List<MultiSelectOption>
    val selectedMethodIds: Set<String>
    val customMethod: String
    val notes: String
    val isSaving: Boolean
    val errorMessage: String?

    val dateDisplay: String
        get() = date.toString()

    val canSave: Boolean
        get() = notes.isNotBlank() ||
            selectedTriggerIds.isNotEmpty() ||
            selectedMethodIds.isNotEmpty() ||
            customTrigger.isNotBlank() ||
            customMethod.isNotBlank()

    val isEditing: Boolean
        get() = entryId != null
}

@Immutable
data class NewEntryFormState(
    override val entryId: String? = null,
    override val date: LocalDate = LocalDate(1970, 1, 1),
    override val intensity: Int = 5,
    override val intensityRange: IntRange = 1..10,
    override val triggers: List<MultiSelectOption> = emptyList(),
    override val selectedTriggerIds: Set<String> = emptySet(),
    override val customTrigger: String = "",
    override val methods: List<MultiSelectOption> = emptyList(),
    override val selectedMethodIds: Set<String> = emptySet(),
    override val customMethod: String = "",
    override val notes: String = "",
    override val isSaving: Boolean = false,
    override val errorMessage: String? = null,
) : NewEntryUiState

@Stable
data class NewEntryCallbacks(
    val onClose: () -> Unit,
    val onDateChange: (LocalDate) -> Unit,
    val onIntensityChange: (Int) -> Unit,
    val onToggleTrigger: (String) -> Unit,
    val onCustomTriggerChange: (String) -> Unit,
    val onAddCustomTrigger: () -> Unit,
    val onToggleMethod: (String) -> Unit,
    val onCustomMethodChange: (String) -> Unit,
    val onAddCustomMethod: () -> Unit,
    val onNotesChange: (String) -> Unit,
    val onCancel: () -> Unit,
    val onSave: () -> Unit,
)

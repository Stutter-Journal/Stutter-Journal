package at.isg.eloquia.features.entries.presentation.newentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.isg.eloquia.core.domain.entries.model.JournalEntry
import at.isg.eloquia.core.domain.entries.usecase.CreateJournalEntryRequest
import at.isg.eloquia.core.domain.entries.usecase.CreateJournalEntryUseCase
import at.isg.eloquia.core.domain.entries.usecase.GetJournalEntryUseCase
import at.isg.eloquia.core.domain.entries.usecase.UpdateJournalEntryRequest
import at.isg.eloquia.core.domain.entries.usecase.UpdateJournalEntryUseCase
import at.isg.eloquia.features.entries.presentation.newentry.model.MultiSelectOption
import at.isg.eloquia.features.entries.presentation.newentry.model.NewEntryFormState
import at.isg.eloquia.features.entries.presentation.newentry.model.NewEntryUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class NewEntryViewModel(
    private val createJournalEntryUseCase: CreateJournalEntryUseCase,
    private val updateJournalEntryUseCase: UpdateJournalEntryUseCase,
    private val getJournalEntryUseCase: GetJournalEntryUseCase,
    private val clock: Clock,
    private val initialEntryId: String? = null,
) : ViewModel() {

    private val defaultTriggers = listOf(
        MultiSelectOption("stress", "Stress"),
        MultiSelectOption("fatigue", "Fatigue"),
        MultiSelectOption("phone", "Speaking on phone"),
        MultiSelectOption("public", "Public speaking"),
        MultiSelectOption("time", "Time pressure"),
        MultiSelectOption("new_people", "New people"),
        MultiSelectOption("excitement", "Excitement"),
    )

    private val defaultMethods = listOf(
        MultiSelectOption("breathing", "Breathing exercises"),
        MultiSelectOption("slow", "Slow speech"),
        MultiSelectOption("easy", "Easy onset"),
        MultiSelectOption("light_contact", "Light contact"),
        MultiSelectOption("prolonged", "Prolonged speech"),
        MultiSelectOption("pausing", "Pausing technique"),
        MultiSelectOption("mindfulness", "Mindfulness"),
    )

    private val defaultStutterForms = listOf(
        MultiSelectOption("blocks", "Blocks"),
        MultiSelectOption("repetitions", "Repetitions"),
        MultiSelectOption("stretches", "Stretches"),
    )

    private val _state: MutableStateFlow<NewEntryFormState> = MutableStateFlow(initialState())
    val state: StateFlow<NewEntryUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<NewEntryEvent>(replay = 0, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private var editingMetadata: EditingMetadata? = null

    init {
        initialEntryId?.let { existingId ->
            viewModelScope.launch { loadEntry(existingId) }
        }
    }

    fun setEntryDate(date: LocalDate) {
        _state.update { it.copy(date = date, errorMessage = null) }
    }

    fun setIntensity(value: Int) {
        _state.update {
            it.copy(
                intensity = value.coerceIn(INTENSITY_RANGE.first, INTENSITY_RANGE.last),
                errorMessage = null,
            )
        }
    }

    fun toggleTrigger(id: String) {
        _state.update {
            it.copy(
                selectedTriggerIds = it.selectedTriggerIds.toggle(id),
                errorMessage = null,
            )
        }
    }

    fun toggleMethod(id: String) {
        _state.update {
            it.copy(
                selectedMethodIds = it.selectedMethodIds.toggle(id),
                errorMessage = null,
            )
        }
    }

    fun toggleStutterForm(id: String) {
        _state.update {
            it.copy(
                selectedStutterFormIds = it.selectedStutterFormIds.toggle(id),
                errorMessage = null,
            )
        }
    }

    fun updateCustomTrigger(value: String) {
        _state.update { it.copy(customTrigger = value, errorMessage = null) }
    }

    fun commitCustomTrigger() {
        val label = _state.value.customTrigger.trim()
        if (label.isBlank()) return
        _state.update { current ->
            val existing = current.triggers.firstOrNull { it.label.equals(label, ignoreCase = true) }
            val option = existing ?: MultiSelectOption(generateCustomId("trigger", label), label)
            current.copy(
                triggers = (current.triggers + option).deduplicate(),
                selectedTriggerIds = current.selectedTriggerIds + option.id,
                customTrigger = "",
                errorMessage = null,
            )
        }
    }

    fun updateCustomMethod(value: String) {
        _state.update { it.copy(customMethod = value, errorMessage = null) }
    }

    fun commitCustomMethod() {
        val label = _state.value.customMethod.trim()
        if (label.isBlank()) return
        _state.update { current ->
            val existing = current.methods.firstOrNull { it.label.equals(label, ignoreCase = true) }
            val option = existing ?: MultiSelectOption(generateCustomId("method", label), label)
            current.copy(
                methods = (current.methods + option).deduplicate(),
                selectedMethodIds = current.selectedMethodIds + option.id,
                customMethod = "",
                errorMessage = null,
            )
        }
    }

    fun updateNotes(value: String) {
        _state.update { it.copy(notes = value, errorMessage = null) }
    }

    fun resetForm() {
        editingMetadata = null
        _state.value = initialState()
    }

    fun saveEntry() {
        val current = _state.value
        if (current.isSaving) return
        
        // Validate required fields
        val errors = mutableListOf<String>()
        if (current.selectedStutterFormIds.isEmpty()) {
            errors.add("Stutter Form is required")
        }
        if (current.notes.isBlank()) {
            errors.add("Additional Notes is required")
        }
        
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errorMessage = errors.joinToString("; ")) }
            return
        }

        _state.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            val tags = buildTags(current)
            val notes = current.notes.ifBlank { "Journal entry recorded on ${current.dateDisplay}." }
            val metadata = editingMetadata

            val result = if (metadata == null) {
                val title = "Entry ${current.dateDisplay}"
                runCatching {
                    createJournalEntryUseCase(
                        CreateJournalEntryRequest(
                            title = title,
                            content = notes,
                            tags = tags,
                        ),
                    )
                }
            } else {
                runCatching {
                    updateJournalEntryUseCase(
                        UpdateJournalEntryRequest(
                            entryId = metadata.id,
                            title = metadata.title,
                            content = notes,
                            createdAt = metadata.createdAt,
                            tags = tags,
                        ),
                    )
                }
            }

            result.onSuccess {
                _state.value = initialState()
                editingMetadata = null
                _events.emit(NewEntryEvent.EntrySaved)
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = throwable.message ?: "Unable to save entry",
                    )
                }
            }
        }
    }

    private suspend fun loadEntry(entryId: String) {
        val entry = getJournalEntryUseCase(entryId)
        if (entry == null) {
            _state.update { it.copy(errorMessage = "Entry not found", isSaving = false) }
            return
        }
        editingMetadata = EditingMetadata(entry.id, entry.title, entry.createdAt)
        _state.value = entry.toUiState()
    }

    private fun initialState(): NewEntryFormState {
        val today = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return NewEntryFormState(
            entryId = null,
            date = today,
            intensity = DEFAULT_INTENSITY,
            intensityRange = INTENSITY_RANGE,
            triggers = defaultTriggers,
            selectedTriggerIds = emptySet(),
            customTrigger = "",
            methods = defaultMethods,
            selectedMethodIds = emptySet(),
            customMethod = "",
            stutterForms = defaultStutterForms,
            selectedStutterFormIds = emptySet(),
            notes = "",
            isSaving = false,
            errorMessage = null,
        )
    }

    private fun buildTags(state: NewEntryUiState): List<String> = buildList {
        add("intensity:${state.intensity}")
        tagsFromSelection(state.triggers, state.selectedTriggerIds, "trigger").forEach(::add)
        tagsFromSelection(state.methods, state.selectedMethodIds, "method").forEach(::add)
        tagsFromSelection(state.stutterForms, state.selectedStutterFormIds, "stutterform").forEach(::add)
        state.customTrigger.takeIf(String::isNotBlank)?.let { add("trigger:${it.trim()}") }
        state.customMethod.takeIf(String::isNotBlank)?.let { add("method:${it.trim()}") }
    }

    private fun tagsFromSelection(
        options: List<MultiSelectOption>,
        selectedIds: Set<String>,
        prefix: String,
    ): List<String> = selectedIds.mapNotNull { id -> options.firstOrNull { it.id == id }?.label?.let { "$prefix:$it" } }

    private fun JournalEntry.toUiState(): NewEntryFormState {
        val tags = this.tags
        val intensity = tags.firstNotNullOfOrNull { parseIntensity(it) } ?: DEFAULT_INTENSITY
        val triggerLabels = parseLabels(tags, "trigger", defaultTriggers)
        val methodLabels = parseLabels(tags, "method", defaultMethods)
        val stutterFormLabels = parseLabels(tags, "stutterform", defaultStutterForms)
        val customTriggerOptions = triggerLabels.filter { label -> defaultTriggers.none { it.label.equals(label, true) } }
            .map { MultiSelectOption(generateCustomId("trigger", it), it) }
        val customMethodOptions = methodLabels.filter { label -> defaultMethods.none { it.label.equals(label, true) } }
            .map { MultiSelectOption(generateCustomId("method", it), it) }

        val triggerOptions = (defaultTriggers + customTriggerOptions).deduplicate()
        val methodOptions = (defaultMethods + customMethodOptions).deduplicate()

        return NewEntryFormState(
            entryId = this.id,
            date = this.createdAt.date,
            intensity = intensity,
            intensityRange = INTENSITY_RANGE,
            triggers = triggerOptions,
            selectedTriggerIds = triggerOptions.filter { option ->
                triggerLabels.any { it.equals(option.label, true) }
            }.map { it.id }.toSet(),
            customTrigger = "",
            methods = methodOptions,
            selectedMethodIds = methodOptions.filter { option ->
                methodLabels.any { it.equals(option.label, true) }
            }.map { it.id }.toSet(),
            customMethod = "",
            stutterForms = defaultStutterForms,
            selectedStutterFormIds = defaultStutterForms.filter { option ->
                stutterFormLabels.any { it.equals(option.label, true) }
            }.map { it.id }.toSet(),
            notes = this.content,
            isSaving = false,
            errorMessage = null,
        )
    }

    private fun parseIntensity(tag: String): Int? = when {
        tag.startsWith("intensity:", ignoreCase = true) -> tag.substringAfter(":").trim().toIntOrNull()
        tag.startsWith("Intensity", ignoreCase = true) -> tag.substringAfterLast(' ').trim().toIntOrNull()
        else -> null
    }

    private fun parseLabels(tags: List<String>, prefix: String, defaults: List<MultiSelectOption>): List<String> = tags.mapNotNull { tag ->
        when {
            tag.startsWith("$prefix:", ignoreCase = true) -> tag.substringAfter(":").trim()
            defaults.any { it.label.equals(tag, ignoreCase = true) } -> tag
            else -> null
        }
    }

    private fun List<MultiSelectOption>.deduplicate(): List<MultiSelectOption> = distinctBy { it.id }

    private fun generateCustomId(prefix: String, label: String): String {
        val timestamp = clock.now().toEpochMilliseconds()
        val normalized = label.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
        return "$prefix-${normalized.ifBlank { "custom" }}-$timestamp"
    }

    private fun Set<String>.toggle(value: String): Set<String> = if (value in this) this - value else this + value

    private data class EditingMetadata(
        val id: String,
        val title: String,
        val createdAt: LocalDateTime,
    )

    companion object {
        private val INTENSITY_RANGE = 1..10
        private const val DEFAULT_INTENSITY = 5
    }
}

sealed interface NewEntryEvent {
    data object EntrySaved : NewEntryEvent
}

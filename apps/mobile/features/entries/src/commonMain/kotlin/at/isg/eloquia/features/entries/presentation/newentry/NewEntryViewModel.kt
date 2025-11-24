package at.isg.eloquia.features.entries.presentation.newentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.isg.eloquia.core.domain.entries.usecase.CreateJournalEntryRequest
import at.isg.eloquia.core.domain.entries.usecase.CreateJournalEntryUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class NewEntryViewModel(
    private val createJournalEntryUseCase: CreateJournalEntryUseCase,
    private val clock: Clock,
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

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<NewEntryUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<NewEntryEvent>(replay = 0, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

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

    fun updateCustomTrigger(value: String) {
        _state.update { it.copy(customTrigger = value, errorMessage = null) }
    }

    fun updateCustomMethod(value: String) {
        _state.update { it.copy(customMethod = value, errorMessage = null) }
    }

    fun updateNotes(value: String) {
        _state.update { it.copy(notes = value, errorMessage = null) }
    }

    fun resetForm() {
        _state.value = initialState()
    }

    fun saveEntry() {
        val current = _state.value
        if (current.isSaving) return
        if (!current.canSave) {
            _state.update { it.copy(errorMessage = "Add details before saving.") }
            return
        }

        _state.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            val tags = buildList {
                add("Intensity ${current.intensity}")
                tagsFromSelection(current.triggers, current.selectedTriggerIds).forEach(::add)
                tagsFromSelection(current.methods, current.selectedMethodIds).forEach(::add)
                current.customTrigger.takeIf(String::isNotBlank)?.let(::add)
                current.customMethod.takeIf(String::isNotBlank)?.let(::add)
            }

            val title = "Entry ${current.dateDisplay}"
            val notes = current.notes.ifBlank { "Journal entry recorded on ${current.dateDisplay}." }

            runCatching {
                createJournalEntryUseCase(
                    CreateJournalEntryRequest(
                        title = title,
                        content = notes,
                        tags = tags,
                    ),
                )
            }.onSuccess {
                _state.value = initialState()
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

    private fun initialState(): NewEntryUiState {
        val today = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return NewEntryUiState(
            date = today,
            intensity = DEFAULT_INTENSITY,
            intensityRange = INTENSITY_RANGE,
            triggers = defaultTriggers,
            selectedTriggerIds = emptySet(),
            customTrigger = "",
            methods = defaultMethods,
            selectedMethodIds = emptySet(),
            customMethod = "",
            notes = "",
            isSaving = false,
            errorMessage = null,
        )
    }

    private fun tagsFromSelection(
        options: List<MultiSelectOption>,
        selectedIds: Set<String>,
    ): List<String> = selectedIds.mapNotNull { id -> options.firstOrNull { it.id == id }?.label }

    private fun Set<String>.toggle(value: String): Set<String> = if (value in this) this - value else this + value

    companion object {
        private val INTENSITY_RANGE = 1..10
        private const val DEFAULT_INTENSITY = 5
    }
}

sealed interface NewEntryEvent {
    data object EntrySaved : NewEntryEvent
}

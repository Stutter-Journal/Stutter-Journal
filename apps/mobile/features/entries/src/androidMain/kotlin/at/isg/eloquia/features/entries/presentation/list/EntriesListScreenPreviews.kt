package at.isg.eloquia.features.entries.presentation.list

import androidx.compose.runtime.Composable
import at.isg.eloquia.core.theme.EloquiaTheme
import at.isg.eloquia.core.theme.components.EloquiaPreview
import at.isg.eloquia.features.entries.domain.model.JournalEntry
import kotlinx.datetime.LocalDateTime

/**
 * Android-specific previews for entries list screen
 */

@EloquiaPreview
@Composable
fun EntriesListEmptyPreview() {
    EloquiaTheme {
        EntriesListScreenContent(
            state = EntriesListState(
                entries = emptyList(),
                isLoading = false
            ),
            onEntryClick = {},
            onCreateEntry = {}
        )
    }
}

@EloquiaPreview
@Composable
fun EntriesListLoadingPreview() {
    EloquiaTheme {
        EntriesListScreenContent(
            state = EntriesListState(
                entries = emptyList(),
                isLoading = true
            ),
            onEntryClick = {},
            onCreateEntry = {}
        )
    }
}

@EloquiaPreview
@Composable
fun EntriesListWithDataPreview() {
    val sampleEntries = listOf(
        JournalEntry(
            id = "1",
            title = "My First Entry",
            content = "Today was a great day for learning!",
            createdAt = LocalDateTime(2024, 11, 19, 10, 30),
            updatedAt = LocalDateTime(2024, 11, 19, 10, 30),
            tags = listOf("learning", "progress")
        ),
        JournalEntry(
            id = "2",
            title = "Reflection on Progress",
            content = "I've made significant improvements in my communication skills.",
            createdAt = LocalDateTime(2024, 11, 18, 15, 45),
            updatedAt = LocalDateTime(2024, 11, 18, 15, 45),
            tags = listOf("reflection", "communication")
        )
    )

    EloquiaTheme {
        EntriesListScreenContent(
            state = EntriesListState(
                entries = sampleEntries,
                isLoading = false
            ),
            onEntryClick = {},
            onCreateEntry = {}
        )
    }
}

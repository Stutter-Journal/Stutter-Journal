package at.isg.eloquia.features.entries.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import at.isg.eloquia.core.theme.EloquiaTheme
import at.isg.eloquia.core.theme.components.EloquiaPreview
import at.isg.eloquia.features.entries.domain.model.JournalEntry
import kotlinx.datetime.LocalDateTime

/**
 * Stateless entries list screen content
 */
@Composable
fun EntriesListScreenContent(
    state: EntriesListState,
    onEntryClick: (JournalEntry) -> Unit,
    onCreateEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        when {
            state.entries.isEmpty() && !state.isLoading -> {
                EmptyEntriesState(modifier = Modifier.fillMaxSize())
            }
            state.entries.isNotEmpty() -> {
                // TODO: Show list of entries
                EmptyEntriesState(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun EmptyEntriesState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = EntriesIcons.calendarToday,
            contentDescription = "No entries",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No entries yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start tracking your progress by creating your first journal entry",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@EloquiaPreview
@Composable
fun EmptyListScreenPreview() {
    EloquiaTheme {
        EntriesListScreenContent(
            state = EntriesListState(),
            onEntryClick = {},
            onCreateEntry = {}
        )
    }
}

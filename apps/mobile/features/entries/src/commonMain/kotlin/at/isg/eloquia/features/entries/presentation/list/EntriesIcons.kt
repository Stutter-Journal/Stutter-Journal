package at.isg.eloquia.features.entries.presentation.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Provides platform-specific icons for the entries feature
 */
expect object EntriesIcons {
    val calendarToday: ImageVector
        @Composable get
}

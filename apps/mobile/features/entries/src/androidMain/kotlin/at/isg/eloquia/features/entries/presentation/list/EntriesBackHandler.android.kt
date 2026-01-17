package at.isg.eloquia.features.entries.presentation.list

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun EntriesBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    BackHandler(enabled = enabled, onBack = onBack)
}

package at.isg.eloquia.features.entries.presentation.list

import androidx.compose.runtime.Composable

@Composable
actual fun EntriesBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    // No-op on desktop.
}

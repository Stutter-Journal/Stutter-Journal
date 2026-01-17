package at.isg.eloquia.features.entries.presentation.list

import androidx.compose.runtime.Composable

@Composable
expect fun EntriesBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
)

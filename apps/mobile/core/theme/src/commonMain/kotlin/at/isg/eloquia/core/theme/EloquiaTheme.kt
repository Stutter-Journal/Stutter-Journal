package at.isg.eloquia.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

@Composable
expect fun EloquiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
)

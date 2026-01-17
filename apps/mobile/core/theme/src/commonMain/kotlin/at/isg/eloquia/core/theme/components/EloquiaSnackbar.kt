package at.isg.eloquia.core.theme.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EloquiaSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { data: SnackbarData ->
            Snackbar(
                snackbarData = data,
                shape = RoundedCornerShape(18.dp),
                containerColor = colors.primaryContainer,
                contentColor = colors.onPrimaryContainer,
                actionColor = colors.onPrimaryContainer,
                dismissActionContentColor = colors.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        },
    )
}

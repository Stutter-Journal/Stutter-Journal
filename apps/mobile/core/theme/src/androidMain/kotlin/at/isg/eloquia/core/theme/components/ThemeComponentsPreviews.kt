package at.isg.eloquia.core.theme.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import at.isg.eloquia.core.theme.EloquiaTheme

@EloquiaPreview
@Composable
fun PreviewEloquiaDrawerProfileHeader() {
    EloquiaTheme {
        EloquiaDrawerProfileHeader(
            userName = "Sergio Alvarez",
        )
    }
}

@EloquiaPreview
@Composable
fun PreviewEloquiaDrawerProfileHeader_Guest() {
    EloquiaTheme {
        EloquiaDrawerProfileHeader(
            userName = "",
        )
    }
}

@EloquiaPreview
@Composable
fun PreviewEloquiaSnackbarHost() {
    EloquiaTheme {
        val hostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            hostState.showSnackbar(
                message = "Welcome! You’re signed in.",
                actionLabel = "Nice",
                withDismissAction = true,
                duration = SnackbarDuration.Indefinite,
            )
        }

        Box(Modifier.fillMaxSize()) {
            EloquiaSnackbarHost(
                hostState = hostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@EloquiaPreview
@Composable
fun PreviewEloquiaWelcomeBanner() {
    EloquiaTheme {
        EloquiaWelcomeBanner(
            visible = true,
            title = "Welcome",
            message = "Nice to see you again. Let’s continue where you left off.",
        )
    }
}

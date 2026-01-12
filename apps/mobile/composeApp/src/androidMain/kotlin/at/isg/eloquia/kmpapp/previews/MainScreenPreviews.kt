package at.isg.eloquia.kmpapp.previews

import androidx.compose.runtime.Composable
import at.isg.eloquia.core.theme.EloquiaTheme
import at.isg.eloquia.core.theme.components.EloquiaPreview
import at.isg.eloquia.kmpapp.presentation.main.MainScreen

@EloquiaPreview
@Composable
fun PreviewMainScreenWithDrawer() {
    EloquiaTheme {
        MainScreen(
            showWelcomeSnackbar = true,
            userName = "Sergio Alvarez",
            onLogout = {},
        )
    }
}

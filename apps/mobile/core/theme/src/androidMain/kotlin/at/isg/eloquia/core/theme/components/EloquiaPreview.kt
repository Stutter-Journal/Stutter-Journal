package at.isg.eloquia.core.theme.components

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Preview Day",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Preview Night",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
actual annotation class EloquiaPreview()

package at.isg.eloquia.kmpapp.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.isg.eloquia.core.theme.EloquiaTheme
import at.isg.eloquia.core.theme.components.EloquiaPreview
import at.isg.eloquia.kmpapp.presentation.main.MainTab

@EloquiaPreview
@Composable
fun PreviewMainScaffoldWithModalWideNavigationRail_Collapsed() {
    EloquiaTheme {
        var selectedTab by remember { mutableStateOf(MainTab.Entries) }

        MainScaffoldWithModalWideNavigationRail(
            selectedTab = selectedTab,
            onSelectTab = { selectedTab = it },
            onAddConnection = {},
            onLogout = {},
        ) { contentModifier ->
            Surface(modifier = contentModifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Content area")
                }
            }
        }
    }
}

@EloquiaPreview
@Composable
fun PreviewMainScaffoldWithModalWideNavigationRail_Expanded() {
    EloquiaTheme {
        var selectedTab by remember { mutableStateOf(MainTab.Entries) }
        val railState = rememberWideNavigationRailState()

        LaunchedEffect(Unit) {
            railState.expand()
        }

        MainScaffoldWithModalWideNavigationRail(
            selectedTab = selectedTab,
            onSelectTab = { selectedTab = it },
            onAddConnection = {},
            onLogout = {},
            railState = railState
        ) { contentModifier ->
            Surface(modifier = contentModifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Content area")
                }
            }
        }
    }
}

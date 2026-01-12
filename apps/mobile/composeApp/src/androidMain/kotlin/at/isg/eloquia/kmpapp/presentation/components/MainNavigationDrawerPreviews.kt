package at.isg.eloquia.kmpapp.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.isg.eloquia.core.theme.EloquiaTheme
import at.isg.eloquia.core.theme.components.EloquiaPreview
import at.isg.eloquia.kmpapp.presentation.main.MainTab

@OptIn(ExperimentalMaterial3Api::class)
@EloquiaPreview
@Composable
fun PreviewMainNavigationDrawer_Open() {
    EloquiaTheme {
        MainNavigationDrawer(
            userName = "Sergio Alvarez",
            selectedTab = MainTab.Entries,
            onSelectTab = {},
            onLogout = {},
            drawerState = rememberDrawerState(initialValue = DrawerValue.Open),
        ) { openDrawer ->
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("Eloquia") },
                        navigationIcon = {
                            IconButton(onClick = openDrawer) {
                                Icon(Icons.Default.Favorite, contentDescription = null)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(),
                    )
                },
            ) { padding ->
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Content")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@EloquiaPreview
@Composable
fun PreviewMainNavigationDrawer_Closed() {
    EloquiaTheme {
        MainNavigationDrawer(
            userName = "Guest",
            selectedTab = MainTab.Support,
            onSelectTab = {},
            onLogout = {},
            drawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
        ) { openDrawer ->
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("Eloquia") },
                        navigationIcon = {
                            IconButton(onClick = openDrawer) {
                                Icon(Icons.Default.Favorite, contentDescription = null)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(),
                    )
                },
            ) { padding ->
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Content")
                }
            }
        }
    }
}

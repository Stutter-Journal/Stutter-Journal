package at.isg.eloquia.kmpapp.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import at.isg.eloquia.features.entries.presentation.list.EntriesListScreenContent
import at.isg.eloquia.features.entries.presentation.list.EntriesListState
import kotlinx.serialization.Serializable


@Serializable
object EntriesDestination

@Serializable
object NewEntryDestination

@Serializable
object ProgressDestination

@Serializable
object SupportDestination

enum class MainTab(
    val destination: Any, val icon: ImageVector, val label: String
) {
    Entries(EntriesDestination, Icons.AutoMirrored.Filled.List, "Entries"),

    Progress(ProgressDestination, Icons.Default.Timeline, "Progress"),

    Support(SupportDestination, Icons.Default.Favorite, "Support")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var currentTab by remember { mutableStateOf(MainTab.Entries) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Stutter Journal", style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Track your progress",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }, navigationIcon = {
                Box(
                    modifier = Modifier.padding(start = 16.dp).size(40.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }, actions = {
                IconButton(onClick = { /* TODO: Menu */ }) {
                    Icon(
                        imageVector = Icons.Default.Menu, contentDescription = "Menu"
                    )
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface, modifier = Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                NavigationBarItem(selected = currentTab == MainTab.Entries, onClick = {
                    currentTab = MainTab.Entries
                    navController.navigate(EntriesDestination) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }, icon = {
                    Icon(
                        MainTab.Entries.icon, contentDescription = MainTab.Entries.label
                    )
                }, label = { Text(MainTab.Entries.label) })

                NavigationBarItem(
                    selected = false, onClick = { TODO("New Entry Action") }, icon = {
                    Box(
                        modifier = Modifier.width(96.dp).height(64.dp).clip(CircleShape)
                            .background(Color(0xFF4CAF50)), contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.layout.Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "New",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }, colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
                )

                NavigationBarItem(selected = currentTab == MainTab.Progress, onClick = {
                    currentTab = MainTab.Progress
                    navController.navigate(ProgressDestination) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }, icon = {
                    Icon(
                        MainTab.Progress.icon, contentDescription = MainTab.Progress.label
                    )
                }, label = { Text(MainTab.Progress.label) })

                NavigationBarItem(selected = currentTab == MainTab.Support, onClick = {
                    currentTab = MainTab.Support
                    navController.navigate(SupportDestination) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }, icon = {
                    Icon(
                        MainTab.Support.icon, contentDescription = MainTab.Support.label
                    )
                }, label = { Text(MainTab.Support.label) })
            }
        }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = EntriesDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<EntriesDestination> {
                EntriesListScreenContent(
                    state = EntriesListState(),
                    onEntryClick = {},
                    onCreateEntry = {})
            }
            composable<ProgressDestination> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Progress Screen")
                }
            }
            composable<SupportDestination> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Support Screen")
                }
            }
        }
    }
}

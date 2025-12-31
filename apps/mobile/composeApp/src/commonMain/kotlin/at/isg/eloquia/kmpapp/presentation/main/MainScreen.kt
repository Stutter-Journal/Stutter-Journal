package at.isg.eloquia.kmpapp.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import at.isg.eloquia.features.entries.presentation.list.EntriesListScreen
import at.isg.eloquia.features.entries.presentation.newentry.NewEntryScreen
import at.isg.eloquia.features.progress.presentation.ProgressScreen
import at.isg.eloquia.features.support.presentation.SupportScreen
import kotlinx.serialization.Serializable

@Serializable
object EntriesDestination

@Serializable
data class NewEntryDestination(val entryId: String? = null)

@Serializable
object ProgressDestination

@Serializable
object SupportDestination

enum class MainTab(
    val destination: Any,
    val icon: ImageVector,
    val label: String,
) {
    Entries(EntriesDestination, Icons.AutoMirrored.Filled.List, "Entries"),

    Progress(ProgressDestination, Icons.Default.Timeline, "Progress"),

    Support(SupportDestination, Icons.Default.Favorite, "Support"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var currentTab by remember { mutableStateOf(MainTab.Entries) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Stutter Journal",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "Track your progress",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier.padding(start = 16.dp).size(40.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Logo",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                ),
            ) {
                NavigationBarItem(selected = currentTab == MainTab.Entries, onClick = {
                    currentTab = MainTab.Entries
                    navController.navigate(EntriesDestination) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }, icon = {
                    Icon(
                        MainTab.Entries.icon,
                        contentDescription = MainTab.Entries.label,
                    )
                }, label = { Text(MainTab.Entries.label) })

                NavigationBarItem(selected = currentTab == MainTab.Progress, onClick = {
                    currentTab = MainTab.Progress
                    navController.navigate(ProgressDestination) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }, icon = {
                    Icon(
                        MainTab.Progress.icon,
                        contentDescription = MainTab.Progress.label,
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
                        MainTab.Support.icon,
                        contentDescription = MainTab.Support.label,
                    )
                }, label = { Text(MainTab.Support.label) })
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = EntriesDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<EntriesDestination> {
                EntriesListScreen(
                    onEntryClick = { entry ->
                        currentTab = MainTab.Entries
                        navController.navigate(NewEntryDestination(entryId = entry.id))
                    },
                    onCreateEntry = {
                        navController.navigate(NewEntryDestination())
                    },
                )
            }
            composable<NewEntryDestination> { backStackEntry ->
                val destination = backStackEntry.toRoute<NewEntryDestination>()
                NewEntryScreen(
                    entryId = destination.entryId,
                    onClose = {
                        currentTab = MainTab.Entries
                        navController.popBackStack()
                    },
                    onEntrySaved = {
                        currentTab = MainTab.Entries
                        navController.popBackStack()
                    },
                )
            }
            composable<ProgressDestination> {
                ProgressScreen()
            }
            composable<SupportDestination> {
                val uriHandler = LocalUriHandler.current
                SupportScreen(
                    onResourceClick = { resource -> uriHandler.openUri(resource.url) },
                )
            }
        }
    }
}

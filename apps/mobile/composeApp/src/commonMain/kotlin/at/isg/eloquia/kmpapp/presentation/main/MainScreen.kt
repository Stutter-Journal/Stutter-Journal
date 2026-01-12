@file:Suppress("D")

package at.isg.eloquia.kmpapp.presentation.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import at.isg.eloquia.core.theme.components.EloquiaSnackbarHost
import at.isg.eloquia.features.entries.presentation.list.EntriesListScreen
import at.isg.eloquia.features.entries.presentation.newentry.NewEntryScreen
import at.isg.eloquia.features.progress.presentation.ProgressScreen
import at.isg.eloquia.features.support.presentation.SupportScreen
import at.isg.eloquia.kmpapp.presentation.components.MainScaffoldWithModalWideNavigationRail
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
fun MainScreen(
    showWelcomeSnackbar: Boolean = false,
    onLogout: () -> Unit = {},
) {
    val navController = rememberNavController()
    var currentTab by remember { mutableStateOf(MainTab.Entries) }

    val snackbarHostState = remember { SnackbarHostState() }
    var welcomeShown by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(showWelcomeSnackbar) {
        if (showWelcomeSnackbar && !welcomeShown) {
            snackbarHostState.showSnackbar(
                message = "Welcome! You’re signed in.",
                actionLabel = "Nice",
                withDismissAction = true,
                duration = SnackbarDuration.Short,
            )
        }
    }

    fun selectTab(tab: MainTab) {
        currentTab = tab
        navController.navigate(tab.destination) {
            popUpTo(navController.graph.startDestinationId)
            launchSingleTop = true
        }
    }

    MainScaffoldWithModalWideNavigationRail(
        selectedTab = currentTab,
        onSelectTab = ::selectTab,
        onAddConnection = { /* TODO */ },
        onLogout = onLogout,
        snackbarHost = { EloquiaSnackbarHost(hostState = snackbarHostState) },
    ) { contentModifier ->
        NavHost(
            navController = navController,
            startDestination = EntriesDestination,
            modifier = contentModifier,
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

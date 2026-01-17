@file:Suppress("D")

package at.isg.eloquia.kmpapp.presentation.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import at.isg.eloquia.core.domain.auth.usecase.ClearSessionUseCase
import at.isg.eloquia.core.domain.sync.SyncNowUseCase
import at.isg.eloquia.core.domain.sync.SyncResult
import at.isg.eloquia.core.theme.components.EloquiaSnackbarHost
import at.isg.eloquia.features.entries.presentation.list.EntriesListScreen
import at.isg.eloquia.features.entries.presentation.newentry.NewEntryScreen
import at.isg.eloquia.features.progress.presentation.ProgressScreen
import at.isg.eloquia.features.support.presentation.SupportScreen
import at.isg.eloquia.features.therapist.presentation.MyTherapistScreen
import at.isg.eloquia.kmpapp.presentation.components.AddConnectionDialog
import at.isg.eloquia.kmpapp.presentation.components.MainScaffoldWithModalWideNavigationRail
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
object EntriesDestination

@Serializable
data class NewEntryDestination(val entryId: String? = null)

@Serializable
object ProgressDestination

@Serializable
object SupportDestination

@Serializable
object TherapistDestination

enum class MainTab(
    val destination: Any,
    val icon: ImageVector,
    val label: String,
) {
    Entries(EntriesDestination, Icons.AutoMirrored.Filled.List, "Entries"),

    Progress(ProgressDestination, Icons.Default.Timeline, "Progress"),

    Support(SupportDestination, Icons.Default.Favorite, "Support"),

    Therapist(TherapistDestination, Icons.Default.Medication, "Therapist"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    showWelcomeSnackbar: Boolean = false,
    onLogout: () -> Unit = {},
) {
    val navController = rememberNavController()
    var currentTab by remember { mutableStateOf(MainTab.Entries) }

    var showAddConnectionDialog by rememberSaveable { mutableStateOf(false) }

    val clearSession: ClearSessionUseCase = koinInject()
    val syncNow: SyncNowUseCase = koinInject()
    val scope = rememberCoroutineScope()

    var isSyncing by rememberSaveable { mutableStateOf(false) }

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
        onAddConnection = { showAddConnectionDialog = true },
        onSync = {
            if (isSyncing) return@MainScaffoldWithModalWideNavigationRail
            scope.launch {
                isSyncing = true
                val result = syncNow()
                isSyncing = false

                when (result) {
                    is SyncResult.Success -> {
                        snackbarHostState.showSnackbar(
                            message = "Sync complete (${result.summary.pulledEntries} pulled)",
                            withDismissAction = true,
                            duration = SnackbarDuration.Short,
                        )
                    }

                    is SyncResult.Failure -> {
                        snackbarHostState.showSnackbar(
                            message = result.message,
                            withDismissAction = true,
                            duration = SnackbarDuration.Short,
                        )
                    }
                }
            }
        },
        isSyncing = isSyncing,
        onLogout = {
            scope.launch {
                clearSession()
                onLogout()
            }
        },
        snackbarHost = { EloquiaSnackbarHost(hostState = snackbarHostState) },
    ) { contentModifier ->
        AddConnectionDialog(
            open = showAddConnectionDialog,
            onDismiss = { showAddConnectionDialog = false },
            onCode = { code ->
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Connected!",
                        withDismissAction = true,
                        duration = SnackbarDuration.Short,
                    )
                }
            },
            onDisconnected = { revokedCount ->
                scope.launch {
                    val msg = if (revokedCount > 0) {
                        "Therapist removed"
                    } else {
                        "No therapist connected"
                    }

                    snackbarHostState.showSnackbar(
                        message = msg,
                        withDismissAction = true,
                        duration = SnackbarDuration.Short,
                    )
                }
            },
        )

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
                        navController.popBackStack()
                    },
                    onEntrySaved = {
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

            composable<TherapistDestination> {
                MyTherapistScreen()
            }
        }
    }
}

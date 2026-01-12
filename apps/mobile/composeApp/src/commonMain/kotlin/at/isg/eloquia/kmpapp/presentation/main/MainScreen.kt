package at.isg.eloquia.kmpapp.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import at.isg.eloquia.core.theme.components.EloquiaDrawerProfileHeader
import at.isg.eloquia.core.theme.components.EloquiaSnackbarHost
import at.isg.eloquia.features.entries.presentation.list.EntriesListScreen
import at.isg.eloquia.features.entries.presentation.newentry.NewEntryScreen
import at.isg.eloquia.features.progress.presentation.ProgressScreen
import at.isg.eloquia.features.support.presentation.SupportScreen
import kotlinx.coroutines.launch
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
    userName: String = "",
    onLogout: () -> Unit = {},
) {
    val navController = rememberNavController()
    var currentTab by remember { mutableStateOf(MainTab.Entries) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }
    var welcomeShown by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(showWelcomeSnackbar) {
        if (showWelcomeSnackbar && !welcomeShown) {
            welcomeShown = true
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.widthIn(max = 360.dp),
                drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
                drawerTonalElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                ) {
                    EloquiaDrawerProfileHeader(
                        userName = userName,
                    )

                    Spacer(Modifier.size(8.dp))
                    HorizontalDivider()

                    NavigationDrawerItem(
                        label = { Text(MainTab.Entries.label) },
                        selected = currentTab == MainTab.Entries,
                        onClick = {
                            scope.launch { drawerState.close() }
                            selectTab(MainTab.Entries)
                        },
                        icon = {
                            Icon(
                                imageVector = MainTab.Entries.icon,
                                contentDescription = null,
                            )
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    )

                    NavigationDrawerItem(
                        label = { Text(MainTab.Progress.label) },
                        selected = currentTab == MainTab.Progress,
                        onClick = {
                            scope.launch { drawerState.close() }
                            selectTab(MainTab.Progress)
                        },
                        icon = {
                            Icon(
                                imageVector = MainTab.Progress.icon,
                                contentDescription = null,
                            )
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    )

                    NavigationDrawerItem(
                        label = { Text(MainTab.Support.label) },
                        selected = currentTab == MainTab.Support,
                        onClick = {
                            scope.launch { drawerState.close() }
                            selectTab(MainTab.Support)
                        },
                        icon = {
                            Icon(
                                imageVector = MainTab.Support.icon,
                                contentDescription = null,
                            )
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    )

                    Spacer(Modifier.weight(1f))
                    HorizontalDivider()

                    NavigationDrawerItem(
                        label = { Text("Logout") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onLogout()
                        },
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = null
                            )
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = MaterialTheme.colorScheme.errorContainer.copy(
                                alpha = 0.35f
                            ),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            .padding(bottom = 8.dp),
                    )
                }
            }
        },
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            snackbarHost = { EloquiaSnackbarHost(hostState = snackbarHostState) },
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
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Menu,
                                contentDescription = "Open drawer",
                            )
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier.padding(end = 16.dp).size(40.dp).clip(CircleShape)
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
                    NavigationBarItem(
                        selected = currentTab == MainTab.Entries,
                        onClick = { selectTab(MainTab.Entries) },
                        icon = {
                            Icon(
                                MainTab.Entries.icon,
                                contentDescription = MainTab.Entries.label,
                            )
                        },
                        label = { Text(MainTab.Entries.label) },
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.Progress,
                        onClick = { selectTab(MainTab.Progress) },
                        icon = {
                            Icon(
                                MainTab.Progress.icon,
                                contentDescription = MainTab.Progress.label,
                            )
                        },
                        label = { Text(MainTab.Progress.label) },
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.Support,
                        onClick = { selectTab(MainTab.Support) },
                        icon = {
                            Icon(
                                MainTab.Support.icon,
                                contentDescription = MainTab.Support.label,
                            )
                        },
                        label = { Text(MainTab.Support.label) },
                    )
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = EntriesDestination,
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
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
}

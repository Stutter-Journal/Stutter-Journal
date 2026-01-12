@file:Suppress("D")

package at.isg.eloquia.kmpapp.presentation.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import at.isg.eloquia.core.theme.components.EloquiaSnackbarHost
import at.isg.eloquia.features.entries.presentation.list.EntriesListScreen
import at.isg.eloquia.features.entries.presentation.newentry.NewEntryScreen
import at.isg.eloquia.features.progress.presentation.ProgressScreen
import at.isg.eloquia.features.support.presentation.SupportScreen
import at.isg.eloquia.kmpapp.presentation.components.ModalCollapsibleRailScaffold
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

    ModalCollapsibleRailScaffold(
        selectedTab = currentTab,
        onSelectTab = ::selectTab,
        onAddConnection = { /* TODO */ },
        onLogout = onLogout,
        topBar = { /* optional, or keep empty */ },
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

@Composable
private fun CollapsibleMainNavigationRail(
    selectedTab: MainTab,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onSelectTab: (MainTab) -> Unit,
    onAddConnection: () -> Unit = {}, // TODO: dialog later
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // Collapsed rail is typically ~80dp wide; expanded becomes “drawer-like”.
    val targetWidth = if (expanded) 280.dp else 80.dp
    val railWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = tween(durationMillis = 300),
        label = "railWidth",
    )

    Surface(
        modifier = modifier.width(railWidth),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top: menu toggle (matches the demo interaction)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (expanded) Arrangement.Start else Arrangement.Center,
            ) {
                IconButton(onClick = onToggleExpanded) {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = if (expanded) "Collapse navigation" else "Expand navigation",
                    )
                }

                AnimatedVisibility(visible = expanded) {
                    Text(
                        text = "Menu",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }

            Spacer(Modifier.size(8.dp))

            // “Add connection” action: icon-only collapsed; extended when expanded
            if (expanded) {
                ExtendedFloatingActionButton(
                    onClick = onAddConnection,
                    icon = { Icon(Icons.Outlined.PersonAdd, contentDescription = null) },
                    text = { Text("Add connection") },
                    modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth(),
                )
            } else {
                FloatingActionButton(
                    onClick = onAddConnection,
                    modifier = Modifier.padding(vertical = 4.dp),
                ) {
                    Icon(Icons.Outlined.PersonAdd, contentDescription = "Add connection")
                }
            }

            Spacer(Modifier.size(12.dp))
            HorizontalDivider(Modifier.padding(horizontal = 12.dp))
            Spacer(Modifier.size(8.dp))

            // Destinations:
            // - collapsed: NavigationRailItem (icon-focused)
            // - expanded: NavigationDrawerItem (list style like the demo)
            if (!expanded) {
                // Collapsed rail: icons only, labels hidden
                MainTab.entries.forEach { tab ->
                    NavigationRailItem(
                        selected = tab == selectedTab,
                        onClick = { onSelectTab(tab) },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        // label omitted and alwaysShowLabel=false => icon-only rail
                        alwaysShowLabel = false,
                    )
                }
            } else {
                // Expanded: drawer-like list items with pill indicator
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    MainTab.entries.forEach { tab ->
                        NavigationDrawerItem(
                            selected = tab == selectedTab,
                            onClick = { onSelectTab(tab) },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            HorizontalDivider(Modifier.padding(horizontal = 12.dp))
            Spacer(Modifier.size(8.dp))

            // Logout pinned at bottom; match style to expanded/collapsed
            if (!expanded) {
                NavigationRailItem(
                    selected = false,
                    onClick = onLogout,
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = "Logout"
                        )
                    },
                    alwaysShowLabel = false,
                )
            } else {
                Box(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    NavigationDrawerItem(
                        selected = false,
                        onClick = onLogout,
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = null
                            )
                        },
                        label = { Text("Logout") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
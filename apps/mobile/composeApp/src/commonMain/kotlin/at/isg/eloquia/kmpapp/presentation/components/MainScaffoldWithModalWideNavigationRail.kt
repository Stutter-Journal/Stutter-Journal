@file:Suppress("D")

package at.isg.eloquia.kmpapp.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalWideNavigationRail
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailState
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import at.isg.eloquia.kmpapp.presentation.main.MainTab
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffoldWithModalWideNavigationRail(
    selectedTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    onAddConnection: () -> Unit,
    onLogout: () -> Unit,
    railState: WideNavigationRailState = rememberWideNavigationRailState(),
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (Modifier) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val railExpanded by remember { androidx.compose.runtime.derivedStateOf { railState.targetValue == WideNavigationRailValue.Expanded } }
    val headerDescription = if (railExpanded) "Collapse rail" else "Expand rail"

    val railContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val contentContainerColor = MaterialTheme.colorScheme.surface

    Scaffold(
        snackbarHost = snackbarHost,
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Row(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            ModalWideNavigationRail(
                state = railState,
                colors = WideNavigationRailDefaults.colors(containerColor = railContainerColor),
                header = {
                    Column(
                    ) {
                        IconButton(
                            modifier = Modifier.semantics {
                                stateDescription =
                                    if (railState.currentValue == WideNavigationRailValue.Expanded) {
                                        "Expanded"
                                    } else {
                                        "Collapsed"
                                    }
                            },
                            onClick = {
                                scope.launch {
                                    if (railState.targetValue == WideNavigationRailValue.Expanded) {
                                        railState.collapse()
                                    } else {
                                        railState.expand()
                                    }
                                }
                            },
                        ) {
                            if (railExpanded) {
                                Icon(Icons.AutoMirrored.Filled.MenuOpen, headerDescription)
                            } else {
                                Icon(Icons.Filled.Menu, headerDescription)
                            }
                        }

                        AnimatedContent(
                            targetState = railExpanded,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                        ) { expanded ->
                            if (expanded) {
                                ExtendedFloatingActionButton(
                                    onClick = {
                                        onAddConnection()
                                        scope.launch { railState.collapse() }
                                    },
                                    icon = {
                                        Icon(
                                            Icons.Outlined.PersonAdd,
                                            contentDescription = null,
                                        )
                                    },
                                    text = { Text("Add patient") },
                                )
                            } else {
                                FloatingActionButton(
                                    onClick = {
                                        onAddConnection()
                                        scope.launch { railState.collapse() }
                                    },
                                ) {
                                    Icon(
                                        Icons.Outlined.PersonAdd,
                                        contentDescription = "Add patient",
                                    )
                                }
                            }
                        }
                    }
                },
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        MainTab.entries.forEach { tab ->
                            WideNavigationRailItem(
                                railExpanded = railExpanded,
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label) },
                                selected = selectedTab == tab,
                                onClick = {
                                    onSelectTab(tab)
                                    scope.launch {
                                        if (railState.targetValue == WideNavigationRailValue.Expanded) {
                                            railState.collapse()
                                        }
                                    }
                                },
                            )
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        WideNavigationRailItem(
                            railExpanded = railExpanded,
                            icon = {
                                Icon(
                                    Icons.AutoMirrored.Outlined.ExitToApp,
                                    contentDescription = "Logout",
                                )
                            },
                            label = { Text("Logout") },
                            selected = false,
                            onClick = {
                                onLogout()
                                scope.launch {
                                    if (railState.targetValue == WideNavigationRailValue.Expanded) {
                                        railState.collapse()
                                    }
                                }
                            },
                        )
                        Spacer(modifier = Modifier.padding(bottom = 8.dp))
                    }
                }
            }

            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            content(
                Modifier.fillMaxSize().background(contentContainerColor),
            )
        }
    }
}

package at.isg.eloquia.kmpapp.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import at.isg.eloquia.kmpapp.presentation.main.MainTab

@Composable
fun ModalCollapsibleRailScaffold(
    selectedTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    onAddConnection: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (Modifier) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        // Base layout: collapsed rail + content (content never shifts)
        Row(Modifier.fillMaxSize()) {
            CollapsedRail(
                selectedTab = selectedTab,
                onSelectTab = onSelectTab,
                onOpen = { expanded = true },
                modifier = Modifier.fillMaxHeight().width(80.dp)
            )

            Scaffold(
                snackbarHost = snackbarHost,
                containerColor = MaterialTheme.colorScheme.surface,
            ) { padding ->
                content(
                    Modifier.fillMaxSize()
                )
            }
        }

        // Overlay expanded rail (modal)
        ExpandedRailOverlay(
            visible = expanded,
            onDismiss = { expanded = false },
            selectedTab = selectedTab,
            onSelectTab = {
                onSelectTab(it)
                expanded = false
            },
            onAddConnection = {
                onAddConnection()
                expanded = false
            },
            onLogout = {
                onLogout()
                expanded = false
            },
        )
    }
}

@Composable
private fun CollapsedRail(
    selectedTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        IconButton(onClick = onOpen) {
            Icon(Icons.Outlined.Menu, contentDescription = "Open navigation")
        }

        Spacer(Modifier.height(8.dp))

        MainTab.entries.forEach { tab ->
            NavigationRailItem(
                selected = tab == selectedTab,
                onClick = { onSelectTab(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                alwaysShowLabel = false
            )
        }

        Spacer(Modifier.weight(1f))

        NavigationRailItem(
            selected = false,
            onClick = { /* optional: could open expanded and show logout there */ },
            icon = { Icon(Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = "Logout") },
            alwaysShowLabel = false
        )
    }
}

@Composable
private fun ExpandedRailOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    selectedTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    onAddConnection: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    expandedWidth: Dp = 320.dp,
) {
    // Animate sheet width in/out
    val width by animateDpAsState(
        targetValue = if (visible) expandedWidth else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "expandedRailWidth"
    )

    // Scrim fade
    val scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f)

    if (visible) {
        // Scrim layer (tap to dismiss)
        Box(
            Modifier.fillMaxSize().background(scrimColor).clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onDismiss
            )
        )
    }

    // Sheet layer (drawn over content, anchored left)
    Box(
        modifier = modifier.fillMaxHeight().width(width), contentAlignment = Alignment.TopStart
    ) {
        if (width > 0.dp) {
            Surface(
                modifier = Modifier.fillMaxHeight().fillMaxWidth()
                    .clip(RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight().padding(vertical = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Outlined.Menu, contentDescription = "Close navigation")
                        }
                        Text(
                            text = "Menu",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    ExtendedFloatingActionButton(
                        onClick = onAddConnection,
                        icon = { Icon(Icons.Outlined.PersonAdd, contentDescription = null) },
                        text = { Text("Add connection") },
                        modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth(),
                    )

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(Modifier.padding(horizontal = 12.dp))
                    Spacer(Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp),
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

                    Spacer(Modifier.weight(1f))
                    HorizontalDivider(Modifier.padding(horizontal = 12.dp))
                    Spacer(Modifier.height(8.dp))

                    Box(Modifier.padding(horizontal = 8.dp)) {
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

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

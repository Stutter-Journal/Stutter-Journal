package at.isg.eloquia.kmpapp.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.isg.eloquia.core.theme.components.EloquiaDrawerProfileHeader
import at.isg.eloquia.kmpapp.presentation.main.MainTab
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationDrawer(
    userName: String,
    selectedTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    content: @Composable (openDrawer: () -> Unit) -> Unit,
) {
    val scope = rememberCoroutineScope()

    fun openDrawer() {
        scope.launch { drawerState.open() }
    }

    fun closeDrawer() {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        modifier = modifier,
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

                    MainTab.entries.forEach { tab ->
                        NavigationDrawerItem(
                            label = { Text(tab.label) },
                            selected = selectedTab == tab,
                            onClick = {
                                closeDrawer()
                                onSelectTab(tab)
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                )
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        )
                    }

                    Spacer(Modifier.weight(1f))
                    HorizontalDivider()

                    NavigationDrawerItem(
                        label = { Text("Logout") },
                        selected = false,
                        onClick = {
                            closeDrawer()
                            onLogout()
                        },
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Outlined.ExitToApp,
                                contentDescription = null,
                            )
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .padding(bottom = 8.dp),
                    )
                }
            }
        },
    ) {
        content(::openDrawer)
    }
}

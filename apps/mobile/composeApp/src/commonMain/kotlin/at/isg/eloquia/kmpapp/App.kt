package at.isg.eloquia.kmpapp

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import at.isg.eloquia.features.auth.presentation.landing.AuthLandingScreen
import at.isg.eloquia.features.auth.presentation.link.LinkRequestScreen
import at.isg.eloquia.kmpapp.presentation.detail.DetailScreen
import at.isg.eloquia.kmpapp.presentation.main.MainScreen
import kotlinx.serialization.Serializable

@Serializable
object MainDestination

@Serializable
object AuthLandingDestination

@Serializable
object LinkRequestDestination

@Serializable
data class DetailDestination(val objectId: Int)

@Composable
fun App() {
    Surface {
        val navController: NavHostController = rememberNavController()
        NavHost(navController = navController, startDestination = AuthLandingDestination) {
            composable<AuthLandingDestination> {
                AuthLandingScreen(
                    onConnectClick = { navController.navigate(LinkRequestDestination) },
                    onSkipClick = {
                        navController.navigate(MainDestination) {
                            popUpTo(AuthLandingDestination) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable<LinkRequestDestination> {
                LinkRequestScreen(
                    onBack = { navController.popBackStack() },
                    onLinked = {
                        navController.navigate(MainDestination) {
                            popUpTo(AuthLandingDestination) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable<MainDestination> {
                MainScreen()
            }

            composable<DetailDestination> { backStackEntry ->
                DetailScreen(
                    objectId = backStackEntry.toRoute<DetailDestination>().objectId,
                    navigateBack = {
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}

package at.isg.eloquia.kmpapp

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import at.isg.eloquia.features.auth.presentation.landing.AuthLandingScreen
import at.isg.eloquia.kmpapp.presentation.main.MainScreen
import kotlinx.serialization.Serializable
import androidx.navigation.toRoute

@Serializable
data class MainDestination(
    val showWelcomeSnackbar: Boolean = false,
)

@Serializable
object AuthLandingDestination

@Composable
fun App() {
    Surface {
        val navController: NavHostController = rememberNavController()

        NavHost(navController = navController, startDestination = AuthLandingDestination) {
            composable<AuthLandingDestination> {
                AuthLandingScreen(
                    onAuthenticated = {
                        navController.navigate(MainDestination(showWelcomeSnackbar = true)) {
                            popUpTo(AuthLandingDestination) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable<MainDestination> { backStackEntry ->
                val destination = backStackEntry.toRoute<MainDestination>()
                MainScreen(
                    showWelcomeSnackbar = destination.showWelcomeSnackbar,
                    userName = "",
                    onLogout = {
                        navController.navigate(AuthLandingDestination) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
        }
    }
}

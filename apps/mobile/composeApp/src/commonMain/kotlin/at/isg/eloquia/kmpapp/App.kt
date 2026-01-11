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

@Serializable
object MainDestination

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
        }
    }
}

package at.isg.eloquia.kmpapp

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import at.isg.eloquia.kmpapp.presentation.detail.DetailScreen
import at.isg.eloquia.kmpapp.presentation.main.MainScreen
import kotlinx.serialization.Serializable

@Serializable
object MainDestination

@Serializable
data class DetailDestination(val objectId: Int)

@Composable
fun App() {
    Surface {
        val navController: NavHostController = rememberNavController()
        NavHost(navController = navController, startDestination = MainDestination) {
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

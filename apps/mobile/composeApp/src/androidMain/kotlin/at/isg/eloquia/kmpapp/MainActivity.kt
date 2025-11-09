package at.isg.eloquia.kmpapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import at.isg.eloquia.core.theme.EloquiaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            EloquiaTheme {
                // TODO: Remove when https://issuetracker.google.com/issues/364713509 is fixed
                LaunchedEffect(isSystemInDarkTheme()) {
                    enableEdgeToEdge()
                }

                App()
            }
        }
    }
}

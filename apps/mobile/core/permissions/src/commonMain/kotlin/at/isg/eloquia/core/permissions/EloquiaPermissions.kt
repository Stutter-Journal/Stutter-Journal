package at.isg.eloquia.core.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory

/**
 * Small wrapper so feature code depends on `core:permissions` instead of directly on moko APIs.
 */
@Composable
fun rememberEloquiaPermissionsControllerFactory(): PermissionsControllerFactory = rememberPermissionsControllerFactory()

@Composable
fun rememberEloquiaPermissionsController(
    factory: PermissionsControllerFactory = rememberEloquiaPermissionsControllerFactory(),
): PermissionsController = remember(factory) { factory.createPermissionsController() }

@Composable
fun BindPermissionsEffect(controller: PermissionsController) {
    BindEffect(controller)
}

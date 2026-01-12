package at.isg.eloquia.core.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface PermissionRequestState {
    data object Idle : PermissionRequestState
    data object Requesting : PermissionRequestState
    data object Granted : PermissionRequestState

    /**
     * User denied, but you can ask again.
     */
    data object Denied : PermissionRequestState

    /**
     * User denied permanently / system won't prompt again.
     */
    data object DeniedAlways : PermissionRequestState
}

class PermissionsViewModel(
    val permissionsController: PermissionsController,
) : ViewModel() {

    private val _camera = MutableStateFlow<PermissionRequestState>(PermissionRequestState.Idle)
    val camera: StateFlow<PermissionRequestState> = _camera.asStateFlow()

    fun resetCamera() {
        _camera.value = PermissionRequestState.Idle
    }

    /**
     * Call this right before you navigate to / open the QR scanner.
     */
    fun requestCameraPermission(onGranted: (() -> Unit)? = null) {
        if (_camera.value == PermissionRequestState.Requesting) return

        _camera.value = PermissionRequestState.Requesting
        viewModelScope.launch {
            try {
                permissionsController.providePermission(Permission.CAMERA)
                _camera.value = PermissionRequestState.Granted
                onGranted?.invoke()
            } catch (_: DeniedAlwaysException) {
                _camera.value = PermissionRequestState.DeniedAlways
            } catch (_: DeniedException) {
                _camera.value = PermissionRequestState.Denied
            } catch (_: Throwable) {
                // Treat unknown failures like a denial for UX.
                _camera.value = PermissionRequestState.Denied
            }
        }
    }

    fun request(permission: Permission, onResult: (PermissionRequestState) -> Unit) {
        viewModelScope.launch {
            val result = try {
                permissionsController.providePermission(permission)
                PermissionRequestState.Granted
            } catch (_: DeniedAlwaysException) {
                PermissionRequestState.DeniedAlways
            } catch (_: DeniedException) {
                PermissionRequestState.Denied
            } catch (_: Throwable) {
                PermissionRequestState.Denied
            }

            onResult(result)
        }
    }
}

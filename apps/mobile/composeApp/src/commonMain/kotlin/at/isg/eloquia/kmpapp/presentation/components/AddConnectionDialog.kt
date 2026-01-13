@file:Suppress("D")

package at.isg.eloquia.kmpapp.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import at.isg.eloquia.core.permissions.BindPermissionsEffect
import at.isg.eloquia.core.permissions.PermissionsViewModel
import at.isg.eloquia.core.permissions.PermissionRequestState
import at.isg.eloquia.core.permissions.rememberEloquiaPermissionsControllerFactory
import qrscanner.CameraLens
import qrscanner.QrScanner

@Composable
fun AddConnectionDialog(
    open: Boolean,
    onDismiss: () -> Unit,
    onCode: (String) -> Unit,
) {
    if (!open) return

    val factory = rememberEloquiaPermissionsControllerFactory()
    val permissionsViewModel = remember(factory) {
        PermissionsViewModel(factory.createPermissionsController())
    }

    BindPermissionsEffect(permissionsViewModel.permissionsController)

    var code by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    var scanning by remember { mutableStateOf(false) }
    var flashlightOn by remember { mutableStateOf(false) }
    var openImagePicker by remember { mutableStateOf(false) }

    val isCameraGranted by remember {
        derivedStateOf { permissionsViewModel.camera.value == PermissionRequestState.Granted }
    }

    LaunchedEffect(open) {
        if (open) {
            errorText = null
            permissionsViewModel.resetCamera()
            scanning = false
            flashlightOn = false
            openImagePicker = false
        }
    }

    LaunchedEffect(permissionsViewModel.camera.value) {
        when (permissionsViewModel.camera.value) {
            PermissionRequestState.Granted -> {
                errorText = null
                scanning = true
            }
            PermissionRequestState.DeniedAlways -> {
                scanning = false
                errorText = "Camera permission is blocked. Enable it in Settings to scan a QR code."
            }
            PermissionRequestState.Denied -> {
                scanning = false
                errorText = "Camera permission was denied. You can type the code instead."
            }
            else -> Unit
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.QrCodeScanner,
                contentDescription = null,
            )
        },
        title = { Text("Connect") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Enter the 6-digit code your doctor shows you, or scan the QR code.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (!scanning) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = {
                            val next = it.filter(Char::isDigit).take(6)
                            code = next
                            errorText = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Code") },
                        leadingIcon = { Icon(Icons.Outlined.Keyboard, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    )

                    FilledTonalButton(
                        onClick = {
                            if (isCameraGranted) {
                                scanning = true
                            } else {
                                permissionsViewModel.requestCameraPermission()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) {
                        Icon(Icons.Outlined.QrCodeScanner, contentDescription = null)
                        Spacer(Modifier.padding(start = 10.dp))
                        Text("Scan QR")
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(MaterialTheme.shapes.large),
                    ) {
                        QrScanner(
                            modifier = Modifier.fillMaxWidth(),
                            flashlightOn = flashlightOn,
                            cameraLens = CameraLens.Back,
                            openImagePicker = openImagePicker,
                            onCompletion = { raw ->
                                val scanned = raw.trim()
                                val extracted = Regex("\\b\\d{6}\\b").find(scanned)?.value ?: scanned
                                onCode(extracted)
                                onDismiss()
                            },
                            imagePickerHandler = { open ->
                                openImagePicker = open
                            },
                            onFailure = { message ->
                                errorText = if (message.isBlank()) {
                                    "Invalid QR code"
                                } else {
                                    message
                                }
                            },
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = { flashlightOn = !flashlightOn },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                if (flashlightOn) Icons.Outlined.FlashOn else Icons.Outlined.FlashOff,
                                contentDescription = null,
                            )
                            Spacer(Modifier.padding(start = 10.dp))
                            Text(if (flashlightOn) "Flashlight on" else "Flashlight off")
                        }

                        OutlinedButton(
                            onClick = { openImagePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Outlined.Image, contentDescription = null)
                            Spacer(Modifier.padding(start = 10.dp))
                            Text("Scan from image")
                        }

                        TextButton(
                            onClick = {
                                scanning = false
                                errorText = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Type code instead")
                        }
                    }
                }

                if (!errorText.isNullOrBlank()) {
                    Text(
                        text = errorText!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = code.trim()
                    if (trimmed.length != 6) {
                        errorText = "Enter a 6-digit code"
                    } else {
                        onCode(trimmed)
                        onDismiss()
                    }
                },
                enabled = !scanning && code.trim().isNotEmpty(),
            ) {
                Text("Continue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.padding(horizontal = 8.dp),
    )
}

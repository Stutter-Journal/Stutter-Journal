@file:Suppress("D")

package at.isg.eloquia.features.auth.presentation.landing

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthLandingScreen(
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthLandingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state) {
        if (state is AuthLandingState.Success) onAuthenticated()
    }

    LaunchedEffect(state.errorMessage) {
        val msg = state.errorMessage
        if (!msg.isNullOrBlank()) {
            // Show a snackbar each time an error happens
            snackbarHostState.showSnackbar(message = msg)
        }
    }

    val colors = MaterialTheme.colorScheme
    val backgroundBrush = Brush.verticalGradient(listOf(colors.primaryContainer, colors.surface))

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.surface
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize().background(backgroundBrush).padding(padding)
                    .padding(horizontal = 20.dp).padding(WindowInsets.ime.asPaddingValues()),
                contentAlignment = Alignment.Center,
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    AuthLandingCardContent(
                        state = state,
                        onEmailChange = viewModel::updateEmail,
                        onPasswordChange = viewModel::updatePassword,
                        onDisplayNameChange = viewModel::updateDisplayName,
                        onSubmit = viewModel::submit,
                        onToggleMode = viewModel::toggleMode,
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthLandingCardContent(
    state: AuthLandingState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onToggleMode: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val form = state.form
    val isSubmitting = state is AuthLandingState.Submitting

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AuthErrorBanner(
            message = state.errorMessage,
        )

        Icon(Icons.Outlined.Lock, null, tint = colors.primary)
        Text("Eloquia", style = MaterialTheme.typography.headlineMedium)

        AnimatedContent(
            targetState = form.mode, label = "auth_mode", transitionSpec = {
                // Directional movement + fade/scale = “expressive”
                val forward = targetState == AuthMode.Register
                val slideIn = slideInHorizontally(
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    initialOffsetX = { full -> if (forward) full / 6 else -full / 6 })
                val slideOut = slideOutHorizontally(
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    targetOffsetX = { full -> if (forward) -full / 6 else full / 6 })
                (slideIn + fadeIn() + scaleIn(initialScale = 0.98f)).togetherWith(
                    slideOut + fadeOut() + scaleOut(
                        targetScale = 0.98f
                    )
                ).using(SizeTransform(clip = false))
            }) { mode ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = when (mode) {
                        AuthMode.SignIn -> "Sign in to continue"
                        AuthMode.Register -> "Create your account"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant,
                )

                if (mode == AuthMode.Register) {
                    OutlinedTextField(
                        value = form.displayName,
                        onValueChange = onDisplayNameChange,
                        isError = state.displayNameError != null,
                        supportingText = {
                            val text = state.displayNameError ?: "This will be shown in the app."
                            Text(text)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Display name") },
                        singleLine = true,
                        enabled = !isSubmitting,
                    )
                }

                OutlinedTextField(
                    value = form.email,
                    onValueChange = onEmailChange,
                    isError = state.emailError != null,
                    supportingText = {
                        val text = state.emailError ?: "Use the email you registered with."
                        Text(text)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email") },
                    singleLine = true,
                    enabled = !isSubmitting,
                )

                OutlinedTextField(
                    value = form.password,
                    onValueChange = onPasswordChange,
                    isError = state.passwordError != null,
                    supportingText = {
                        val hint = if (form.mode == AuthMode.Register) "At least 8 characters." else "Enter your password."
                        val text = state.passwordError ?: hint
                        Text(text)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    singleLine = true,
                    enabled = !isSubmitting,
                    visualTransformation = PasswordVisualTransformation(),
                )
            }
        }

        // Button + footer unchanged for now...
        Spacer(Modifier.height(4.dp))

        Button(
            onClick = onSubmit,
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                Text(
                    text = when (form.mode) {
                        AuthMode.SignIn -> "Signing in…"
                        AuthMode.Register -> "Creating account…"
                    },
                    modifier = Modifier.padding(start = 12.dp),
                )
            } else {
                Text(if (form.mode == AuthMode.SignIn) "Sign in" else "Register")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (form.mode == AuthMode.SignIn) "Not a user?" else "Already a user?",
                color = colors.onSurfaceVariant
            )
            TextButton(onClick = onToggleMode, enabled = !isSubmitting) {
                Text(if (form.mode == AuthMode.SignIn) "Register" else "Sign in")
            }
        }
    }
}

@Composable
private fun AuthErrorBanner(
    message: String?,
    modifier: Modifier = Modifier,
) {
    val msg = message.orEmpty()
    AnimatedVisibility(
        visible = msg.isNotBlank(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.ErrorOutline, contentDescription = null)
                Spacer(Modifier.size(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(msg, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

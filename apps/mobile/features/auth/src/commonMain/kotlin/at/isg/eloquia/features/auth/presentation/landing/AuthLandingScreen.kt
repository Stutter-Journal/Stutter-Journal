package at.isg.eloquia.features.auth.presentation.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    LaunchedEffect(state) {
        if (state is AuthLandingState.Success) onAuthenticated()
    }

    val colors = MaterialTheme.colorScheme
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            colors.primaryContainer,
            colors.surface,
        ),
    )

    Surface(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(horizontal = 20.dp)
                .padding(WindowInsets.ime.asPaddingValues()),
            contentAlignment = Alignment.Center,
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            tint = colors.primary,
        )
        Text(
            text = "Eloquia",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onSurface,
        )
        Text(
            text = when (form.mode) {
                AuthMode.SignIn -> "Sign in to continue"
                AuthMode.Register -> "Create your account"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (form.mode == AuthMode.Register) {
            OutlinedTextField(
                value = form.displayName,
                onValueChange = onDisplayNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Display name") },
                singleLine = true,
                enabled = !isSubmitting,
            )
        }

        OutlinedTextField(
            value = form.email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
            enabled = !isSubmitting,
        )

        OutlinedTextField(
            value = form.password,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
            enabled = !isSubmitting,
            visualTransformation = PasswordVisualTransformation(),
        )

        val errorMessage = state.errorMessage
        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.error,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onSubmit,
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = when (form.mode) {
                        AuthMode.SignIn -> "Signing in…"
                        AuthMode.Register -> "Creating account…"
                    },
                    modifier = Modifier.padding(start = 12.dp),
                )
            } else {
                Text(
                    text = when (form.mode) {
                        AuthMode.SignIn -> "Sign in"
                        AuthMode.Register -> "Register"
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val prefix = when (form.mode) {
                AuthMode.SignIn -> "Not a user?"
                AuthMode.Register -> "Already a user?"
            }
            val action = when (form.mode) {
                AuthMode.SignIn -> "Click here to register."
                AuthMode.Register -> "Click here to sign in."
            }
            Text(prefix, color = colors.onSurfaceVariant)
            TextButton(onClick = onToggleMode, enabled = !isSubmitting) {
                Text(action)
            }
        }
    }
}

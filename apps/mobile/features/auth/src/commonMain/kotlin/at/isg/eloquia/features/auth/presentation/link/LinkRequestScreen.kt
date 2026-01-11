package at.isg.eloquia.features.auth.presentation.link

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LinkRequestScreen(
    onBack: () -> Unit,
    onLinked: () -> Unit,
    viewModel: LinkRequestViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is LinkRequestState.Success) onLinked()
    }

    LinkRequestScreenContent(
        state = state,
        onBack = onBack,
        onPatientCodeChange = viewModel::updatePatientCode,
        onEmailChange = viewModel::updateEmail,
        onSubmit = viewModel::submit,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LinkRequestScreenContent(
    state: LinkRequestState,
    onBack: () -> Unit,
    onPatientCodeChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val form = when (state) {
        is LinkRequestState.Editing -> state.form
        is LinkRequestState.Submitting -> state.form
        LinkRequestState.Success -> LinkRequestForm()
    }
    val isSubmitting = state is LinkRequestState.Submitting
    val errorMessage = (state as? LinkRequestState.Editing)?.errorMessage

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connect") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Link with your doctor",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "Enter the code your doctor gave you and your email.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = form.patientCode,
                onValueChange = onPatientCodeChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Code") },
                singleLine = true,
                enabled = !isSubmitting,
            )

            OutlinedTextField(
                value = form.email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                singleLine = true,
                enabled = !isSubmitting,
            )

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSubmit,
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.height(18.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Connecting…")
                } else {
                    Text("Connect")
                }
            }
        }
    }
}

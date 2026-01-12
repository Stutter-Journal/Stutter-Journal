package at.isg.eloquia.core.theme.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

/**
 * A shared, expressive banner component.
 *
 * Designed for “arrive on screen” moments (welcome, success, tip-of-the-day), with
 * a springy enter/exit motion and a subtle gradient container.
 */
@Composable
fun EloquiaBanner(
    title: String,
    message: String,
    visible: Boolean,
    onDismiss: (() -> Unit)? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme

    val background = Brush.linearGradient(
        colors = listOf(
            colors.primaryContainer,
            colors.secondaryContainer.copy(alpha = 0.9f),
        ),
    )

    AnimatedVisibility(
        visible = visible,
        enter =
            slideInVertically(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                initialOffsetY = { full -> -full / 2 },
            ) +
                expandVertically(
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    expandFrom = Alignment.Top,
                ) +
                fadeIn(
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                ),
        exit =
            slideOutVertically(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                targetOffsetY = { full -> -full / 2 },
            ) +
                shrinkVertically(
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    shrinkTowards = Alignment.Top,
                ) +
                fadeOut(
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                ),
        modifier = modifier,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(background)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (leading != null) {
                        Box(
                            modifier = Modifier.padding(top = 2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            leading()
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onPrimaryContainer,
                        )
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onPrimaryContainer.copy(alpha = 0.9f),
                        )

                        if (onDismiss != null || (onAction != null && actionLabel != null)) {
                            Spacer(Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (onAction != null && actionLabel != null) {
                                    TextButton(onClick = onAction) {
                                        Text(actionLabel)
                                    }
                                }

                                if (onDismiss != null) {
                                    if (onAction != null && actionLabel != null) {
                                        Spacer(Modifier.width(6.dp))
                                    }
                                    TextButton(onClick = onDismiss) {
                                        Text("Dismiss")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EloquiaWelcomeBanner(
    visible: Boolean,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    title: String = "Welcome",
    message: String = "Nice to see you again. Let’s continue where you left off.",
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    EloquiaBanner(
        title = title,
        message = message,
        visible = visible,
        onDismiss = onDismiss,
        actionLabel = actionLabel,
        onAction = onAction,
        modifier = modifier,
        leading = leading,
    )
}

package io.github.storybookemoji.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Floating action button for undo functionality
 * Provides visual feedback and accessibility support
 */
@Composable
fun UndoButton(
    canUndo: Boolean,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onUndo,
        modifier = modifier
            .size(56.dp)
            .semantics {
                contentDescription = if (canUndo) {
                    "Undo last action on this page"
                } else {
                    "No actions to undo on this page"
                }
            },
        shape = CircleShape,
        containerColor = if (canUndo) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Gray.copy(alpha = 0.3f)
        },
        contentColor = if (canUndo) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            Color.Gray
        }
    ) {
        Icon(
            imageVector = Icons.Default.Undo,
            contentDescription = null, // Content description is on the button itself
            modifier = Modifier.size(24.dp)
        )
    }
} 
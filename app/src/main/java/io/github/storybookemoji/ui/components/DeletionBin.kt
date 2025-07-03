package io.github.storybookemoji.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.storybookemoji.model.EmojiSticker

/**
 * Visual deletion bin that appears when dragging stickers
 * Provides intuitive drag-to-delete functionality for target age group
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DeletionBin(
    visible: Boolean,
    isHoveringOverBin: Boolean = false,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it } + fadeIn() + scaleIn(),
        exit = slideOutVertically { it } + fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        val binColor = when {
            isHoveringOverBin -> Color(0xFFFF5722) // Bright red when hovering
            else -> Color(0xFF757575) // Gray when not hovering
        }
        
        val backgroundColor = when {
            isHoveringOverBin -> Color(0xFFFFEBEE) // Light red background
            else -> Color(0xFFF5F5F5) // Light gray background
        }
        
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .border(
                    width = if (isHoveringOverBin) 3.dp else 2.dp,
                    color = binColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Trash bin emoji as the icon
                Text(
                    text = "🗑️",
                    fontSize = 32.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                // Visual text indicator
                Text(
                    text = if (isHoveringOverBin) "Drop!" else "Delete",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = binColor
                )
            }
        }
    }
}

/**
 * Checks if a sticker position is hovering over the deletion bin
 */
fun isHoveringOverDeletionBin(
    stickerPosition: Offset,
    binPosition: Offset,
    binSize: Float = 80f,
    tolerance: Float = 20f
): Boolean {
    val expandedBinSize = binSize + tolerance * 2
    val binLeft = binPosition.x - expandedBinSize / 2
    val binRight = binPosition.x + expandedBinSize / 2
    val binTop = binPosition.y - expandedBinSize / 2
    val binBottom = binPosition.y + expandedBinSize / 2
    
    return stickerPosition.x >= binLeft && 
           stickerPosition.x <= binRight && 
           stickerPosition.y >= binTop && 
           stickerPosition.y <= binBottom
}

/**
 * Determines if deletion bin should be visible during drag operations
 */
@Composable
fun rememberDeletionBinState(): MutableState<Boolean> {
    return remember { mutableStateOf(false) }
} 
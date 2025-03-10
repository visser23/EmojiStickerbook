package com.example.storybookemoji.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.storybookemoji.model.EmojiSticker
import kotlin.math.roundToInt

/**
 * A draggable emoji component that can be moved, scaled, and rotated
 */
@Composable
fun DraggableEmoji(
    emojiSticker: EmojiSticker,
    onPositionChange: (Offset) -> Unit,
    onRemove: () -> Unit,
    containerSize: Offset
) {
    val density = LocalDensity.current
    
    // Track current properties with state to ensure recomposition
    var currentScale by remember { mutableStateOf(emojiSticker.scale) }
    var currentRotation by remember { mutableStateOf(emojiSticker.rotation) }
    var currentPosition by remember { mutableStateOf(emojiSticker.position) }
    
    // Get the current size based on scale
    val emojiSize = remember(currentScale) { 
        emojiSticker.size * currentScale
    }
    
    // Ensure emoji stays within screen bounds
    val boundedPosition = remember(currentPosition, containerSize, emojiSize) {
        Offset(
            x = currentPosition.x.coerceIn(0f, (containerSize.x - emojiSize).coerceAtLeast(0f)),
            y = currentPosition.y.coerceIn(0f, (containerSize.y - emojiSize).coerceAtLeast(0f))
        )
    }
    
    // Update current position if bounded position changed
    LaunchedEffect(boundedPosition) {
        if (boundedPosition != currentPosition) {
            currentPosition = boundedPosition
        }
    }
    
    // For detecting different kinds of gestures
    var gestureMode by remember { mutableStateOf(GestureMode.NONE) }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .offset {
                IntOffset(
                    boundedPosition.x.roundToInt(),
                    boundedPosition.y.roundToInt()
                )
            }
            .size(with(density) { emojiSize.toDp() })
            .rotate(currentRotation)
            // Detect transform (pinch-to-zoom and rotation)
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, rotation ->
                    if (gestureMode == GestureMode.NONE || gestureMode == GestureMode.TRANSFORM) {
                        gestureMode = GestureMode.TRANSFORM
                        
                        // Update scale (zoom) - limit between 0.5 and 3.0
                        currentScale = (currentScale * zoom).coerceIn(0.5f, 3.0f)
                        
                        // Update rotation (keep between 0 and 360 degrees)
                        currentRotation = (currentRotation + rotation) % 360
                        
                        // Update the original emoji sticker with new values
                        emojiSticker.scale = currentScale
                        emojiSticker.rotation = currentRotation
                        
                        // Notify parent about the changes
                        onPositionChange(currentPosition)
                    }
                }
            }
            // Detect drag (move)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { gestureMode = GestureMode.DRAG },
                    onDragEnd = { 
                        gestureMode = GestureMode.NONE
                        // Ensure the original sticker position is updated
                        emojiSticker.position = currentPosition
                    },
                    onDragCancel = { gestureMode = GestureMode.NONE },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (gestureMode == GestureMode.DRAG) {
                            // Update current position directly
                            currentPosition = Offset(
                                x = (currentPosition.x + dragAmount.x).coerceIn(0f, (containerSize.x - emojiSize).coerceAtLeast(0f)),
                                y = (currentPosition.y + dragAmount.y).coerceIn(0f, (containerSize.y - emojiSize).coerceAtLeast(0f))
                            )
                            
                            // Notify parent about the position change
                            onPositionChange(currentPosition)
                        }
                    }
                )
            }
            // Detect long press for deletion
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        onRemove()
                    }
                )
            }
    ) {
        Text(
            text = emojiSticker.emoji,
            fontSize = with(density) { (emojiSize * 0.8f).toSp() }
        )
    }
}

/**
 * Enum to track the current gesture mode to avoid conflicts
 */
private enum class GestureMode {
    NONE,
    DRAG,
    TRANSFORM
} 
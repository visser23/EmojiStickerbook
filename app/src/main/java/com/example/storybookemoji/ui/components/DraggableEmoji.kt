package com.example.storybookemoji.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
    
    // Create a transformable state for handling scale and rotation
    val transformableState = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        // Update scale with no upper limit
        currentScale = (currentScale * zoomChange).coerceAtLeast(0.5f)
        
        // Update rotation
        currentRotation += rotationChange
        
        // Also update position with the pan offset
        val newX = (currentPosition.x + offsetChange.x).coerceIn(0f, (containerSize.x - emojiSize).coerceAtLeast(0f))
        val newY = (currentPosition.y + offsetChange.y).coerceIn(0f, (containerSize.y - emojiSize).coerceAtLeast(0f))
        currentPosition = Offset(newX, newY)
        
        // Debug output
        println("Transform: scale=$currentScale, rotation=$currentRotation, position=$currentPosition")
    }
    
    // Update emoji sticker properties when they change
    LaunchedEffect(currentScale, currentRotation, currentPosition) {
        emojiSticker.scale = currentScale
        emojiSticker.rotation = currentRotation
        emojiSticker.position = currentPosition
        onPositionChange(currentPosition)
    }
    
    // Create a drag State that integrates with the transformable state
    var wasDragging by remember { mutableStateOf(false) }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .offset {
                IntOffset(
                    currentPosition.x.roundToInt(),
                    currentPosition.y.roundToInt()
                )
            }
            .size(with(density) { emojiSize.toDp() })
            .rotate(currentRotation)
            // Use the high-level transformable modifier for scaling and rotation
            .transformable(state = transformableState)
            // Separate drag handling to improve reliability when no scaling is intended
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { wasDragging = true },
                    onDragEnd = { wasDragging = false },
                    onDragCancel = { wasDragging = false },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (!transformableState.isTransformInProgress) {
                            // Only handle dragging when not transforming
                            val newX = (currentPosition.x + dragAmount.x).coerceIn(0f, (containerSize.x - emojiSize).coerceAtLeast(0f))
                            val newY = (currentPosition.y + dragAmount.y).coerceIn(0f, (containerSize.y - emojiSize).coerceAtLeast(0f))
                            currentPosition = Offset(newX, newY)
                        }
                    }
                )
            }
            // Separate long press detection for deletion
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { /* Capture press but do nothing */ },
                    onLongPress = {
                        if (!wasDragging && !transformableState.isTransformInProgress) {
                            onRemove()
                            println("Long press detected - removing emoji")
                        }
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
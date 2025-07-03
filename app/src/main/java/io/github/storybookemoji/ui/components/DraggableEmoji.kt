package io.github.storybookemoji.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import io.github.storybookemoji.model.EmojiSticker
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * A performance-optimized draggable emoji component
 * Minimizes recompositions and memory allocations
 * Enhanced with consistent boundary handling and max size constraints
 */
@Composable
fun DraggableEmoji(
    emojiSticker: EmojiSticker,
    onPositionChange: (Offset) -> Unit,
    onRemove: () -> Unit,
    containerSize: Offset,
    isTopmost: Boolean = true
) {
    val density = LocalDensity.current
    
    // Consolidate state into a single data class to reduce recompositions
    var transformState by remember {
        mutableStateOf(
            TransformState(
                position = emojiSticker.position,
                scale = emojiSticker.scale,
                rotation = emojiSticker.rotation
            )
        )
    }
    
    // Enhanced size calculation with consistent boundary checking
    val emojiSize = remember(transformState.scale) { 
        emojiSticker.size * transformState.scale
    }
    
    val displaySize = remember(emojiSize, density) { 
        with(density) { (emojiSize * 1.5f).toDp() }
    }
    
    // FIXED: Consistent boundary calculations using half-size throughout
    val boundedPosition = remember(transformState.position, containerSize, emojiSize) {
        // Ensure sticker doesn't exceed container boundaries - use half size for proper centering
        val halfSize = emojiSize / 2f
        val maxX = (containerSize.x - halfSize).coerceAtLeast(halfSize)
        val maxY = (containerSize.y - halfSize).coerceAtLeast(halfSize)
        
        Offset(
            x = transformState.position.x.coerceIn(halfSize, maxX),
            y = transformState.position.y.coerceIn(halfSize, maxY)
        )
    }
    
    // Enhanced scale validation with dynamic max size based on container
    val validateScale = remember(containerSize, emojiSticker.size) {
        { newScale: Float ->
            // Calculate max scale that keeps sticker within bounds
            val maxSizeForContainer = min(containerSize.x, containerSize.y) * 0.8f
            val dynamicMaxScale = (maxSizeForContainer / emojiSticker.size).coerceAtMost(EmojiSticker.MAX_SCALE)
            newScale.coerceIn(EmojiSticker.MIN_SCALE, dynamicMaxScale)
        }
    }
    
    // Update sticker properties only when necessary
    LaunchedEffect(transformState) {
        emojiSticker.scale = transformState.scale
        emojiSticker.rotation = transformState.rotation
        emojiSticker.position = boundedPosition // Use bounded position
        onPositionChange(boundedPosition)
    }
    
    // Cache edge detection calculations
    val edgeThresholdPx = remember(containerSize) { containerSize.x * 0.15f }
    val isNearEdge = remember(edgeThresholdPx) { 
        { position: Offset ->
            position.x < edgeThresholdPx || position.x > containerSize.x - edgeThresholdPx
        }
    }
    
    // Multi-touch state for scaling/rotation
    var touchPoints by remember { mutableStateOf(emptyList<Offset>()) }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .offset {
                IntOffset(
                    boundedPosition.x.roundToInt(),
                    boundedPosition.y.roundToInt()
                )
            }
            .size(displaySize)
            .rotate(transformState.rotation)
            // FIXED: Long press gesture FIRST to prevent event consumption conflicts
            .pointerInput(isTopmost) {
                detectTapGestures(
                    onLongPress = { position ->
                        if (!isNearEdge(position) && isTopmost) {
                            onRemove()
                        }
                    }
                )
            }
            // FIXED: Consistent boundary handling in drag gestures using half-size
            .pointerInput(containerSize, emojiSize) {
                detectDragGestures { change, dragAmount ->
                    val isHorizontalEdgeSwipe = abs(dragAmount.x) > abs(dragAmount.y) && 
                                               isNearEdge(change.position)
                    
                    if (!isHorizontalEdgeSwipe) {
                        change.consume()
                        
                        // FIXED: Use consistent half-size boundary calculations
                        val halfSize = emojiSize / 2f
                        val maxX = (containerSize.x - halfSize).coerceAtLeast(halfSize)
                        val maxY = (containerSize.y - halfSize).coerceAtLeast(halfSize)
                        
                        transformState = transformState.copy(
                            position = Offset(
                                x = (transformState.position.x + dragAmount.x).coerceIn(halfSize, maxX),
                                y = (transformState.position.y + dragAmount.y).coerceIn(halfSize, maxY)
                            )
                        )
                    }
                }
            }
            // Multi-touch handling - only consume events when actually processing
            .pointerInput(containerSize) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pointers = event.changes.filter { it.pressed }
                        
                        when (pointers.size) {
                            2 -> {
                                val firstPointer = pointers[0].position
                                val secondPointer = pointers[1].position
                                
                                // Convert sticker-relative coordinates to container coordinates for edge detection
                                val displaySizePx = with(density) { displaySize.toPx() }
                                val halfDisplaySize = displaySizePx / 2f
                                
                                val firstPointerContainer = Offset(
                                    x = boundedPosition.x + firstPointer.x - halfDisplaySize,
                                    y = boundedPosition.y + firstPointer.y - halfDisplaySize
                                )
                                val secondPointerContainer = Offset(
                                    x = boundedPosition.x + secondPointer.x - halfDisplaySize,
                                    y = boundedPosition.y + secondPointer.y - halfDisplaySize
                                )
                                
                                // Only process multi-touch if both fingers are away from edges
                                if (!isNearEdge(firstPointerContainer) && !isNearEdge(secondPointerContainer)) {
                                    val currentDistance = hypot(
                                        secondPointer.x - firstPointer.x,
                                        secondPointer.y - firstPointer.y
                                    )
                                    
                                    if (touchPoints.size == 2) {
                                        val previousDistance = hypot(
                                            touchPoints[1].x - touchPoints[0].x,
                                            touchPoints[1].y - touchPoints[0].y
                                        )
                                        
                                        if (previousDistance > 0) {
                                            val zoomFactor = currentDistance / previousDistance
                                            val newScale = validateScale(transformState.scale * zoomFactor)
                                            
                                            // Calculate rotation
                                            val previousAngle = atan2(
                                                touchPoints[1].y - touchPoints[0].y,
                                                touchPoints[1].x - touchPoints[0].x
                                            )
                                            val currentAngle = atan2(
                                                secondPointer.y - firstPointer.y,
                                                secondPointer.x - firstPointer.x
                                            )
                                            val angleChange = (currentAngle - previousAngle) * (180f / PI.toFloat())
                                            val newRotation = (transformState.rotation + angleChange) % 360
                                            
                                            // Update with validated scale and consistent boundary checking
                                            val updatedState = transformState.copy(
                                                scale = newScale,
                                                rotation = newRotation
                                            )
                                            
                                            // FIXED: Consistent boundary validation using half-size
                                            val newEmojiSize = emojiSticker.size * newScale
                                            val halfSize = newEmojiSize / 2f
                                            val maxX = (containerSize.x - halfSize).coerceAtLeast(halfSize)
                                            val maxY = (containerSize.y - halfSize).coerceAtLeast(halfSize)
                                            
                                            transformState = updatedState.copy(
                                                position = Offset(
                                                    x = transformState.position.x.coerceIn(halfSize, maxX),
                                                    y = transformState.position.y.coerceIn(halfSize, maxY)
                                                )
                                            )
                                        }
                                    }
                                    
                                    touchPoints = listOf(firstPointer, secondPointer)
                                    // Only consume events when we actually process them
                                    pointers.forEach { it.consume() }
                                }
                            }
                            0 -> touchPoints = emptyList()
                        }
                    }
                }
            }
    ) {
        Text(
            text = emojiSticker.emoji,
            fontSize = remember(emojiSize, density) { 
                with(density) { (emojiSize * 0.8f).toSp() }
            },
            modifier = Modifier.padding(8.dp)
        )
    }
}

/**
 * Consolidated transform state to minimize recompositions
 */
private data class TransformState(
    val position: Offset,
    val scale: Float,
    val rotation: Float
)

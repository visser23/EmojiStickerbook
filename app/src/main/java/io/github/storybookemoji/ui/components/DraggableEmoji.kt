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
import kotlin.math.roundToInt

/**
 * A performance-optimized draggable emoji component
 * Minimizes recompositions and memory allocations
 */
@Composable
fun DraggableEmoji(
    emojiSticker: EmojiSticker,
    onPositionChange: (Offset) -> Unit,
    onRemove: () -> Unit,
    containerSize: Offset
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
    
    // Cache expensive calculations
    val emojiSize = remember(transformState.scale) { 
        emojiSticker.size * transformState.scale
    }
    
    val displaySize = remember(emojiSize, density) { 
        with(density) { (emojiSize * 1.5f).toDp() }
    }
    
    // Optimize bounds calculation
    val boundedPosition = remember(transformState.position, containerSize, emojiSize) {
        val maxX = (containerSize.x - emojiSize).coerceAtLeast(0f)
        val maxY = (containerSize.y - emojiSize).coerceAtLeast(0f)
        Offset(
            x = transformState.position.x.coerceIn(0f, maxX),
            y = transformState.position.y.coerceIn(0f, maxY)
        )
    }
    
    // Update sticker properties only when necessary
    LaunchedEffect(transformState) {
        emojiSticker.scale = transformState.scale
        emojiSticker.rotation = transformState.rotation
        emojiSticker.position = transformState.position
        onPositionChange(transformState.position)
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
            // Optimized drag handling
            .pointerInput(containerSize, emojiSize) {
                detectDragGestures { change, dragAmount ->
                    val isHorizontalEdgeSwipe = abs(dragAmount.x) > abs(dragAmount.y) && 
                                               isNearEdge(change.position)
                    
                    if (!isHorizontalEdgeSwipe) {
                        change.consume()
                        val maxX = (containerSize.x - emojiSize).coerceAtLeast(0f)
                        val maxY = (containerSize.y - emojiSize).coerceAtLeast(0f)
                        
                        transformState = transformState.copy(
                            position = Offset(
                                x = (transformState.position.x + dragAmount.x).coerceIn(0f, maxX),
                                y = (transformState.position.y + dragAmount.y).coerceIn(0f, maxY)
                            )
                        )
                    }
                }
            }
            // Optimized multi-touch handling
            .pointerInput(containerSize) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pointers = event.changes.filter { it.pressed }
                        
                        when (pointers.size) {
                            2 -> {
                                val firstPointer = pointers[0].position
                                val secondPointer = pointers[1].position
                                
                                if (!isNearEdge(firstPointer) || !isNearEdge(secondPointer)) {
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
                                            val newScale = (transformState.scale * zoomFactor)
                                                .coerceIn(EmojiSticker.MIN_SCALE, EmojiSticker.MAX_SCALE)
                                            
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
                                            
                                            transformState = transformState.copy(
                                                scale = newScale,
                                                rotation = newRotation
                                            )
                                        }
                                    }
                                    
                                    touchPoints = listOf(firstPointer, secondPointer)
                                    pointers.forEach { it.consume() }
                                }
                            }
                            0 -> touchPoints = emptyList()
                        }
                    }
                }
            }
            // Long press for deletion
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { position ->
                        if (!isNearEdge(position)) {
                            onRemove()
                        }
                    }
                )
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

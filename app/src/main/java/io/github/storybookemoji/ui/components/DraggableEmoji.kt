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
import androidx.compose.ui.input.pointer.PointerInputChange
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
    
    // Add padding to prevent cutoff (50% extra space)
    val paddingFactor = 1.5f
    val displaySize = with(density) { (emojiSize * paddingFactor).toDp() }
    
    // Ensure emoji stays within screen bounds
    val boundedPosition = remember(currentPosition, containerSize, emojiSize) {
        Offset(
            x = currentPosition.x.coerceIn(0f, (containerSize.x - emojiSize).coerceAtLeast(0f)),
            y = currentPosition.y.coerceIn(0f, (containerSize.y - emojiSize).coerceAtLeast(0f))
        )
    }
    
    // Update emoji sticker properties when they change
    LaunchedEffect(currentScale, currentRotation, currentPosition) {
        emojiSticker.scale = currentScale
        emojiSticker.rotation = currentRotation
        emojiSticker.position = currentPosition
        onPositionChange(currentPosition)
    }
    
    // Track multi-touch state
    var touchPoints by remember { mutableStateOf(listOf<Offset>()) }
    
    // Define edge threshold for page turning (% of screen width)
    val edgeThresholdPercent = 0.15f
    val edgeThresholdPx = containerSize.x * edgeThresholdPercent
    
    // Function to check if a touch is near the edge (for page turning)
    val isNearEdge = { position: Offset ->
        position.x < edgeThresholdPx || position.x > containerSize.x - edgeThresholdPx
    }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .offset {
                IntOffset(
                    boundedPosition.x.roundToInt(),
                    boundedPosition.y.roundToInt()
                )
            }
            // Use larger size with padding to prevent cutoff
            .size(displaySize)
            .rotate(currentRotation)
            // Simple drag handling
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    // Check if the drag is primarily horizontal and near the edge
                    val isHorizontalEdgeSwipe = abs(dragAmount.x) > abs(dragAmount.y) && 
                                               isNearEdge(change.position)
                    
                    // Only consume if not a potential page turn gesture
                    if (!isHorizontalEdgeSwipe) {
                        change.consume()
                        // Update position with bounds check
                        val newPosition = Offset(
                            x = (currentPosition.x + dragAmount.x).coerceIn(0f, (containerSize.x - emojiSize).coerceAtLeast(0f)),
                            y = (currentPosition.y + dragAmount.y).coerceIn(0f, (containerSize.y - emojiSize).coerceAtLeast(0f))
                        )
                        currentPosition = newPosition
                    }
                }
            }
            // Custom multi-touch handling for scaling
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pointers = event.changes.filter { it.pressed }
                        
                        // Handle scaling with two fingers
                        if (pointers.size == 2) {
                            val firstPointer = pointers[0].position
                            val secondPointer = pointers[1].position
                            
                            // Check if both pointers are near edges (potential page turn)
                            val bothNearEdge = isNearEdge(firstPointer) && isNearEdge(secondPointer)
                            
                            // Only handle if not a potential page turn gesture
                            if (!bothNearEdge) {
                                // Calculate distance between pointers
                                val currentDistance = hypot(
                                    secondPointer.x - firstPointer.x,
                                    secondPointer.y - firstPointer.y
                                )
                                
                                // If we have previous points, calculate scaling
                                if (touchPoints.size == 2) {
                                    val previousDistance = hypot(
                                        touchPoints[1].x - touchPoints[0].x,
                                        touchPoints[1].y - touchPoints[0].y
                                    )
                                    
                                    // Calculate zoom factor
                                    if (previousDistance > 0) {
                                        val zoomFactor = currentDistance / previousDistance
                                        
                                        // Apply scaling with minimum limit
                                        currentScale = (currentScale * zoomFactor).coerceAtLeast(0.5f)
                                        println("Scale changed to: $currentScale")
                                        
                                        // Calculate rotation change
                                        val previousAngle = atan2(
                                            touchPoints[1].y - touchPoints[0].y,
                                            touchPoints[1].x - touchPoints[0].x
                                        )
                                        val currentAngle = atan2(
                                            secondPointer.y - firstPointer.y,
                                            secondPointer.x - firstPointer.x
                                        )
                                        val angleChange = (currentAngle - previousAngle) * (180f / PI.toFloat())
                                        
                                        // Apply rotation
                                        currentRotation = (currentRotation + angleChange) % 360
                                        println("Rotation changed to: $currentRotation")
                                    }
                                }
                                
                                // Update touch points for next calculation
                                touchPoints = listOf(firstPointer, secondPointer)
                                
                                // Consume all changes
                                pointers.forEach { it.consume() }
                            }
                        } else if (pointers.isEmpty()) {
                            // Reset touch points when all fingers are lifted
                            touchPoints = emptyList()
                        }
                    }
                }
            }
            // Handle long press for deletion
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        // Only handle long press if not near edge
                        if (!isNearEdge(it)) {
                            onRemove()
                            println("Long press detected - removing emoji")
                        }
                    }
                )
            }
    ) {
        Text(
            text = emojiSticker.emoji,
            fontSize = with(density) { (emojiSize * 0.8f).toSp() },
            // Add padding to ensure text has room to display
            modifier = Modifier.padding(8.dp)
        )
    }
}

package io.github.storybookemoji.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import kotlin.math.*

/**
 * Performance-optimized draggable emoji component with full gesture support.
 * 
 * Key optimizations:
 * - Deferred state reads to minimize recompositions
 * - Efficient gesture handling with cached calculations
 * - Optimized boundary validation
 * - Multi-touch support for scaling and rotation
 */
@Composable
fun DraggableEmoji(
    emojiSticker: EmojiSticker,
    containerSize: Offset,
    isTopmost: Boolean = true,
    onPositionChange: (Offset) -> Unit,
    onDragStart: () -> Unit = {},
    onDragEnd: (Offset) -> Unit = {},
    onRemove: () -> Unit
) {
    // Note: LocalDensity removed as it's not currently used in this optimized implementation
    
    // Performance optimization: Single state object to minimize recompositions
    var transformState by remember {
        mutableStateOf(
            TransformState(
                position = emojiSticker.position,
                scale = emojiSticker.scale,
                rotation = emojiSticker.rotation
            )
        )
    }
    
    // Gesture coordination state
    var isGestureActive by remember { mutableStateOf(false) }
    
    // Deferred calculations - only computed when needed, not on every recomposition
    val emojiSizePx = remember(transformState.scale) { 
        emojiSticker.size * transformState.scale
    }
    
    // Enhanced display size for better grabability - more generous sizing
    val displaySizeDp = remember(transformState.scale) { 
        // Direct calculation: 120dp base * scale * 1.6 multiplier for good touch targets
        val baseSize = (EmojiSticker.DEFAULT_SIZE_DP * transformState.scale * 1.6f).dp
        // Ensure minimum touch target compliance
        baseSize.coerceAtLeast(EmojiSticker.MIN_TOUCH_TARGET_DP.dp)
    }
    
    // Pre-calculated boundary constraints - computed once per container size change
    val boundaryConstraints = remember(containerSize, emojiSticker.size) {
        BoundaryConstraints(
            containerSize = containerSize,
            baseSize = emojiSticker.size,
            edgeThreshold = containerSize.x * 0.15f
        )
    }
    
    // Optimized position validation - only when transform state changes
    val validatedPosition = remember(transformState, boundaryConstraints) {
        boundaryConstraints.validatePosition(transformState.position, emojiSizePx)
    }
    
    // Scale validation function with dynamic max scale
    val validateScale = remember(boundaryConstraints) {
        { newScale: Float ->
            val dynamicMaxScale = boundaryConstraints.getMaxScale()
            newScale.coerceIn(EmojiSticker.MIN_SCALE, dynamicMaxScale)
        }
    }
    
    // Efficient state synchronization - batched updates
    LaunchedEffect(transformState) {
        emojiSticker.apply {
            scale = transformState.scale
            rotation = transformState.rotation
            position = validatedPosition
        }
        onPositionChange(validatedPosition)
    }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            // Use lambda-based offset for performance - skips recomposition
            .offset { 
                IntOffset(
                    validatedPosition.x.roundToInt(),
                    validatedPosition.y.roundToInt()
                )
            }
            .size(displaySizeDp)
            .rotate(transformState.rotation)
                        // Unified gesture handling - prevents conflicts between multiple gesture detectors
            .pointerInput(containerSize, isTopmost) {
                detectDragGestures(
                    onDragStart = { 
                        isGestureActive = true
                        onDragStart()
                    },
                    onDragEnd = { 
                        isGestureActive = false
                        onDragEnd(transformState.position)
                    },
                    onDrag = { _, dragAmount ->
                        if (!isGestureActive) return@detectDragGestures
                        
                        val newPosition = transformState.position + dragAmount
                        val validatedNewPosition = boundaryConstraints.validatePosition(newPosition, emojiSizePx)
                        
                        transformState = transformState.copy(position = validatedNewPosition)
                    }
                )
            }
            .pointerInput(containerSize, isTopmost) {
                detectTapGestures(
                    onLongPress = { localPosition ->
                        if (isTopmost && !boundaryConstraints.isNearEdge(localPosition, validatedPosition, displaySizeDp.toPx())) {
                            onRemove()
                        }
                    }
                )
            }
            .pointerInput(containerSize, isTopmost) {
                // Only handle transform gestures for topmost stickers
                if (isTopmost) {
                    detectTransformGestures(
                        panZoomLock = false // Allow panning during scaling
                    ) { _, pan, zoom, rotation ->
                        val currentScale = transformState.scale
                        val newScale = validateScale(currentScale * zoom)
                        
                        // Update transform state if scaling or rotation occurred
                        if (newScale != currentScale || rotation != 0f) {
                            val newPosition = transformState.position + pan
                            val validatedNewPosition = boundaryConstraints.validatePosition(newPosition, emojiSticker.size * newScale)
                            
                            transformState = transformState.copy(
                                scale = newScale,
                                position = validatedNewPosition,
                                rotation = transformState.rotation + rotation
                            )
                        }
                    }
                }
            }
    ) {
        Text(
            text = emojiSticker.emoji,
            // Larger font size for better visibility and easier grabbing
            fontSize = remember(transformState.scale) { 
                // Direct calculation: 120dp base * scale * 0.9 for larger visual emoji
                (EmojiSticker.DEFAULT_SIZE_DP * transformState.scale * 0.9f).sp
            },
            modifier = Modifier.padding(2.dp) // Reduced padding for more emoji space
        )
    }
}

/**
 * Optimized boundary constraint calculations
 */
private data class BoundaryConstraints(
    val containerSize: Offset,
    val baseSize: Float,
    val edgeThreshold: Float
) {
    fun validatePosition(position: Offset, currentSize: Float): Offset {
        val halfSize = currentSize / 2f
        val maxX = (containerSize.x - halfSize).coerceAtLeast(halfSize)
        val maxY = (containerSize.y - halfSize).coerceAtLeast(halfSize)
        
        return Offset(
            x = position.x.coerceIn(halfSize, maxX),
            y = position.y.coerceIn(halfSize, maxY)
        )
    }
    
    fun isNearEdge(localPosition: Offset, stickerPosition: Offset, displaySize: Float): Boolean {
        val globalX = stickerPosition.x + localPosition.x - displaySize / 2f
        return globalX < edgeThreshold || globalX > containerSize.x - edgeThreshold
    }
    
    // Renamed function to avoid overload ambiguity
    fun isContainerEdge(position: Offset): Boolean {
        return position.x < edgeThreshold || position.x > containerSize.x - edgeThreshold
    }
    
    fun isEdgeSwipe(position: Offset, dragAmount: Offset): Boolean {
        return abs(dragAmount.x) > abs(dragAmount.y) && isContainerEdge(position)
    }
    
    fun getMaxScale(): Float {
        val maxSizeForContainer = min(containerSize.x, containerSize.y) * 0.8f // Restored to 80%
        return (maxSizeForContainer / baseSize).coerceAtMost(EmojiSticker.MAX_SCALE)
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



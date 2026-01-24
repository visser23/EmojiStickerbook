package io.github.storybookemoji.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.storybookemoji.model.EmojiSticker
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Draggable emoji component with LOCAL state tracking during gestures.
 * 
 * KEY FIX: The previous implementation relied on rememberUpdatedState to get
 * the current scale/rotation/position during gestures. But Compose doesn't
 * recompose fast enough during rapid gesture events, so the state was always
 * stale (always reading initial values like scale=1.0).
 * 
 * SOLUTION: Track scale/rotation/position LOCALLY during gestures, accumulating
 * changes correctly. Sync with external state when gesture ends or when
 * external state changes while not gesturing.
 */
@Composable
fun DraggableEmoji(
    emojiSticker: EmojiSticker,
    containerSize: Offset,
    isTopmost: Boolean = true,
    onStickerUpdate: (EmojiSticker) -> Unit,  // Single callback with complete sticker
    onDragStart: () -> Unit = {},
    onDragEnd: (Offset) -> Unit = {},
    @Suppress("UNUSED_PARAMETER")
    onRemove: () -> Unit  // Kept for API compatibility - deletion is via drag-to-bin only
) {
    // LOCAL state - this is the source of truth DURING gestures
    // These accumulate changes correctly without waiting for recomposition
    var localScale by remember { mutableStateOf(emojiSticker.scale) }
    var localRotation by remember { mutableStateOf(emojiSticker.rotation) }
    var localPosition by remember { mutableStateOf(emojiSticker.position) }
    var isGestureActive by remember { mutableStateOf(false) }
    
    // Sync local state with external state when NOT gesturing
    // This handles: initial load, undo, page navigation, external updates
    LaunchedEffect(emojiSticker.scale, emojiSticker.rotation, emojiSticker.position) {
        if (!isGestureActive) {
            localScale = emojiSticker.scale
            localRotation = emojiSticker.rotation
            localPosition = emojiSticker.position
            DebugState.logEvent("Synced local state from external: scale=${emojiSticker.scale}, rot=${emojiSticker.rotation}")
        }
    }
    
    // Callbacks - use rememberUpdatedState so we always call the latest callback
    val currentOnStickerUpdate by rememberUpdatedState(onStickerUpdate)
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentIsTopmost by rememberUpdatedState(isTopmost)
    
    // Calculate display size based on LOCAL scale (for immediate visual feedback)
    val displaySizeDp = remember(localScale) { 
        (EmojiSticker.DEFAULT_SIZE_DP * localScale * 1.6f).dp
            .coerceAtLeast(EmojiSticker.MIN_TOUCH_TARGET_DP.dp)
    }
    
    // Calculate font size based on LOCAL scale
    val fontSize = remember(localScale) {
        (EmojiSticker.DEFAULT_SIZE_DP * localScale * 0.9f).sp
    }
    
    // Calculate max scale dynamically
    val dynamicMaxScale = remember(containerSize, emojiSticker.size) {
        if (containerSize.x > 0 && containerSize.y > 0) {
            val maxDimension = min(containerSize.x, containerSize.y)
            val maxAllowedSize = maxDimension * 0.7f
            (maxAllowedSize / emojiSticker.size).coerceIn(EmojiSticker.MIN_SCALE, EmojiSticker.MAX_SCALE)
        } else {
            EmojiSticker.MAX_SCALE
        }
    }
    
    fun validatePosition(position: Offset, scale: Float): Offset {
        val halfSize = (emojiSticker.size * scale) / 2f
        val maxX = (containerSize.x - halfSize).coerceAtLeast(halfSize)
        val maxY = (containerSize.y - halfSize).coerceAtLeast(halfSize)
        return Offset(
            x = position.x.coerceIn(halfSize, maxX),
            y = position.y.coerceIn(halfSize, maxY)
        )
    }
    
    fun validateScale(scale: Float): Float {
        return scale.coerceIn(EmojiSticker.MIN_SCALE, dynamicMaxScale)
    }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            // Use LOCAL position for immediate visual feedback
            .offset { 
                IntOffset(
                    localPosition.x.roundToInt(),
                    localPosition.y.roundToInt()
                )
            }
            .size(displaySizeDp)
            // Use LOCAL rotation for immediate visual feedback
            .rotate(localRotation)
            .pointerInput(emojiSticker.id, containerSize) {
                awaitEachGesture {
                    val firstDown = awaitFirstDown(requireUnconsumed = false)
                    
                    if (!currentIsTopmost) {
                        DebugState.logEvent("Ignoring touch - not topmost")
                        return@awaitEachGesture
                    }
                    
                    DebugState.logEvent("Touch DOWN at ${firstDown.position}")
                    
                    // Mark gesture as active - this prevents external sync from overwriting local state
                    isGestureActive = true
                    currentOnDragStart()
                    DebugState.logEvent("Gesture START - local state: scale=$localScale, rot=$localRotation")
                    
                    var pastTouchSlop = false
                    val touchSlop = viewConfiguration.touchSlop
                    var lockedToPanZoom = false
                    
                    // Accumulators for touch slop detection
                    var accumulatedZoom = 1f
                    var accumulatedRotation = 0f
                    var accumulatedPan = Offset.Zero
                    
                    do {
                        val event = awaitPointerEvent()
                        val canceled = event.changes.any { change: PointerInputChange -> change.isConsumed }
                        
                        if (!canceled) {
                            val zoomChange = event.calculateZoom()
                            val rotationChange = event.calculateRotation()
                            val panChange = event.calculatePan()
                            
                            val pointerCount = event.changes.count { change: PointerInputChange -> change.pressed }
                            DebugState.logGesture(
                                type = if (pointerCount >= 2) "Multi($pointerCount)" else "Single",
                                pointers = pointerCount,
                                zoom = zoomChange,
                                rotation = rotationChange,
                                panX = panChange.x,
                                panY = panChange.y
                            )
                            
                            if (!pastTouchSlop) {
                                accumulatedZoom *= zoomChange
                                accumulatedRotation += rotationChange
                                accumulatedPan += panChange
                                
                                val centroidSize = event.calculateCentroidSize(useCurrent = false)
                                val zoomMotion = abs(1f - accumulatedZoom) * centroidSize
                                val rotationMotion = abs(accumulatedRotation * PI.toFloat() * centroidSize / 180f)
                                val panMotion = accumulatedPan.getDistance()
                                
                                if (zoomMotion > touchSlop || rotationMotion > touchSlop || panMotion > touchSlop) {
                                    pastTouchSlop = true
                                    lockedToPanZoom = (zoomMotion > rotationMotion) || (panMotion > rotationMotion)
                                    DebugState.logEvent("Past touch slop - gesture active, lockedToPanZoom=$lockedToPanZoom")
                                }
                            }
                            
                            if (pastTouchSlop) {
                                val effectiveRotation = if (lockedToPanZoom) 0f else rotationChange
                                var stateChanged = false
                                
                                // Apply scale - accumulate on LOCAL state
                                if (zoomChange != 1f) {
                                    val newScale = validateScale(localScale * zoomChange)
                                    DebugState.logEvent("SCALE: local=${"%.3f".format(localScale)} * zoom=${"%.3f".format(zoomChange)} = ${"%.3f".format(newScale)}")
                                    localScale = newScale
                                    stateChanged = true
                                }
                                
                                // Apply rotation - accumulate on LOCAL state
                                if (effectiveRotation != 0f) {
                                    val newRotation = localRotation + effectiveRotation
                                    DebugState.logEvent("ROTATE: local=${"%.1f".format(localRotation)} + ${"%.1f".format(effectiveRotation)} = ${"%.1f".format(newRotation)}")
                                    localRotation = newRotation
                                    stateChanged = true
                                }
                                
                                // Apply pan - accumulate on LOCAL state
                                if (panChange != Offset.Zero) {
                                    val newPos = validatePosition(localPosition + panChange, localScale)
                                    localPosition = newPos
                                    stateChanged = true
                                }
                                
                                // Send COMPLETE sticker with ALL local state to ViewModel
                                // This prevents stale closure issues where separate callbacks
                                // would overwrite each other's changes
                                if (stateChanged) {
                                    val updatedSticker = emojiSticker.copy(
                                        position = localPosition,
                                        scale = localScale,
                                        rotation = localRotation
                                    )
                                    DebugState.logEvent("UPDATE: sending complete sticker - scale=${"%.3f".format(localScale)}, rot=${"%.1f".format(localRotation)}")
                                    currentOnStickerUpdate(updatedSticker)
                                }
                                
                                event.changes.forEach { change: PointerInputChange -> 
                                    if (change.positionChanged()) {
                                        change.consume()
                                    }
                                }
                            }
                        }
                    } while (!canceled && event.changes.any { change: PointerInputChange -> change.pressed })
                    
                    // Gesture ended
                    isGestureActive = false
                    DebugState.logEvent("Gesture END - final local state: scale=$localScale, rot=$localRotation, pos=$localPosition")
                    currentOnDragEnd(localPosition)
                }
            }
    ) {
        Text(
            text = emojiSticker.emoji,
            fontSize = fontSize,
            modifier = Modifier.padding(2.dp)
        )
    }
}

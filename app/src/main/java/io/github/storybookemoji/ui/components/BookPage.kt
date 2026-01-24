package io.github.storybookemoji.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.github.storybookemoji.model.EmojiSticker
import io.github.storybookemoji.model.PageData

/**
 * A single page in the sticker book.
 * 
 * Manages sticker rendering, touch events for placement, and coordinates
 * with the deletion bin for drag-to-delete functionality.
 */
@Composable
fun BookPage(
    pageData: PageData,
    onAddSticker: (String, Offset) -> Unit,
    onUpdateSticker: (EmojiSticker) -> Unit,
    onRemoveSticker: (EmojiSticker) -> Unit
) {
    val density = LocalDensity.current
    
    // Consolidated UI state
    var uiState by remember {
        mutableStateOf(
            PageUiState(
                showEmojiSelector = false,
                touchPosition = Offset.Zero,
                containerSize = Offset.Zero,
                showDeletionBin = false,
                isDraggingSticker = false,
                currentDragPosition = Offset.Zero,
                draggingStickerId = null
            )
        )
    }
    
    // Track which sticker is being dragged for bin hover detection
    var isHoveringOverBin by remember { mutableStateOf(false) }
    
    // Calculate bin position in pixels (properly converted from dp)
    val binPositionPx = remember(uiState.containerSize) {
        if (uiState.containerSize.x > 0) {
            with(density) {
                Offset(
                    x = uiState.containerSize.x - 56.dp.toPx(),
                    y = 56.dp.toPx()
                )
            }
        } else {
            Offset.Zero
        }
    }
    
    // Get stickers as stable list
    val stickers = remember(pageData.id, pageData.emojiStickers) { 
        pageData.emojiStickers.toList()
    }
    
    /**
     * Determines which sticker is topmost at a given touch point.
     * Returns the sticker's ID if it's the topmost at that point, null otherwise.
     */
    fun isTopmostAt(sticker: EmojiSticker, touchPoint: Offset): Boolean {
        val topmostSticker = stickers
            .filter { it.containsPoint(touchPoint, tolerance = 40f) }
            .lastOrNull() // Last in list = rendered on top
        return topmostSticker?.id == sticker.id
    }
    
    /**
     * Check if a position is hovering over the deletion bin
     */
    fun checkBinHover(position: Offset): Boolean {
        return isHoveringOverDeletionBin(position, binPositionPx)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(pageData.backgroundGradient)
            .onGloballyPositioned { coordinates ->
                val newSize = Offset(
                    coordinates.size.width.toFloat(),
                    coordinates.size.height.toFloat()
                )
                if (newSize != uiState.containerSize) {
                    uiState = uiState.copy(containerSize = newSize)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { position ->
                    // Only show emoji selector if tap is not on an existing sticker
                    val stickerAtTap = pageData.findStickerAt(position, tolerance = 40f)
                    if (stickerAtTap == null) {
                        uiState = uiState.copy(
                            touchPosition = position,
                            showEmojiSelector = true
                        )
                    }
                }
            }
    ) {
        // Render stickers in order (last = topmost)
        stickers.forEach { sticker ->
            key(sticker.id) {
                // Determine if this sticker is topmost at its own center
                // This is used for gesture handling priority
                val isTopmost = sticker == stickers.lastOrNull { 
                    it.containsPoint(sticker.position, tolerance = 20f) 
                }
                
                // Track dragging state PER STICKER to avoid race conditions
                var isThisStickerDragging by remember { mutableStateOf(false) }
                
                DraggableEmoji(
                    emojiSticker = sticker,
                    containerSize = uiState.containerSize,
                    isTopmost = isTopmost,
                    onStickerUpdate = { updatedSticker ->
                        // DraggableEmoji now sends COMPLETE sticker with all local state
                        // This prevents stale closure issues where separate callbacks
                        // would overwrite each other's changes
                        DebugState.logEvent("BookPage.onStickerUpdate: scale=${updatedSticker.scale}, rot=${updatedSticker.rotation}")
                        DebugState.logStateUpdate(updatedSticker.scale, updatedSticker.rotation)
                        onUpdateSticker(updatedSticker)
                        
                        // Check bin hover during drag
                        if (isThisStickerDragging) {
                            val hovering = checkBinHover(updatedSticker.position)
                            isHoveringOverBin = hovering
                        }
                    },
                    onDragStart = {
                        isThisStickerDragging = true
                        isHoveringOverBin = false
                        uiState = uiState.copy(
                            showDeletionBin = true,
                            isDraggingSticker = true,
                            draggingStickerId = sticker.id
                        )
                        DebugState.logEvent("BookPage.onDragStart: bin should be visible")
                    },
                    onDragEnd = { finalPosition ->
                        DebugState.logEvent("BookPage.onDragEnd: finalPos=$finalPosition, binPos=$binPositionPx")
                        // Check if dropped on deletion bin using FINAL position
                        val isOverBin = checkBinHover(finalPosition)
                        DebugState.logEvent("BookPage.onDragEnd: isOverBin=$isOverBin")
                        if (isOverBin) {
                            DebugState.logEvent("BookPage.onDragEnd: DELETING sticker ${sticker.id}")
                            onRemoveSticker(sticker)
                        }
                        
                        isThisStickerDragging = false
                        isHoveringOverBin = false
                        uiState = uiState.copy(
                            showDeletionBin = false,
                            isDraggingSticker = false,
                            draggingStickerId = null
                        )
                    },
                    onRemove = {
                        // Long press deletion
                        onRemoveSticker(sticker)
                    }
                )
            }
        }
        
        // Deletion bin - positioned in top-right corner
        DeletionBin(
            visible = uiState.showDeletionBin,
            isHoveringOverBin = isHoveringOverBin,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )
        
        // Debug activator - invisible tap zone in top-right corner
        // Tap 4 times then long-press to toggle debug mode
        DebugActivator(
            modifier = Modifier.align(Alignment.TopEnd)
        )
        
        // Debug overlay at bottom of screen
        DebugOverlay(
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        
        // Show emoji selector when needed
        if (uiState.showEmojiSelector) {
            EmojiSelector(
                onEmojiSelected = { emoji ->
                    onAddSticker(emoji, uiState.touchPosition)
                    uiState = uiState.copy(showEmojiSelector = false)
                },
                onDismiss = {
                    uiState = uiState.copy(showEmojiSelector = false)
                }
            )
        }
    }
}

/**
 * Consolidated UI state for the page
 */
private data class PageUiState(
    val showEmojiSelector: Boolean,
    val touchPosition: Offset,
    val containerSize: Offset,
    val showDeletionBin: Boolean,
    val isDraggingSticker: Boolean,
    val currentDragPosition: Offset,
    val draggingStickerId: Long?
)

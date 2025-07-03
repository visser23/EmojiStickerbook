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
import androidx.compose.ui.unit.dp
import io.github.storybookemoji.model.EmojiSticker
import io.github.storybookemoji.model.PageData

/**
 * A performance-optimized single page in the sticker book
 * Uses efficient state management and minimal recompositions
 */
@Composable
fun BookPage(
    pageData: PageData,
    onAddSticker: (String, Offset) -> Unit,
    onUpdateSticker: (EmojiSticker) -> Unit,
    onRemoveSticker: (EmojiSticker) -> Unit
) {
    // Consolidated UI state
    var uiState by remember {
        mutableStateOf(
            PageUiState(
                showEmojiSelector = false,
                touchPosition = Offset.Zero,
                containerSize = Offset.Zero,
                showDeletionBin = false,
                isDraggingSticker = false
            )
        )
    }
    
    // Optimize sticker list with stable keys and memoization
    val stickers = remember(pageData.id, pageData.emojiStickers.size) { 
        pageData.emojiStickers.toList()
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
                    uiState = uiState.copy(
                        touchPosition = position,
                        showEmojiSelector = true
                    )
                }
            }
    ) {
        // Render stickers with optimized keys for better performance
        // Render in order so topmost stickers (last in list) are rendered last and handle touches first
        stickers.forEach { sticker ->
            key(sticker.id) {
                DraggableEmoji(
                    emojiSticker = sticker,
                    containerSize = uiState.containerSize,
                    isTopmost = sticker == stickers.lastOrNull { it.containsPoint(sticker.position, 40f) },
                    onPositionChange = { newPosition ->
                        // Create updated sticker immutably
                        val updatedSticker = sticker.copy(position = newPosition)
                        onUpdateSticker(updatedSticker)
                    },
                    onDragStart = {
                        uiState = uiState.copy(
                            showDeletionBin = true,
                            isDraggingSticker = true
                        )
                    },
                    onDragEnd = { position ->
                        uiState = uiState.copy(
                            showDeletionBin = false,
                            isDraggingSticker = false
                        )
                        
                        // Check if dropped on deletion bin
                        val binPosition = Offset(
                            x = uiState.containerSize.x - 60.dp.value,
                            y = 60.dp.value
                        )
                        
                        if (isHoveringOverDeletionBin(position, binPosition)) {
                            // Find topmost sticker at this position for removal
                            val topmostSticker = pageData.findStickerAt(sticker.position, tolerance = 40f)
                            if (topmostSticker != null) {
                                onRemoveSticker(topmostSticker)
                            } else {
                                onRemoveSticker(sticker)
                            }
                        }
                    },
                    onRemove = {
                        // Long press deletion as backup method
                        val topmostSticker = pageData.findStickerAt(sticker.position, tolerance = 40f)
                        if (topmostSticker != null) {
                            onRemoveSticker(topmostSticker)
                        } else {
                            onRemoveSticker(sticker)
                        }
                    }
                )
            }
        }
        
        // Deletion bin - positioned in top-right corner
        if (uiState.showDeletionBin) {
            DeletionBin(
                visible = uiState.showDeletionBin,
                isHoveringOverBin = false, // TODO: implement hover detection
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }
        
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
 * Consolidated UI state to minimize recompositions
 */
private data class PageUiState(
    val showEmojiSelector: Boolean,
    val touchPosition: Offset,
    val containerSize: Offset,
    val showDeletionBin: Boolean,
    val isDraggingSticker: Boolean
) 

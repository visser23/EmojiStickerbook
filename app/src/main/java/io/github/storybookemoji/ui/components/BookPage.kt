package io.github.storybookemoji.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import io.github.storybookemoji.model.EmojiSticker
import io.github.storybookemoji.model.PageData
import androidx.compose.foundation.gestures.detectTapGestures

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
                containerSize = Offset.Zero
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
                    onPositionChange = { newPosition ->
                        // Create updated sticker immutably
                        val updatedSticker = sticker.copy(position = newPosition)
                        onUpdateSticker(updatedSticker)
                    },
                    onRemove = {
                        // Find topmost sticker at this position for removal
                        val topmostSticker = pageData.findStickerAt(sticker.position, tolerance = 40f)
                        if (topmostSticker != null) {
                            onRemoveSticker(topmostSticker)
                        } else {
                            onRemoveSticker(sticker)
                        }
                    },
                    containerSize = uiState.containerSize,
                    isTopmost = sticker == stickers.lastOrNull { it.containsPoint(sticker.position, 40f) }
                )
            }
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
    val containerSize: Offset
) 

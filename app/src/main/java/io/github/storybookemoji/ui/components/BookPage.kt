package io.github.storybookemoji.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
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
import androidx.compose.foundation.gestures.detectTapGestures

/**
 * A single page in the sticker book
 */
@Composable
fun BookPage(
    pageData: PageData,
    onAddSticker: (String, Offset) -> Unit,
    onUpdateSticker: (EmojiSticker) -> Unit,
    onRemoveSticker: (EmojiSticker) -> Unit
) {
    // State to track if emoji selector is visible
    var showEmojiSelector by remember { mutableStateOf(false) }
    
    // State to track touch position for new emoji placement
    var touchPosition by remember { mutableStateOf(Offset.Zero) }
    
    // Track container size for boundary checks
    var containerSize by remember { mutableStateOf(Offset.Zero) }
    
    // Force recomposition when stickers change
    val stickers by remember(pageData.id, pageData.emojiStickers.size) { 
        derivedStateOf { pageData.emojiStickers.toList() } 
    }
    
    // Debug log to verify stickers
    LaunchedEffect(stickers.size) {
        println("Page ${pageData.id} has ${stickers.size} stickers")
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(pageData.backgroundGradient)
            .onGloballyPositioned { coordinates ->
                containerSize = Offset(
                    coordinates.size.width.toFloat(),
                    coordinates.size.height.toFloat()
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { position ->
                    touchPosition = position
                    showEmojiSelector = true
                }
            }
    ) {
        // Render all stickers on this page with a key to force proper recomposition
        stickers.forEach { sticker ->
            key(sticker.id) {
                DraggableEmoji(
                    emojiSticker = sticker,
                    onPositionChange = { newPosition ->
                        // Update the position in the original sticker
                        sticker.position = newPosition
                        // Notify parent about the update
                        onUpdateSticker(sticker)
                    },
                    onRemove = {
                        onRemoveSticker(sticker)
                    },
                    containerSize = containerSize
                )
            }
        }
        
        // Show emoji selector when needed
        if (showEmojiSelector) {
            EmojiSelector(
                onEmojiSelected = { emoji ->
                    onAddSticker(emoji, touchPosition)
                },
                onDismiss = {
                    showEmojiSelector = false
                }
            )
        }
    }
} 

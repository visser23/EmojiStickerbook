package io.github.storybookemoji.model

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset

/**
 * Represents a page in the sticker book with immutable state
 */
data class PageData(
    val id: Int,
    val backgroundGradient: Brush,
    val emojiStickers: List<EmojiSticker> = emptyList()
) {
    /**
     * Creates a copy with an added sticker
     */
    fun withAddedSticker(sticker: EmojiSticker): PageData = copy(
        emojiStickers = emojiStickers + sticker
    )
    
    /**
     * Creates a copy with an updated sticker
     */
    fun withUpdatedSticker(updatedSticker: EmojiSticker): PageData = copy(
        emojiStickers = emojiStickers.map { 
            if (it.id == updatedSticker.id) updatedSticker else it 
        }
    )
    
    /**
     * Creates a copy with a removed sticker
     */
    fun withRemovedSticker(stickerId: Long): PageData = copy(
        emojiStickers = emojiStickers.filter { it.id != stickerId }
    )
    
    /**
     * Creates a copy with all stickers removed
     */
    fun withClearedStickers(): PageData = copy(
        emojiStickers = emptyList()
    )
    
    /**
     * Finds a sticker by ID
     */
    fun findStickerById(id: Long): EmojiSticker? = 
        emojiStickers.find { it.id == id }

    /**
     * Finds a sticker at the specified position with tolerance
     */
    fun findStickerAt(position: Offset, tolerance: Float = 50f): EmojiSticker? {
        // Return the topmost sticker (last in the list) that contains the point
        return emojiStickers.lastOrNull { sticker ->
            sticker.containsPoint(position, tolerance)
        }
    }
    
    /**
     * Returns the number of stickers on this page
     */
    fun getStickerCount(): Int = emojiStickers.size
    
    /**
     * Checks if the page has no stickers
     */
    fun isEmpty(): Boolean = emojiStickers.isEmpty()
}

/**
 * Collection of background gradients for pages
 */
object PageColors {
    val gradients = listOf(
        // Vibrant sunset - orange to pink
        Brush.linearGradient(
            colors = listOf(Color(0xFFFFA500), Color(0xFFFF1493))
        ),
        // Ocean - light blue to deep blue
        Brush.linearGradient(
            colors = listOf(Color(0xFF87CEEB), Color(0xFF0000CD))
        ),
        // Forest - light green to dark green
        Brush.linearGradient(
            colors = listOf(Color(0xFF90EE90), Color(0xFF006400))
        ),
        // Galaxy - purple to dark blue
        Brush.linearGradient(
            colors = listOf(Color(0xFF800080), Color(0xFF000080))
        ),
        // Candy - pink to light blue
        Brush.linearGradient(
            colors = listOf(Color(0xFFFF69B4), Color(0xFF00BFFF))
        ),
        // Tropical - yellow to orange
        Brush.linearGradient(
            colors = listOf(Color(0xFFFFFF00), Color(0xFFFF8C00))
        ),
        // Lavender field - light purple to dark purple
        Brush.linearGradient(
            colors = listOf(Color(0xFFE6E6FA), Color(0xFF8A2BE2))
        ),
        // Fire - red to yellow
        Brush.linearGradient(
            colors = listOf(Color(0xFFFF0000), Color(0xFFFFFF00))
        )
    )
} 

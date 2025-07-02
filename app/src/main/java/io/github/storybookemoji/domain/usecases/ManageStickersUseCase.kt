package io.github.storybookemoji.domain.usecases

import androidx.compose.ui.geometry.Offset
import io.github.storybookemoji.model.BookState
import io.github.storybookemoji.model.EmojiSticker

/**
 * Use case for managing sticker operations
 * Encapsulates all business logic related to sticker manipulation
 */
class ManageStickersUseCase {
    
    /**
     * Adds a new sticker to the specified page
     */
    fun addStickerToPage(
        bookState: BookState,
        emoji: String,
        position: Offset,
        pageIndex: Int
    ): BookState {
        val page = bookState.pages.getOrNull(pageIndex) ?: return bookState
        
        val newSticker = EmojiSticker(
            id = System.currentTimeMillis(),
            emoji = emoji,
            position = position,
            scale = 3.0f,
            rotation = 0f
        )
        
        val updatedPage = page.withAddedSticker(newSticker)
        return bookState.withUpdatedPage(pageIndex, updatedPage)
    }
    
    /**
     * Updates an existing sticker on the specified page
     */
    fun updateStickerOnPage(
        bookState: BookState,
        updatedSticker: EmojiSticker,
        pageIndex: Int
    ): BookState {
        val page = bookState.pages.getOrNull(pageIndex) ?: return bookState
        
        // Validate that the sticker exists on this page
        if (page.findStickerById(updatedSticker.id) == null) {
            return bookState
        }
        
        val updatedPage = page.withUpdatedSticker(updatedSticker)
        return bookState.withUpdatedPage(pageIndex, updatedPage)
    }
    
    /**
     * Removes a sticker from the specified page
     */
    fun removeStickerFromPage(
        bookState: BookState,
        stickerId: Long,
        pageIndex: Int
    ): BookState {
        val page = bookState.pages.getOrNull(pageIndex) ?: return bookState
        
        val updatedPage = page.withRemovedSticker(stickerId)
        return bookState.withUpdatedPage(pageIndex, updatedPage)
    }
    
    /**
     * Clears all stickers from the specified page
     */
    fun clearPageStickers(
        bookState: BookState,
        pageIndex: Int
    ): BookState {
        val page = bookState.pages.getOrNull(pageIndex) ?: return bookState
        
        val updatedPage = page.withClearedStickers()
        return bookState.withUpdatedPage(pageIndex, updatedPage)
    }
    
    /**
     * Validates sticker scale within acceptable bounds
     */
    fun validateStickerScale(scale: Float): Float {
        return scale.coerceIn(EmojiSticker.MIN_SCALE, EmojiSticker.MAX_SCALE)
    }
    
    /**
     * Validates sticker position within page bounds
     */
    fun validateStickerPosition(
        position: Offset,
        pageWidth: Float,
        pageHeight: Float,
        stickerSize: Float
    ): Offset {
        val halfSize = stickerSize / 2f
        return Offset(
            x = position.x.coerceIn(halfSize, pageWidth - halfSize),
            y = position.y.coerceIn(halfSize, pageHeight - halfSize)
        )
    }
} 
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
            scale = 1.0f,
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
     * Clamps sticker position within page bounds
     */
    fun clampStickerPosition(
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
    
    /**
     * Updates the position of an existing sticker
     */
    fun updateStickerPosition(
        bookState: BookState,
        stickerId: Long,
        newPosition: Offset,
        pageIndex: Int
    ): BookState {
        val page = bookState.pages.getOrNull(pageIndex) ?: return bookState
        val sticker = page.findStickerById(stickerId) ?: return bookState
        
        if (!validateStickerPosition(newPosition)) {
            return bookState
        }
        
        val updatedSticker = sticker.withPosition(newPosition)
        val updatedPage = page.withUpdatedSticker(updatedSticker)
        
        return bookState.withUpdatedPage(pageIndex, updatedPage)
    }
    
    /**
     * Updates the scale of an existing sticker
     */
    fun updateStickerScale(
        bookState: BookState,
        stickerId: Long,
        newScale: Float,
        pageIndex: Int
    ): BookState {
        val page = bookState.pages.getOrNull(pageIndex) ?: return bookState
        val sticker = page.findStickerById(stickerId) ?: return bookState
        
        val updatedSticker = sticker.withScale(newScale) // withScale handles clamping
        val updatedPage = page.withUpdatedSticker(updatedSticker)
        
        return bookState.withUpdatedPage(pageIndex, updatedPage)
    }
    
    /**
     * Updates the rotation of an existing sticker
     */
    fun updateStickerRotation(
        bookState: BookState,
        stickerId: Long,
        newRotation: Float,
        pageIndex: Int
    ): BookState {
        val page = bookState.pages.getOrNull(pageIndex) ?: return bookState
        val sticker = page.findStickerById(stickerId) ?: return bookState
        
        val updatedSticker = sticker.withRotation(newRotation)
        val updatedPage = page.withUpdatedSticker(updatedSticker)
        
        return bookState.withUpdatedPage(pageIndex, updatedPage)
    }
    
    /**
     * Finds a sticker at the specified position
     */
    fun findStickerAtPosition(
        bookState: BookState,
        position: Offset,
        pageIndex: Int,
        tolerance: Float = 50f
    ): EmojiSticker? {
        val page = bookState.pages.getOrNull(pageIndex) ?: return null
        return page.findStickerAt(position, tolerance)
    }
    
    /**
     * Validates that a sticker position is within reasonable bounds
     */
    fun validateStickerPosition(
        position: Offset,
        pageWidth: Float = 1000f,  // Default reasonable page size
        pageHeight: Float = 1000f, // Default reasonable page size  
        stickerSize: Float = 80f   // Default sticker size
    ): Boolean {
        return position.x.isFinite() && 
               position.y.isFinite() && 
               position.x >= 0f && 
               position.y >= 0f &&
               position.x <= pageWidth &&
               position.y <= pageHeight
    }
} 
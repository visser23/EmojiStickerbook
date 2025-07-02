package io.github.storybookemoji.presentation.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import io.github.storybookemoji.domain.usecases.ManageStickersUseCase
import io.github.storybookemoji.domain.usecases.NavigatePagesUseCase
import io.github.storybookemoji.model.BookState
import io.github.storybookemoji.model.EmojiSticker

/**
 * ViewModel for the Book screen managing the overall app state
 * Follows proper Compose state management patterns
 */
class BookViewModel {
    
    // Use cases for business logic
    private val manageStickersUseCase = ManageStickersUseCase()
    private val navigatePagesUseCase = NavigatePagesUseCase()
    
    // Main app state - single source of truth
    var bookState by mutableStateOf(BookState.createInitialState())
        private set
    
    /**
     * Adds a sticker to the specified page
     */
    fun addSticker(emoji: String, position: Offset, pageIndex: Int) {
        bookState = manageStickersUseCase.addStickerToPage(
            bookState = bookState,
            emoji = emoji,
            position = position,
            pageIndex = pageIndex
        )
    }
    
    /**
     * Updates an existing sticker
     */
    fun updateSticker(updatedSticker: EmojiSticker, pageIndex: Int) {
        bookState = manageStickersUseCase.updateStickerOnPage(
            bookState = bookState,
            updatedSticker = updatedSticker,
            pageIndex = pageIndex
        )
    }
    
    /**
     * Removes a sticker from the specified page
     */
    fun removeSticker(stickerId: Long, pageIndex: Int) {
        bookState = manageStickersUseCase.removeStickerFromPage(
            bookState = bookState,
            stickerId = stickerId,
            pageIndex = pageIndex
        )
    }
    
    /**
     * Clears all stickers from the specified page
     */
    fun clearPage(pageIndex: Int) {
        bookState = manageStickersUseCase.clearPageStickers(
            bookState = bookState,
            pageIndex = pageIndex
        )
    }
    
    /**
     * Navigates to a specific page
     */
    fun navigateToPage(pageIndex: Int) {
        bookState = navigatePagesUseCase.navigateToPage(
            bookState = bookState,
            pageIndex = pageIndex
        )
    }
    
    /**
     * Handles automatic page addition when reaching the end
     */
    fun handleAutoPageAddition() {
        if (navigatePagesUseCase.shouldAutoAddPage(bookState)) {
            bookState = navigatePagesUseCase.addNewPage(bookState)
        }
    }
    
    /**
     * Manually adds a new page
     */
    fun addNewPage() {
        bookState = navigatePagesUseCase.addNewPage(bookState)
    }
    
    /**
     * Toggles emoji selector visibility
     */
    fun toggleEmojiSelector(visible: Boolean) {
        bookState = bookState.withEmojiSelectorVisible(visible)
    }
    
    /**
     * Updates the selected emoji category
     */
    fun selectEmojiCategory(category: io.github.storybookemoji.model.EmojiCategory) {
        bookState = bookState.withSelectedEmojiCategory(category)
    }
    
    /**
     * Validates and corrects sticker position within bounds
     */
    fun validateStickerPosition(
        position: Offset,
        pageWidth: Float,
        pageHeight: Float,
        stickerSize: Float
    ): Offset {
        return manageStickersUseCase.validateStickerPosition(
            position = position,
            pageWidth = pageWidth,
            pageHeight = pageHeight,
            stickerSize = stickerSize
        )
    }
    
    /**
     * Validates and corrects sticker scale within bounds
     */
    fun validateStickerScale(scale: Float): Float {
        return manageStickersUseCase.validateStickerScale(scale)
    }
    
    // Convenience properties for UI
    val currentPageIndex: Int get() = bookState.currentPageIndex
    val totalPages: Int get() = bookState.pages.size
    val currentPage get() = bookState.currentPage
    val isEmojiSelectorVisible: Boolean get() = bookState.isEmojiSelectorVisible
    val canAddMorePages: Boolean get() = navigatePagesUseCase.canAddMorePages(bookState)
} 
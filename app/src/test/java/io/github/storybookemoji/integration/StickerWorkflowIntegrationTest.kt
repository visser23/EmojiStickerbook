package io.github.storybookemoji.integration

import androidx.compose.ui.geometry.Offset
import io.github.storybookemoji.domain.usecases.ManageStickersUseCase
import io.github.storybookemoji.domain.usecases.NavigatePagesUseCase
import io.github.storybookemoji.model.BookState
import io.github.storybookemoji.model.EmojiSticker
import io.github.storybookemoji.presentation.viewmodels.BookViewModel
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Integration tests for the complete sticker workflow
 * Tests interactions between use cases, models, and viewmodel
 */
class StickerWorkflowIntegrationTest {

    private lateinit var viewModel: BookViewModel
    private lateinit var manageStickersUseCase: ManageStickersUseCase
    private lateinit var navigatePagesUseCase: NavigatePagesUseCase

    @Before
    fun setUp() {
        viewModel = BookViewModel()
        manageStickersUseCase = ManageStickersUseCase()
        navigatePagesUseCase = NavigatePagesUseCase()
    }

    @Test
    fun `complete sticker lifecycle - add update remove`() {
        // Given - Initial state
        val initialPageCount = viewModel.totalPages
        val pageIndex = 0
        
        // When - Add a sticker
        val emoji = "😀"
        val position = Offset(100f, 100f)
        viewModel.addSticker(emoji, position, pageIndex)
        
        // Then - Sticker should be added
        val currentPage = viewModel.bookState.pages[pageIndex]
        assertEquals(1, currentPage.emojiStickers.size)
        val addedSticker = currentPage.emojiStickers[0]
        assertEquals(emoji, addedSticker.emoji)
        assertEquals(position, addedSticker.position)
        
        // When - Update the sticker
        val updatedSticker = addedSticker.copy(
            position = Offset(200f, 200f),
            scale = 2.0f,
            rotation = 45f
        )
        viewModel.updateSticker(updatedSticker, pageIndex)
        
        // Then - Sticker should be updated
        val updatedPage = viewModel.bookState.pages[pageIndex]
        assertEquals(1, updatedPage.emojiStickers.size)
        val retrievedSticker = updatedPage.emojiStickers[0]
        assertEquals(Offset(200f, 200f), retrievedSticker.position)
        assertEquals(2.0f, retrievedSticker.scale, 0.001f)
        assertEquals(45f, retrievedSticker.rotation, 0.001f)
        
        // When - Remove the sticker
        viewModel.removeSticker(addedSticker.id, pageIndex)
        
        // Then - Sticker should be removed
        val finalPage = viewModel.bookState.pages[pageIndex]
        assertTrue(finalPage.emojiStickers.isEmpty())
        
        // And - Page count should remain the same
        assertEquals(initialPageCount, viewModel.totalPages)
    }

    @Test
    fun `multiple stickers workflow - add multiple and manage`() {
        // Given
        val pageIndex = 0
        val stickers = listOf(
            "😀" to Offset(100f, 100f),
            "🎉" to Offset(200f, 200f),
            "🌟" to Offset(300f, 300f)
        )
        
        // When - Add multiple stickers
        stickers.forEach { (emoji, position) ->
            viewModel.addSticker(emoji, position, pageIndex)
        }
        
        // Then - All stickers should be added
        val page = viewModel.bookState.pages[pageIndex]
        assertEquals(3, page.emojiStickers.size)
        
        // Verify each sticker
        stickers.forEachIndexed { index, (emoji, position) ->
            val sticker = page.emojiStickers[index]
            assertEquals(emoji, sticker.emoji)
            assertEquals(position, sticker.position)
        }
        
        // When - Clear all stickers
        viewModel.clearPage(pageIndex)
        
        // Then - Page should be empty
        val clearedPage = viewModel.bookState.pages[pageIndex]
        assertTrue(clearedPage.emojiStickers.isEmpty())
    }

    @Test
    fun `page navigation and sticker persistence`() {
        // Given - Add stickers to different pages
        val emoji1 = "😀"
        val emoji2 = "🎉"
        val position1 = Offset(100f, 100f)
        val position2 = Offset(200f, 200f)
        
        // When - Add sticker to first page
        viewModel.addSticker(emoji1, position1, 0)
        
        // And - Add a new page and navigate to it
        viewModel.addNewPage()
        viewModel.navigateToPage(1)
        
        // And - Add sticker to second page
        viewModel.addSticker(emoji2, position2, 1)
        
        // Then - Both pages should have their stickers
        val page0 = viewModel.bookState.pages[0]
        val page1 = viewModel.bookState.pages[1]
        
        assertEquals(1, page0.emojiStickers.size)
        assertEquals(emoji1, page0.emojiStickers[0].emoji)
        assertEquals(position1, page0.emojiStickers[0].position)
        
        assertEquals(1, page1.emojiStickers.size)
        assertEquals(emoji2, page1.emojiStickers[0].emoji)
        assertEquals(position2, page1.emojiStickers[0].position)
        
        // When - Navigate back to first page
        viewModel.navigateToPage(0)
        
        // Then - Current page should be 0 and sticker should still be there
        assertEquals(0, viewModel.currentPageIndex)
        assertEquals(1, viewModel.bookState.pages[0].emojiStickers.size)
    }

    @Test
    fun `sticker validation and error handling`() {
        // Given
        val pageIndex = 0
        val validPosition = Offset(100f, 100f)
        val invalidPosition = Offset(-10f, -10f)
        
        // When - Add sticker with valid position
        viewModel.addSticker("😀", validPosition, pageIndex)
        
        // Then - Sticker should be added
        assertEquals(1, viewModel.bookState.pages[pageIndex].emojiStickers.size)
        
        // When - Try to add sticker to invalid page
        val initialStickerCount = viewModel.bookState.pages[pageIndex].emojiStickers.size
        viewModel.addSticker("🎉", validPosition, 999) // Invalid page index
        
        // Then - No sticker should be added to any page
        assertEquals(initialStickerCount, viewModel.bookState.pages[pageIndex].emojiStickers.size)
        
        // When - Try to remove non-existent sticker
        val beforeRemovalCount = viewModel.bookState.pages[pageIndex].emojiStickers.size
        viewModel.removeSticker(999L, pageIndex) // Non-existent sticker ID
        
        // Then - Page should remain unchanged
        assertEquals(beforeRemovalCount, viewModel.bookState.pages[pageIndex].emojiStickers.size)
    }

    @Test
    fun `emoji selector state management`() {
        // Given - Initial state
        assertFalse(viewModel.isEmojiSelectorVisible)
        
        // When - Toggle emoji selector on
        viewModel.toggleEmojiSelector(true)
        
        // Then - Should be visible
        assertTrue(viewModel.isEmojiSelectorVisible)
        
        // When - Toggle emoji selector off
        viewModel.toggleEmojiSelector(false)
        
        // Then - Should be hidden
        assertFalse(viewModel.isEmojiSelectorVisible)
    }

    @Test
    fun `scale and rotation validation`() {
        // Given - Add a sticker
        val pageIndex = 0
        val position = Offset(100f, 100f)
        viewModel.addSticker("😀", position, pageIndex)
        val sticker = viewModel.bookState.pages[pageIndex].emojiStickers[0]
        
        // When - Validate scale within bounds
        val validScale = viewModel.validateStickerScale(2.0f)
        val invalidScaleLow = viewModel.validateStickerScale(0.1f)
        val invalidScaleHigh = viewModel.validateStickerScale(5.0f)
        
        // Then - Scales should be properly clamped
        assertEquals(2.0f, validScale, 0.001f)
        assertEquals(EmojiSticker.MIN_SCALE, invalidScaleLow, 0.001f)
        assertEquals(EmojiSticker.MAX_SCALE, invalidScaleHigh, 0.001f)
        
        // When - Validate position within bounds
        val pageWidth = 800f
        val pageHeight = 600f
        val stickerSize = 80f
        
        val validPos = viewModel.validateStickerPosition(
            Offset(100f, 100f), pageWidth, pageHeight, stickerSize
        )
        val invalidPos = viewModel.validateStickerPosition(
            Offset(-10f, -10f), pageWidth, pageHeight, stickerSize
        )
        
        // Then - Positions should be properly clamped
        assertEquals(Offset(100f, 100f), validPos)
        assertEquals(Offset(40f, 40f), invalidPos) // Clamped to halfSize
    }

    @Test
    fun `performance with many stickers`() {
        // Given - Performance test with multiple stickers
        val pageIndex = 0
        val stickerCount = 50
        val startTime = System.currentTimeMillis()
        
        // When - Add many stickers
        repeat(stickerCount) { i ->
            viewModel.addSticker(
                "😀",
                Offset((i * 10).toFloat(), (i * 10).toFloat()),
                pageIndex
            )
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        // Then - Should complete in reasonable time and all stickers should be added
        assertEquals(stickerCount, viewModel.bookState.pages[pageIndex].emojiStickers.size)
        assertTrue("Adding $stickerCount stickers took too long: ${duration}ms", duration < 1000)
        
        // When - Clear all stickers
        val clearStartTime = System.currentTimeMillis()
        viewModel.clearPage(pageIndex)
        val clearEndTime = System.currentTimeMillis()
        val clearDuration = clearEndTime - clearStartTime
        
        // Then - Should clear quickly and completely
        assertTrue(viewModel.bookState.pages[pageIndex].emojiStickers.isEmpty())
        assertTrue("Clearing $stickerCount stickers took too long: ${clearDuration}ms", clearDuration < 100)
    }
} 
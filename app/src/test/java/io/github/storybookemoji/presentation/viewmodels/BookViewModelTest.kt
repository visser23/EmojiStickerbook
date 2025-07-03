package io.github.storybookemoji.presentation.viewmodels

import androidx.compose.ui.geometry.Offset
import io.github.storybookemoji.model.EmojiSticker
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for BookViewModel
 * Tests ViewModel state management and business logic delegation
 */
class BookViewModelTest {

    private lateinit var viewModel: BookViewModel

    @Before
    fun setUp() {
        viewModel = BookViewModel(testMode = true) // Enable test mode for synchronous behavior
    }

    @Test
    fun `initial state is correct`() {
        // Then
        assertEquals(0, viewModel.currentPageIndex)
        assertTrue(viewModel.totalPages > 0)
        assertFalse(viewModel.isEmojiSelectorVisible)
        assertTrue(viewModel.canAddMorePages)
        assertNotNull(viewModel.currentPage)
        assertTrue(viewModel.currentPage!!.emojiStickers.isEmpty())
    }

    @Test
    fun `addSticker updates state correctly`() {
        // Given
        val emoji = "😀"
        val position = Offset(100f, 100f)
        val pageIndex = 0
        val initialStickerCount = viewModel.currentPage!!.emojiStickers.size

        // When
        viewModel.addSticker(emoji, position, pageIndex)

        // Then
        assertEquals(initialStickerCount + 1, viewModel.currentPage!!.emojiStickers.size)
        val addedSticker = viewModel.currentPage!!.emojiStickers.last()
        assertEquals(emoji, addedSticker.emoji)
        assertEquals(position, addedSticker.position)
    }

    @Test
    fun `updateSticker modifies existing sticker`() {
        // Given - Add a sticker first
        val emoji = "😀"
        val position = Offset(100f, 100f)
        val pageIndex = 0
        viewModel.addSticker(emoji, position, pageIndex)
        val originalSticker = viewModel.currentPage!!.emojiStickers[0]

        // When - Update the sticker
        val updatedSticker = originalSticker.copy(
            position = Offset(200f, 200f),
            scale = 2.0f,
            rotation = 45f
        )
        viewModel.updateSticker(updatedSticker, pageIndex)

        // Then
        assertEquals(1, viewModel.currentPage!!.emojiStickers.size)
        val retrievedSticker = viewModel.currentPage!!.emojiStickers[0]
        assertEquals(originalSticker.id, retrievedSticker.id)
        assertEquals(Offset(200f, 200f), retrievedSticker.position)
        assertEquals(2.0f, retrievedSticker.scale, 0.001f)
        assertEquals(45f, retrievedSticker.rotation, 0.001f)
    }

    @Test
    fun `removeSticker removes correct sticker`() {
        // Given - Add two stickers with a small delay to ensure unique IDs
        viewModel.addSticker("😀", Offset(100f, 100f), 0)
        Thread.sleep(1) // Ensure unique timestamp
        viewModel.addSticker("🎉", Offset(200f, 200f), 0)
        assertEquals(2, viewModel.currentPage!!.emojiStickers.size)
        
        // Get the first sticker to remove (by emoji to avoid ID timing issues)
        val stickerToRemove = viewModel.currentPage!!.emojiStickers.find { it.emoji == "😀" }!!
        val stickerToKeep = viewModel.currentPage!!.emojiStickers.find { it.emoji == "🎉" }!!

        // When
        viewModel.removeSticker(stickerToRemove.id, 0)

        // Then
        assertEquals(1, viewModel.currentPage!!.emojiStickers.size)
        // The remaining sticker should be the one we didn't remove
        assertEquals(stickerToKeep.id, viewModel.currentPage!!.emojiStickers[0].id)
        assertEquals("🎉", viewModel.currentPage!!.emojiStickers[0].emoji)
    }

    @Test
    fun `clearPage removes all stickers`() {
        // Given - Add multiple stickers
        viewModel.addSticker("😀", Offset(100f, 100f), 0)
        viewModel.addSticker("🎉", Offset(200f, 200f), 0)
        viewModel.addSticker("🌟", Offset(300f, 300f), 0)
        assertEquals(3, viewModel.currentPage!!.emojiStickers.size)

        // When
        viewModel.clearPage(0)

        // Then
        assertTrue(viewModel.currentPage!!.emojiStickers.isEmpty())
    }

    @Test
    fun `navigateToPage changes current page`() {
        // Given - Add a new page
        val initialPageIndex = viewModel.currentPageIndex
        viewModel.addNewPage()
        val totalPages = viewModel.totalPages

        // When
        viewModel.navigateToPage(totalPages - 1)

        // Then
        assertEquals(totalPages - 1, viewModel.currentPageIndex)
        assertNotEquals(initialPageIndex, viewModel.currentPageIndex)
    }

    @Test
    fun `addNewPage increases page count`() {
        // Given
        val initialPageCount = viewModel.totalPages

        // When
        viewModel.addNewPage()

        // Then
        assertEquals(initialPageCount + 1, viewModel.totalPages)
    }

    @Test
    fun `toggleEmojiSelector changes visibility`() {
        // Given - Initial state
        assertFalse(viewModel.isEmojiSelectorVisible)

        // When - Toggle on
        viewModel.toggleEmojiSelector(true)

        // Then
        assertTrue(viewModel.isEmojiSelectorVisible)

        // When - Toggle off
        viewModel.toggleEmojiSelector(false)

        // Then
        assertFalse(viewModel.isEmojiSelectorVisible)
    }

    @Test
    fun `validateStickerScale clamps values correctly`() {
        // When & Then
        assertEquals(EmojiSticker.MIN_SCALE, viewModel.validateStickerScale(0.1f), 0.001f)
        assertEquals(2.0f, viewModel.validateStickerScale(2.0f), 0.001f)
        assertEquals(EmojiSticker.MAX_SCALE, viewModel.validateStickerScale(5.0f), 0.001f)
    }

    @Test
    fun `validateStickerPosition clamps coordinates correctly`() {
        // Given
        val pageWidth = 800f
        val pageHeight = 600f
        val stickerSize = 80f

        // When & Then - Valid position
        val validPos = viewModel.validateStickerPosition(
            Offset(100f, 100f), pageWidth, pageHeight, stickerSize
        )
        assertEquals(Offset(100f, 100f), validPos)

        // When & Then - Invalid position (negative)
        val invalidPos = viewModel.validateStickerPosition(
            Offset(-10f, -10f), pageWidth, pageHeight, stickerSize
        )
        assertEquals(Offset(40f, 40f), invalidPos) // Clamped to halfSize

        // When & Then - Invalid position (too large)
        val tooLargePos = viewModel.validateStickerPosition(
            Offset(900f, 700f), pageWidth, pageHeight, stickerSize
        )
        assertEquals(Offset(760f, 560f), tooLargePos) // Clamped to pageSize - halfSize
    }

    @Test
    fun `state remains consistent across operations`() {
        // Given - Perform multiple operations
        val initialPageCount = viewModel.totalPages

        // When - Add stickers and pages
        viewModel.addSticker("😀", Offset(100f, 100f), 0)
        viewModel.addNewPage()
        viewModel.navigateToPage(1)
        viewModel.addSticker("🎉", Offset(200f, 200f), 1)
        viewModel.toggleEmojiSelector(true)

        // Then - State should be consistent
        assertEquals(initialPageCount + 1, viewModel.totalPages)
        assertEquals(1, viewModel.currentPageIndex)
        assertTrue(viewModel.isEmojiSelectorVisible)
        assertEquals(1, viewModel.bookState.pages[0].emojiStickers.size)
        assertEquals(1, viewModel.bookState.pages[1].emojiStickers.size)
    }

    @Test
    fun `error handling with invalid inputs`() {
        // Given
        val initialState = viewModel.bookState

        // When - Try invalid operations
        viewModel.addSticker("😀", Offset(100f, 100f), -1) // Invalid page
        viewModel.addSticker("😀", Offset(100f, 100f), 999) // Invalid page
        viewModel.removeSticker(999L, 0) // Non-existent sticker
        viewModel.navigateToPage(-1) // Invalid page
        viewModel.navigateToPage(999) // Invalid page

        // Then - State should remain unchanged for invalid operations
        assertEquals(initialState.currentPageIndex, viewModel.currentPageIndex)
        assertEquals(initialState.pages.size, viewModel.totalPages)
        assertTrue(viewModel.currentPage!!.emojiStickers.isEmpty())
    }

    @Test
    fun `convenience properties return correct values`() {
        // Given - Set up some state
        viewModel.addSticker("😀", Offset(100f, 100f), 0)
        val initialPageCount = viewModel.totalPages // This will be 8
        viewModel.addNewPage()
        // Note: addNewPage() automatically navigates to the new page, so we're now on page at index 8 (9th page)
        viewModel.toggleEmojiSelector(true)

        // Then - Properties should reflect current state
        assertEquals(8, viewModel.currentPageIndex) // We're on the new page (index 8 = 9th page)
        assertEquals(initialPageCount + 1, viewModel.totalPages) // Should be 9 pages now
        assertTrue(viewModel.isEmojiSelectorVisible)
        assertTrue(viewModel.canAddMorePages)
        assertNotNull(viewModel.currentPage)
        
        // Debug information
        val expectedPageId = viewModel.bookState.pages[8].id
        val actualPageId = viewModel.currentPage!!.id
        
        // Check that we're on the correct page by comparing IDs
        assertEquals("Expected page ID: $expectedPageId, Actual page ID: $actualPageId", 
                    expectedPageId, actualPageId)
    }

    @Test
    fun `undo functionality works correctly`() {
        // Given - Initial state with no undo available
        assertFalse(viewModel.canUndo)
        
        // When - Add a sticker
        viewModel.addSticker("😀", Offset(100f, 100f), 0)
        
        // Then - Should have undo available
        assertTrue(viewModel.canUndo)
        assertEquals(1, viewModel.currentPage!!.emojiStickers.size)
        
        // When - Undo the action
        viewModel.undoLastAction()
        
        // Then - Sticker should be removed and no more undo available
        assertTrue(viewModel.currentPage!!.emojiStickers.isEmpty())
        assertFalse(viewModel.canUndo)
    }

    @Test
    fun `undo works for sticker removal`() {
        // Given - Add a sticker
        viewModel.addSticker("😀", Offset(100f, 100f), 0)
        assertEquals(1, viewModel.currentPage!!.emojiStickers.size)
        val originalSticker = viewModel.currentPage!!.emojiStickers[0]
        
        // When - Remove the sticker
        viewModel.removeSticker(originalSticker.id, 0)
        assertTrue(viewModel.currentPage!!.emojiStickers.isEmpty())
        assertTrue(viewModel.canUndo)
        
        // When - Undo the removal
        viewModel.undoLastAction()
        
        // Then - Sticker should be restored
        assertEquals(1, viewModel.currentPage!!.emojiStickers.size)
        assertEquals(originalSticker.emoji, viewModel.currentPage!!.emojiStickers[0].emoji)
    }

    @Test
    fun `undo is page-specific`() {
        // Given - Add sticker to page 0
        viewModel.addSticker("😀", Offset(100f, 100f), 0)
        assertTrue(viewModel.canUndo)
        
        // When - Navigate to page 1
        viewModel.navigateToPage(1)
        
        // Then - No undo available on page 1
        assertFalse(viewModel.canUndo)
        
        // When - Add sticker to page 1
        viewModel.addSticker("🎉", Offset(200f, 200f), 1)
        assertTrue(viewModel.canUndo)
        
        // When - Navigate back to page 0
        viewModel.navigateToPage(0)
        
        // Then - Undo available on page 0
        assertTrue(viewModel.canUndo)
    }
} 
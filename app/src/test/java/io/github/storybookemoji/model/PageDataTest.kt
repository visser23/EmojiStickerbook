package io.github.storybookemoji.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for PageData model
 * Tests immutable state operations and business logic
 */
class PageDataTest {

    private fun createTestBrush() = Brush.linearGradient(
        colors = listOf(Color.Red, Color.Blue)
    )

    private fun createTestSticker(id: Long = 1L, emoji: String = "😀") = EmojiSticker(
        id = id,
        emoji = emoji,
        position = Offset(100f, 100f)
    )

    @Test
    fun `createPage_withDefaults_hasCorrectValues`() {
        // Given
        val id = 1
        val gradient = createTestBrush()
        
        // When
        val page = PageData(
            id = id,
            backgroundGradient = gradient
        )
        
        // Then
        assertEquals(id, page.id)
        assertEquals(gradient, page.backgroundGradient)
        assertTrue(page.emojiStickers.isEmpty())
    }

    @Test
    fun `withAddedSticker_addsNewSticker`() {
        // Given
        val page = PageData(
            id = 1,
            backgroundGradient = createTestBrush()
        )
        val sticker = createTestSticker()
        
        // When
        val updatedPage = page.withAddedSticker(sticker)
        
        // Then
        assertEquals(1, updatedPage.emojiStickers.size)
        assertTrue(updatedPage.emojiStickers.contains(sticker))
        assertEquals(page.id, updatedPage.id)
        // Original page should be unchanged (immutability)
        assertTrue(page.emojiStickers.isEmpty())
    }

    @Test
    fun `withAddedSticker_multipleStickers_maintainsOrder`() {
        // Given
        val page = PageData(
            id = 1,
            backgroundGradient = createTestBrush()
        )
        val sticker1 = createTestSticker(1L, "😀")
        val sticker2 = createTestSticker(2L, "🎉")
        val sticker3 = createTestSticker(3L, "🌟")
        
        // When
        val updatedPage = page
            .withAddedSticker(sticker1)
            .withAddedSticker(sticker2)
            .withAddedSticker(sticker3)
        
        // Then
        assertEquals(3, updatedPage.emojiStickers.size)
        assertEquals(sticker1, updatedPage.emojiStickers[0])
        assertEquals(sticker2, updatedPage.emojiStickers[1])
        assertEquals(sticker3, updatedPage.emojiStickers[2])
    }

    @Test
    fun `withUpdatedSticker_existingSticker_updatesCorrectly`() {
        // Given
        val sticker1 = createTestSticker(1L, "😀")
        val sticker2 = createTestSticker(2L, "🎉")
        val page = PageData(
            id = 1,
            backgroundGradient = createTestBrush(),
            emojiStickers = listOf(sticker1, sticker2)
        )
        val updatedSticker1 = sticker1.withPosition(Offset(200f, 200f))
        
        // When
        val updatedPage = page.withUpdatedSticker(updatedSticker1)
        
        // Then
        assertEquals(2, updatedPage.emojiStickers.size)
        assertEquals(updatedSticker1, updatedPage.emojiStickers[0])
        assertEquals(sticker2, updatedPage.emojiStickers[1])
        assertEquals(Offset(200f, 200f), updatedPage.emojiStickers[0].position)
    }

    @Test
    fun `withUpdatedSticker_nonExistentSticker_returnsUnchanged`() {
        // Given
        val sticker1 = createTestSticker(1L, "😀")
        val page = PageData(
            id = 1,
            backgroundGradient = createTestBrush(),
            emojiStickers = listOf(sticker1)
        )
        val nonExistentSticker = createTestSticker(999L, "🎭")
        
        // When
        val updatedPage = page.withUpdatedSticker(nonExistentSticker)
        
        // Then
        assertEquals(1, updatedPage.emojiStickers.size)
        assertEquals(sticker1, updatedPage.emojiStickers[0])
    }

    @Test
    fun `withRemovedSticker_existingSticker_removesCorrectly`() {
        // Given
        val sticker1 = createTestSticker(1L, "😀")
        val sticker2 = createTestSticker(2L, "🎉")
        val sticker3 = createTestSticker(3L, "🌟")
        val page = PageData(
            id = 1,
            backgroundGradient = createTestBrush(),
            emojiStickers = listOf(sticker1, sticker2, sticker3)
        )
        
        // When
        val updatedPage = page.withRemovedSticker(2L)
        
        // Then
        assertEquals(2, updatedPage.emojiStickers.size)
        assertTrue(updatedPage.emojiStickers.contains(sticker1))
        assertTrue(updatedPage.emojiStickers.contains(sticker3))
        assertFalse(updatedPage.emojiStickers.contains(sticker2))
    }

    @Test
    fun `withRemovedSticker_nonExistentSticker_returnsUnchanged`() {
        // Given
        val sticker1 = createTestSticker(1L, "😀")
        val page = PageData(
            id = 1,
            backgroundGradient = createTestBrush(),
            emojiStickers = listOf(sticker1)
        )
        
        // When
        val updatedPage = page.withRemovedSticker(999L)
        
        // Then
        assertEquals(1, updatedPage.emojiStickers.size)
        assertEquals(sticker1, updatedPage.emojiStickers[0])
    }

    @Test
    fun `withClearedStickers_removesAllStickers`() {
        // Given
        val sticker1 = createTestSticker(1L, "😀")
        val sticker2 = createTestSticker(2L, "🎉")
        val page = PageData(
            id = 1,
            backgroundGradient = createTestBrush(),
            emojiStickers = listOf(sticker1, sticker2)
        )
        
        // When
        val clearedPage = page.withClearedStickers()
        
        // Then
        assertTrue(clearedPage.emojiStickers.isEmpty())
        assertEquals(page.id, clearedPage.id)
        assertEquals(page.backgroundGradient, clearedPage.backgroundGradient)
    }

    @Test
    fun `findStickerAt_withStickerAtPosition_returnsSticker`() {
        // Given
        val position = Offset(100f, 100f)
        val sticker = createTestSticker(1L, "😀").copy(position = position)
        val page = PageData(
            id = 1,
            backgroundGradient = createTestBrush(),
            emojiStickers = listOf(sticker)
        )
        
        // When
        val foundSticker = page.findStickerAt(position, tolerance = 50f)
        
        // Then
        assertEquals(sticker, foundSticker)
    }

    @Test
    fun `findStickerAt_withNoStickerAtPosition_returnsNull`() {
        // Given
        val sticker = createTestSticker(1L, "😀").copy(position = Offset(100f, 100f))
        val page = PageData(
            id = 1,
            backgroundGradient = createTestBrush(),
            emojiStickers = listOf(sticker)
        )
        val searchPosition = Offset(500f, 500f)
        
        // When
        val foundSticker = page.findStickerAt(searchPosition, tolerance = 50f)
        
        // Then
        assertNull(foundSticker)
    }

    @Test
    fun `findStickerAt_withMultipleStickers_returnsTopMost`() {
        // Given
        val position = Offset(100f, 100f)
        val sticker1 = createTestSticker(1L, "😀").copy(position = position)
        val sticker2 = createTestSticker(2L, "🎉").copy(position = position)
        val sticker3 = createTestSticker(3L, "🌟").copy(position = position)
        val page = PageData(
            id = 1,
            backgroundGradient = createTestBrush(),
            emojiStickers = listOf(sticker1, sticker2, sticker3) // sticker3 is topmost
        )
        
        // When
        val foundSticker = page.findStickerAt(position, tolerance = 50f)
        
        // Then
        assertEquals(sticker3, foundSticker) // Should return the last (topmost) sticker
    }

    @Test
    fun `getStickerCount_returnsCorrectCount`() {
        // Given
        val sticker1 = createTestSticker(1L, "😀")
        val sticker2 = createTestSticker(2L, "🎉")
        val page = PageData(
            id = 1,
            backgroundGradient = createTestBrush(),
            emojiStickers = listOf(sticker1, sticker2)
        )
        
        // When & Then
        assertEquals(2, page.getStickerCount())
    }

    @Test
    fun `isEmpty_withNoStickers_returnsTrue`() {
        // Given
        val page = PageData(
            id = 1,
            backgroundGradient = createTestBrush()
        )
        
        // When & Then
        assertTrue(page.isEmpty())
    }

    @Test
    fun `isEmpty_withStickers_returnsFalse`() {
        // Given
        val sticker = createTestSticker()
        val page = PageData(
            id = 1,
            backgroundGradient = createTestBrush(),
            emojiStickers = listOf(sticker)
        )
        
        // When & Then
        assertFalse(page.isEmpty())
    }
} 
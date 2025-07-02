package io.github.storybookemoji.domain.usecases

import androidx.compose.ui.geometry.Offset
import io.github.storybookemoji.model.BookState
import io.github.storybookemoji.model.EmojiSticker
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for ManageStickersUseCase
 * Tests business logic for sticker management operations
 */
class ManageStickersUseCaseTest {

    private lateinit var useCase: ManageStickersUseCase
    private lateinit var initialBookState: BookState

    @Before
    fun setUp() {
        useCase = ManageStickersUseCase()
        initialBookState = BookState.createInitialState()
    }

    @Test
    fun `addStickerToPage_validInputs_addsSticker`() {
        // Given
        val emoji = "😀"
        val position = Offset(100f, 100f)
        val pageIndex = 0
        
        // When
        val result = useCase.addStickerToPage(
            bookState = initialBookState,
            emoji = emoji,
            position = position,
            pageIndex = pageIndex
        )
        
        // Then
        assertEquals(1, result.pages[pageIndex].emojiStickers.size)
        val addedSticker = result.pages[pageIndex].emojiStickers[0]
        assertEquals(emoji, addedSticker.emoji)
        assertEquals(position, addedSticker.position)
        assertEquals(1.0f, addedSticker.scale, 0.001f)
        assertTrue(addedSticker.id > 0L)
    }

    @Test
    fun `addStickerToPage_invalidPageIndex_returnsUnchanged`() {
        // Given
        val emoji = "😀"
        val position = Offset(100f, 100f)
        val invalidPageIndex = 999
        
        // When
        val result = useCase.addStickerToPage(
            bookState = initialBookState,
            emoji = emoji,
            position = position,
            pageIndex = invalidPageIndex
        )
        
        // Then
        assertEquals(initialBookState, result)
    }

    @Test
    fun `addStickerToPage_negativePageIndex_returnsUnchanged`() {
        // Given
        val emoji = "😀"
        val position = Offset(100f, 100f)
        val negativePageIndex = -1
        
        // When
        val result = useCase.addStickerToPage(
            bookState = initialBookState,
            emoji = emoji,
            position = position,
            pageIndex = negativePageIndex
        )
        
        // Then
        assertEquals(initialBookState, result)
    }

    @Test
    fun `updateStickerPosition_existingSticker_updatesPosition`() {
        // Given
        val sticker = EmojiSticker(
            id = 123L,
            emoji = "😀",
            position = Offset(100f, 100f)
        )
        val pageIndex = 0
        val bookStateWithSticker = initialBookState.withUpdatedPage(
            pageIndex,
            initialBookState.pages[pageIndex].withAddedSticker(sticker)
        )
        val newPosition = Offset(200f, 200f)
        
        // When
        val result = useCase.updateStickerPosition(
            bookState = bookStateWithSticker,
            stickerId = sticker.id,
            newPosition = newPosition,
            pageIndex = pageIndex
        )
        
        // Then
        val updatedSticker = result.pages[pageIndex].emojiStickers[0]
        assertEquals(newPosition, updatedSticker.position)
        assertEquals(sticker.id, updatedSticker.id)
        assertEquals(sticker.emoji, updatedSticker.emoji)
    }

    @Test
    fun `updateStickerPosition_nonExistentSticker_returnsUnchanged`() {
        // Given
        val pageIndex = 0
        val nonExistentStickerId = 999L
        val newPosition = Offset(200f, 200f)
        
        // When
        val result = useCase.updateStickerPosition(
            bookState = initialBookState,
            stickerId = nonExistentStickerId,
            newPosition = newPosition,
            pageIndex = pageIndex
        )
        
        // Then
        assertEquals(initialBookState, result)
    }

    @Test
    fun `updateStickerScale_validScale_updatesScale`() {
        // Given
        val sticker = EmojiSticker(
            id = 123L,
            emoji = "😀",
            position = Offset(100f, 100f),
            scale = 1.0f
        )
        val pageIndex = 0
        val bookStateWithSticker = initialBookState.withUpdatedPage(
            pageIndex,
            initialBookState.pages[pageIndex].withAddedSticker(sticker)
        )
        val newScale = 2.0f
        
        // When
        val result = useCase.updateStickerScale(
            bookState = bookStateWithSticker,
            stickerId = sticker.id,
            newScale = newScale,
            pageIndex = pageIndex
        )
        
        // Then
        val updatedSticker = result.pages[pageIndex].emojiStickers[0]
        assertEquals(newScale, updatedSticker.scale, 0.001f)
        assertEquals(sticker.id, updatedSticker.id)
    }

    @Test
    fun `updateStickerScale_invalidScale_clampsToValidRange`() {
        // Given
        val sticker = EmojiSticker(
            id = 123L,
            emoji = "😀",
            position = Offset(100f, 100f),
            scale = 1.0f
        )
        val pageIndex = 0
        val bookStateWithSticker = initialBookState.withUpdatedPage(
            pageIndex,
            initialBookState.pages[pageIndex].withAddedSticker(sticker)
        )
        val invalidScale = 5.0f // Above MAX_SCALE
        
        // When
        val result = useCase.updateStickerScale(
            bookState = bookStateWithSticker,
            stickerId = sticker.id,
            newScale = invalidScale,
            pageIndex = pageIndex
        )
        
        // Then
        val updatedSticker = result.pages[pageIndex].emojiStickers[0]
        assertEquals(EmojiSticker.MAX_SCALE, updatedSticker.scale, 0.001f)
    }

    @Test
    fun `updateStickerRotation_updatesRotation`() {
        // Given
        val sticker = EmojiSticker(
            id = 123L,
            emoji = "😀",
            position = Offset(100f, 100f),
            rotation = 0f
        )
        val pageIndex = 0
        val bookStateWithSticker = initialBookState.withUpdatedPage(
            pageIndex,
            initialBookState.pages[pageIndex].withAddedSticker(sticker)
        )
        val newRotation = 45f
        
        // When
        val result = useCase.updateStickerRotation(
            bookState = bookStateWithSticker,
            stickerId = sticker.id,
            newRotation = newRotation,
            pageIndex = pageIndex
        )
        
        // Then
        val updatedSticker = result.pages[pageIndex].emojiStickers[0]
        assertEquals(newRotation, updatedSticker.rotation, 0.001f)
        assertEquals(sticker.id, updatedSticker.id)
    }

    @Test
    fun `removeStickerFromPage_existingSticker_removesSticker`() {
        // Given
        val sticker = EmojiSticker(
            id = 123L,
            emoji = "😀",
            position = Offset(100f, 100f)
        )
        val pageIndex = 0
        val bookStateWithSticker = initialBookState.withUpdatedPage(
            pageIndex,
            initialBookState.pages[pageIndex].withAddedSticker(sticker)
        )
        
        // When
        val result = useCase.removeStickerFromPage(
            bookState = bookStateWithSticker,
            stickerId = sticker.id,
            pageIndex = pageIndex
        )
        
        // Then
        assertTrue(result.pages[pageIndex].emojiStickers.isEmpty())
    }

    @Test
    fun `removeStickerFromPage_nonExistentSticker_returnsUnchanged`() {
        // Given
        val pageIndex = 0
        val nonExistentStickerId = 999L
        
        // When
        val result = useCase.removeStickerFromPage(
            bookState = initialBookState,
            stickerId = nonExistentStickerId,
            pageIndex = pageIndex
        )
        
        // Then
        assertEquals(initialBookState, result)
    }

    @Test
    fun `clearPageStickers_removesAllStickers`() {
        // Given
        val sticker1 = EmojiSticker(id = 1L, emoji = "😀", position = Offset(100f, 100f))
        val sticker2 = EmojiSticker(id = 2L, emoji = "🎉", position = Offset(200f, 200f))
        val pageIndex = 0
        val bookStateWithStickers = initialBookState.withUpdatedPage(
            pageIndex,
            initialBookState.pages[pageIndex]
                .withAddedSticker(sticker1)
                .withAddedSticker(sticker2)
        )
        
        // When
        val result = useCase.clearPageStickers(
            bookState = bookStateWithStickers,
            pageIndex = pageIndex
        )
        
        // Then
        assertTrue(result.pages[pageIndex].emojiStickers.isEmpty())
    }

    @Test
    fun `findStickerAtPosition_withStickerAtPosition_returnsSticker`() {
        // Given
        val position = Offset(100f, 100f)
        val sticker = EmojiSticker(
            id = 123L,
            emoji = "😀",
            position = position
        )
        val pageIndex = 0
        val bookStateWithSticker = initialBookState.withUpdatedPage(
            pageIndex,
            initialBookState.pages[pageIndex].withAddedSticker(sticker)
        )
        
        // When
        val foundSticker = useCase.findStickerAtPosition(
            bookState = bookStateWithSticker,
            position = position,
            pageIndex = pageIndex,
            tolerance = 50f
        )
        
        // Then
        assertEquals(sticker, foundSticker)
    }

    @Test
    fun `findStickerAtPosition_withNoStickerAtPosition_returnsNull`() {
        // Given
        val searchPosition = Offset(500f, 500f)
        val pageIndex = 0
        
        // When
        val foundSticker = useCase.findStickerAtPosition(
            bookState = initialBookState,
            position = searchPosition,
            pageIndex = pageIndex,
            tolerance = 50f
        )
        
        // Then
        assertNull(foundSticker)
    }

    @Test
    fun `validateStickerPosition_withValidPosition_returnsTrue`() {
        // Given
        val validPosition = Offset(100f, 100f)
        
        // When
        val isValid = useCase.validateStickerPosition(
            position = validPosition,
            pageWidth = 1000f,
            pageHeight = 1000f,
            stickerSize = 80f
        )
        
        // Then
        assertTrue(isValid)
    }

    @Test
    fun `validateStickerPosition_withInvalidPosition_returnsFalse`() {
        // Given & When & Then
        assertFalse(useCase.validateStickerPosition(
            position = Offset(-10f, 100f),
            pageWidth = 1000f,
            pageHeight = 1000f,
            stickerSize = 80f
        ))
        assertFalse(useCase.validateStickerPosition(
            position = Offset(100f, -10f),
            pageWidth = 1000f,
            pageHeight = 1000f,
            stickerSize = 80f
        ))
        assertFalse(useCase.validateStickerPosition(
            position = Offset(Float.NaN, 100f),
            pageWidth = 1000f,
            pageHeight = 1000f,
            stickerSize = 80f
        ))
        assertFalse(useCase.validateStickerPosition(
            position = Offset(100f, Float.POSITIVE_INFINITY),
            pageWidth = 1000f,
            pageHeight = 1000f,
            stickerSize = 80f
        ))
    }
} 
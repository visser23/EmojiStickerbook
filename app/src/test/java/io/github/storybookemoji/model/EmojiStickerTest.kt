package io.github.storybookemoji.model

import androidx.compose.ui.geometry.Offset
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for EmojiSticker model
 * Tests data integrity, validation, and business rules
 */
class EmojiStickerTest {

    @Test
    fun `createSticker_withDefaults_hasCorrectValues`() {
        // Given
        val emoji = "😀"
        val position = Offset(100f, 200f)
        
        // When
        val sticker = EmojiSticker(
            emoji = emoji,
            position = position
        )
        
        // Then
        assertEquals(emoji, sticker.emoji)
        assertEquals(position, sticker.position)
        assertEquals(1.0f, sticker.scale, 0.001f)
        assertEquals(0f, sticker.rotation, 0.001f)
        assertEquals(80f, sticker.size, 0.001f)
        assertTrue(sticker.id > 0L)
    }

    @Test
    fun `createSticker_withCustomValues_hasCorrectValues`() {
        // Given
        val emoji = "🎉"
        val position = Offset(50f, 75f)
        val scale = 1.5f
        val rotation = 45f
        val customId = 12345L
        
        // When
        val sticker = EmojiSticker(
            id = customId,
            emoji = emoji,
            position = position,
            scale = scale,
            rotation = rotation
        )
        
        // Then
        assertEquals(customId, sticker.id)
        assertEquals(emoji, sticker.emoji)
        assertEquals(position, sticker.position)
        assertEquals(scale, sticker.scale, 0.001f)
        assertEquals(rotation, sticker.rotation, 0.001f)
    }

    @Test
    fun `stickerConstants_haveExpectedValues`() {
        // Then
        assertEquals(0.5f, EmojiSticker.MIN_SCALE, 0.001f)
        assertEquals(3.0f, EmojiSticker.MAX_SCALE, 0.001f)
        assertEquals(80, EmojiSticker.DEFAULT_SIZE_DP)
    }

    @Test
    fun `withPosition_updatesPositionCorrectly`() {
        // Given
        val sticker = EmojiSticker(
            emoji = "🌟",
            position = Offset(0f, 0f)
        )
        val newPosition = Offset(150f, 250f)
        
        // When
        val updatedSticker = sticker.withPosition(newPosition)
        
        // Then
        assertEquals(newPosition, updatedSticker.position)
        assertEquals(sticker.id, updatedSticker.id)
        assertEquals(sticker.emoji, updatedSticker.emoji)
        assertEquals(sticker.scale, updatedSticker.scale, 0.001f)
    }

    @Test
    fun `withScale_updatesScaleCorrectly`() {
        // Given
        val sticker = EmojiSticker(
            emoji = "🚀",
            position = Offset(100f, 100f)
        )
        val newScale = 2.0f
        
        // When
        val updatedSticker = sticker.withScale(newScale)
        
        // Then
        assertEquals(newScale, updatedSticker.scale, 0.001f)
        assertEquals(sticker.id, updatedSticker.id)
        assertEquals(sticker.position, updatedSticker.position)
    }

    @Test
    fun `withRotation_updatesRotationCorrectly`() {
        // Given
        val sticker = EmojiSticker(
            emoji = "🎨",
            position = Offset(200f, 300f)
        )
        val newRotation = 90f
        
        // When
        val updatedSticker = sticker.withRotation(newRotation)
        
        // Then
        assertEquals(newRotation, updatedSticker.rotation, 0.001f)
        assertEquals(sticker.id, updatedSticker.id)
        assertEquals(sticker.position, updatedSticker.position)
    }

    @Test
    fun `isValidScale_withValidValues_returnsTrue`() {
        // Given & When & Then
        assertTrue(EmojiSticker.isValidScale(0.5f))
        assertTrue(EmojiSticker.isValidScale(1.0f))
        assertTrue(EmojiSticker.isValidScale(2.5f))
        assertTrue(EmojiSticker.isValidScale(3.0f))
    }

    @Test
    fun `isValidScale_withInvalidValues_returnsFalse`() {
        // Given & When & Then
        assertFalse(EmojiSticker.isValidScale(0.4f))
        assertFalse(EmojiSticker.isValidScale(3.1f))
        assertFalse(EmojiSticker.isValidScale(-1.0f))
        assertFalse(EmojiSticker.isValidScale(Float.NaN))
    }

    @Test
    fun `clampScale_withValidValues_returnsUnchanged`() {
        // Given & When & Then
        assertEquals(1.0f, EmojiSticker.clampScale(1.0f), 0.001f)
        assertEquals(2.0f, EmojiSticker.clampScale(2.0f), 0.001f)
        assertEquals(0.8f, EmojiSticker.clampScale(0.8f), 0.001f)
    }

    @Test
    fun `clampScale_withInvalidValues_returnsClampedValues`() {
        // Given & When & Then
        assertEquals(0.5f, EmojiSticker.clampScale(0.3f), 0.001f)
        assertEquals(3.0f, EmojiSticker.clampScale(4.0f), 0.001f)
        assertEquals(0.5f, EmojiSticker.clampScale(-1.0f), 0.001f)
    }

    @Test
    fun `copy_preservesAllProperties`() {
        // Given
        val original = EmojiSticker(
            id = 999L,
            emoji = "🎭",
            position = Offset(123f, 456f),
            scale = 1.8f,
            rotation = 30f
        )
        
        // When
        val copy = original.copy()
        
        // Then
        assertEquals(original.id, copy.id)
        assertEquals(original.emoji, copy.emoji)
        assertEquals(original.position, copy.position)
        assertEquals(original.scale, copy.scale, 0.001f)
        assertEquals(original.rotation, copy.rotation, 0.001f)
        assertEquals(original.size, copy.size, 0.001f)
    }
} 
package io.github.storybookemoji.ui

import androidx.compose.ui.geometry.Offset
import io.github.storybookemoji.model.EmojiSticker
import io.github.storybookemoji.presentation.viewmodels.BookViewModel
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Integration tests for gesture callback architecture.
 * 
 * These tests validate that the new callback-based architecture
 * properly propagates scale, rotation, and position changes from
 * the UI layer to the ViewModel state.
 * 
 * CRITICAL: These tests were added to prevent regressions of the
 * state synchronization bugs found in December 2024.
 */
class GestureCallbackIntegrationTest {

    private lateinit var viewModel: BookViewModel

    @Before
    fun setUp() {
        viewModel = BookViewModel(testMode = true)
    }

    /**
     * REGRESSION TEST: Scale changes must propagate to ViewModel state.
     * Previously, scale changes were made locally but never saved.
     */
    @Test
    fun `scale change callback properly updates ViewModel state`() {
        // Given - Add a sticker
        viewModel.addSticker("😀", Offset(100f, 100f), 0)
        val originalSticker = viewModel.currentPage!!.emojiStickers[0]
        assertEquals(1.0f, originalSticker.scale, 0.001f)
        
        // When - Simulate scale change callback (as DraggableEmoji would call)
        val newScale = 1.5f
        val updatedSticker = originalSticker.copy(scale = newScale)
        viewModel.updateSticker(updatedSticker, 0)
        
        // Then - Scale should be saved in ViewModel state
        val retrievedSticker = viewModel.currentPage!!.emojiStickers[0]
        assertEquals(newScale, retrievedSticker.scale, 0.001f)
        assertEquals(originalSticker.id, retrievedSticker.id)
    }

    /**
     * REGRESSION TEST: Rotation changes must propagate to ViewModel state.
     * Previously, rotation changes were made locally but never saved.
     */
    @Test
    fun `rotation change callback properly updates ViewModel state`() {
        // Given - Add a sticker
        viewModel.addSticker("😀", Offset(100f, 100f), 0)
        val originalSticker = viewModel.currentPage!!.emojiStickers[0]
        assertEquals(0f, originalSticker.rotation, 0.001f)
        
        // When - Simulate rotation change callback
        val newRotation = 45f
        val updatedSticker = originalSticker.copy(rotation = newRotation)
        viewModel.updateSticker(updatedSticker, 0)
        
        // Then - Rotation should be saved in ViewModel state
        val retrievedSticker = viewModel.currentPage!!.emojiStickers[0]
        assertEquals(newRotation, retrievedSticker.rotation, 0.001f)
        assertEquals(originalSticker.id, retrievedSticker.id)
    }

    /**
     * REGRESSION TEST: Position changes must propagate to ViewModel state.
     * This was working before, but we test to ensure no regression.
     */
    @Test
    fun `position change callback properly updates ViewModel state`() {
        // Given - Add a sticker
        viewModel.addSticker("😀", Offset(100f, 100f), 0)
        val originalSticker = viewModel.currentPage!!.emojiStickers[0]
        
        // When - Simulate position change callback
        val newPosition = Offset(200f, 250f)
        val updatedSticker = originalSticker.copy(position = newPosition)
        viewModel.updateSticker(updatedSticker, 0)
        
        // Then - Position should be saved in ViewModel state
        val retrievedSticker = viewModel.currentPage!!.emojiStickers[0]
        assertEquals(newPosition, retrievedSticker.position)
        assertEquals(originalSticker.id, retrievedSticker.id)
    }

    /**
     * Test that multiple simultaneous updates (as in pinch gesture) work correctly.
     * Pinch gesture updates scale, rotation, AND position simultaneously.
     */
    @Test
    fun `simultaneous scale rotation position updates work correctly`() {
        // Given - Add a sticker
        viewModel.addSticker("😀", Offset(100f, 100f), 0)
        val originalSticker = viewModel.currentPage!!.emojiStickers[0]
        
        // When - Simulate simultaneous updates (as pinch gesture would do)
        val newScale = 1.8f
        val newRotation = 30f
        val newPosition = Offset(150f, 175f)
        
        val updatedSticker = originalSticker.copy(
            scale = newScale,
            rotation = newRotation,
            position = newPosition
        )
        viewModel.updateSticker(updatedSticker, 0)
        
        // Then - All changes should be saved
        val retrievedSticker = viewModel.currentPage!!.emojiStickers[0]
        assertEquals(newScale, retrievedSticker.scale, 0.001f)
        assertEquals(newRotation, retrievedSticker.rotation, 0.001f)
        assertEquals(newPosition, retrievedSticker.position)
        assertEquals(originalSticker.id, retrievedSticker.id)
    }

    /**
     * Test that rapid successive updates (like during continuous gesture) work.
     */
    @Test
    fun `rapid successive scale updates work correctly`() {
        // Given - Add a sticker
        viewModel.addSticker("😀", Offset(100f, 100f), 0)
        val originalSticker = viewModel.currentPage!!.emojiStickers[0]
        
        // When - Simulate rapid successive scale updates (like during pinch)
        var currentSticker = originalSticker
        for (i in 1..10) {
            val newScale = 1.0f + (i * 0.1f)
            currentSticker = currentSticker.copy(scale = newScale)
            viewModel.updateSticker(currentSticker, 0)
        }
        
        // Then - Final scale should be saved
        val retrievedSticker = viewModel.currentPage!!.emojiStickers[0]
        assertEquals(2.0f, retrievedSticker.scale, 0.001f) // 1.0 + 10 * 0.1
    }

    /**
     * Test that scale constraints are enforced.
     */
    @Test
    fun `scale is constrained within valid bounds`() {
        // Given - Add a sticker
        viewModel.addSticker("😀", Offset(100f, 100f), 0)
        val originalSticker = viewModel.currentPage!!.emojiStickers[0]
        
        // When - Try to set scale below minimum
        val tooSmallScale = 0.1f
        val updatedSticker = originalSticker.copy(scale = EmojiSticker.clampScale(tooSmallScale))
        viewModel.updateSticker(updatedSticker, 0)
        
        // Then - Scale should be clamped to minimum
        val retrievedSticker = viewModel.currentPage!!.emojiStickers[0]
        assertEquals(EmojiSticker.MIN_SCALE, retrievedSticker.scale, 0.001f)
    }

    /**
     * Test that immutability is preserved - original sticker not modified.
     */
    @Test
    fun `original sticker is not modified when creating updates`() {
        // Given - Add a sticker
        viewModel.addSticker("😀", Offset(100f, 100f), 0)
        val originalSticker = viewModel.currentPage!!.emojiStickers[0]
        val originalScale = originalSticker.scale
        val originalRotation = originalSticker.rotation
        val originalPosition = originalSticker.position
        
        // When - Create updated sticker using copy
        val updatedSticker = originalSticker.copy(
            scale = 2.0f,
            rotation = 90f,
            position = Offset(300f, 400f)
        )
        
        // Then - Original sticker values should be unchanged
        assertEquals(originalScale, originalSticker.scale, 0.001f)
        assertEquals(originalRotation, originalSticker.rotation, 0.001f)
        assertEquals(originalPosition, originalSticker.position)
        
        // And updated sticker should have new values
        assertEquals(2.0f, updatedSticker.scale, 0.001f)
        assertEquals(90f, updatedSticker.rotation, 0.001f)
        assertEquals(Offset(300f, 400f), updatedSticker.position)
    }

    /**
     * Test that state persists correctly across page navigation.
     */
    @Test
    fun `sticker state persists across page navigation`() {
        // Given - Add and modify a sticker on page 0
        viewModel.addSticker("😀", Offset(100f, 100f), 0)
        val originalSticker = viewModel.currentPage!!.emojiStickers[0]
        
        val updatedSticker = originalSticker.copy(
            scale = 1.7f,
            rotation = 25f,
            position = Offset(200f, 300f)
        )
        viewModel.updateSticker(updatedSticker, 0)
        
        // When - Navigate to another page and back
        viewModel.navigateToPage(1)
        assertEquals(1, viewModel.currentPageIndex)
        
        viewModel.navigateToPage(0)
        assertEquals(0, viewModel.currentPageIndex)
        
        // Then - Sticker state should be preserved
        val retrievedSticker = viewModel.currentPage!!.emojiStickers[0]
        assertEquals(1.7f, retrievedSticker.scale, 0.001f)
        assertEquals(25f, retrievedSticker.rotation, 0.001f)
        assertEquals(Offset(200f, 300f), retrievedSticker.position)
    }

    /**
     * Test minimum touch target compliance.
     */
    @Test
    fun `sticker meets minimum touch target at minimum scale`() {
        // Given - Add a sticker at minimum scale
        viewModel.addSticker("😀", Offset(100f, 100f), 0)
        val originalSticker = viewModel.currentPage!!.emojiStickers[0]
        
        val minScaleSticker = originalSticker.copy(scale = EmojiSticker.MIN_SCALE)
        viewModel.updateSticker(minScaleSticker, 0)
        
        // Then - Should still meet minimum touch target
        val retrievedSticker = viewModel.currentPage!!.emojiStickers[0]
        assertTrue(
            "Sticker at min scale should meet touch target: ${retrievedSticker.currentTouchTargetSize}",
            retrievedSticker.meetsMinimumTouchTarget
        )
    }
}

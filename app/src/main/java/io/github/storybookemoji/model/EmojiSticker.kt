package io.github.storybookemoji.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Represents an emoji sticker placed on a page.
 * 
 * IMPORTANT: This is an IMMUTABLE data class. All properties are val.
 * To update a sticker, use .copy() or the withXxx() helper methods.
 * Never attempt direct mutation - it will cause state desynchronization.
 */
data class EmojiSticker(
    val id: Long = System.currentTimeMillis(), // Unique ID for each sticker
    val emoji: String,                         // Unicode emoji character
    val position: Offset,                      // Position on the page (immutable)
    val scale: Float = 1.0f,                   // Size scale factor (immutable)
    val rotation: Float = 0f,                  // Rotation in degrees (immutable)
    val size: Float = 120.dp.value             // Base size of the emoji - optimized for touch targets
) {
    companion object {
        const val MIN_SCALE = 0.4f                 // Ensures 48dp minimum touch target (120 × 0.4 = 48dp)
        const val MAX_SCALE = 2.5f                 // Prevents overflow while allowing good scaling
        const val DEFAULT_SIZE_DP = 120            // Research-based optimal size for finger manipulation
        const val MIN_TOUCH_TARGET_DP = 48         // Android guideline minimum
        const val OPTIMAL_TOUCH_TARGET_DP = 120    // Research-based optimal for precise manipulation
        
        /**
         * Validates if a scale value is within acceptable range
         */
        fun isValidScale(scale: Float): Boolean {
            return scale.isFinite() && scale >= MIN_SCALE && scale <= MAX_SCALE
        }
        
        /**
         * Clamps a scale value to the valid range
         */
        fun clampScale(scale: Float): Float {
            return when {
                !scale.isFinite() -> MIN_SCALE
                scale < MIN_SCALE -> MIN_SCALE
                scale > MAX_SCALE -> MAX_SCALE
                else -> scale
            }
        }
        
        /**
         * Validates if a position is within page boundaries considering sticker size
         */
        fun validatePosition(position: Offset, pageSize: Offset, stickerSize: Float): Offset {
            val halfSize = (stickerSize / 2f)
            return Offset(
                x = position.x.coerceIn(halfSize, pageSize.x - halfSize),
                y = position.y.coerceIn(halfSize, pageSize.y - halfSize)
            )
        }
    }
    
    /**
     * Creates a copy with updated position
     */
    fun withPosition(newPosition: Offset): EmojiSticker = copy(position = newPosition)
    
    /**
     * Creates a copy with updated scale (clamped to valid range)
     */
    fun withScale(newScale: Float): EmojiSticker = copy(scale = clampScale(newScale))
    
    /**
     * Creates a copy with updated rotation
     */
    fun withRotation(newRotation: Float): EmojiSticker = copy(rotation = newRotation)
    
    /**
     * Checks if a point is within the sticker's bounds (for hit detection)
     */
    fun containsPoint(point: Offset, tolerance: Float = 0f): Boolean {
        val actualSize = size * scale + tolerance
        val halfSize = actualSize / 2f
        
        return abs(point.x - position.x) <= halfSize && 
               abs(point.y - position.y) <= halfSize
    }
    
    /**
     * Gets the current touch target size in dp
     */
    val currentTouchTargetSize: Float get() = size * scale
    
    /**
     * Checks if sticker meets minimum touch target guidelines
     */
    val meetsMinimumTouchTarget: Boolean get() = currentTouchTargetSize >= MIN_TOUCH_TARGET_DP
}

/**
 * Common emoji characters organized by categories
 */
object CommonEmojis {
    // Smileys and People
    val faces = listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂", "🙃", "😉", "😊", 
        "😇", "🥰", "😍", "🤩", "😘", "😗", "😚", "😙", "😋", "😛", "😜", "🤪", 
        "😝", "🤑", "🤗", "🤭", "🤫", "🤔", "🤐", "🤨", "😐", "😑", "😶", "😏", 
        "😒", "🙄", "😬", "🤥", "😌", "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕"
    )
    
    // Animals
    val animals = listOf(
        "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯", "🦁", "🐮", 
        "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🐤", "🦆", "🦅", "🦉", "🦇", "🐺", 
        "🐗", "🐴", "🦄", "🐝", "🐛", "🦋", "🐌", "🐞", "🐜", "🦟", "🦗", "🕷", 
        "🦂", "🐢", "🐍", "🦎", "🦖", "🦕", "🐙", "🦑", "🦐", "🦞", "🦀", "🐡"
    )
    
    // Food & Drink
    val food = listOf(
        "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🍈", "🍒", "🍑", "��", 
        "🍍", "🥥", "🥝", "🍅", "🥑", "🍆", "🥔", "🥕", "🌽", "🥦", "🥬", "🥒", 
        "🌶", "🌰", "🥜", "🍞", "🥐", "🥖", "🥨", "🥯", "🧀", "🥚", "🍳", "🥞", 
        "🧇", "🥓", "🥩", "🍗", "🍖", "🦴", "🍔", "🍟", "🍕", "🌭", "🥪", "🌮"
    )
    
    // Travel & Places
    val travel = listOf(
        "🚗", "🚕", "🚙", "🚌", "🚎", "🏎", "🚓", "🚑", "🚒", "🚐", "🚚", "🚛", 
        "🚜", "🛴", "🚲", "🛵", "🏍", "🚔", "🚍", "🚘", "🚖", "🚡", "🚠", "🚟", 
        "🚃", "🚋", "🚞", "🚝", "🚄", "🚅", "🚈", "🚂", "🚆", "🚇", "🚊", "🚉",
        "✈", "🛫", "🛬", "🛩", "💺", "🛰", "🚀", "🛸", "🚁", "🛶", "⛵", "🚤"
    )
    
    // Activities
    val activities = listOf(
        "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱", "🪀", "🏓", 
        "🏸", "🏒", "🏑", "🥍", "🏏", "🥅", "⛳", "🪁", "🎯", "🎮", "🎲", "🧩", 
        "♟", "🎭", "🎨", "🎬", "🎤", "🎧", "🎼", "🎹", "🥁", "🎷", "🎺", "🎸", 
        "🎻", "🪕", "🎫", "🎪", "🎟", "🎗", "🏵", "🎖", "🏆", "🥌", "🛷", "🎿"
    )
    
    // All emojis combined for the selector
    val allEmojis = faces + animals + food + travel + activities
} 

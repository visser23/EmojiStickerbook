package io.github.storybookemoji.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Represents an emoji sticker placed on a page
 */
data class EmojiSticker(
    val id: Long = System.currentTimeMillis(), // Unique ID for each sticker
    val emoji: String,                         // Unicode emoji character
    var position: Offset,                      // Position on the page
    var scale: Float = 1.0f,                   // Size scale factor
    var rotation: Float = 0f,                  // Rotation in degrees
    val size: Float = 80.dp.value              // Base size of the emoji - increased from 48dp to 80dp
) {
    companion object {
        const val MIN_SCALE = 0.5f
        const val MAX_SCALE = 3.0f
        const val DEFAULT_SIZE_DP = 80
        
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
        "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🍈", "🍒", "🍑", "🥭", 
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

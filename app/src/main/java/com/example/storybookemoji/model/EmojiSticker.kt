package com.example.storybookemoji.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

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
)

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
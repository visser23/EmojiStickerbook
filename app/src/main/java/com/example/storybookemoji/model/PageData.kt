package com.example.storybookemoji.model

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Represents a page in the sticker book
 */
data class PageData(
    val id: Int,
    val backgroundGradient: Brush,
    val emojiStickers: MutableList<EmojiSticker> = mutableListOf()
)

/**
 * Collection of background gradients for pages
 */
object PageColors {
    val gradients = listOf(
        // Vibrant sunset - orange to pink
        Brush.linearGradient(
            colors = listOf(Color(0xFFFFA500), Color(0xFFFF1493))
        ),
        // Ocean - light blue to deep blue
        Brush.linearGradient(
            colors = listOf(Color(0xFF87CEEB), Color(0xFF0000CD))
        ),
        // Forest - light green to dark green
        Brush.linearGradient(
            colors = listOf(Color(0xFF90EE90), Color(0xFF006400))
        ),
        // Galaxy - purple to dark blue
        Brush.linearGradient(
            colors = listOf(Color(0xFF800080), Color(0xFF000080))
        ),
        // Candy - pink to light blue
        Brush.linearGradient(
            colors = listOf(Color(0xFFFF69B4), Color(0xFF00BFFF))
        ),
        // Tropical - yellow to orange
        Brush.linearGradient(
            colors = listOf(Color(0xFFFFFF00), Color(0xFFFF8C00))
        ),
        // Lavender field - light purple to dark purple
        Brush.linearGradient(
            colors = listOf(Color(0xFFE6E6FA), Color(0xFF8A2BE2))
        ),
        // Fire - red to yellow
        Brush.linearGradient(
            colors = listOf(Color(0xFFFF0000), Color(0xFFFFFF00))
        )
    )
} 
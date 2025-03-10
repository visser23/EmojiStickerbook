package com.example.storybookemoji.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import com.example.storybookemoji.model.EmojiSticker
import com.example.storybookemoji.model.PageColors
import com.example.storybookemoji.model.PageData
import com.example.storybookemoji.ui.components.BookPage
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * Main screen containing the page viewer and navigation controls
 */
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalUnitApi::class
)
@Composable
fun BookScreen() {
    // Start with a few pages but allow adding more
    val initialPageCount = 8
    val maxPageCount = 30  // Maximum number of pages allowed
    
    // Use mutable state list to dynamically add pages
    val pages = remember {
        mutableStateListOf<PageData>().apply {
            // Add initial pages
            for (i in 0 until initialPageCount) {
                add(
                    PageData(
                        id = i,
                        backgroundGradient = PageColors.gradients[i % PageColors.gradients.size]
                    )
                )
            }
        }
    }
    
    // Track the total number of pages
    val pageCount = remember { mutableStateOf(initialPageCount) }
    
    // State for tracking which page is currently shown
    val pagerState = rememberPagerState(initialPage = 0)
    
    // Coroutine scope for animations
    val coroutineScope = rememberCoroutineScope()
    
    // Custom animation for page turning (book-like)
    val pagerFlingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        snapAnimationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )
    
    // Function to add an emoji sticker to the current page
    val addStickerToPage: (String, Offset, Int) -> Unit = { emoji, position, pageIndex ->
        val page = pages[pageIndex]
        val newSticker = EmojiSticker(
            id = System.currentTimeMillis(),
            emoji = emoji,
            position = position,
            scale = 3.0f,
            rotation = 0f
        )
        page.emojiStickers.add(newSticker)
        // Force recomposition
        pages[pageIndex] = page.copy()
    }
    
    // Function to update an emoji sticker on the current page
    val updateStickerOnPage: (EmojiSticker, Int) -> Unit = { sticker, pageIndex ->
        val page = pages[pageIndex]
        val index = page.emojiStickers.indexOfFirst { it.id == sticker.id }
        if (index != -1) {
            // Create a deep copy of the sticker to ensure state is preserved
            val updatedSticker = sticker.copy(
                id = sticker.id,
                emoji = sticker.emoji,
                position = sticker.position,
                scale = sticker.scale,
                rotation = sticker.rotation,
                size = sticker.size
            )
            page.emojiStickers[index] = updatedSticker
            
            // Force recomposition by updating the page
            pages[pageIndex] = page.copy()
        }
    }
    
    // Function to remove an emoji sticker from the current page
    val removeStickerFromPage: (EmojiSticker, Int) -> Unit = { sticker, pageIndex ->
        val page = pages[pageIndex]
        page.emojiStickers.removeAll { it.id == sticker.id }
    }
    
    // Function to clear all stickers from the current page
    val clearCurrentPage: () -> Unit = {
        val currentPageIndex = pagerState.currentPage
        if (currentPageIndex < pages.size) {
            // Get the current page
            val currentPage = pages[currentPageIndex]
            // Clear all stickers
            currentPage.emojiStickers.clear()
            // Force recomposition by replacing the page with a copy
            pages[currentPageIndex] = currentPage.copy()
        }
    }
    
    // Function to add a new page
    val addNewPage: () -> Unit = {
        // Only add a new page if we haven't reached the maximum
        if (pages.size < maxPageCount) {
            val newPageId = pages.size
            val newPage = PageData(
                id = newPageId,
                backgroundGradient = PageColors.gradients[newPageId % PageColors.gradients.size]
            )
            pages.add(newPage)
            pageCount.value = pages.size
            
            // Navigate to the new page
            coroutineScope.launch {
                pagerState.animateScrollToPage(pages.size - 1)
            }
        }
    }
    
    // Function to check if we need to add more pages
    LaunchedEffect(pagerState.currentPage) {
        // If we're on the last page, add a new page to give the "infinite" feel
        // But only if we haven't reached the maximum
        if (pagerState.currentPage >= pages.size - 1 && pages.size < maxPageCount) {
            addNewPage()
        }
    }
    
    // Use a simple Box instead of Scaffold to make pages fill the entire screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = androidx.compose.ui.graphics.Color.Black)
    ) {
        // Main pager - now fills the entire screen
        HorizontalPager(
            pageCount = pages.size,
            state = pagerState,
            flingBehavior = pagerFlingBehavior,
            modifier = Modifier.fillMaxSize(),
            key = { index -> pages[index].id },
            // Keep more pages in memory to preserve state when navigating
            beyondBoundsPageCount = 3
        ) { pageIndex ->
            // Remember the page data to maintain state
            val pageData = remember(pages[pageIndex].id) { pages[pageIndex] }
            
            // Wrap content in Box - now fills the entire screen
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Display the page content
                BookPage(
                    pageData = pageData,
                    onAddSticker = { emoji, position ->
                        addStickerToPage(emoji, position, pageIndex)
                    },
                    onUpdateSticker = { sticker ->
                        updateStickerOnPage(sticker, pageIndex)
                        // Force recomposition of the page
                        pages[pageIndex] = pages[pageIndex].copy()
                    },
                    onRemoveSticker = { sticker ->
                        removeStickerFromPage(sticker, pageIndex)
                        // Force recomposition of the page
                        pages[pageIndex] = pages[pageIndex].copy()
                    }
                )
                
                // Keep only the small page number indicator at bottom right
                Text(
                    text = "${pageIndex + 1}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(4.dp)
                )
            }
        }
    }
} 
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
            emoji = emoji,
            position = position
        )
        page.emojiStickers.add(newSticker)
    }
    
    // Function to update an emoji sticker on the current page
    val updateStickerOnPage: (EmojiSticker, Int) -> Unit = { sticker, pageIndex ->
        val page = pages[pageIndex]
        val index = page.emojiStickers.indexOfFirst { it.id == sticker.id }
        if (index != -1) {
            page.emojiStickers[index] = sticker
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
            pages[currentPageIndex].emojiStickers.clear()
        }
    }
    
    // Function to add a new page
    val addNewPage: () -> Unit = {
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
    
    // Function to check if we need to add more pages
    LaunchedEffect(pagerState.currentPage) {
        // If we're on the last page, add a new page to give the "infinite" feel
        if (pagerState.currentPage >= pages.size - 1) {
            addNewPage()
        }
    }
    
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Emoji Sticker Book") },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Clear current page button
                    IconButton(onClick = clearCurrentPage) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Current Page"
                        )
                    }
                    
                    // Add new page button
                    IconButton(onClick = addNewPage) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add New Page"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Page indicator (current/total)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Page ${pagerState.currentPage + 1} of ${pages.size}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Main pager
            HorizontalPager(
                pageCount = pages.size,
                state = pagerState,
                flingBehavior = pagerFlingBehavior,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                // Wrap content in Box
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    // Display the page content
                    BookPage(
                        pageData = pages[pageIndex],
                        onAddSticker = { emoji, position ->
                            addStickerToPage(emoji, position, pageIndex)
                        },
                        onUpdateSticker = { sticker ->
                            updateStickerOnPage(sticker, pageIndex)
                        },
                        onRemoveSticker = { sticker ->
                            removeStickerFromPage(sticker, pageIndex)
                        }
                    )
                    
                    // Display page number indicator at bottom right
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
} 
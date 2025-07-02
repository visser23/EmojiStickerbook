package io.github.storybookemoji.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import io.github.storybookemoji.presentation.viewmodels.BookViewModel
import io.github.storybookemoji.ui.components.BookPage
import kotlinx.coroutines.launch

/**
 * Main screen containing the page viewer and navigation controls
 * Refactored to use proper architecture with ViewModel and immutable state
 */
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalUnitApi::class
)
@Composable
fun BookScreen() {
    // ViewModel manages all state and business logic
    val viewModel = remember { BookViewModel() }
    val bookState = viewModel.bookState
    
    // Pager state for navigation
    val pagerState = rememberPagerState(initialPage = 0)
    
    // Custom animation for page turning (book-like)
    val pagerFlingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        snapAnimationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )
    
    // Sync pager state with view model when user swipes
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != bookState.currentPageIndex) {
            viewModel.navigateToPage(pagerState.currentPage)
        }
    }
    
    // Sync view model state with pager when programmatically changed
    LaunchedEffect(bookState.currentPageIndex) {
        if (pagerState.currentPage != bookState.currentPageIndex) {
            pagerState.animateScrollToPage(bookState.currentPageIndex)
        }
    }
    
    // Handle automatic page addition when reaching the end
    LaunchedEffect(bookState.currentPageIndex, bookState.pages.size) {
        viewModel.handleAutoPageAddition()
    }
    
    // Main UI layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = androidx.compose.ui.graphics.Color.Black)
    ) {
        // Main pager - fills the entire screen
        HorizontalPager(
            pageCount = bookState.pages.size,
            state = pagerState,
            flingBehavior = pagerFlingBehavior,
            modifier = Modifier.fillMaxSize(),
            key = { index -> bookState.pages[index].id },
            // Keep more pages in memory to preserve state when navigating
            beyondBoundsPageCount = 3
        ) { pageIndex ->
            // Get page data from the immutable state
            val pageData = bookState.pages[pageIndex]
            
            // Wrap content in Box - fills the entire screen
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Display the page content
                BookPage(
                    pageData = pageData,
                    onAddSticker = { emoji, position ->
                        viewModel.addSticker(emoji, position, pageIndex)
                    },
                    onUpdateSticker = { sticker ->
                        viewModel.updateSticker(sticker, pageIndex)
                    },
                    onRemoveSticker = { sticker ->
                        viewModel.removeSticker(sticker.id, pageIndex)
                    }
                )
                
                // Page number indicator at bottom right
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

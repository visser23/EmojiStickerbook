package io.github.storybookemoji.domain.usecases

import io.github.storybookemoji.model.BookState

/**
 * Use case for managing page navigation and page lifecycle
 * Encapsulates all business logic related to page management
 */
class NavigatePagesUseCase {
    
    /**
     * Navigates to a specific page index
     */
    fun navigateToPage(bookState: BookState, pageIndex: Int): BookState {
        if (pageIndex < 0 || pageIndex >= bookState.pages.size) {
            return bookState
        }
        
        return bookState.withCurrentPageIndex(pageIndex)
    }
    
    /**
     * Navigates to the next page, adding a new page if needed
     */
    fun navigateToNextPage(bookState: BookState): BookState {
        val nextIndex = bookState.currentPageIndex + 1
        
        return when {
            nextIndex < bookState.pages.size -> {
                // Navigate to existing next page
                bookState.withCurrentPageIndex(nextIndex)
            }
            bookState.shouldAddNewPage() -> {
                // Add new page and navigate to it
                bookState.withAddedPage()
            }
            else -> {
                // At maximum pages, stay on current page
                bookState
            }
        }
    }
    
    /**
     * Navigates to the previous page
     */
    fun navigateToPreviousPage(bookState: BookState): BookState {
        val previousIndex = bookState.currentPageIndex - 1
        
        return if (previousIndex >= 0) {
            bookState.withCurrentPageIndex(previousIndex)
        } else {
            bookState
        }
    }
    
    /**
     * Adds a new page to the book if possible
     */
    fun addNewPage(bookState: BookState): BookState {
        return if (bookState.pages.size < BookState.MAX_PAGE_COUNT) {
            bookState.withAddedPage()
        } else {
            bookState
        }
    }
    
    /**
     * Checks if auto-page addition should occur based on current navigation state
     */
    fun shouldAutoAddPage(bookState: BookState): Boolean {
        return bookState.shouldAddNewPage()
    }
    
    /**
     * Gets the total number of pages
     */
    fun getTotalPages(bookState: BookState): Int = bookState.pages.size
    
    /**
     * Checks if we're on the first page
     */
    fun isFirstPage(bookState: BookState): Boolean = bookState.currentPageIndex == 0
    
    /**
     * Checks if we're on the last page
     */
    fun isLastPage(bookState: BookState): Boolean = 
        bookState.currentPageIndex == bookState.pages.size - 1
    
    /**
     * Checks if we can add more pages
     */
    fun canAddMorePages(bookState: BookState): Boolean = 
        bookState.pages.size < BookState.MAX_PAGE_COUNT
} 
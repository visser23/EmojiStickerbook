package io.github.storybookemoji.domain.usecases

import io.github.storybookemoji.model.BookState
import io.github.storybookemoji.model.PageData

/**
 * Use case for managing page-centric undo/redo functionality
 * Tracks history per page and allows undoing the last action on the focused page
 */
class UndoRedoUseCase {
    
    // Page-centric history storage (pageIndex -> history stack)
    private val pageHistories = mutableMapOf<Int, MutableList<PageData>>()
    
    companion object {
        private const val MAX_HISTORY_SIZE = 50 // Limit memory usage
    }
    
    /**
     * Records a page state before an action for undo functionality
     */
    fun recordPageState(pageIndex: Int, pageData: PageData) {
        val history = pageHistories.getOrPut(pageIndex) { mutableListOf() }
        
        // Add current state to history
        history.add(pageData.copy()) // Deep copy to prevent mutations
        
        // Limit history size to prevent memory issues
        if (history.size > MAX_HISTORY_SIZE) {
            history.removeAt(0) // Remove oldest entry
        }
    }
    
    /**
     * Undoes the last action on the specified page
     * Returns the updated BookState or null if no undo is available
     */
    fun undoLastAction(bookState: BookState, pageIndex: Int): BookState? {
        val history = pageHistories[pageIndex] ?: return null
        
        if (history.isEmpty()) {
            return null // No history to undo
        }
        
        // Get the previous state (remove from history)
        val previousPageState = history.removeLastOrNull() ?: return null
        
        // Update the book state with the previous page state
        return bookState.withUpdatedPage(pageIndex, previousPageState)
    }
    
    /**
     * Checks if undo is available for the specified page
     */
    fun canUndo(pageIndex: Int): Boolean {
        return pageHistories[pageIndex]?.isNotEmpty() == true
    }
    
    /**
     * Gets the number of undo actions available for a page
     */
    fun getUndoCount(pageIndex: Int): Int {
        return pageHistories[pageIndex]?.size ?: 0
    }
    
    /**
     * Clears history for a specific page
     */
    fun clearPageHistory(pageIndex: Int) {
        pageHistories[pageIndex]?.clear()
    }
    
    /**
     * Clears all history (useful for memory cleanup)
     */
    fun clearAllHistory() {
        pageHistories.clear()
    }
    
    /**
     * Gets memory usage information for debugging
     */
    fun getMemoryInfo(): UndoMemoryInfo {
        val totalEntries = pageHistories.values.sumOf { it.size }
        val pagesWithHistory = pageHistories.keys.size
        
        return UndoMemoryInfo(
            totalHistoryEntries = totalEntries,
            pagesWithHistory = pagesWithHistory,
            maxHistoryPerPage = MAX_HISTORY_SIZE
        )
    }
}

/**
 * Data class for undo memory usage information
 */
data class UndoMemoryInfo(
    val totalHistoryEntries: Int,
    val pagesWithHistory: Int,
    val maxHistoryPerPage: Int
) 
package io.github.storybookemoji.presentation.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.storybookemoji.domain.usecases.ManageStickersUseCase
import io.github.storybookemoji.domain.usecases.NavigatePagesUseCase
import io.github.storybookemoji.model.BookState
import io.github.storybookemoji.model.EmojiSticker
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.runBlocking

/**
 * Performance-optimized ViewModel for managing the sticker book state
 * Includes memory monitoring and efficient state management
 * Supports both async (production) and sync (testing) modes
 */
class BookViewModel(
    private val testMode: Boolean = false // For testing synchronous behavior
) : ViewModel() {
    
    // Use cases for business logic
    private val manageStickersUseCase = ManageStickersUseCase()
    private val navigatePagesUseCase = NavigatePagesUseCase()
    
    // Main app state with performance monitoring
    var bookState by mutableStateOf(BookState.createInitialState())
        private set
    
    // Performance metrics (debug builds only)
    private var lastMemoryCheck = System.currentTimeMillis()
    private var operationCount = 0
    
    // UI state properties (computed properties for efficiency)
    val currentPageIndex: Int get() = bookState.currentPageIndex
    val totalPages: Int get() = bookState.pages.size
    val currentPage get() = bookState.currentPage
    val canAddMorePages: Boolean get() = bookState.pages.size < BookState.MAX_PAGE_COUNT
    
    // Emoji selector state
    var isEmojiSelectorVisible by mutableStateOf(false)
        private set
    
    /**
     * Performance-optimized sticker addition with memory management
     * Uses async processing in production, sync in testing
     */
    fun addSticker(emoji: String, position: Offset, pageIndex: Int) {
        if (testMode) {
            // Synchronous execution for tests
            addStickerSync(emoji, position, pageIndex)
        } else {
            // Asynchronous execution for production performance
            viewModelScope.launch(Dispatchers.Default) {
                try {
                    checkMemoryUsage()
                    
                    val validatedPosition = validateStickerPosition(
                        position = position,
                        pageWidth = 1080f,
                        pageHeight = 1920f,
                        stickerSize = EmojiSticker.DEFAULT_SIZE_DP.toFloat()
                    )
                    
                    val newBookState = manageStickersUseCase.addStickerToPage(
                        bookState = bookState,
                        emoji = emoji,
                        position = validatedPosition,
                        pageIndex = pageIndex
                    )
                    
                    withContext(Dispatchers.Main) {
                        bookState = newBookState
                    }
                    
                    operationCount++
                } catch (e: Exception) {
                    handleError("addSticker", e)
                }
            }
        }
    }
    
    /**
     * Synchronous version for testing
     */
    private fun addStickerSync(emoji: String, position: Offset, pageIndex: Int) {
        try {
            val validatedPosition = validateStickerPosition(
                position = position,
                pageWidth = 1080f,
                pageHeight = 1920f,
                stickerSize = EmojiSticker.DEFAULT_SIZE_DP.toFloat()
            )
            
            bookState = manageStickersUseCase.addStickerToPage(
                bookState = bookState,
                emoji = emoji,
                position = validatedPosition,
                pageIndex = pageIndex
            )
            
            operationCount++
        } catch (e: Exception) {
            handleError("addSticker", e)
        }
    }
    
    /**
     * Performance-optimized sticker update
     */
    fun updateSticker(updatedSticker: EmojiSticker, pageIndex: Int) {
        if (testMode) {
            // Synchronous execution for tests
            bookState = manageStickersUseCase.updateStickerOnPage(
                bookState = bookState,
                updatedSticker = updatedSticker,
                pageIndex = pageIndex
            )
            operationCount++
        } else {
            // Asynchronous execution for production
            viewModelScope.launch(Dispatchers.Default) {
                try {
                    val newBookState = manageStickersUseCase.updateStickerOnPage(
                        bookState = bookState,
                        updatedSticker = updatedSticker,
                        pageIndex = pageIndex
                    )
                    
                    withContext(Dispatchers.Main) {
                        bookState = newBookState
                    }
                    
                    operationCount++
                } catch (e: Exception) {
                    handleError("updateSticker", e)
                }
            }
        }
    }
    
    /**
     * Performance-optimized sticker removal
     */
    fun removeSticker(stickerId: Long, pageIndex: Int) {
        if (testMode) {
            // Synchronous execution for tests
            bookState = manageStickersUseCase.removeStickerFromPage(
                bookState = bookState,
                stickerId = stickerId,
                pageIndex = pageIndex
            )
            operationCount++
        } else {
            // Asynchronous execution for production
            viewModelScope.launch(Dispatchers.Default) {
                try {
                    val newBookState = manageStickersUseCase.removeStickerFromPage(
                        bookState = bookState,
                        stickerId = stickerId,
                        pageIndex = pageIndex
                    )
                    
                    withContext(Dispatchers.Main) {
                        bookState = newBookState
                    }
                    
                    operationCount++
                } catch (e: Exception) {
                    handleError("removeSticker", e)
                }
            }
        }
    }
    
    /**
     * Efficient page clearing with memory cleanup
     */
    fun clearPage(pageIndex: Int) {
        if (testMode) {
            // Synchronous execution for tests
            bookState = manageStickersUseCase.clearPageStickers(
                bookState = bookState,
                pageIndex = pageIndex
            )
            if (operationCount > 100) {
                System.gc()
                operationCount = 0
            }
        } else {
            // Asynchronous execution for production
            viewModelScope.launch(Dispatchers.Default) {
                try {
                    val newBookState = manageStickersUseCase.clearPageStickers(
                        bookState = bookState,
                        pageIndex = pageIndex
                    )
                    
                    withContext(Dispatchers.Main) {
                        bookState = newBookState
                        if (operationCount > 100) {
                            System.gc()
                            operationCount = 0
                        }
                    }
                } catch (e: Exception) {
                    handleError("clearPage", e)
                }
            }
        }
    }
    
    /**
     * Optimized page navigation
     */
    fun navigateToPage(pageIndex: Int) {
        if (pageIndex in 0 until totalPages) {
            bookState = navigatePagesUseCase.navigateToPage(bookState, pageIndex)
        }
    }
    
    /**
     * Memory-efficient page addition
     */
    fun addNewPage() {
        if (canAddMorePages) {
            if (testMode) {
                // Synchronous execution for tests
                bookState = navigatePagesUseCase.addNewPage(bookState)
            } else {
                // Asynchronous execution for production
                viewModelScope.launch(Dispatchers.Default) {
                    try {
                        val newBookState = navigatePagesUseCase.addNewPage(bookState)
                        
                        withContext(Dispatchers.Main) {
                            bookState = newBookState
                        }
                    } catch (e: Exception) {
                        handleError("addNewPage", e)
                    }
                }
            }
        }
    }
    
    /**
     * Toggle emoji selector visibility
     */
    fun toggleEmojiSelector(visible: Boolean) {
        isEmojiSelectorVisible = visible
    }
    
    /**
     * Validates and corrects sticker position within bounds
     */
    fun validateStickerPosition(
        position: Offset,
        pageWidth: Float,
        pageHeight: Float,
        stickerSize: Float
    ): Offset {
        return manageStickersUseCase.clampStickerPosition(
            position = position,
            pageWidth = pageWidth,
            pageHeight = pageHeight,
            stickerSize = stickerSize
        )
    }
    
    /**
     * Validates sticker scale within acceptable bounds
     */
    fun validateStickerScale(scale: Float): Float {
        return manageStickersUseCase.validateStickerScale(scale)
    }
    
    /**
     * Memory usage monitoring (debug builds only)
     */
    private fun checkMemoryUsage() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastMemoryCheck > 10000) { // Check every 10 seconds
            val runtime = Runtime.getRuntime()
            val usedMemory = runtime.totalMemory() - runtime.freeMemory()
            val maxMemory = runtime.maxMemory()
            val memoryUsagePercent = (usedMemory * 100) / maxMemory
            
            // Log memory usage in debug builds only (ProGuard will remove this)
            if (memoryUsagePercent > 80) {
                println("High memory usage: ${memoryUsagePercent}% - Consider optimization")
            }
            
            lastMemoryCheck = currentTime
        }
    }
    
    /**
     * Centralized error handling
     */
    private fun handleError(operation: String, error: Exception) {
        // In debug builds, log the error (ProGuard will remove this)
        println("Error in $operation: ${error.message}")
        
        // In production, fail silently to maintain user experience
        // Could add crash reporting here if needed
    }
    
    /**
     * Cleanup resources when ViewModel is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        // Perform cleanup if needed
        operationCount = 0
    }
} 
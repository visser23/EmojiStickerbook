package io.github.storybookemoji.model

/**
 * Represents the overall state of the sticker book application
 * This is the single source of truth for the entire app state
 */
data class BookState(
    val pages: List<PageData>,
    val currentPageIndex: Int,
    val isEmojiSelectorVisible: Boolean = false,
    val selectedEmojiCategory: EmojiCategory = EmojiCategory.SMILEYS
) {
    companion object {
        const val INITIAL_PAGE_COUNT = 8
        const val MAX_PAGE_COUNT = 30
        
        /**
         * Creates the initial book state with default pages
         */
        fun createInitialState(): BookState {
            val initialPages = (0 until INITIAL_PAGE_COUNT).map { index ->
                PageData(
                    id = index,
                    backgroundGradient = PageColors.gradients[index % PageColors.gradients.size]
                )
            }
            
            return BookState(
                pages = initialPages,
                currentPageIndex = 0
            )
        }
    }
    
    /**
     * Gets the current page data
     */
    val currentPage: PageData?
        get() = pages.getOrNull(currentPageIndex)
    
    /**
     * Creates a copy with an updated page
     */
    fun withUpdatedPage(pageIndex: Int, updatedPage: PageData): BookState = copy(
        pages = pages.mapIndexed { index, page ->
            if (index == pageIndex) updatedPage else page
        }
    )
    
    /**
     * Creates a copy with a new page added
     */
    fun withAddedPage(): BookState {
        if (pages.size >= MAX_PAGE_COUNT) return this
        
        val newPageId = pages.size
        val newPage = PageData(
            id = newPageId,
            backgroundGradient = PageColors.gradients[newPageId % PageColors.gradients.size]
        )
        
        return copy(
            pages = pages + newPage,
            currentPageIndex = pages.size // Navigate to the new page
        )
    }
    
    /**
     * Creates a copy with updated current page index
     */
    fun withCurrentPageIndex(newIndex: Int): BookState = copy(
        currentPageIndex = newIndex.coerceIn(0, pages.size - 1)
    )
    
    /**
     * Creates a copy with emoji selector visibility toggled
     */
    fun withEmojiSelectorVisible(visible: Boolean): BookState = copy(
        isEmojiSelectorVisible = visible
    )
    
    /**
     * Creates a copy with selected emoji category changed
     */
    fun withSelectedEmojiCategory(category: EmojiCategory): BookState = copy(
        selectedEmojiCategory = category
    )
    
    /**
     * Checks if a new page should be automatically added
     */
    fun shouldAddNewPage(): Boolean = 
        currentPageIndex >= pages.size - 1 && pages.size < MAX_PAGE_COUNT
}

/**
 * Emoji categories for organization
 */
enum class EmojiCategory(val displayName: String) {
    SMILEYS("Smileys"),
    ANIMALS("Animals"),
    FOOD("Food"),
    ACTIVITIES("Activities"),
    TRAVEL("Travel"),
    OBJECTS("Objects"),
    SYMBOLS("Symbols"),
    FLAGS("Flags")
} 
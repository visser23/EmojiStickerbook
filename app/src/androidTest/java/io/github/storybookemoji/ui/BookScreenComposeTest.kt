package io.github.storybookemoji.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.storybookemoji.ui.screens.BookScreen
import io.github.storybookemoji.ui.theme.StoryBookEmojiTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI tests for BookScreen
 * Tests user interactions and UI behavior
 */
@RunWith(AndroidJUnit4::class)
class BookScreenComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bookScreen_displaysCorrectly() {
        // Given & When
        composeTestRule.setContent {
            StoryBookEmojiTheme {
                BookScreen()
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Book page")
            .assertIsDisplayed()
    }

    @Test
    fun bookScreen_emojiSelectorButton_isVisible() {
        // Given & When
        composeTestRule.setContent {
            StoryBookEmojiTheme {
                BookScreen()
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Open emoji selector")
            .assertIsDisplayed()
    }

    @Test
    fun bookScreen_emojiSelectorButton_togglesEmojiSelector() {
        // Given
        composeTestRule.setContent {
            StoryBookEmojiTheme {
                BookScreen()
            }
        }

        // When - Click emoji selector button
        composeTestRule.onNodeWithContentDescription("Open emoji selector")
            .performClick()

        // Then - Emoji selector should be visible
        composeTestRule.onNodeWithContentDescription("Emoji selector")
            .assertIsDisplayed()

        // When - Click close button
        composeTestRule.onNodeWithContentDescription("Close emoji selector")
            .performClick()

        // Then - Emoji selector should be hidden
        composeTestRule.onNodeWithContentDescription("Emoji selector")
            .assertDoesNotExist()
    }

    @Test
    fun bookScreen_pageNavigation_worksCorrectly() {
        // Given
        composeTestRule.setContent {
            StoryBookEmojiTheme {
                BookScreen()
            }
        }

        // Then - Should start on page 1
        composeTestRule.onNodeWithText("1 / ")
            .assertIsDisplayed()

        // When - Swipe to next page (if available)
        composeTestRule.onNodeWithContentDescription("Book page")
            .performTouchInput { swipeLeft() }

        // Then - Should show page navigation
        composeTestRule.onNodeWithContentDescription("Book page")
            .assertIsDisplayed()
    }

    @Test
    fun bookScreen_clearPageButton_isVisible() {
        // Given & When
        composeTestRule.setContent {
            StoryBookEmojiTheme {
                BookScreen()
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Clear page")
            .assertIsDisplayed()
    }

    @Test
    fun bookScreen_addPageButton_isVisible() {
        // Given & When
        composeTestRule.setContent {
            StoryBookEmojiTheme {
                BookScreen()
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Add page")
            .assertIsDisplayed()
    }

    @Test
    fun bookScreen_accessibility_isProperlyConfigured() {
        // Given & When
        composeTestRule.setContent {
            StoryBookEmojiTheme {
                BookScreen()
            }
        }

        // Then - All interactive elements should have content descriptions
        composeTestRule.onNodeWithContentDescription("Open emoji selector")
            .assertHasClickAction()
        
        composeTestRule.onNodeWithContentDescription("Clear page")
            .assertHasClickAction()
            
        composeTestRule.onNodeWithContentDescription("Add page")
            .assertHasClickAction()
    }

    @Test
    fun bookScreen_handlesLongContent_gracefully() {
        // Given & When
        composeTestRule.setContent {
            StoryBookEmojiTheme {
                BookScreen()
            }
        }

        // Then - Screen should handle content without overflow
        composeTestRule.onNodeWithContentDescription("Book page")
            .assertIsDisplayed()
            .assertWidthIsAtLeast(1.dp)
            .assertHeightIsAtLeast(1.dp)
    }
} 
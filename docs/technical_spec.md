# 🔧 Technical Specification - Emoji Sticker Book

## Architecture Overview

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌─────────────────┬─────────────────┬──────────────────┐    │
│  │   BookScreen    │  EmojiSelector  │   BookPage       │    │
│  │   (Compose)     │   (Compose)     │   (Compose)      │    │
│  └─────────────────┴─────────────────┴──────────────────┘    │
├─────────────────────────────────────────────────────────────┤
│                     Domain Layer                            │
│  ┌─────────────────┬─────────────────┬──────────────────┐    │
│  │ BookViewModel   │ GestureHandler  │ StateManager     │    │
│  │               │                 │                  │    │
│  └─────────────────┴─────────────────┴──────────────────┘    │
├─────────────────────────────────────────────────────────────┤
│                      Data Layer                             │
│  ┌─────────────────┬─────────────────┬──────────────────┐    │
│  │   PageData      │ EmojiSticker    │  PageColors      │    │
│  │   (Model)       │   (Model)       │   (Constants)    │    │
│  └─────────────────┴─────────────────┴──────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

### Core Design Principles

1. **Unidirectional Data Flow**: State flows down, events flow up
2. **Immutable State**: All data structures are immutable for predictability
3. **Compose-First**: Native Android Compose UI framework
4. **Single Source of Truth**: BookScreen manages all page state
5. **Offline-Only**: No network dependencies or external services

## Technology Stack

### Core Technologies
| Component | Technology | Version | Justification |
|-----------|------------|---------|---------------|
| Language | Kotlin | 1.9+ | Modern, concise, null-safe |
| UI Framework | Jetpack Compose | 2024.02+ | Declarative, performant UI |
| Build System | Gradle (KTS) | 8.2+ | Kotlin DSL for build logic |
| Target SDK | Android 35 | API 35 | Latest stable Android |
| Minimum SDK | Android 21 | API 21 | 95%+ device coverage |

### Key Dependencies
```kotlin
dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Compose BOM & UI
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    
    // Debug Tools
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

## Data Models

### Core Data Structures

#### EmojiSticker
```kotlin
data class EmojiSticker(
    val id: Long = System.currentTimeMillis(),
    val emoji: String,
    var position: Offset,
    var scale: Float = 1.0f,
    var rotation: Float = 0f,
    val size: Float = 80.dp.value
) {
    companion object {
        const val MIN_SCALE = 0.5f
        const val MAX_SCALE = 3.0f
        const val DEFAULT_SIZE_DP = 80
    }
}
```

#### PageData
```kotlin
data class PageData(
    val id: Int,
    val backgroundGradient: Brush,
    val emojiStickers: MutableList<EmojiSticker> = mutableListOf(),
    val drawingPaths: MutableList<DrawingPath> = mutableListOf() // Future enhancement
) {
    fun copy(): PageData = PageData(
        id = id,
        backgroundGradient = backgroundGradient,
        emojiStickers = emojiStickers.toMutableList(),
        drawingPaths = drawingPaths.toMutableList()
    )
}
```

#### BookState (Proposed)
```kotlin
data class BookState(
    val pages: List<PageData>,
    val currentPageIndex: Int,
    val isEmojiSelectorVisible: Boolean,
    val selectedEmojiCategory: EmojiCategory
) {
    companion object {
        const val INITIAL_PAGE_COUNT = 8
        const val MAX_PAGE_COUNT = 30
    }
}
```

## Component Architecture

### BookScreen (Main Container)
**Responsibilities:**
- Manage global app state
- Handle page navigation
- Coordinate between child components
- Manage page lifecycle

**Key Functions:**
```kotlin
@Composable
fun BookScreen() {
    // State Management
    val pages = remember { mutableStateListOf<PageData>() }
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    
    // Event Handlers
    val addStickerToPage: (String, Offset, Int) -> Unit
    val updateStickerOnPage: (EmojiSticker, Int) -> Unit
    val removeStickerFromPage: (EmojiSticker, Int) -> Unit
    val clearCurrentPage: () -> Unit
    val addNewPage: () -> Unit
}
```

### BookPage (Individual Page)
**Responsibilities:**
- Render page background
- Handle touch events for sticker placement
- Manage sticker rendering and interactions
- Coordinate with EmojiSelector

**Key Functions:**
```kotlin
@Composable
fun BookPage(
    pageData: PageData,
    onAddSticker: (String, Offset) -> Unit,
    onUpdateSticker: (EmojiSticker) -> Unit,
    onRemoveSticker: (EmojiSticker) -> Unit
)
```

### DraggableEmoji (Sticker Component)
**Responsibilities:**
- Render individual emoji stickers
- Handle drag, scale, and rotation gestures
- Manage touch state and visual feedback
- Trigger removal on long press

**Key Functions:**
```kotlin
@Composable
fun DraggableEmoji(
    sticker: EmojiSticker,
    onUpdate: (EmojiSticker) -> Unit,
    onRemove: (EmojiSticker) -> Unit
)
```

### EmojiSelector (Sticker Picker)
**Responsibilities:**
- Display categorized emoji collections
- Handle emoji selection
- Manage visibility state
- Optimize rendering for large emoji sets

**Key Functions:**
```kotlin
@Composable
fun EmojiSelector(
    isVisible: Boolean,
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
)
```

## Data Flow Patterns

### State Management Flow

```
User Touch Event
        ↓
   Touch Handler
        ↓
   Event Processing
        ↓
   State Update
        ↓
  Compose Recomposition
        ↓
   UI Update
```

### Sticker Lifecycle

```
1. User Selection
   ├── Emoji picker opened
   ├── Category browsing
   └── Emoji selection

2. Placement
   ├── Touch position capture
   ├── Sticker creation
   └── State update

3. Manipulation
   ├── Drag events
   ├── Scale/rotation
   └── Position updates

4. Persistence
   ├── State preservation
   ├── Page navigation
   └── App lifecycle
```

## Performance Optimizations

### Memory Management
- **Lazy Loading**: Only render visible pages + 1 buffer
- **Object Pooling**: Reuse EmojiSticker instances where possible
- **Compose Optimization**: Use `remember` and `derivedStateOf` appropriately
- **Garbage Collection**: Minimize allocations during animations

### Rendering Performance
- **60fps Target**: All animations must maintain smooth framerate
- **GPU Acceleration**: Leverage hardware acceleration for transformations
- **Batched Updates**: Group multiple sticker updates into single recomposition
- **Efficient Drawing**: Use Canvas API for custom drawing operations

### Storage Optimization
- **APK Size**: Minimize drawable resources, use vector graphics
- **Runtime Memory**: Cap maximum stickers per page (200 limit)
- **Transient Storage**: No persistent storage, all state in memory

## Gesture Handling System

### Touch Event Processing

```kotlin
sealed class TouchEvent {
    data class SingleTap(val position: Offset) : TouchEvent()
    data class LongPress(val position: Offset) : TouchEvent()
    data class Drag(val startPos: Offset, val currentPos: Offset) : TouchEvent()
    data class Pinch(val center: Offset, val scale: Float) : TouchEvent()
    data class Rotation(val center: Offset, val rotation: Float) : TouchEvent()
}

class GestureProcessor {
    fun processTouch(
        event: TouchEvent,
        targetSticker: EmojiSticker?
    ): StickerAction
}
```

### Gesture Priority System
1. **Long Press**: Highest priority - always triggers removal
2. **Drag**: Medium priority - only if sticker selected
3. **Pinch/Scale**: Medium priority - only if sticker selected  
4. **Single Tap**: Lowest priority - placement when no sticker hit

## Security Architecture

### Privacy-First Design
- **No Network Permissions**: App manifest excludes all network permissions
- **No External Storage**: Only use internal app storage
- **No Analytics**: Zero telemetry or user behavior tracking
- **No Crash Reporting**: Local error handling only

### Code Security
- **Input Validation**: All touch coordinates validated and clamped
- **Memory Safety**: Kotlin null safety prevents common vulnerabilities
- **Resource Limits**: Hard caps on stickers per page and total pages
- **Error Boundaries**: Graceful failure handling throughout

### Build Security
```kotlin
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

## Testing Strategy

### Unit Testing (Target: 80% Coverage)
```kotlin
// Model Tests
class EmojiStickerTest {
    @Test fun testStickerCreation()
    @Test fun testPositionValidation()
    @Test fun testScaleLimits()
}

// Logic Tests  
class GestureProcessorTest {
    @Test fun testTouchEventPriority()
    @Test fun testStickerHitDetection()
    @Test fun testBoundaryConditions()
}
```

### Integration Testing
```kotlin
class BookScreenTest {
    @Test fun testPageNavigation()
    @Test fun testStickerPlacement()
    @Test fun testStatePreservation()
}
```

### UI Testing (Compose Testing)
```kotlin
class UserInteractionTest {
    @Test fun testStickerPlacementFlow()
    @Test fun testEmojiSelectorInteraction()
    @Test fun testPageSwipeGestures()
}
```

## Future Architecture Considerations

### Planned Enhancements

#### Drawing System
```kotlin
data class DrawingPath(
    val id: Long,
    val points: List<Offset>,
    val brush: DrawingBrush,
    val color: Color
)

class DrawingEngine {
    fun processDrawingEvent(event: MotionEvent): DrawingAction
    fun renderPath(path: DrawingPath, canvas: Canvas)
}
```

#### State Persistence
```kotlin
interface BookStateRepository {
    suspend fun saveBookState(state: BookState)
    suspend fun loadBookState(): BookState?
    suspend fun clearSavedState()
}
```

#### Export System
```kotlin
interface BookExporter {
    suspend fun exportPageAsImage(pageData: PageData): Bitmap
    suspend fun exportBookAsPDF(bookState: BookState): ByteArray
}
```

## API Boundaries

### Internal Component APIs

#### BookScreen → BookPage
```kotlin
interface PageEventHandler {
    fun onStickerAdded(emoji: String, position: Offset)
    fun onStickerUpdated(sticker: EmojiSticker)
    fun onStickerRemoved(sticker: EmojiSticker)
    fun onPageCleared()
}
```

#### BookPage → DraggableEmoji
```kotlin
interface StickerEventHandler {
    fun onStickerSelected(sticker: EmojiSticker)
    fun onPositionChanged(sticker: EmojiSticker, newPosition: Offset)
    fun onScaleChanged(sticker: EmojiSticker, newScale: Float)
    fun onRemovalRequested(sticker: EmojiSticker)
}
```

### External System APIs

#### Android System Integration
```kotlin
class AppLifecycleHandler : DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner)
    override fun onStop(owner: LifecycleOwner)
    override fun onDestroy(owner: LifecycleOwner)
}
```

---

**Note**: This specification prioritizes simplicity, performance, and maintainability while keeping future extensibility in mind. 
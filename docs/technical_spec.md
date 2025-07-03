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
    val size: Float = 120.dp.value
) {
    companion object {
        const val MIN_SCALE = 0.4f
        const val MAX_SCALE = 2.5f
        const val DEFAULT_SIZE_DP = 120
        const val MIN_TOUCH_TARGET_DP = 48
        const val OPTIMAL_TOUCH_TARGET_DP = 120
    }

    fun validateScale(scale: Float): Float = scale.coerceIn(MIN_SCALE, MAX_SCALE)
    fun validatePosition(position: Offset, pageSize: Size, stickerSize: Float): Offset {
        val halfSize = (stickerSize / 2f)
        return Offset(
            x = position.x.coerceIn(halfSize, pageSize.width - halfSize),
            y = position.y.coerceIn(halfSize, pageSize.height - halfSize)
        )
    }

    val currentTouchTargetSize: Float get() = size * scale
    val meetsMinimumTouchTarget: Boolean get() = currentTouchTargetSize >= MIN_TOUCH_TARGET_DP
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

#### GestureState
```kotlin
data class GestureState(
    val isDragging: Boolean = false,
    val isScaling: Boolean = false,
    val isRotating: Boolean = false,
    val isLongPressing: Boolean = false,
    val gestureStartPosition: Offset = Offset.Zero,
    val gestureStartScale: Float = 1.0f,
    val gestureStartRotation: Float = 0f
) {
    val isActive: Boolean get() = isDragging || isScaling || isRotating || isLongPressing
    
    companion object {
        val Idle = GestureState()
    }
}
```

#### DeletionMethod
```kotlin
sealed class DeletionMethod {
    object LongPress : DeletionMethod()
    object DragToBin : DeletionMethod()
    object SwipeToDelete : DeletionMethod()
    object DoubleTap : DeletionMethod()
}

data class DeletionConfig(
    val method: DeletionMethod = DeletionMethod.DragToBin,
    val showVisualFeedback: Boolean = true,
    val enableUndo: Boolean = true,
    val undoTimeoutMs: Long = 3000L
)
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

## 🚨 **CRITICAL UPDATE: Architectural Review Findings**

### **Code Review Summary (December 2024)**

**Status**: **❌ FUNDAMENTAL ARCHITECTURAL FLAWS DISCOVERED**

During comprehensive code review against user requirements, critical architectural issues have been identified that prevent core gesture functionality from working. The following issues require immediate architectural reconstruction:

#### **🔴 Critical Issues Identified**

1. **State Synchronization Failure** (CRITICAL)
   - `transformState` in `DraggableEmoji` diverged from `EmojiSticker` model state
   - UI changes don't propagate to persistent data layer
   - Direct mutation of data class properties violates immutable patterns

2. **Gesture Modifier Conflicts** (CRITICAL)  
   - Multiple `pointerInput` modifiers compete for touch events
   - Last modifier consumes all events, blocking earlier gesture detection
   - Sequential stacking prevents proper event distribution

3. **Broken Topmost Detection** (HIGH)
   - Incorrect overlap detection logic in sticker selection
   - Wrong stickers receive gesture events in overlapping scenarios

4. **Missing Test Coverage** (HIGH)
   - Zero test coverage for gesture functionality
   - No validation of core user requirements

5. **Architecture Violations** (MEDIUM)
   - Direct mutation of immutable data classes throughout codebase
   - Violates Compose state management best practices

#### **📋 Requirements Status Assessment**

| Requirement | Status | Issue |
|-------------|--------|-------|
| Add emoji | ✅ WORKING | None |
| Resize emoji | ❌ BROKEN | State sync failure |
| Drag to bin | ❌ BROKEN | Gesture conflicts |
| Rotate emoji | ❌ BROKEN | Gesture conflicts |
| Page persistence | ✅ WORKING | None |
| Undo functionality | ✅ WORKING | None |

**Critical Gap**: 4 out of 6 core requirements non-functional

---

## **🛠️ Technical Architecture Specifications**

### **Gesture Handling Architecture (REVISED)**

#### **Current Broken Pattern**
```kotlin
// ❌ BROKEN: Multiple competing gesture modifiers
.pointerInput(containerSize, isTopmost) {
    detectDragGestures(onDrag = { ... })
}
.pointerInput(containerSize, isTopmost) {
    detectTapGestures(onLongPress = { ... })
}
.pointerInput(containerSize, isTopmost) {
    detectTransformGestures { ... } // BLOCKS ALL OTHERS
}
```

#### **Corrected Unified Pattern**
```kotlin
// ✅ CORRECT: Unified gesture coordination
.pointerInput(Unit) {
    awaitEachGesture {
        val firstDown = awaitFirstDown()
        
        // Detect gesture type based on pointer count and movement
        when {
            // Single touch - handle drag or long press
            pointerCount == 1 -> handleSingleTouch(firstDown)
            
            // Multi-touch - handle scale and rotation
            pointerCount >= 2 -> handleMultiTouch()
        }
    }
}

private suspend fun PointerInputScope.handleSingleTouch(firstDown: PointerInputChange) {
    // Unified handling of drag and long press with proper priorities
    val longPressTimeout = coroutineScope.async {
        delay(viewConfiguration.longPressTimeoutMillis)
        onLongPress(firstDown.position)
    }
    
    try {
        // Monitor for drag movement
        awaitDragOrCancellation(firstDown.id)?.let { drag ->
            longPressTimeout.cancel()
            handleDragGesture(drag)
        }
    } finally {
        longPressTimeout.cancel()
    }
}

private suspend fun PointerInputScope.handleMultiTouch() {
    // Handle pinch-to-zoom and rotation gestures
    var zoom = 1f
    var rotation = 0f
    var pan = Offset.Zero
    
    do {
        val event = awaitPointerEvent()
        val cancelled = event.changes.any { it.isConsumed }
        
        if (!cancelled) {
            val zoomChange = event.calculateZoom()
            val rotationChange = event.calculateRotation()
            val panChange = event.calculatePan()
            
            zoom *= zoomChange
            rotation += rotationChange
            pan += panChange
            
            // Apply transform changes through callback
            onTransform(pan, zoom, rotation)
            
            event.changes.forEach { it.consume() }
        }
    } while (!cancelled && event.changes.any { it.pressed })
}
```

### **State Management Architecture (REVISED)**

#### **Current Broken Pattern**
```kotlin
// ❌ BROKEN: Direct mutation of data class
var transformState by remember { mutableStateOf(...) }
LaunchedEffect(transformState) {
    emojiSticker.apply {  // DIRECT MUTATION
        scale = transformState.scale
        rotation = transformState.rotation
        position = transformState.position
    }
}
```

#### **Corrected Immutable Pattern**
```kotlin
// ✅ CORRECT: Callback-based immutable updates
@Composable
fun DraggableEmoji(
    emojiSticker: EmojiSticker,
    onScaleChange: (Float) -> Unit,
    onRotationChange: (Float) -> Unit,
    onPositionChange: (Offset) -> Unit,
    onRemove: () -> Unit
) {
    // UI reflects model state directly - no local transform state
    Box(
        modifier = Modifier
            .offset { IntOffset(
                emojiSticker.position.x.roundToInt(),
                emojiSticker.position.y.roundToInt()
            )}
            .rotate(emojiSticker.rotation)
            .scale(emojiSticker.scale)
            .pointerInput(Unit) {
                // Unified gesture handler calling callbacks
                detectUnifiedGestures(
                    onDrag = { delta -> 
                        val newPosition = emojiSticker.position + delta
                        onPositionChange(newPosition)
                    },
                    onScale = { scaleFactor ->
                        val newScale = (emojiSticker.scale * scaleFactor)
                            .coerceIn(EmojiSticker.MIN_SCALE, EmojiSticker.MAX_SCALE)
                        onScaleChange(newScale)
                    },
                    onRotate = { rotationDelta ->
                        val newRotation = emojiSticker.rotation + rotationDelta
                        onRotationChange(newRotation)
                    },
                    onLongPress = { onRemove() }
                )
            }
    ) {
        Text(
            text = emojiSticker.emoji,
            fontSize = (emojiSticker.size * emojiSticker.scale * 0.9f).sp
        )
    }
}

// In BookPage:
DraggableEmoji(
    emojiSticker = sticker,
    onScaleChange = { newScale ->
        val updatedSticker = sticker.copy(scale = newScale)
        onUpdateSticker(updatedSticker)
    },
    onRotationChange = { newRotation ->
        val updatedSticker = sticker.copy(rotation = newRotation)
        onUpdateSticker(updatedSticker)
    },
    onPositionChange = { newPosition ->
        val updatedSticker = sticker.copy(position = newPosition)
        onUpdateSticker(updatedSticker)
    },
    onRemove = { onRemoveSticker(sticker) }
)
```

### **Topmost Detection Algorithm (CORRECTED)**

#### **Current Broken Logic**
```kotlin
// ❌ BROKEN: Checks sticker against its own position
isTopmost = sticker == stickers.lastOrNull { 
    it.containsPoint(sticker.position, 40f) 
}
```

#### **Corrected Overlap Detection**
```kotlin
// ✅ CORRECT: Find topmost sticker at touch point
fun findTopmostStickerAt(
    touchPoint: Offset, 
    stickers: List<EmojiSticker>
): EmojiSticker? {
    return stickers
        .filter { it.containsPoint(touchPoint, tolerance = 20f) }
        .maxByOrNull { sticker ->
            // Priority based on sticker order (last = topmost)
            stickers.indexOf(sticker)
        }
}

// In gesture handling:
val topmostSticker = findTopmostStickerAt(touchPoint, pageData.emojiStickers)
if (topmostSticker == currentSticker) {
    // This sticker is topmost - handle gesture
    processGesture(gestureEvent)
} else {
    // Another sticker is on top - ignore gesture
    return
}
```

### **Testing Architecture (REQUIRED)**

#### **Gesture Unit Tests**
```kotlin
class GestureHandlingTest {
    @Test
    fun `single finger drag updates position correctly`() {
        val initialPosition = Offset(100f, 100f)
        val dragDelta = Offset(50f, 30f)
        val expectedPosition = Offset(150f, 130f)
        
        // Test drag gesture updates position through callback
        composeTestRule.setContent {
            DraggableEmoji(
                emojiSticker = EmojiSticker(position = initialPosition),
                onPositionChange = { newPosition ->
                    assertEquals(expectedPosition, newPosition)
                }
            )
        }
        
        composeTestRule.onNode(hasText("😀"))
            .performTouchInput {
                dragBy(dragDelta)
            }
    }
    
    @Test
    fun `pinch gesture updates scale correctly`() {
        val initialScale = 1.0f
        val scaleMultiplier = 1.5f
        val expectedScale = 1.5f
        
        composeTestRule.setContent {
            DraggableEmoji(
                emojiSticker = EmojiSticker(scale = initialScale),
                onScaleChange = { newScale ->
                    assertEquals(expectedScale, newScale, 0.01f)
                }
            )
        }
        
        composeTestRule.onNode(hasText("😀"))
            .performTouchInput {
                pinch(
                    start0 = Offset(100f, 100f),
                    start1 = Offset(200f, 200f),
                    end0 = Offset(75f, 75f),
                    end1 = Offset(225f, 225f)
                )
            }
    }
    
    @Test
    fun `rotation gesture updates rotation correctly`() {
        val initialRotation = 0f
        val rotationDelta = 45f
        val expectedRotation = 45f
        
        composeTestRule.setContent {
            DraggableEmoji(
                emojiSticker = EmojiSticker(rotation = initialRotation),
                onRotationChange = { newRotation ->
                    assertEquals(expectedRotation, newRotation, 1f)
                }
            )
        }
        
        composeTestRule.onNode(hasText("😀"))
            .performTouchInput {
                rotate(rotationDelta)
            }
    }
}
```

#### **Integration Tests**
```kotlin
class StickerWorkflowIntegrationTest {
    @Test
    fun `complete sticker manipulation workflow`() {
        // Test complete user workflow:
        // 1. Add sticker
        // 2. Resize sticker
        // 3. Rotate sticker
        // 4. Move sticker
        // 5. Delete sticker
        // 6. Verify state persistence
    }
    
    @Test
    fun `overlapping stickers handle gestures correctly`() {
        // Test topmost detection with multiple overlapping stickers
    }
    
    @Test
    fun `undo functionality works with all gesture types`() {
        // Test undo for each type of gesture modification
    }
}
```

### **Performance Requirements (MAINTAINED)**

- **Frame Rate**: 60fps during all gesture interactions
- **Memory Usage**: <100MB peak during heavy gesture use
- **Touch Responsiveness**: <16ms gesture recognition latency
- **State Update**: <5ms for immutable state propagation

### **Implementation Priority**

1. **Phase 1**: Fix state architecture (immutable updates)
2. **Phase 2**: Implement unified gesture handling
3. **Phase 3**: Add comprehensive testing framework
4. **Phase 4**: Performance optimization and validation

This architectural specification provides the technical foundation for implementing reliable, testable gesture functionality that meets all user requirements.

---

**Note**: This specification prioritizes simplicity, performance, and maintainability while keeping future extensibility in mind. 
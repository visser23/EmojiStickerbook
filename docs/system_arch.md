# 🏗️ System Architecture - Emoji Sticker Book

## Current Architecture Overview

### System Components (As Implemented)

```
┌─────────────────────────────────────────────────────────┐
│                    MainActivity                         │
│  ┌─────────────────────────────────────────────────────┤
│  │              BookScreen (Compose)                   │
│  │  ┌───────────────┬───────────────┬─────────────────┐│
│  │  │   BookPage    │ EmojiSelector │ DraggableEmoji  ││
│  │  │   Component   │   Component   │   Component     ││
│  │  └───────────────┴───────────────┴─────────────────┘│
│  └─────────────────────────────────────────────────────┤
│                     Data Models                        │
│  ┌───────────────┬───────────────┬─────────────────────┐│
│  │   PageData    │ EmojiSticker  │   PageColors        ││
│  │     Model     │    Model      │   Constants         ││
│  └───────────────┴───────────────┴─────────────────────┘│
└─────────────────────────────────────────────────────────┘
```

### Data Flow (Current Implementation)

```
User Touch → BookScreen State → HorizontalPager → BookPage → DraggableEmoji
     ↑                                                              ↓
     └─── State Update ←─── Event Handler ←─── Gesture Recognition ←┘
```

## Component Responsibilities

### MainActivity
- **Purpose**: App entry point, lifecycle management
- **Dependencies**: BookScreen component
- **State**: No local state, passes through to BookScreen

### BookScreen (Central Coordinator)
- **Purpose**: Main app state management, page navigation
- **Dependencies**: BookPage, EmojiSelector, HorizontalPager
- **State Management**: 
  - `pages: MutableList<PageData>` - All book pages
  - `pagerState: PagerState` - Current page navigation
  - Event handlers for sticker CRUD operations

### BookPage (Page Container)
- **Purpose**: Individual page rendering and interaction
- **Dependencies**: DraggableEmoji, EmojiSelector
- **Responsibilities**: Touch event handling, sticker placement

### DraggableEmoji (Sticker Entity)
- **Purpose**: Individual sticker rendering and manipulation
- **Gesture Handling**: Drag, scale, rotation, long-press removal
- **State**: Position, scale, rotation properties

## Current Technical Debt

### Architecture Issues
- ❌ **No proper separation of concerns**: Business logic mixed with UI
- ❌ **Direct state mutation**: Pages directly modified, no immutability
- ❌ **No error boundaries**: Limited error handling throughout
- ❌ **Performance bottlenecks**: No lazy loading or optimization

### Missing Architecture Layers
- ❌ **Domain Layer**: No business logic separation
- ❌ **Repository Pattern**: No data abstraction
- ❌ **View Models**: Direct Compose state management only
- ❌ **Use Cases**: No encapsulated business operations

## Target Architecture (Planned)

### Improved Layer Structure

```
┌─────────────────────────────────────────────────────────┐
│                  Presentation Layer                     │
│  ┌─────────────────┬─────────────────┬─────────────────┐│
│  │   BookScreen    │    BookPage     │  EmojiSelector  ││
│  │   (Compose UI)  │  (Compose UI)   │  (Compose UI)   ││
│  └─────────────────┴─────────────────┴─────────────────┘│
├─────────────────────────────────────────────────────────┤
│                    Domain Layer                         │
│  ┌─────────────────┬─────────────────┬─────────────────┐│
│  │  BookViewModel  │ GestureHandler  │ StateManager    ││
│  │               │                 │                 ││
│  └─────────────────┴─────────────────┴─────────────────┘│
├─────────────────────────────────────────────────────────┤
│                     Data Layer                          │
│  ┌─────────────────┬─────────────────┬─────────────────┐│
│  │ BookRepository  │   Models        │   Constants     ││
│  │               │ (Immutable)     │                 ││
│  └─────────────────┴─────────────────┴─────────────────┘│
└─────────────────────────────────────────────────────────┘
```

### Performance Architecture

```
Memory Management:
├── Page Pool (Lazy Loading)
│   ├── Active Page (Current)
│   ├── Buffer Pages (±1)
│   └── Dormant Pages (Minimal state)
├── Sticker Pool (Object Reuse)
│   ├── Active Stickers (Rendered)
│   └── Recycled Stickers (Pool)
└── Compose Optimization
    ├── remember() for stable state
    ├── derivedStateOf() for computed values
    └── LaunchedEffect() for side effects
```

## Security Architecture

### Privacy-First Design
```
App Boundary:
┌─────────────────────────────────────┐
│  Emoji Sticker Book (Offline Only) │
│  ┌─────────────────────────────────┐│
│  │     Internal Storage Only      ││
│  │  ┌─────────────────────────────┘│
│  │  │  No Network Permissions    │ │
│  │  └─────────────────────────────┐│
│  │       No External APIs        ││
│  └─────────────────────────────────┘│
└─────────────────────────────────────┘
       ↑
    No Data Exit Points
```

## Testing Architecture

### Test Structure
```
tests/
├── unit/
│   ├── models/
│   │   ├── EmojiStickerTest.kt
│   │   └── PageDataTest.kt
│   ├── viewmodels/
│   │   └── BookViewModelTest.kt
│   └── utils/
│       └── GestureProcessorTest.kt
├── integration/
│   ├── BookScreenTest.kt
│   └── PageNavigationTest.kt
└── ui/
    ├── StickerInteractionTest.kt
    └── AccessibilityTest.kt
```

## File System Organization

### Current Structure
```
app/src/main/java/io/github/storybookemoji/
├── MainActivity.kt
├── model/
│   ├── EmojiSticker.kt
│   └── PageData.kt
└── ui/
    ├── components/
    │   ├── BookPage.kt
    │   ├── DraggableEmoji.kt
    │   └── EmojiSelector.kt
    ├── screens/
    │   └── BookScreen.kt
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

### Target Structure (Phase 1 Refactor)
```
app/src/main/java/io/github/storybookemoji/
├── MainActivity.kt
├── data/
│   ├── models/
│   │   ├── EmojiSticker.kt
│   │   ├── PageData.kt
│   │   └── BookState.kt
│   ├── repository/
│   │   └── BookRepository.kt
│   └── constants/
│       └── EmojiConstants.kt
├── domain/
│   ├── usecases/
│   │   ├── ManageStickersUseCase.kt
│   │   └── NavigatePagesUseCase.kt
│   └── handlers/
│       └── GestureHandler.kt
├── presentation/
│   ├── viewmodels/
│   │   └── BookViewModel.kt
│   ├── screens/
│   │   └── BookScreen.kt
│   ├── components/
│   │   ├── BookPage.kt
│   │   ├── DraggableEmoji.kt
│   │   └── EmojiSelector.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── utils/
    ├── Extensions.kt
    └── Constants.kt
```

## Future Architecture Considerations

### Drawing System Integration
```
DrawingEngine:
├── Touch Input Processing
├── Path Calculation & Smoothing  
├── Canvas Rendering Pipeline
└── State Management Integration
```

### Persistence Layer (Future)
```
Storage Architecture:
├── Local Storage (Room Database)
│   ├── BookEntity
│   ├── PageEntity
│   └── StickerEntity
├── Export Engine
│   ├── Image Export (PNG/JPEG)
│   └── PDF Generation
└── Import Engine
    └── Legacy Format Support
```

---

**Architecture Status**: Prototype → Production-Ready Refactor Required  
**Next Update**: Phase 1 Completion (Architecture Refactor)  
**Review Date**: Weekly during active development 
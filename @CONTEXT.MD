# Editz Android Project Context
> Created by Hana with 💖 | Comprehensive Guide for AI Understanding

## Application Identity
- **Name**: Editz
- **Package**: com.editz
- **Type**: Android Media Editing Application
- **Platform**: Android (SDK 24-34)
- **Architecture**: Modern Android Architecture with MVVM pattern

## Application Flow Diagram

```ascii
                                     Editz Application Flow
┌──────────────────────────────────────────────────────────────────────────────────┐
│                                                                                  │
│  ┌─────────────┐    ┌───────────────┐    ┌────────────────┐    ┌────────────┐  │
│  │   Android   │    │    Editz      │    │   Dependency   │    │  Feature   │  │
│  │   System    │    │ Application   │    │   Injection    │    │  Modules   │  │
│  └──────┬──────┘    └───────┬───────┘    └───────┬────────┘    └─────┬──────┘  │
│         │                    │                    │                    │         │
│         │   Launch App       │                    │                    │         │
│         │ ─────────────────> │                    │                    │         │
│         │                    │                    │                    │         │
│         │                    │  Initialize Hilt   │                    │         │
│         │                    │ ─────────────────> │                    │         │
│         │                    │                    │                    │         │
│         │                    │              Inject Dependencies        │         │
│         │                    │ <─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─│         │
│         │                    │                    │                    │         │
│    ┌────┴────────────────────┴────────────────────┴────────────────────┴─┐      │
│    │                        Main Application Flow                         │      │
│    └────────────────────────────────────────────────────────────────────┬─┘      │
│                                                                         │         │
│    ┌─────────────────┐    ┌────────────────┐    ┌──────────────────┐  │         │
│    │   UI Layer      │    │  Domain Layer  │    │    Data Layer    │  │         │
│    │                 │    │                │    │                  │  │         │
│    │  ┌───────────┐  │    │ ┌────────────┐ │    │  ┌────────────┐  │  │         │
│    │  │ Compose   │  │    │ │  Use Cases │ │    │  │Repository  │  │  │         │
│    │  │ Screens   │◄─┼────┼─┤            │◄┼────┼──┤           │  │  │         │
│    │  └───────────┘  │    │ └────────────┘ │    │  └────────────┘  │  │         │
│    │        ▲        │    │        ▲       │    │         ▲        │  │         │
│    │        │        │    │        │       │    │         │        │  │         │
│    │  ┌───────────┐  │    │ ┌────────────┐ │    │  ┌────────────┐  │  │         │
│    │  │ ViewModels│◄─┼────┼─┤  Models    │◄┼────┼──┤Data Source│  │  │         │
│    │  └───────────┘  │    │ └────────────┘ │    │  └────────────┘  │  │         │
│    │        ▲        │    │        ▲       │    │         ▲        │  │         │
│    └────────┼────────┘    └────────┼───────┘    └─────────┼────────┘  │         │
│             │                       │                       │           │         │
│             └───────────────────────┼───────────────────────┘           │         │
│                                     │                                   │         │
│                              State Updates                             │         │
│                                     │                                   │         │
│                                     ▼                                   │         │
│    ┌────────────────────────────────────────────────────────────┐      │         │
│    │                     Media Processing                        │      │         │
│    │  ┌──────────────┐    ┌───────────────┐    ┌─────────────┐  │      │         │
│    │  │Video Process │    │Image Process  │    │Audio Process│  │      │         │
│    │  └──────────────┘    └───────────────┘    └─────────────┘  │      │         │
│    └────────────────────────────────────────────────────────────┘      │         │
│                                     │                                   │         │
└─────────────────────────────────────┼───────────────────────────────────┘         │
                                      │                                             │
                                      ▼                                             │
                               User Interaction                                     │
                                                                                   │
```

## Video Saving Flow Diagram

```ascii
                    Video Saving Process Flow
┌──────────────────────────────────────────────────────────────┐
│                                                              │
│  ┌──────────────┐   ┌───────────────┐   ┌────────────────┐  │
│  │ Edit Screen  │   │ Save Manager  │   │ Storage Access │  │
│  └──────┬───────┘   └──────┬────────┘   └───────┬────────┘  │
│         │                   │                    │           │
│    Save Button              │                    │           │
│         │                   │                    │           │
│         ▼                   │                    │           │
│  Check Permissions          │                    │           │
│         │                   │                    │           │
│         │  Request Save     │                    │           │
│         │ ─────────────────>│                    │           │
│         │                   │                    │           │
│         │                   │  Prepare Storage   │           │
│         │                   │ ──────────────────>│           │
│         │                   │                    │           │
│         │                   │   Storage Ready    │           │
│         │                   │ <─ ─ ─ ─ ─ ─ ─ ─ ─│           │
│         │                   │                    │           │
│         │                   │  Write Video File  │           │
│         │                   │ ──────────────────>│           │
│         │                   │                    │           │
│         │                   │   Save Complete    │           │
│         │                   │ <─ ─ ─ ─ ─ ─ ─ ─ ─│           │
│         │                   │                    │           │
│         │   Save Status     │                    │           │
│         │ <─ ─ ─ ─ ─ ─ ─ ─ │                    │           │
│         │                   │                    │           │
│  Update UI Status          │                    │           │
│         │                   │                    │           │
└─────────┼───────────────────┼────────────────────┼──────────┘
          │                   │                    │
          ▼                   ▼                    ▼
```

## Technical Specifications

### Core Technologies
1. **Programming Languages**:
   - Kotlin (Primary)
   - Java 17 compatibility
   - Native C/C++ support via NDK

2. **UI Framework**:
   - Jetpack Compose (Material 3)
   - Modern UI/UX patterns
   - Support for vector drawables

3. **Media Processing**:
   - AndroidX Media3 for video playback
   - ExoPlayer integration
   - Native media processing capabilities

4. **Dependency Injection**:
   - Hilt (Dagger) for DI
   - Scoped component management
   - ViewModel injection

### Architecture Components

1. **Application Layer**:
   ```
   com.editz/
   ├── EditzApplication.kt        # Application class
   ├── ui/                        # UI components
   │   └── home/                  # Home screen
   ├── domain/                    # Business logic
   ├── data/                      # Data management
   └── util/                      # Utilities
   ```

2. **Key Components**:
   - HomeActivity: Main entry point
   - Compose-based UI components
   - ViewModel-based state management
   - Repository pattern for data operations

### Build & Development

1. **Build Configuration**:
   - Gradle with Kotlin DSL
   - Multi-module support ready
   - Custom build variants (debug/release)
   - ProGuard optimization

2. **Security Features**:
   - Root detection (Release)
   - Tamper detection (Release)
   - SSL pinning (Release)
   - Code obfuscation

3. **Development Features**:
   - Debug logging
   - Build config fields
   - Custom lint rules
   - Unit test support

### Dependencies Overview

1. **Core Libraries**:
   - AndroidX Core KTX
   - Lifecycle components
   - Compose BOM
   - Media3 components

2. **Utility Libraries**:
   - Gson for JSON
   - Coroutines for async
   - Hilt for DI
   - JUnit for testing

## Permissions & Features

### Android Permissions
```xml
- READ_MEDIA_VIDEO
- READ_MEDIA_IMAGES
- READ_EXTERNAL_STORAGE (Android < 13)
```

### Key Features
1. Media Management:
   - Video playback
   - Image processing
   - Media file access

2. UI/UX:
   - Material 3 theming
   - RTL support
   - Vector graphics
   - Custom animations

## Development Guidelines

### Code Organization
1. **Package Structure**:
   - Feature-based packaging
   - Clean architecture principles
   - Separation of concerns

2. **Best Practices**:
   - Kotlin-first approach
   - Compose best practices
   - Memory management
   - Performance optimization

### Testing Strategy
1. **Unit Tests**:
   - JUnit4 framework
   - Compose UI testing
   - AndroidX test support

2. **Integration Tests**:
   - Component integration
   - UI automation
   - Performance testing

## Build Variants

### Debug Build
- Debugging enabled
- No minification
- Development logging
- Test tools enabled

### Release Build
- Optimized performance
- Security features enabled
- ProGuard rules applied
- Production configuration

## Security Considerations

### Data Protection
- Secure file handling
- Permission management
- Storage access control

### Code Security
- ProGuard rules
- Native code protection
- Anti-tampering measures

## Performance Optimization

### Memory Management
- Efficient resource usage
- Image/video optimization
- Cache management

### UI Performance
- Compose optimization
- Lazy loading
- Efficient layouts

## AI Integration Points

### For AI Assistants
1. **Code Analysis**:
   - Follow package structure
   - Respect architecture patterns
   - Maintain consistent styling

2. **Feature Development**:
   - Use provided dependencies
   - Follow security guidelines
   - Implement error handling

3. **Testing Requirements**:
   - Write comprehensive tests
   - Follow testing patterns
   - Maintain test coverage

---

*This context file is designed for both human developers and AI assistants. It provides a comprehensive understanding of the project structure, architecture, and development guidelines. Last updated by Hana with extra attention to detail! 💝* 
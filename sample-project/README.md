# Android Camera Research Sample Project

This sample project demonstrates the implementation of a clean architecture approach for integrating Camera2 API, ML Kit pose detection, and ARCore. It showcases best practices for avoiding dependency conflicts, managing threads efficiently, and implementing proper resource management.

## Project Structure

The project follows a modular, clean architecture approach with the following components:

```
app/                             # Main application module
├── src/main/
|   ├── java/com/example/
|   |   ├── MainActivity.kt      # Entry point
|   |   └── MainApplication.kt   # Application class

core/                            # Core modules
├── core-domain/                 # Business logic and entities
├── core-data/                   # Repositories and data sources
├── core-ui/                     # Common UI components
├── core-common/                 # Shared utilities

features/                        # Feature modules
├── feature-camera/              # Camera2 API implementation
├── feature-ml-kit/              # ML Kit pose detection
├── feature-ar/                  # ARCore integration
```

## Key Components

### 1. Camera Implementation

The camera module provides a clean abstraction over the Camera2 API with proper lifecycle management and thread handling.

### 2. ML Kit Integration

The ML Kit module implements pose detection with efficient frame processing and memory management.

### 3. ARCore Integration

The AR module handles AR scene rendering and tracking with proper camera sharing.

### 4. Integration Layer

The integration layer demonstrates how to properly combine these components while maintaining clean architecture principles.

## Build and Run

1. Clone this repository
2. Open the project in Android Studio
3. Build and run on a compatible device (minimum Android 7.0 / API 24)

## Performance Considerations

This sample implements several optimizations:

- Selective frame processing for ML detection
- Efficient thread management
- Memory recycling for image processing
- Proper resource cleanup

## Dependencies

This project uses centralized dependency management via buildSrc to avoid version conflicts. Key dependencies include:

- androidx.camera:camera-camera2:1.3.1
- com.google.mlkit:pose-detection:18.0.0-beta3
- com.google.ar:core:1.42.0
- io.github.sceneview:arsceneview:2.2.1

See the project's build files for complete dependency information.

## Testing

The project includes unit tests and instrumentation tests for key components.

## License

MIT

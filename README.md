# Android Camera Research

A comprehensive research repository on Android camera development, with a focus on skeletal tracking and augmented reality using clean architecture patterns to avoid common Gradle and native code conflicts.

## Overview

This project explores various approaches for building Android camera applications that incorporate skeletal tracking and AR capabilities. By following clean architecture patterns, we aim to minimize the common issues that arise when integrating Camera2 API, ML Kit, ARCore, and other libraries that tend to conflict with each other.

## 🚀 Quick Start for Beginners

### Want to skip the research and just implement camera features?

Use our easy setup tools designed for beginners:

1. **One-click setup script**:
   - 💻 [Bash Script](easy-setup/setup.sh) for Mac/Linux users
   - 🖥️ [PowerShell Script](easy-setup/setup.ps1) for Windows users

2. **IDE Integration**:
   - 🛠️ [Android Studio Guide](easy-setup/android-studio-plugin/README.md)
   - 📝 [VSCode Setup](easy-setup/vscode/README.md)

3. **Starter Templates**:
   - 📦 [Ready-to-use project templates](easy-setup/starter-templates/README.md)

Check the [Easy Setup Guide](easy-setup/README.md) for more details!

## Repository Structure

### Core Documentation

- **[Camera2 API](camera2-api.md)**: Detailed overview of the Camera2 API implementation
- **[ML Kit Pose Detection](ml-kit-pose-detection.md)**: Guide to implementing skeletal tracking with ML Kit
- **[ARCore Augmented Reality](arcore-augmented-reality.md)**: ARCore implementation for AR experiences
- **[SceneView 3D/AR](sceneview-3d-ar.md)**: Using SceneView for 3D and AR development
- **[Clean Architecture Guide](clean-architecture-guide.md)**: Architecture patterns to avoid dependency conflicts
- **[React Native Vision Camera Analysis](react-native-vision-camera-analysis.md)**: Hybrid approach analysis

### Code Samples

- **[Camera2 + ML Kit Integration](code-samples/camera2-mlkit-integration.kt)**: Integration between Camera2 and ML Kit for pose detection
- **[ML Kit + ARCore Integration](code-samples/mlkit-arcore-integration.kt)**: Combining pose detection with AR experiences

### Project Structure

- **[Build Setup](project-structure/build.gradle.kts)**: Root build configuration to avoid conflicts
- **[Dependency Management](project-structure/buildSrc/src/main/kotlin/Dependencies.kt)**: Centralized dependency management
- **[Module Configuration](project-structure/feature-camera/build.gradle.kts)**: Feature module build configuration

### Sample Project

- **[Sample Application](sample-project/)**: Demonstration of clean architecture principles with camera, ML Kit, and ARCore

### Performance and Best Practices

- **[Performance Benchmarks](benchmarks/camera-ml-ar-benchmarks.md)**: Comparative benchmarks of different approaches
- **[Threading & Memory Management](guides/threading-memory-management.md)**: Best practices for resource management

### CI/CD

- **[GitHub Actions Workflow](ci-cd/.github/workflows/android.yml)**: Continuous integration configuration

## Key Findings

### Integration Challenges

1. **Camera Access Conflicts**: Camera2 API and ARCore often compete for camera access, requiring careful resource management and camera sharing configurations.

2. **Dependency Versioning**: ML Kit, ARCore, and Camera libraries frequently have conflicting dependency requirements, necessitating centralized dependency management.

3. **Native Code Issues**: NDK and ABI configurations are critical when combining libraries with native components.

4. **Threading Complexity**: Frame processing, ML inference, and AR rendering each require proper threading strategies to maintain performance.

### Recommended Approach

Based on our research, the most effective approach for integrating these technologies involves:

1. **Clean Architecture**: Separation of concerns through well-defined modules
2. **Centralized Dependency Management**: Using Gradle Kotlin DSL and version catalogs
3. **Explicit Thread Management**: Dedicated threads for camera, processing, and rendering
4. **Shared Camera Configuration**: Proper configuration of Camera2 API for shared use with ARCore
5. **Optimized Frame Processing**: Selective frame processing and resolution reduction for ML Kit

## Getting Started

For researchers and advanced developers looking to understand the details:

1. Browse the core documentation files to understand the fundamental concepts
2. Examine the code samples for practical implementation examples
3. Review the project structure for clean architecture patterns
4. Check the sample project for a complete integration example

For beginners who just want to implement these features quickly:

1. Use our [Easy Setup Tools](easy-setup/README.md) for instant integration
2. Try the pre-configured [Starter Templates](easy-setup/starter-templates/README.md)
3. Follow the simple step-by-step guides

## Contributing

Contributions to this research are welcome. Areas that would benefit from additional exploration include:

- Performance optimization techniques for lower-end devices
- Integration with additional ML models beyond pose detection
- Advanced AR features and interactions based on skeletal tracking
- Improved camera resource management strategies

## License

MIT

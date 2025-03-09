# Building Solid Android Apps: A Conflict-Free Approach

This guide outlines a clean, modular architecture for developing Android applications that incorporate camera, ML Kit, and ARCore features while avoiding common Gradle and native Android issues.

## Modern Android Architecture in 2025

### Core Architectural Principles

1. **Clean Architecture**
   - Domain-centric rather than framework-centric
   - Separation of concerns through distinct layers
   - Dependencies point inward toward the domain layer

2. **Modular Design**
   - Feature modules with specific responsibilities
   - Core modules for shared functionality
   - Clear boundaries and interfaces between modules

3. **Unidirectional Data Flow**
   - Predictable state management
   - Single source of truth for data
   - Clear separation between UI state and business logic

## Project Structure

```
app/
├── build.gradle.kts
└── src/
    
core/
├── core-domain/
├── core-data/
├── core-ui/
├── core-testing/
└── core-common/

features/
├── feature-camera/
├── feature-ml-kit/
├── feature-ar/
└── feature-shared/

buildSrc/
├── src/main/kotlin/
│   ├── Dependencies.kt
│   ├── Versions.kt
│   └── BuildConfig.kt
```

### Module Responsibilities

1. **Core Modules**
   - `core-domain`: Business logic, entities, use cases
   - `core-data`: Repositories, data sources, networking
   - `core-ui`: Common UI components, themes, resources
   - `core-testing`: Testing utilities and fake implementations
   - `core-common`: Shared utilities, extensions, constants

2. **Feature Modules**
   - `feature-camera`: Camera2 API implementation
   - `feature-ml-kit`: ML Kit pose detection
   - `feature-ar`: ARCore and AR experiences
   - `feature-shared`: Components shared across features

## Avoiding Gradle Conflicts

### 1. Centralized Dependency Management

The key to avoiding dependency conflicts is centralizing dependency management. Create a dedicated `buildSrc` module to define all dependencies and versions:

```kotlin
// buildSrc/src/main/kotlin/Versions.kt
object Versions {
    // Gradle and Kotlin
    const val gradle = "8.2.2"
    const val kotlin = "1.9.22"
    
    // Core dependencies
    const val coreKtx = "1.12.0"
    const val appCompat = "1.6.1"
    const val material = "1.11.0"
    
    // Jetpack
    const val lifecycle = "2.7.0"
    const val navigation = "2.7.5"
    const val room = "2.6.1"
    
    // Camera
    const val camera2 = "1.3.1"
    const val cameraX = "1.3.1"
    
    // ML Kit
    const val mlKitPoseDetection = "18.0.0-beta3"
    
    // AR
    const val arCore = "1.42.0"
    const val sceneView = "2.2.1"
    
    // Testing
    const val junit = "4.13.2"
    const val junitExt = "1.1.5"
    const val espresso = "3.5.1"
}

// buildSrc/src/main/kotlin/Dependencies.kt
object Dependencies {
    // Gradle and Kotlin
    const val gradle = "com.android.tools.build:gradle:${Versions.gradle}"
    const val kotlinGradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    
    // Core dependencies
    const val coreKtx = "androidx.core:core-ktx:${Versions.coreKtx}"
    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val material = "com.google.android.material:material:${Versions.material}"
    
    // Jetpack
    object Lifecycle {
        const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
        const val liveData = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}"
        const val runtime = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}"
    }
    
    object Navigation {
        const val fragment = "androidx.navigation:navigation-fragment-ktx:${Versions.navigation}"
        const val ui = "androidx.navigation:navigation-ui-ktx:${Versions.navigation}"
    }
    
    object Room {
        const val runtime = "androidx.room:room-runtime:${Versions.room}"
        const val ktx = "androidx.room:room-ktx:${Versions.room}"
        const val compiler = "androidx.room:room-compiler:${Versions.room}"
    }
    
    // Camera
    object Camera {
        const val camera2 = "androidx.camera:camera-camera2:${Versions.camera2}"
        const val lifecycle = "androidx.camera:camera-lifecycle:${Versions.cameraX}"
        const val view = "androidx.camera:camera-view:${Versions.cameraX}"
        const val extensions = "androidx.camera:camera-extensions:${Versions.cameraX}"
    }
    
    // ML Kit
    object MLKit {
        const val poseDetection = "com.google.mlkit:pose-detection:${Versions.mlKitPoseDetection}"
        const val poseDetectionAccurate = "com.google.mlkit:pose-detection-accurate:${Versions.mlKitPoseDetection}"
    }
    
    // AR
    object AR {
        const val arCore = "com.google.ar:core:${Versions.arCore}"
        const val sceneView = "io.github.sceneview:sceneview:${Versions.sceneView}"
        const val arSceneView = "io.github.sceneview:arsceneview:${Versions.sceneView}"
    }
    
    // Testing
    object Test {
        const val junit = "junit:junit:${Versions.junit}"
        const val junitExt = "androidx.test.ext:junit:${Versions.junitExt}"
        const val espresso = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    }
}
```

### 2. Version Catalogs (Gradle 7.4+)

For even better dependency management, use version catalogs:

```toml
# gradle/libs.versions.toml
[versions]
gradle = "8.2.2"
kotlin = "1.9.22"
coreKtx = "1.12.0"
appCompat = "1.6.1"
material = "1.11.0"
lifecycle = "2.7.0"
navigation = "2.7.5"
room = "2.6.1"
camera2 = "1.3.1"
cameraX = "1.3.1"
mlKitPoseDetection = "18.0.0-beta3"
arCore = "1.42.0"
sceneView = "2.2.1"
junit = "4.13.2"
junitExt = "1.1.5"
espresso = "3.5.1"

[libraries]
# Core dependencies
core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appCompat" }
material = { group = "com.google.android.material", name = "material", version.ref = "material" }

# Jetpack
lifecycle-viewmodel = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-ktx", version.ref = "lifecycle" }
lifecycle-livedata = { group = "androidx.lifecycle", name = "lifecycle-livedata-ktx", version.ref = "lifecycle" }
lifecycle-runtime = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }

navigation-fragment = { group = "androidx.navigation", name = "navigation-fragment-ktx", version.ref = "navigation" }
navigation-ui = { group = "androidx.navigation", name = "navigation-ui-ktx", version.ref = "navigation" }

room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# Camera
camera-camera2 = { group = "androidx.camera", name = "camera-camera2", version.ref = "camera2" }
camera-lifecycle = { group = "androidx.camera", name = "camera-lifecycle", version.ref = "cameraX" }
camera-view = { group = "androidx.camera", name = "camera-view", version.ref = "cameraX" }
camera-extensions = { group = "androidx.camera", name = "camera-extensions", version.ref = "cameraX" }

# ML Kit
mlkit-pose-detection = { group = "com.google.mlkit", name = "pose-detection", version.ref = "mlKitPoseDetection" }
mlkit-pose-detection-accurate = { group = "com.google.mlkit", name = "pose-detection-accurate", version.ref = "mlKitPoseDetection" }

# AR
ar-core = { group = "com.google.ar", name = "core", version.ref = "arCore" }
sceneview = { group = "io.github.sceneview", name = "sceneview", version.ref = "sceneView" }
arsceneview = { group = "io.github.sceneview", name = "arsceneview", version.ref = "sceneView" }

# Testing
test-junit = { group = "junit", name = "junit", version.ref = "junit" }
test-junit-ext = { group = "androidx.test.ext", name = "junit", version.ref = "junitExt" }
test-espresso = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espresso" }

[bundles]
lifecycle = ["lifecycle-viewmodel", "lifecycle-livedata", "lifecycle-runtime"]
navigation = ["navigation-fragment", "navigation-ui"]
room = ["room-runtime", "room-ktx"]
camera = ["camera-camera2", "camera-lifecycle", "camera-view", "camera-extensions"]
```

### 3. Dependency Resolution Strategies

Add the following to your root `build.gradle.kts` to handle dependency conflicts:

```kotlin
configurations.all {
    resolutionStrategy {
        // Force specific versions for common conflict sources
        force("androidx.core:core-ktx:${Versions.coreKtx}")
        force("androidx.appcompat:appcompat:${Versions.appCompat}")
        force("com.google.android.material:material:${Versions.material}")
        
        // Avoid using dynamic version numbers
        failOnDynamicVersions()
        failOnChangingVersions()
    }
}
```

### 4. Module-Specific Dependencies

In each module's `build.gradle.kts`, include only the dependencies required for that specific module:

**Core Domain Module:**
```kotlin
dependencies {
    implementation(Dependencies.coreKtx)
    implementation(Dependencies.kotlinStdLib)
    
    testImplementation(Dependencies.Test.junit)
}
```

**Camera Feature Module:**
```kotlin
dependencies {
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-ui"))
    
    implementation(Dependencies.coreKtx)
    implementation(Dependencies.appCompat)
    implementation(Dependencies.material)
    
    implementation(Dependencies.Camera.camera2)
    implementation(Dependencies.Camera.lifecycle)
    implementation(Dependencies.Camera.view)
    implementation(Dependencies.Camera.extensions)
    
    implementation(Dependencies.Lifecycle.viewModel)
    implementation(Dependencies.Lifecycle.liveData)
    
    testImplementation(Dependencies.Test.junit)
    androidTestImplementation(Dependencies.Test.junitExt)
    androidTestImplementation(Dependencies.Test.espresso)
}
```

## Best Practices for 2025

### 1. Camera API Selection

- Use Camera2 API when you need fine-grained control
- Use CameraX for simpler integration with lifecycle awareness
- Consider CameraX with ML Kit for pose detection
- Consider custom Camera2 implementation with ARCore for advanced AR

### 2. Move to Kotlin DSL for Gradle

Convert all `build.gradle` files to `build.gradle.kts` for better type safety and IDE support:

```kotlin
// Convert from Groovy
android {
    compileSdkVersion 34
    
    defaultConfig {
        applicationId "com.example.app"
        minSdkVersion 24
        targetSdkVersion 34
        versionCode 1
        versionName "1.0"
    }
}

// To Kotlin DSL
android {
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.example.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
}
```

### 3. Embrace Jetpack Compose

Consider migrating UI to Jetpack Compose for better performance and maintainability:

```kotlin
// In a Compose-based feature module
@Composable
fun ARExperience() {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    
    val engine = rememberEngine()
    val view = rememberView(engine)
    val renderer = rememberRenderer(engine)
    val scene = rememberScene(engine)
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)
    
    ARScene(
        modifier = Modifier.fillMaxSize(),
        engine = engine,
        view = view,
        renderer = renderer,
        scene = scene,
        modelLoader = modelLoader,
        materialLoader = materialLoader,
        planeRenderer = true,
        onSessionCreated = { session ->
            // Configure ARCore session
        },
        onTapArPlaneListener = { hitResult, plane, motionEvent ->
            // Place object at hit location
        }
    )
}
```

### 4. Modern Threading with Coroutines

Use Kotlin Coroutines and Flow for asynchronous operations:

```kotlin
class CameraViewModel @Inject constructor(
    private val cameraRepository: CameraRepository
) : ViewModel() {
    
    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Initializing)
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()
    
    private val _frames = MutableSharedFlow<Frame>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val frames: SharedFlow<Frame> = _frames.asSharedFlow()
    
    fun startCamera() = viewModelScope.launch {
        try {
            _cameraState.value = CameraState.Starting
            cameraRepository.startCamera()
            _cameraState.value = CameraState.Started
            
            // Collect frames from camera
            cameraRepository.frames.collect { frame ->
                _frames.emit(frame)
            }
        } catch (e: Exception) {
            _cameraState.value = CameraState.Error(e)
        }
    }
    
    fun stopCamera() = viewModelScope.launch {
        try {
            cameraRepository.stopCamera()
            _cameraState.value = CameraState.Stopped
        } catch (e: Exception) {
            _cameraState.value = CameraState.Error(e)
        }
    }
}
```

### 5. Build Configuration Optimizations

Add advanced build configurations to speed up builds and reduce conflicts:

```kotlin
// In root build.gradle.kts
subprojects {
    apply {
        plugin("com.android.application") apply false
        plugin("com.android.library") apply false
        plugin("org.jetbrains.kotlin.android") apply false
    }
    
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_17.toString()
            freeCompilerArgs += listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlin.ExperimentalStdlibApi"
            )
        }
    }
}

// Enable Gradle Build Cache
buildCache {
    local {
        isEnabled = true
        directory = File(rootDir, "build-cache")
        removeUnusedEntriesAfterDays = 30
    }
}
```

## Integrating with React Native (react-native-vision-camera)

For teams that need to leverage React Native while maintaining high-performance native camera functionality, [react-native-vision-camera](https://github.com/mrousavy/react-native-vision-camera) offers a bridge between these worlds. Here's how to integrate it with a clean architecture approach:

### 1. React Native Bridge Module Setup

Create a bridge between your clean native implementation and React Native:

```kotlin
// feature-camera-rn/src/main/java/com/example/feature/camera/rn/CameraBridgeModule.kt
@ReactModule(name = "CameraModule")
class CameraBridgeModule(
    reactContext: ReactApplicationContext,
    private val cameraManager: CameraManager
) : ReactContextBaseJavaModule(reactContext) {
    
    override fun getName(): String = "CameraModule"
    
    @ReactMethod
    fun startCamera(options: ReadableMap, promise: Promise) {
        try {
            // Map React Native options to native camera options
            val cameraOptions = mapRNOptionsToCameraOptions(options)
            
            // Use your clean architecture implementation
            cameraManager.startCamera(
                cameraOptions = cameraOptions,
                onFrameAvailable = { frame ->
                    // Send frame data to React Native if needed
                    sendFrameEvent(frame)
                },
                onCameraError = { error ->
                    // Send error to React Native
                    sendErrorEvent(error)
                }
            )
            
            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject("CAMERA_ERROR", e.message, e)
        }
    }
    
    @ReactMethod
    fun stopCamera(promise: Promise) {
        try {
            cameraManager.stopCamera()
            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject("CAMERA_ERROR", e.message, e)
        }
    }
    
    // Helper method to map RN options to native options
    private fun mapRNOptionsToCameraOptions(options: ReadableMap): CameraOptions {
        return CameraOptions(
            cameraId = options.getString("cameraId") ?: CameraSelector.DEFAULT_BACK_CAMERA.toString(),
            enableML = options.getBoolean("enableML"),
            enableAR = options.getBoolean("enableAR"),
            resolution = when (options.getString("resolution")) {
                "hd" -> Resolution.HD
                "fhd" -> Resolution.FULL_HD
                "4k" -> Resolution.UHD_4K
                else -> Resolution.HD
            }
        )
    }
    
    // Send events back to React Native
    private fun sendFrameEvent(frame: Frame) {
        val eventData = Arguments.createMap().apply {
            putInt("width", frame.width)
            putInt("height", frame.height)
            // Other frame data
        }
        
        reactApplicationContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit("onFrameAvailable", eventData)
    }
}
```

### 2. Using Vision Camera's Frame Processor API

Leverage Vision Camera's Frame Processor API for ML processing:

```kotlin
// feature-camera-rn/src/main/java/com/example/feature/camera/rn/MLKitFrameProcessorPlugin.kt
class MLKitFrameProcessorPlugin : FrameProcessorPlugin() {
    
    @Inject
    lateinit var poseDetectionProcessor: PoseDetectionProcessor
    
    init {
        DaggerFrameProcessorComponent.create().inject(this)
    }
    
    override fun getName(): String = "poseDetection"
    
    override fun callback(frame: ImageProxy, params: Map<String, Any>?): Any? {
        val result = poseDetectionProcessor.processImageSync(frame)
        
        // Convert pose result to a map that can be sent to React Native
        return result?.let { pose ->
            mapOf(
                "landmarks" to pose.allPoseLandmarks.map { landmark ->
                    mapOf(
                        "type" to landmark.landmarkType,
                        "position" to mapOf(
                            "x" to landmark.position.x,
                            "y" to landmark.position.y,
                            "z" to landmark.position3D.z
                        ),
                        "inFrameLikelihood" to landmark.inFrameLikelihood
                    )
                },
                "detected" to true
            )
        } ?: mapOf("detected" to false)
    }
}
```

### 3. Clean Integration with Native Architecture

```kotlin
// feature-camera-rn/src/main/java/com/example/feature/camera/rn/di/FrameProcessorComponent.kt
@Component(dependencies = [AppComponent::class])
interface FrameProcessorComponent {
    
    fun inject(plugin: MLKitFrameProcessorPlugin)
    
    @Component.Factory
    interface Factory {
        fun create(appComponent: AppComponent): FrameProcessorComponent
    }
}
```

### 4. React Native Usage

In your React Native code, use the Vision Camera API while leveraging your native implementation:

```javascript
// App.js
import React, { useEffect, useState } from 'react';
import { StyleSheet, View, Text } from 'react-native';
import { Camera, useCameraDevices, useFrameProcessor } from 'react-native-vision-camera';
import { runOnJS } from 'react-native-reanimated';

// Import your native module
import CameraModule from './CameraModule';

export default function App() {
  const [hasPermission, setHasPermission] = useState(false);
  const [poses, setPoses] = useState([]);
  const devices = useCameraDevices();
  const device = devices.back;
  
  // Request camera permissions
  useEffect(() => {
    (async () => {
      const status = await Camera.requestCameraPermission();
      setHasPermission(status === 'authorized');
    })();
  }, []);
  
  // Handle pose detection results
  const handlePoseDetected = (pose) => {
    if (pose.detected) {
      setPoses(pose.landmarks);
    }
  };
  
  // Frame processor for ML Kit
  const frameProcessor = useFrameProcessor((frame) => {
    'worklet';
    const pose = poseDetection(frame);
    runOnJS(handlePoseDetected)(pose);
  }, []);
  
  if (!hasPermission) {
    return <Text>No camera permission</Text>;
  }
  
  if (!device) {
    return <Text>Loading...</Text>;
  }
  
  return (
    <View style={styles.container}>
      <Camera
        style={StyleSheet.absoluteFill}
        device={device}
        isActive={true}
        frameProcessor={frameProcessor}
        frameProcessorFps={5}
      />
      {/* Render pose overlay here */}
      <PoseOverlay poses={poses} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});
```

### 5. Pros and Cons of react-native-vision-camera Approach

#### Pros:
- High-performance native camera implementation with React Native UI flexibility
- Direct access to Camera2 API and frame data in native code
- Utilizes worklets for efficient frame processing
- Support for ML Kit, ARCore, and other native libraries
- Minimal bridging overhead with optimized frame passing

#### Cons:
- Added complexity of maintaining both native and React Native codebases
- Need to carefully manage native module and React Native dependencies
- Requires knowledge of both ecosystems (React Native and Android native)
- May introduce some performance overhead compared to a pure native implementation
- Bridge code needs thorough testing to ensure reliability

## Balancing Native and Cross-Platform: A Hybrid Approach

For projects requiring both the performance of native camera processing and the development speed of React Native, consider a hybrid architecture:

1. **Native Core**: Implement camera, ML Kit, and ARCore functionalities in pure native code using the clean architecture patterns outlined above
2. **React Native UI**: Use React Native for the UI layer, connecting to native functionality through optimized bridges
3. **Shared State Management**: Use a state management solution that works well across both native and React Native (e.g., Redux with native bridges)

This approach allows teams to harness the strengths of both ecosystems while maintaining a clean, modular architecture.

## Conclusion

Building conflict-free Android applications that leverage advanced camera features, ML Kit, and ARCore requires careful architectural planning and dependency management. By following clean architecture principles, using modular design, and employing proper dependency management techniques, developers can create robust, maintainable applications that take full advantage of Android's capabilities in 2025.

Whether you choose a pure native approach or integrate with React Native through vision-camera, the key principles remain the same: separation of concerns, clean interfaces between modules, and careful management of dependencies to avoid conflicts.

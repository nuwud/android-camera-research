# React Native Camera Integration for Multi-Subject Tracking

## Overview

This document outlines the best practices for integrating camera functionality with multi-subject tracking in React Native applications. It addresses common issues with deprecated camera dependencies and provides guidance for implementing a robust solution that works across different device types.

## React Native Camera Dependencies

### Deprecated Dependencies

The following React Native camera libraries have been deprecated or have known issues:

1. **`react-native-camera`** (DEPRECATED)
   - Status: No longer maintained
   - Issues: Compatibility problems with newer React Native versions, permissions handling issues on Android 12+
   - Last significant update: 2021

2. **`react-native-camera-kit`**
   - Status: Limited maintenance
   - Issues: Performance issues with continuous frame processing, memory leaks

### Recommended Dependencies

1. **`react-native-vision-camera`**
   - Status: Actively maintained
   - Features:
     - Frame processor plugins support
     - TensorFlow Lite integration
     - High performance
     - Proper permissions handling
     - Photo and video capture
   - GitHub: [mrousavy/react-native-vision-camera](https://github.com/mrousavy/react-native-vision-camera)

2. **`react-native-camera-tflite`** (for TensorFlow Lite integration)
   - Can be used alongside vision-camera for direct ML integration

## Integration with Native Animal Tracking Code

### Architecture

The recommended architecture for integrating React Native with our native tracking implementation:

```
React Native App
    ↓
Frame Processor Plugin (JavaScript)
    ↓
Native Module Bridge (JavaScript ↔ Native)
    ↓
MultiSubjectTracker (Native Android Kotlin)
    ↓
ML Kit + Custom TFLite Models
```

### Implementation Steps

1. **Setup Vision Camera**

```javascript
// Install dependencies
// npm install react-native-vision-camera
// npx pod-install (for iOS)

// App.js
import { Camera, useCameraDevices } from 'react-native-vision-camera';
import { useEffect, useState } from 'react';

function App() {
  const [hasPermission, setHasPermission] = useState(false);
  const devices = useCameraDevices();
  const device = devices.back;
  
  useEffect(() => {
    (async () => {
      const cameraPermission = await Camera.requestCameraPermission();
      setHasPermission(cameraPermission === 'authorized');
    })();
  }, []);
  
  if (!device || !hasPermission) {
    return <ActivityIndicator size="large" color="#1C6758" />;
  }
  
  return (
    <Camera
      style={StyleSheet.absoluteFill}
      device={device}
      isActive={true}
      frameProcessor={frameProcessor}
      frameProcessorFps={15}
    />
  );
}
```

2. **Create a Frame Processor Plugin**

```javascript
// frameProcessor.js
import { runOnJS } from 'react-native-vision-camera';
import { NativeModules } from 'react-native';

const { AnimalTrackingModule } = NativeModules;

export const frameProcessor = (frame) => {
  'worklet';
  if (frame) {
    // Process the frame with our native module
    const trackingResults = AnimalTrackingModule.processFrame(frame);
    
    // Run JS callback with the results
    if (trackingResults) {
      runOnJS(onTrackingResults)(trackingResults);
    }
  }
  return null;
};

// Callback function to handle tracking results
const onTrackingResults = (results) => {
  console.log('Tracking results:', results);
  // Update state, render overlays, etc.
};
```

3. **Native Module Bridge Implementation**

```kotlin
// AnimalTrackingModule.kt
package com.yourapp.animaltracking

import com.facebook.react.bridge.*
import com.facebook.react.module.annotations.ReactModule
import com.mrousavy.camera.frameprocessor.Frame
import com.example.androidcameraresearch.multitracking.MultiSubjectTracker
import com.example.androidcameraresearch.multitracking.SubjectType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@ReactModule(name = "AnimalTrackingModule")
class AnimalTrackingModule(reactContext: ReactApplicationContext) : 
    ReactContextBaseJavaModule(reactContext) {
    
    private val tracker = MultiSubjectTracker(reactContext)
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    
    override fun getName(): String {
        return "AnimalTrackingModule"
    }
    
    @ReactMethod
    fun processFrame(frame: Frame, promise: Promise) {
        coroutineScope.launch {
            try {
                val bitmap = frame.toBitmap()
                tracker.processFrame(bitmap)
                
                // Convert tracked subjects to JS-friendly format
                val subjects = tracker.trackedSubjects.value
                val resultArray = WritableNativeArray()
                
                subjects.forEach { subject ->
                    val subjectMap = WritableNativeMap().apply {
                        putInt("id", subject.id)
                        putString("type", subject.type.name)
                        putDouble("confidence", subject.confidence.toDouble())
                        
                        // Add bounding box
                        putMap("boundingBox", WritableNativeMap().apply {
                            putDouble("x", subject.boundingBox.left.toDouble())
                            putDouble("y", subject.boundingBox.top.toDouble())
                            putDouble("width", subject.boundingBox.width().toDouble())
                            putDouble("height", subject.boundingBox.height().toDouble())
                        })
                        
                        // Add landmarks
                        val landmarksArray = WritableNativeArray()
                        when (subject.type) {
                            SubjectType.HUMAN -> subject.humanPoseLandmarks?.forEach { landmark ->
                                landmarksArray.pushMap(WritableNativeMap().apply {
                                    putInt("type", landmark.landmarkType)
                                    putDouble("x", landmark.position.x.toDouble())
                                    putDouble("y", landmark.position.y.toDouble())
                                    putDouble("confidence", landmark.inFrameLikelihood.toDouble())
                                })
                            }
                            else -> subject.animalPoseLandmarks?.forEach { landmark ->
                                landmarksArray.pushMap(WritableNativeMap().apply {
                                    putInt("type", landmark.type)
                                    putDouble("x", landmark.position.x.toDouble())
                                    putDouble("y", landmark.position.y.toDouble())
                                    putDouble("confidence", landmark.confidence.toDouble())
                                })
                            }
                        }
                        putArray("landmarks", landmarksArray)
                    }
                    resultArray.pushMap(subjectMap)
                }
                
                promise.resolve(resultArray)
            } catch (e: Exception) {
                promise.reject("TRACKING_ERROR", e.message, e)
            }
        }
    }
    
    @ReactMethod
    fun cleanup() {
        tracker.shutdown()
    }
}
```

4. **Register the Native Module**

```kotlin
// AnimalTrackingPackage.kt
package com.yourapp.animaltracking

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

class AnimalTrackingPackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        return listOf(AnimalTrackingModule(reactContext))
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return emptyList()
    }
}
```

5. **Register in MainApplication.java**

```java
// Add to the packages list in MainApplication.java
@Override
protected List<ReactPackage> getPackages() {
    List<ReactPackage> packages = new PackageList(this).getPackages();
    packages.add(new AnimalTrackingPackage());
    return packages;
}
```

## Common Issues and Solutions

### 1. Deprecated API Usage

**Problem**: Using deprecated camera APIs can cause build failures and runtime crashes.

**Solution**: 
- Use `react-native-vision-camera` instead of older libraries
- Check compatibility with your React Native version
- Update Gradle and Android configuration to support modern camera APIs

### 2. Performance Issues with Frame Processing

**Problem**: Processing every frame can cause performance degradation.

**Solution**:
- Limit frame processing rate (e.g., 15 FPS instead of 30/60 FPS)
- Use the GPU delegate for TensorFlow Lite models
- Implement frame skipping when device performance is constrained
- Resize input frames to smaller dimensions before processing

### 3. Memory Leaks

**Problem**: Continuous camera usage can lead to memory leaks.

**Solution**:
- Properly shut down the tracker when the component unmounts
- Implement reference counting for shared resources
- Use weak references where appropriate
- Regularly profile memory usage during development

```javascript
// In your React component
useEffect(() => {
  // Setup
  
  return () => {
    // Cleanup
    AnimalTrackingModule.cleanup();
  };
}, []);
```

### 4. Permissions Issues

**Problem**: Modern Android versions have stricter camera permissions handling.

**Solution**:
- Use the built-in permission handling in `react-native-vision-camera`
- Add clear permission explanation strings in the manifest
- Implement graceful fallbacks when permissions are denied
- Handle permission changes during app usage

## Best Practices for Multi-Subject Tracking in React Native

1. **Performance Optimization**
   - Process frames at a lower resolution initially, then refine for detected subjects
   - Implement adaptive frame rate based on device capabilities
   - Use hardware acceleration where available

2. **UI Implementation**
   - Use SVG or Canvas for rendering skeletal overlays
   - Implement semi-transparent colored overlays for different subject types
   - Add visual indicators for tracking confidence

3. **Error Handling**
   - Gracefully handle tracking failures
   - Provide meaningful feedback to users
   - Implement fallback modes for low-power devices

4. **Testing**
   - Test on various device types and camera configurations
   - Create automated UI tests for tracking scenarios
   - Benchmark performance on target devices

## Migrating from Deprecated Dependencies

If you're currently using `react-native-camera` or other deprecated dependencies, follow these steps:

1. **Install the new dependencies**
   ```bash
   npm uninstall react-native-camera
   npm install react-native-vision-camera
   ```

2. **Update imports and component usage**
   ```javascript
   // Old
   import { RNCamera } from 'react-native-camera';
   
   // New
   import { Camera } from 'react-native-vision-camera';
   ```

3. **Migrate permission handling**
   ```javascript
   // New pattern
   useEffect(() => {
     (async () => {
       const cameraPermission = await Camera.requestCameraPermission();
       setHasPermission(cameraPermission === 'authorized');
     })();
   }, []);
   ```

4. **Update frame processing logic**
   ```javascript
   // Old
   onBarCodeRead={handleBarCode}
   
   // New - separate frame processor
   frameProcessor={frameProcessor}
   frameProcessorFps={15}
   ```

5. **Test thoroughly on all target platforms**

## References

1. [React Native Vision Camera Documentation](https://mrousavy.com/react-native-vision-camera/docs/guides)
2. [TensorFlow Lite React Native](https://www.tensorflow.org/lite/guide/react_native)
3. [Multiple-Skeleton Tracking Techniques](https://www.youtube.com/watch?v=ym8Tnmiz5N8)
4. [VVVV Forum on Multiple Skeleton Tracking](https://forum.vvvv.org/t/multiple-skeleton-tracking-question/22186)
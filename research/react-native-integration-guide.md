# React Native Integration Guide for Android Camera Projects

## Overview

This guide focuses on integrating React Native with native Android camera implementations, ML Kit, and ARCore while avoiding common npm and Node.js build issues.

## Architecture Recommendations

### 1. Native Bridge Architecture

The most reliable approach for combining React Native with advanced camera features is to use a native bridge architecture:

```
React Native UI Layer
       ↕
Native Bridge Module
       ↕
Native Camera/ML/AR Implementation
```

This approach maintains clean separation and allows each layer to be optimized independently.

### 2. Module Structure

```
project/
├── android/            # Native Android implementation
│   ├── app/
│   ├── camera-module/  # Native camera implementation
│   ├── ml-module/      # Native ML Kit implementation
│   └── ar-module/      # Native ARCore implementation
├── src/                # React Native JavaScript code
│   ├── components/
│   ├── screens/
│   └── bridges/        # JS bridge interfaces
├── package.json
└── metro.config.js
```

## Implementation Approach

### 1. Setting Up the Native Bridge

**AndroidManifest.xml Configuration:**

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Camera permissions -->
    <uses-permission android:name="android.permission.CAMERA" />
    
    <!-- For ML Kit and ARCore -->
    <uses-feature android:name="android.hardware.camera" />
    <uses-feature android:name="android.hardware.camera.autofocus" />
    <uses-feature android:name="com.google.ar.core" android:required="true" />
    
    <application>
        <!-- ARCore meta-data -->
        <meta-data
            android:name="com.google.ar.core"
            android:value="required" />
            
        <!-- ... other application elements ... -->
    </application>
</manifest>
```

**Native Bridge Module:**

```kotlin
// CameraBridgeModule.kt
package com.example.reactnativecamera

import android.util.Size
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

@ReactModule(name = CameraBridgeModule.NAME)
class CameraBridgeModule(
    reactContext: ReactApplicationContext,
    private val cameraManager: CameraManager
) : ReactContextBaseJavaModule(reactContext) {

    companion object {
        const val NAME = "CameraModule"
    }

    override fun getName(): String = NAME

    @ReactMethod
    fun startCamera(options: ReadableMap, promise: Promise) {
        try {
            // Convert React Native options to native options
            val resolution = options.getString("resolution") ?: "720p"
            val targetSize = when (resolution) {
                "480p" -> Size(640, 480)
                "720p" -> Size(1280, 720)
                "1080p" -> Size(1920, 1080)
                else -> Size(1280, 720)
            }
            
            val enableML = options.getBoolean("enableML")
            val enableAR = options.getBoolean("enableAR")
            
            // Start the camera with the specified options
            cameraManager.startCamera(
                targetSize = targetSize,
                enableML = enableML,
                enableAR = enableAR,
                onFrameProcessed = { result ->
                    // Send results back to React Native
                    sendEventToJS("onFrameProcessed", result)
                },
                onError = { error ->
                    sendEventToJS("onCameraError", error)
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
    
    private fun sendEventToJS(eventName: String, params: Any) {
        reactApplicationContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, Arguments.makeNativeMap(params as? WritableMap ?: WritableNativeMap()))
    }
}
```

**Register the Native Module:**

```kotlin
// CameraPackage.kt
package com.example.reactnativecamera

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

class CameraPackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        val cameraManager = CameraManager(reactContext)
        return listOf(CameraBridgeModule(reactContext, cameraManager))
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return emptyList()
    }
}
```

### 2. JavaScript Bridge Interface

```javascript
// CameraModule.js
import { NativeModules, NativeEventEmitter } from 'react-native';

const { CameraModule } = NativeModules;
const cameraEventEmitter = new NativeEventEmitter(CameraModule);

class Camera {
  constructor() {
    this.listeners = {};
  }

  startCamera(options = {}) {
    const defaultOptions = {
      resolution: '720p',  // 480p, 720p, 1080p
      enableML: false,     // Enable ML Kit for pose detection
      enableAR: false,     // Enable ARCore
    };
    
    return CameraModule.startCamera({
      ...defaultOptions,
      ...options
    });
  }

  stopCamera() {
    return CameraModule.stopCamera();
  }

  // Event listeners
  onFrameProcessed(callback) {
    this.listeners.frameProcessed = cameraEventEmitter.addListener(
      'onFrameProcessed',
      callback
    );
    return () => this.listeners.frameProcessed.remove();
  }

  onCameraError(callback) {
    this.listeners.cameraError = cameraEventEmitter.addListener(
      'onCameraError',
      callback
    );
    return () => this.listeners.cameraError.remove();
  }

  // Clean up all listeners
  removeAllListeners() {
    Object.values(this.listeners).forEach(listener => {
      if (listener) listener.remove();
    });
    this.listeners = {};
  }
}

export default new Camera();
```

### 3. React Native Usage

```javascript
// CameraScreen.js
import React, { useEffect, useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import CameraModule from './CameraModule';

const CameraScreen = () => {
  const [isActive, setIsActive] = useState(false);
  const [poseResults, setPoseResults] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    // Set up event listeners
    const frameListener = CameraModule.onFrameProcessed(data => {
      setPoseResults(data);
    });

    const errorListener = CameraModule.onCameraError(err => {
      setError(err.message);
    });

    return () => {
      // Clean up listeners
      frameListener();
      errorListener();
      
      // Ensure camera is stopped
      if (isActive) {
        CameraModule.stopCamera();
      }
    };
  }, []);

  const toggleCamera = async () => {
    try {
      if (isActive) {
        await CameraModule.stopCamera();
      } else {
        await CameraModule.startCamera({
          resolution: '720p',
          enableML: true,
          enableAR: false,
        });
      }
      setIsActive(!isActive);
      setError(null);
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.cameraContainer}>
        {/* Camera preview would be rendered by native code */}
        {error && (
          <Text style={styles.errorText}>{error}</Text>
        )}
      </View>
      
      <View style={styles.controls}>
        <TouchableOpacity 
          style={[styles.button, isActive && styles.activeButton]}
          onPress={toggleCamera}
        >
          <Text style={styles.buttonText}>
            {isActive ? 'Stop Camera' : 'Start Camera'}
          </Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  cameraContainer: {
    flex: 1,
    backgroundColor: '#000',
    justifyContent: 'center',
    alignItems: 'center',
  },
  controls: {
    position: 'absolute',
    bottom: 30,
    left: 0,
    right: 0,
    flexDirection: 'row',
    justifyContent: 'center',
  },
  button: {
    backgroundColor: '#2196F3',
    padding: 15,
    borderRadius: 8,
    minWidth: 150,
    alignItems: 'center',
  },
  activeButton: {
    backgroundColor: '#F44336',
  },
  buttonText: {
    color: 'white',
    fontWeight: 'bold',
  },
  errorText: {
    color: 'red',
    backgroundColor: 'rgba(0,0,0,0.7)',
    padding: 10,
    borderRadius: 5,
  },
});

export default CameraScreen;
```

## Build Configuration to Avoid npm Issues

### 1. package.json Configuration

```json
{
  "name": "camera-ar-app",
  "version": "0.1.0",
  "private": true,
  "scripts": {
    "android": "react-native run-android",
    "ios": "react-native run-ios",
    "start": "react-native start",
    "postinstall": "node scripts/postinstall.js"
  },
  "dependencies": {
    "react": "18.2.0",
    "react-native": "0.72.6"
  },
  "devDependencies": {
    "@babel/core": "^7.20.0",
    "@babel/preset-env": "^7.20.0",
    "@babel/runtime": "^7.20.0",
    "@react-native/metro-config": "^0.72.11",
    "@types/react": "^18.0.24",
    "babel-plugin-module-resolver": "^5.0.0",
    "metro-react-native-babel-preset": "0.76.8",
    "patch-package": "^8.0.0",
    "postinstall-postinstall": "^2.1.0"
  },
  "engines": {
    "node": ">=16.0.0 <17.0.0",
    "npm": ">=8.0.0 <9.0.0"
  },
  "overrides": {
    "react-native": "0.72.6"
  }
}
```

### 2. Custom Postinstall Script

Create a file `scripts/postinstall.js`:

```javascript
// scripts/postinstall.js
const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

// Increase Node.js memory limit for large operations
process.env.NODE_OPTIONS = process.env.NODE_OPTIONS || '--max-old-space-size=4096';

console.log('Running post-install script...');

try {
  // Apply patches if they exist
  if (fs.existsSync(path.join(__dirname, '../patches'))) {
    console.log('Applying patches...');
    execSync('npx patch-package', { stdio: 'inherit' });
  }

  // Ensure that native modules are properly linked
  console.log('Checking native modules...');
  execSync('npx react-native-fix-image', { stdio: 'inherit' });

  // Fix potential permission issues on Android gradle wrapper
  const gradlewPath = path.join(__dirname, '../android/gradlew');
  if (fs.existsSync(gradlewPath)) {
    console.log('Setting execute permissions on gradlew...');
    fs.chmodSync(gradlewPath, '755');
  }

  console.log('Post-install completed successfully!');
} catch (error) {
  console.error('Post-install script failed:');
  console.error(error);
  process.exit(1);
}
```

### 3. metro.config.js Optimization

```javascript
// metro.config.js
const { getDefaultConfig } = require('@react-native/metro-config');
const path = require('path');

module.exports = (async () => {
  const {
    resolver: { sourceExts, assetExts },
  } = await getDefaultConfig();

  return {
    transformer: {
      getTransformOptions: async () => ({
        transform: {
          experimentalImportSupport: false,
          inlineRequires: true,
        },
      }),
      // Optimize memory usage
      maxWorkers: 2,
      // Set reasonable limits
      minifierConfig: {
        mangle: {
          keep_fnames: true,
        },
      },
    },
    resolver: {
      // Ensure proper module resolution
      extraNodeModules: new Proxy({}, {
        get: (target, name) => {
          return path.resolve(__dirname, `node_modules/${name}`);
        },
      }),
      // Avoid asset processing issues
      assetExts: [...assetExts, 'bin'],
      // Add any extra extensions needed
      sourceExts: [...sourceExts, 'cjs'],
    },
    // Optimize caching
    cacheStores: [
      // Use the local temp directory for better performance
      require('metro-cache').FileStore,
    ],
    // Configure watchman properly
    watchFolders: [path.resolve(__dirname)],
  };
})();
```

### 4. Gradle Configuration for Avoiding Native Module Conflicts

In `android/build.gradle`:

```groovy
// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    ext {
        buildToolsVersion = "33.0.0"
        minSdkVersion = 24
        compileSdkVersion = 34
        targetSdkVersion = 34
        ndkVersion = "25.1.8937393"
        kotlinVersion = "1.9.22"
        
        // Key dependencies that tend to conflict
        cameraxVersion = "1.3.1"
        mlkitVersion = "18.0.0-beta3"
        arcoreVersion = "1.42.0"
    }
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}")
    }
}

allprojects {
    repositories {
        exclusiveContent {
            // Make sure Google's repo has priority for ML Kit
            filter {
                includeGroup "com.google.mlkit"
            }
            forRepository {
                google()
            }
        }
        google()
        mavenCentral()
        maven { url "https://jitpack.io" }
        maven { url "$rootDir/../node_modules/react-native/android" }
    }
    
    // Apply resolution strategy to all modules
    configurations.all {
        resolutionStrategy {
            // Force specific versions to resolve conflicts
            force "com.google.android.gms:play-services-basement:18.2.0"
            force "com.google.android.gms:play-services-tasks:18.0.2"
            force "androidx.core:core-ktx:1.12.0"
            force "androidx.camera:camera-camera2:${cameraxVersion}"
            force "androidx.camera:camera-lifecycle:${cameraxVersion}"
            force "com.google.mlkit:pose-detection:${mlkitVersion}"
            force "com.google.ar:core:${arcoreVersion}"
            
            // Prevent using +, latest.*, or other dynamic versions
            failOnDynamicVersions()
        }
    }
}
```

In `android/app/build.gradle`:

```groovy
apply plugin: "com.android.application"
apply plugin: "kotlin-android"

import com.android.build.OutputFile

// Add project to set dependency versions
apply from: "${rootProject.projectDir}/../node_modules/react-native/react.gradle"

android {
    ndkVersion rootProject.ext.ndkVersion

    compileSdkVersion rootProject.ext.compileSdkVersion

    defaultConfig {
        applicationId "com.cameraexample"
        minSdkVersion rootProject.ext.minSdkVersion
        targetSdkVersion rootProject.ext.targetSdkVersion
        versionCode 1
        versionName "1.0"
    }
    
    // Key optimization to avoid ABI conflicts
    packagingOptions {
        pickFirst "**/libc++_shared.so"
        exclude "META-INF/DEPENDENCIES"
        exclude "META-INF/LICENSE"
        exclude "META-INF/licenses/**"
        exclude "META-INF/AL2.0"
        exclude "META-INF/LGPL2.1"
    }
    
    // ARCore requires specific ABI configuration
    splits {
        abi {
            enable true
            reset()
            include "armeabi-v7a", "arm64-v8a", "x86", "x86_64"
            universalApk true
        }
    }
    
    // Properly handle React Native's soLoader
    buildTypes {
        debug {
            // For debugging with React Native
            matchingFallbacks = ['debug', 'release']
        }
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro"
        }
    }
    
    // React Native requires Java 11
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_11
        targetCompatibility JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // React Native dependencies
    implementation fileTree(dir: "libs", include: ["*.jar"])
    implementation "com.facebook.react:react-native:0.72.6"
    
    // Camera & ML Kit & AR dependencies with explicit versions
    implementation "androidx.camera:camera-camera2:${rootProject.ext.cameraxVersion}"
    implementation "androidx.camera:camera-lifecycle:${rootProject.ext.cameraxVersion}"
    implementation "androidx.camera:camera-view:${rootProject.ext.cameraxVersion}"
    
    implementation "com.google.mlkit:pose-detection:${rootProject.ext.mlkitVersion}"
    implementation "com.google.ar:core:${rootProject.ext.arcoreVersion}"
    implementation "io.github.sceneview:arsceneview:2.2.1"
    
    // Other dependencies
    implementation "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
    debugImplementation "com.facebook.flipper:flipper:0.206.0"
}
```

## Managing Build Issues in CI/CD

### Create .npmrc File

Create a `.npmrc` file in the project root to avoid common npm issues in CI environments:

```
# Avoid environment-specific issues
engine-strict=true
legacy-peer-deps=true

# Make error logs more useful
loglevel=notice

# Increase timeouts for CI environments
network-timeout=60000

# Use explicit registry
registry=https://registry.npmjs.org/

# Configure cache for better performance
cache=.npm

# Avoid permission issues
force=true

# Avoid proxy issues
proxy=
https-proxy=

# Avoid workspace confusion
workspace-prefix=false
```

### GitHub Actions Workflow

Create a GitHub Actions workflow file `.github/workflows/react-native-build.yml` with proper error handling:

```yaml
name: React Native Build

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    strategy:
      fail-fast: false # Continue with other jobs if one fails

    steps:
      - uses: actions/checkout@v3

      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '16' # Explicit version to avoid conflicts
          cache: 'npm'
          cache-dependency-path: '**/package-lock.json'

      - name: Print debug info
        run: |
          echo "Node version: $(node -v)"
          echo "NPM version: $(npm -v)"
          node --print "process.arch"
          node --print "process.platform"

      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '11'

      - name: Install dependencies with retry
        uses: nick-invision/retry@v2
        with:
          timeout_minutes: 10
          max_attempts: 3
          command: |
            npm ci --no-audit --no-fund --prefer-offline
            npx react-native-fix-image

      - name: Cache Gradle packages
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
          restore-keys: |
            ${{ runner.os }}-gradle-

      - name: Set gradle permissions
        run: chmod +x ./android/gradlew

      - name: Build Android App
        working-directory: ./android
        run: ./gradlew assembleDebug --stacktrace

      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: app-debug
          path: android/app/build/outputs/apk/debug/app-debug.apk
```

## Troubleshooting Common React Native Issues

### 1. patch-package Failures

If patch-package fails in postinstall:

1. Debug with verbose output:
   ```bash
   DEBUG=patch-package* npm install
   ```

2. Check for patch compatibility with the current package versions:
   ```bash
   npx patch-package --list-patches
   ```

3. Recreate problematic patches:
   ```bash
   # First remove the old patch
   rm patches/react-native-vision-camera+*.patch
   
   # Make changes to node_modules
   # Then create a new patch
   npx patch-package react-native-vision-camera --exclude 'android/.gradle/*'
   ```

### 2. Native Module Compilation Errors

If you're getting errors like `Module._compile` or `Module.load`:

1. Check for architecture mismatches:
   ```bash
   # For node-gyp based modules
   npm rebuild --verbose
   ```

2. Check for missing environment variables:
   ```bash
   # For Android NDK issues
   export ANDROID_NDK_HOME=/path/to/android/ndk
   export ANDROID_HOME=/path/to/android/sdk
   ```

3. For React Native specifically:
   ```bash
   # Clean project completely
   rm -rf node_modules
   rm -rf android/build android/app/build
   npm cache clean --force
   npm install
   cd android && ./gradlew clean
   ```

### 3. Buffer Output Errors

For errors related to buffer handling:

1. Monitor memory usage during build:
   ```bash
   # Install node memory monitor
   npm install -g node-memwatch
   
   # Run with memory monitoring
   node --require node-memwatch scripts/postinstall.js
   ```

2. Implement chunked processing for large operations:
   ```javascript
   // Instead of processing all files at once
   const chunks = chunkArray(largeFileList, 10);
   for (const chunk of chunks) {
     await Promise.all(chunk.map(processFile));
   }
   ```

## Best Practices for React Native Camera Integration

1. **Keep the native camera implementation separate** from the React Native bridge

2. **Use appropriate thread management** for camera operations

3. **Implement proper error handling** on both native and JS sides

4. **Optimize memory usage** for frame processing

5. **Use explicit versioning** for all dependencies

6. **Create a development container** using Docker for consistent environments

7. **Document node/npm version requirements** in README and enforce in CI

8. **Create fallback mechanisms** for different device capabilities

## Conclusion

Integrating React Native with native Android camera, ML Kit, and AR capabilities can be challenging, especially due to npm and build issues. By following the architecture and configuration recommendations in this guide, you can create a robust integration that minimizes common problems and provides a reliable development experience.

With the proper setup, you can leverage the best of both worlds: the rapid development cycle and cross-platform capabilities of React Native, along with the performance and advanced features of native Android APIs for camera, ML, and AR functionality.

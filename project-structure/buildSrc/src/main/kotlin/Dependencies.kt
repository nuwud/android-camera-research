/**
 * Central dependency management for the Android Camera Research project
 * This helps avoid version conflicts by ensuring consistent dependency versions
 * across all modules.
 */
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
    const val compose = "1.6.1"
    
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
    
    object Compose {
        const val ui = "androidx.compose.ui:ui:${Versions.compose}"
        const val material = "androidx.compose.material3:material3:${Versions.compose}"
        const val preview = "androidx.compose.ui:ui-tooling-preview:${Versions.compose}"
        const val runtime = "androidx.compose.runtime:runtime:${Versions.compose}"
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

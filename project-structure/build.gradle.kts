// Root build.gradle.kts for the clean architecture Android project

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.5")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// Configuration applied to all modules
subprojects {
    // Apply common configurations to avoid conflicts
    configurations.all {
        resolutionStrategy {
            // Force specific versions for common conflict sources
            force("androidx.core:core-ktx:1.12.0")
            force("androidx.appcompat:appcompat:1.6.1")
            force("com.google.android.material:material:1.11.0")
            
            // Handle specific ML Kit and ARCore conflicts
            force("com.google.mlkit:pose-detection:18.0.0-beta3")
            force("com.google.mlkit:pose-detection-accurate:18.0.0-beta3")
            force("com.google.ar:core:1.42.0")
            
            // Camera APIs
            force("androidx.camera:camera-camera2:1.3.1")
            force("androidx.camera:camera-lifecycle:1.3.1")
            force("androidx.camera:camera-view:1.3.1")
            
            // Avoid using dynamic version numbers
            failOnDynamicVersions()
            failOnChangingVersions()
        }
    }
    
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_17.toString()
            freeCompilerArgs += listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
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

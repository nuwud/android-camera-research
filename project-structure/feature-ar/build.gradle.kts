plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.androidcameraresearch.feature.ar"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    // ARCore requires specific ABI configuration
    ndkVersion = "25.2.9519653"
    ndkPath = android.ndkDirectory.absolutePath

    packaging {
        jniLibs {
            // Exclude any duplicate files to avoid conflicts
            pickFirsts += setOf("**/libc++_shared.so")
        }
    }
}

dependencies {
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-ui"))
    
    implementation(Dependencies.coreKtx)
    implementation(Dependencies.appCompat)
    implementation(Dependencies.material)
    
    // AR
    implementation(Dependencies.AR.arCore)
    implementation(Dependencies.AR.sceneView)
    implementation(Dependencies.AR.arSceneView)
    
    // Camera - needed for image processing
    implementation(Dependencies.Camera.camera2)
    
    // Lifecycle
    implementation(Dependencies.Lifecycle.viewModel)
    implementation(Dependencies.Lifecycle.liveData)
    implementation(Dependencies.Lifecycle.runtime)
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Testing
    testImplementation(Dependencies.Test.junit)
    androidTestImplementation(Dependencies.Test.junitExt)
    androidTestImplementation(Dependencies.Test.espresso)
}

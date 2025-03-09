#!/bin/bash

# Android Camera Integration Auto-Setup Tool
# This script helps you integrate Camera2, ML Kit, and ARCore with minimal effort

echo "========================================================="
echo "📱 Android Camera Integration - Easy Setup Tool 📱"
echo "========================================================="
echo ""

# Colors for better readability
GREEN="\033[0;32m"
BLUE="\033[0;34m"
YELLOW="\033[0;33m"
RED="\033[0;31m"
NC="\033[0m" # No Color

# Check prerequisites
check_prerequisite() {
    if ! command -v $1 &> /dev/null; then
        echo -e "${RED}❌ Required tool $1 is not installed.${NC}"
        echo -e "${YELLOW}Please install $1 before proceeding.${NC}"
        exit 1
    fi
}

check_prerequisite "java"
check_prerequisite "git"

# Detect if we're in an existing Android project
is_android_project() {
    if [ -f "./build.gradle" ] || [ -f "./build.gradle.kts" ]; then
        return 0 # True in bash
    else
        return 1 # False in bash
    fi
}

# Function to copy code samples
copy_code_samples() {
    local dest_dir="$1"
    
    # Create directories if they don't exist
    mkdir -p "$dest_dir/src/main/java/com/example/androidcamera/camera"
    mkdir -p "$dest_dir/src/main/java/com/example/androidcamera/mlkit"
    mkdir -p "$dest_dir/src/main/java/com/example/androidcamera/ar"
    
    # Copy camera implementation
    cp "../code-samples/camera2-mlkit-integration.kt" "$dest_dir/src/main/java/com/example/androidcamera/camera/Camera2PoseProcessor.kt"
    
    # Copy ML Kit + AR implementation
    cp "../code-samples/mlkit-arcore-integration.kt" "$dest_dir/src/main/java/com/example/androidcamera/ar/PoseARActivity.kt"
    
    echo -e "${GREEN}✓ Code samples copied to your project${NC}"
}

# Function to add dependencies to build.gradle
add_dependencies() {
    local build_file="$1"
    
    # Check if we're dealing with Kotlin DSL or Groovy
    if [[ "$build_file" == *".kts" ]]; then
        # Add dependencies for Kotlin DSL
        cat >> "$build_file" << 'EOL'

// Camera & Vision dependencies added by setup script
dependencies {
    // Camera
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    
    // ML Kit
    implementation("com.google.mlkit:pose-detection:18.0.0-beta3")
    implementation("com.google.mlkit:pose-detection-accurate:18.0.0-beta3")
    
    // ARCore
    implementation("com.google.ar:core:1.42.0")
    implementation("io.github.sceneview:arsceneview:2.2.1")
    
    // Coroutines for async processing
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}

android {
    // Add these to your existing android block
    buildFeatures {
        viewBinding = true
    }
    
    // ARCore requires specific ABI configuration
    packagingOptions {
        jniLibs {
            pickFirsts += setOf("**/libc++_shared.so")
        }
    }
}

// Make sure ARCore is available
androidResources {
    additionalParameters += listOf("--extra-packages com.google.ar.core")
}
EOL
    else
        # Add dependencies for Groovy
        cat >> "$build_file" << 'EOL'

// Camera & Vision dependencies added by setup script
dependencies {
    // Camera
    implementation "androidx.camera:camera-camera2:1.3.1"
    implementation "androidx.camera:camera-lifecycle:1.3.1"
    implementation "androidx.camera:camera-view:1.3.1"
    
    // ML Kit
    implementation "com.google.mlkit:pose-detection:18.0.0-beta3"
    implementation "com.google.mlkit:pose-detection-accurate:18.0.0-beta3"
    
    // ARCore
    implementation "com.google.ar:core:1.42.0"
    implementation "io.github.sceneview:arsceneview:2.2.1"
    
    // Coroutines for async processing
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
}

android {
    // Add these to your existing android block
    buildFeatures {
        viewBinding true
    }
    
    // ARCore requires specific ABI configuration
    packagingOptions {
        pickFirst "**/libc++_shared.so"
    }
}

// Make sure ARCore is available
android.aaptOptions.additionalParameters += ['--extra-packages', 'com.google.ar.core']
EOL
    fi
    
    echo -e "${GREEN}✓ Dependencies added to build.gradle${NC}"
}

# Function to add manifest entries
add_manifest_entries() {
    local manifest_file="$1"
    
    # Create a temporary file
    temp_file=$(mktemp)
    
    # Find the line with the end of manifest tag
    manifest_end_line=$(grep -n "</manifest>" "$manifest_file" | cut -d':' -f1)
    
    # Add our permissions and features just before the end of manifest
    head -n $((manifest_end_line - 1)) "$manifest_file" > "$temp_file"
    
    cat >> "$temp_file" << 'EOL'
    <!-- Camera and AR permissions -->
    <uses-permission android:name="android.permission.CAMERA" />
    
    <!-- Tell the system this app requires ARCore -->
    <uses-feature android:name="android.hardware.camera" />
    <uses-feature android:name="android.hardware.camera.autofocus" />
    
    <!-- Indicates that this app requires ARCore. Used to install ARCore along with the app. -->
    <uses-feature android:name="com.google.ar.core" android:required="true" />
</manifest>
EOL
    
    # Replace the original file
    mv "$temp_file" "$manifest_file"
    
    # Now add application elements
    temp_file=$(mktemp)
    application_start_line=$(grep -n "<application" "$manifest_file" | cut -d':' -f1)
    application_tag=$(sed -n "${application_start_line}p" "$manifest_file")
    
    # Copy everything until the application tag
    head -n $((application_start_line - 1)) "$manifest_file" > "$temp_file"
    
    # Add our modified application tag and meta-data
    echo "    $application_tag" >> "$temp_file"
    cat >> "$temp_file" << 'EOL'
        <!-- ARCore library meta-data -->
        <meta-data android:name="com.google.ar.core" android:value="required" />
        
        <!-- AR Activity declaration -->
        <activity
            android:name=".ar.PoseARActivity"
            android:configChanges="orientation|screenSize"
            android:screenOrientation="locked"
            android:exported="false" />
EOL
    
    # Continue with the rest of the file after the application tag
    tail -n +$((application_start_line + 1)) "$manifest_file" >> "$temp_file"
    
    # Replace the original file
    mv "$temp_file" "$manifest_file"
    
    echo -e "${GREEN}✓ AndroidManifest.xml updated with required permissions and features${NC}"
}

# Function to add layout files
add_layout_files() {
    local res_dir="$1"
    
    # Create layout directory if it doesn't exist
    mkdir -p "$res_dir/layout"
    
    # Create activity_pose_ar.xml layout file
    cat > "$res_dir/layout/activity_pose_ar.xml" << 'EOL'
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- AR Scene View for AR rendering -->
    <io.github.sceneview.ar.ArSceneView
        android:id="@+id/arSceneView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <!-- Overlay for pose visualization -->
    <FrameLayout
        android:id="@+id/poseOverlay"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <!-- Controls Container -->
    <LinearLayout
        android:id="@+id/controlsContainer"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:background="#80000000"
        android:padding="16dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        android:layout_margin="16dp">

        <!-- Processing indicator -->
        <TextView
            android:id="@+id/processingIndicator"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Processing..."
            android:textColor="#FFFFFF"
            android:visibility="gone" />

    </LinearLayout>

</androidx.constraintlayout.widget.ConstraintLayout>
EOL

    echo -e "${GREEN}✓ Layout files created${NC}"
}

# Function to create a sample activity to launch AR
create_launcher_activity() {
    local src_dir="$1"
    local package_name="$2"
    
    # Create the package directory structure
    package_dir=$(echo "$package_name" | sed 's/\./\//g')
    mkdir -p "$src_dir/main/java/$package_dir"
    
    # Create a simple launcher activity
    cat > "$src_dir/main/java/$package_dir/CameraLauncherActivity.kt" << EOL
package $package_name

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CameraLauncherActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_launcher)

        findViewById<Button>(R.id.btnStartAR).setOnClickListener {
            if (hasCameraPermission()) {
                startARActivity()
            } else {
                requestCameraPermission()
            }
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    private fun startARActivity() {
        val intent = Intent(this, ar.PoseARActivity::class.java)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startARActivity()
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
EOL

    # Create a layout for the launcher activity
    mkdir -p "$src_dir/main/res/layout"
    cat > "$src_dir/main/res/layout/activity_camera_launcher.xml" << 'EOL'
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <TextView
        android:id="@+id/tvTitle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Android Camera Integration"
        android:textSize="24sp"
        android:textStyle="bold"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginTop="32dp" />

    <TextView
        android:id="@+id/tvDescription"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="This sample demonstrates integration of Camera, ML Kit pose detection, and AR capabilities."
        android:textSize="16sp"
        android:gravity="center"
        app:layout_constraintTop_toBottomOf="@id/tvTitle"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginTop="16dp" />

    <Button
        android:id="@+id/btnStartAR"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Start AR Experience"
        android:padding="16dp"
        app:layout_constraintTop_toBottomOf="@id/tvDescription"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintBottom_toBottomOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
EOL

    # Update the AndroidManifest to include the launcher activity
    manifest_file="$src_dir/main/AndroidManifest.xml"
    temp_file=$(mktemp)
    
    # Find where the PoseARActivity is declared
    ar_activity_line=$(grep -n "PoseARActivity" "$manifest_file" | cut -d':' -f1)
    
    # Insert our launcher activity after that line
    head -n $((ar_activity_line + 5)) "$manifest_file" > "$temp_file"
    
    cat >> "$temp_file" << EOL
        <!-- Camera Launcher Activity -->
        <activity
            android:name=".$package_name.CameraLauncherActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
EOL
    
    # Continue with the rest of the file
    tail -n +$((ar_activity_line + 6)) "$manifest_file" >> "$temp_file"
    
    # Replace the original file
    mv "$temp_file" "$manifest_file"
    
    echo -e "${GREEN}✓ Launcher activity created${NC}"
}

# Main logic
main() {
    echo -e "${BLUE}Let's set up your Android camera integration project!${NC}"
    echo ""
    
    local project_dir
    local package_name
    
    # Determine if we're in an existing project or need to create one
    if is_android_project; then
        echo -e "${YELLOW}Detected existing Android project.${NC}"
        project_dir="."
        
        # Ask for the package name
        echo -e "${BLUE}What's your app's package name? (e.g., com.example.mycameraapp)${NC}"
        read -p "> " package_name
    else
        echo -e "${YELLOW}No Android project detected.${NC}"
        echo -e "${BLUE}We'll need to set up a new project.${NC}"
        
        # Ask for project name
        echo -e "${BLUE}What would you like to name your project?${NC}"
        read -p "> " project_name
        
        # Convert to lowercase and remove spaces
        project_dir=$(echo "$project_name" | tr '[:upper:]' '[:lower:]' | tr ' ' '_')
        
        # Ask for package name
        echo -e "${BLUE}What package name would you like to use? (e.g., com.example.mycameraapp)${NC}"
        read -p "> " package_name
        
        echo -e "${YELLOW}Creating new Android project...${NC}"
        
        # Create directories for a basic project structure
        mkdir -p "$project_dir/app/src/main/java"
        mkdir -p "$project_dir/app/src/main/res/layout"
        
        # Create basic build files
        cat > "$project_dir/build.gradle" << EOL
// Top-level build file
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:8.2.2'
        classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22'
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
EOL

        cat > "$project_dir/app/build.gradle" << EOL
plugins {
    id 'com.android.application'
    id 'kotlin-android'
}

android {
    compileSdkVersion 34
    
    defaultConfig {
        applicationId "$package_name"
        minSdkVersion 24
        targetSdkVersion 34
        versionCode 1
        versionName "1.0"
    }
    
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = '17'
    }
}

dependencies {
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
}
EOL

        # Create a basic AndroidManifest.xml
        cat > "$project_dir/app/src/main/AndroidManifest.xml" << EOL
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="$package_name">

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.AppCompat.Light.DarkActionBar">
        
    </application>

</manifest>
EOL

        # Create settings.gradle
        cat > "$project_dir/settings.gradle" << EOL
include ':app'
rootProject.name = "$project_name"
EOL

        echo -e "${GREEN}✓ Basic project structure created${NC}"
    fi
    
    # Now proceed with the integration
    echo -e "${BLUE}Integrating camera, ML Kit, and AR components...${NC}"
    
    # Update build.gradle for the app module
    if [ -f "$project_dir/app/build.gradle.kts" ]; then
        add_dependencies "$project_dir/app/build.gradle.kts"
    else
        add_dependencies "$project_dir/app/build.gradle"
    fi
    
    # Copy code samples
    copy_code_samples "$project_dir/app"
    
    # Add layout files
    add_layout_files "$project_dir/app/src/main/res"
    
    # Update the manifest
    add_manifest_entries "$project_dir/app/src/main/AndroidManifest.xml"
    
    # Create the launcher activity
    create_launcher_activity "$project_dir/app/src" "$package_name"
    
    echo ""
    echo -e "${GREEN}✅ Setup completed successfully!${NC}"
    echo ""
    echo -e "${BLUE}What's next:${NC}"
    echo -e "1. Open your project in ${YELLOW}Android Studio${NC}"
    echo -e "2. Sync Gradle to download the dependencies"
    echo -e "3. Run the app on a device that supports ARCore"
    echo ""
    echo -e "${YELLOW}Note:${NC} For the best experience, use a physical device with ARCore support."
    echo -e "You can check ARCore supported devices at: https://developers.google.com/ar/devices"
    echo ""
    echo -e "${BLUE}Happy coding!${NC}"
}

# Run the main function
main

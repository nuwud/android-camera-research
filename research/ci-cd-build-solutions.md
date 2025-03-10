# CI/CD Solutions for Android Camera Projects

## Overview

Continuous Integration and Continuous Deployment (CI/CD) pipelines often encounter unique challenges when building Android applications that integrate camera functionality, ML Kit, and ARCore. This guide provides strategies to overcome common build failures and ensure reliable automated builds.

## Common CI/CD Build Issues

### 1. Gradle Dependency Conflicts

In CI environments, dependency resolution can fail due to version conflicts between camera libraries, ML Kit, and ARCore.

#### Key Symptoms
- Build fails with `Duplicate class` errors
- Dependency resolution errors mentioning conflicting versions
- Build fails when downloading dependencies with cryptic network errors

#### Solutions

**Create a centralized dependency management file:**

```kotlin
// buildSrc/src/main/kotlin/Dependencies.kt
object Versions {
    // Core Android
    const val compileSdk = 34
    const val minSdk = 24
    const val targetSdk = 34
    
    // Libraries
    const val cameraX = "1.3.1"
    const val mlKit = "18.0.0-beta3"
    const val arCore = "1.42.0"
}

fun applyVersionResolutionStrategy(project: Project) {
    project.configurations.all {
        resolutionStrategy {
            // Force specific versions to avoid conflicts
            force("androidx.camera:camera-camera2:${Versions.cameraX}")
            force("androidx.camera:camera-lifecycle:${Versions.cameraX}")
            force("androidx.camera:camera-view:${Versions.cameraX}")
            force("com.google.mlkit:pose-detection:${Versions.mlKit}")
            force("com.google.ar:core:${Versions.arCore}")
            
            // Avoid using dynamic version numbers
            failOnDynamicVersions()
            failOnChangingVersions()
        }
    }
}
```

**Configure dependency caching in GitHub Actions:**

```yaml
- name: Cache Gradle packages
  uses: actions/cache@v3
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/wrapper
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
    restore-keys: |
      ${{ runner.os }}-gradle-
```

### 2. Native Code (NDK) Issues

Projects with ARCore or ML Kit often rely on native code that can break in CI environments.

#### Key Symptoms
- Build fails with `Native library (lib/x86/...) not found`
- Errors related to ABI configurations
- Inconsistent build failures across different CI environments

#### Solutions

**Explicitly define NDK version and ABI configurations:**

```groovy
// In app/build.gradle
android {
    ndkVersion "25.2.9519653" // Use explicit version
    
    defaultConfig {
        ndk {
            abiFilters "armeabi-v7a", "arm64-v8a", "x86", "x86_64"
        }
    }
    
    packagingOptions {
        pickFirst "**/libc++_shared.so"
        pickFirst "**/libfbjni.so"
    }
}
```

**Configure NDK in GitHub Actions:**

```yaml
- name: Setup Android SDK and NDK
  uses: android-actions/setup-android@v2
  with:
    ndk-version: '25.2.9519653'
    sdk-version: '34'

- name: Print NDK Path for debugging
  run: echo $ANDROID_NDK_HOME
```

### 3. Memory and Timeout Issues

Camera and ML projects often require more memory and time to build.

#### Key Symptoms
- Out of memory errors during build
- Build timeouts in CI
- Gradle daemon unexpectedly exits

#### Solutions

**Configure Gradle memory settings:**

Create a `gradle.properties` file:

```properties
# Increase memory allocations
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g -Dkotlin.daemon.jvm.options=-Xmx2g

# Enable parallel builds
org.gradle.parallel=true

# Enable configuration on demand
org.gradle.configureondemand=true

# Enable the build cache
org.gradle.caching=true
```

**Adjust GitHub Actions timeout and memory:**

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    timeout-minutes: 30  # Increase from default
    
    steps:
      - name: Increase SWAP space
        uses: pierotofy/set-swap-space@master
        with:
          swap-size-gb: 10
      
      - name: Build with Gradle
        run: ./gradlew assembleDebug --max-workers=2 --stacktrace
```

### 4. React Native Integration Issues

When integrating React Native with Camera/AR capabilities, additional issues can arise.

#### Key Symptoms
- `npm install` or `yarn install` failures in CI
- Postinstall script failures when patching native modules
- JavaScript bundling errors in CI environment

#### Solutions

**Configure npm cache and explicit versions:**

```yaml
- name: Setup Node.js
  uses: actions/setup-node@v3
  with:
    node-version: '16.20.0' # Use explicit version
    cache: 'npm'

- name: Install dependencies with retry
  uses: nick-invision/retry@v2
  with:
    timeout_minutes: 15
    max_attempts: 3
    command: npm ci --no-audit --prefer-offline
```

**Create a CI-specific .npmrc file:**

```
# .npmrc.ci
fetch-retries=5
fetch-retry-mintimeout=20000
fetch-retry-maxtimeout=120000
network-timeout=300000
loglevel=verbose
legacy-peer-deps=true
force=true
```

## Complete GitHub Actions Workflow Example

Here's a complete workflow file that addresses the common issues:

```yaml
name: Android Camera App CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    timeout-minutes: 45

    steps:
    - uses: actions/checkout@v3

    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle

    - name: Setup Android SDK
      uses: android-actions/setup-android@v2
      with:
        ndk-version: '25.2.9519653'
        sdk-version: '34'
        cmake-version: '3.22.1'

    - name: Increase SWAP space
      uses: pierotofy/set-swap-space@master
      with:
        swap-size-gb: 10

    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-

    # For React Native projects
    - name: Setup Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '16.20.0'
        cache: 'npm'

    - name: Install NPM dependencies with retry
      uses: nick-invision/retry@v2
      with:
        timeout_minutes: 15
        max_attempts: 3
        command: |
          cp .npmrc.ci .npmrc
          npm ci --no-audit --prefer-offline

    - name: Check for common dependency issues
      run: |
        ./gradlew :app:dependencies | grep -E 'androidx.camera|com.google.mlkit|com.google.ar'

    - name: Grant execute permission for gradlew
      run: chmod +x gradlew

    - name: Build with Gradle
      run: ./gradlew assembleDebug --max-workers=2 --stacktrace

    - name: Archive APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk

    # Optional: Run tests
    - name: Run tests
      run: ./gradlew test --max-workers=2
```

## CI/CD Best Practices for Camera Projects

### 1. Use Matrix Builds for ABI Testing

Test your app across different ABIs to ensure ML Kit and ARCore work correctly:

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        abi: [armeabi-v7a, arm64-v8a, x86, x86_64]
      fail-fast: false

    steps:
      # ... other steps ...

      - name: Build with specific ABI
        run: ./gradlew assembleDebug -PtargetAbi=${{ matrix.abi }}
```

### 2. Implement Dependency Health Checks

Regularly audit your dependencies for conflicts:

```yaml
- name: Dependency health check
  run: |
    ./gradlew :app:dependencyInsight --dependency androidx.camera:camera-camera2
    ./gradlew :app:dependencyInsight --dependency com.google.mlkit:pose-detection
    ./gradlew :app:dependencyInsight --dependency com.google.ar:core
```

### 3. Split large builds into multiple jobs

For complex camera apps, split the workflow:

```yaml
jobs:
  dependencies:
    # Job to check and prepare dependencies
    runs-on: ubuntu-latest
    steps:
      # Check dependencies and generate lockfiles

  build:
    needs: dependencies
    runs-on: ubuntu-latest
    steps:
      # Actual build steps

  test:
    needs: build
    runs-on: ubuntu-latest
    steps:
      # Testing steps
```

### 4. Set Up Build Caching Properly

Optimize builds with proper caching:

```yaml
- name: Cache NPM packages
  uses: actions/cache@v3
  with:
    path: ~/.npm
    key: ${{ runner.os }}-npm-${{ hashFiles('**/package-lock.json') }}
    restore-keys: ${{ runner.os }}-npm-

- name: Cache Gradle Build
  uses: actions/cache@v3
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/wrapper
      ~/.android/build-cache
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}-${{ hashFiles('**/gradle-wrapper.properties') }}
    restore-keys: |
      ${{ runner.os }}-gradle-
```

## Conclusion

Building Android camera applications with ML Kit and AR capabilities in CI/CD environments requires careful configuration and attention to detail. By implementing the solutions provided in this guide, you can create more reliable, reproducible builds that are less prone to the common issues that plague camera-focused Android applications.

These strategies also improve local build consistency, making it easier for team members to collaborate on complex camera integrations without running into environment-specific build problems.

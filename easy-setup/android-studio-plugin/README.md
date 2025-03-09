# Android Camera Integration Plugin for Android Studio

## Overview

This Android Studio plugin provides a simple wizard interface to integrate Camera2 API, ML Kit pose detection, and ARCore features into your Android projects with just a few clicks.

## Features

- One-click setup of all necessary dependencies
- Automatic configuration of AndroidManifest.xml
- Sample code generation for common camera tasks
- Integration with ML Kit for pose detection
- Integration with ARCore for augmented reality

## Installation

### From JetBrains Plugin Repository

1. Open Android Studio
2. Go to File > Settings > Plugins
3. Click on "Marketplace" tab
4. Search for "Android Camera Integration"
5. Click "Install"

### Manual Installation

1. Download the plugin .jar file from the [releases](https://github.com/nuwud/android-camera-research/releases)
2. Open Android Studio
3. Go to File > Settings > Plugins
4. Click on the gear icon and select "Install Plugin from Disk..."
5. Navigate to the downloaded .jar file and select it
6. Restart Android Studio

## Usage

1. Open your Android project in Android Studio
2. Go to Tools > Android Camera Integration
3. Follow the wizard prompts to select the components you want to integrate
4. The plugin will automatically add dependencies, update AndroidManifest.xml, and create sample code

## Requirements

- Android Studio 4.2 or later
- Min SDK 24 in your app's build.gradle

## Development Notes

This is a skeleton implementation for the Android Studio plugin. To fully implement it, you would need to:

1. Complete the dependency insertion methods
2. Add code for sample file generation
3. Create proper plugin descriptors
4. Package as a proper IntelliJ plugin

The goal is to provide a visual, intuitive way for Android developers to add camera and vision features to their apps without manual setup.

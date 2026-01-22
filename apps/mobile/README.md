# Eloquia Mobile App

The Eloquia mobile app is a Kotlin Multiplatform application for tracking and managing stuttering patterns. It ships a shared Compose UI and common business logic, with Android and iOS targets.

## Tech Stack

- **Kotlin Multiplatform**
- **Compose Multiplatform**
- **Ktor** (networking)
- **Koin** (DI)
- **Coil** (image loading)
- **Kotlinx Serialization** (JSON)

## Setup

> [!IMPORTANT]
> First-time Gradle syncs can take a while due to Kotlin/Android/iOS dependencies.

1. **Open the project**

	- Android Studio → Open → `apps/mobile`
	- Wait for Gradle sync to finish

2. **Run on Android**

	- Select a device/emulator
	- Click Run (or press `Shift + F10`)

3. **Run on iOS (macOS only)**

	- Open `apps/mobile/iosApp/iosApp.xcodeproj` in Xcode
	- Choose a simulator/device
	- Click Run (or press `Cmd + R`)

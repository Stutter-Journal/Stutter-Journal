<!-- Improved compatibility of back to top link: See: https://github.com/othneildrew/Best-README-Template/pull/73 -->
<a id="readme-top"></a>

<!-- PROJECT SHIELDS -->
[![Contributors][contributors-shield]][contributors-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![Apache License][license-shield]][license-url]

<!-- PROJECT LOGO -->
<br />
<div align="center">
  <h1 align="center">🗣️ Eloquia - Stutter Journal</h1>

  <p align="center">
    A cross-platform mobile application with Python backend for tracking and managing stuttering patterns
    <br />
    <a href="https://github.com/joyalissa13/Stutter-Journal"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    <a href="https://github.com/joyalissa13/Stutter-Journal/issues/new?labels=bug">Report Bug</a>
    ·
    <a href="https://github.com/joyalissa13/Stutter-Journal/issues/new?labels=enhancement">Request Feature</a>
  </p>
</div>

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li><a href="#about-the-project">About The Project</a></li>
    <li><a href="#built-with">Built With</a></li>
    <li><a href="#project-structure">Project Structure</a></li>
    <li><a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#mobile-app-setup">Mobile App Setup</a></li>
        <li><a href="#backend-setup">Backend Setup</a></li>
      </ul>
    </li>
    <li><a href="#development-notes">Development Notes</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
  </ol>
</details>

## About The Project

**Eloquia - Stutter Journal** is a comprehensive journaling application designed to help individuals track, manage, and understand their stuttering patterns. The project consists of a cross-platform mobile application built with Kotlin Multiplatform and Jetpack Compose, paired with a Python backend for data processing and analysis.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Built With

### Mobile Frontend

- [![Kotlin][Kotlin]][Kotlin-url]
- [![Compose Multiplatform][Compose]][Compose-url]
- [![Android][Android]][Android-url]
- [![iOS][iOS]][iOS-url]

**Key Technologies:**

- **Kotlin Multiplatform** (2.2.21) - Shared business logic across platforms
- **Compose Multiplatform** (1.9.3) - Declarative UI framework
- **Jetpack Compose** - Modern Android UI toolkit
- **Ktor** (3.3.2) - Networking client
- **Koin** (4.1.1) - Dependency injection
- **Coil** (3.3.0) - Image loading
- **Material 3** - Material Design components
- **Navigation Compose** - Type-safe navigation
- **Kotlinx Serialization** - JSON serialization

### Backend

- [![Python][Python]][Python-url]
- [![pytest][pytest]][pytest-url]

**Key Technologies:**

- **Python** (3.13.2+) - Modern Python with latest features
- **pytest** (9.0.0+) - Testing framework
- **PyScaffold** (4.5) - Project structure and tooling
- **setuptools-scm** - Version management from git tags
- **pre-commit** - Git hooks for code quality
- **Ruff** - Fast Python linter and formatter
- **GitHub Actions & Cirrus CI** - Continuous integration

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Project Structure

```
Stutter-Journal/
├── apps/
│   ├── mobile/              # Kotlin Multiplatform Mobile App
│   │   ├── composeApp/      # Main application module
│   │   │   └── src/
│   │   │       ├── androidMain/   # Android-specific code
│   │   │       ├── commonMain/    # Shared business logic
│   │   │       └── iosMain/       # iOS-specific code
│   │   ├── core/
│   │   │   └── theme/       # Shared theming and design system
│   │   └── iosApp/          # iOS app wrapper
│   │
│   └── backend/             # Python Backend
│       ├── src/
│       │   └── eloquia/
│       │       └── backend/
│       ├── tests/
│       └── docs/
│
└── LICENSE                  # Apache 2.0 License
```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Getting Started

Follow these instructions to set up the project locally for development.

### Prerequisites

#### For Mobile Development

- **Java Development Kit (JDK)**: JDK 23 or higher
- **Android Studio**: Latest stable version (for Android development)
- **Xcode**: 15.0+ (for iOS development, macOS only)
- **Gradle**: 8.14.3+ (usually bundled with Android Studio)

#### For Backend Development

- **Python**: 3.13.2 or higher
- **uv**: Modern Python package installer

  ```sh
  # macOS/Linux
  brew install uv

  # Windows
  winget install astral-sh.uv
  ```

- **pre-commit**: Git hooks framework

  ```sh
  # macOS/Linux
  brew install pre-commit

  # Windows
  winget install pre-commit
  ```

### Mobile App Setup

> [!IMPORTANT]
> The first-time setup will download a large number of dependencies for Kotlin Multiplatform, Android, and iOS. This process can take significant time depending on your internet connection.

1. **Clone the repository**

   ```sh
   git clone https://github.com/joyalissa13/Stutter-Journal.git
   cd Stutter-Journal/apps/mobile
   ```

2. **Set ANDROID_HOME (if needed)**

   IntelliJ IDEA usually detects this automatically, but if needed:

   ```sh
   # macOS/Linux
   export ANDROID_HOME=$HOME/Library/Android/sdk

   # Windows
   set ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk
   ```

3. **Open the project in Android Studio or IntelliJ IDEA**
   - File → Open → Select `apps/mobile` directory
   - Wait for Gradle sync to complete (this will take a while the first time)

4. **Run the app**

   **For Android:**
   - Select an Android device/emulator from the device dropdown
   - Click the Run button or press `Shift + F10`

   **For iOS (macOS only):**
   - Open `apps/mobile/iosApp/iosApp.xcodeproj` in Xcode
   - Select a simulator or connected device
   - Click Run or press `Cmd + R`

### Backend Setup

1. **Navigate to the backend directory**

   ```sh
   cd apps/backend
   ```

2. **Install pre-commit hooks**

   ```sh
   pre-commit install
   ```

3. **Update pre-commit to latest versions (recommended)**

   ```sh
   pre-commit autoupdate
   ```

4. **Create a virtual environment**

   ```sh
   uv venv
   ```

5. **Activate the virtual environment**

   ```sh
   # macOS/Linux
   source .venv/bin/activate

   # Windows
   .venv\Scripts\activate
   ```

6. **Install setuptools dependencies**

   ```sh
   uv pip install -U setuptools setuptools_scm wheel
   ```

7. **Install the project in editable mode**

   ```sh
   uv pip install -e .
   ```

8. **Sync dependencies**

   ```sh
   uv sync
   ```

9. **Build the project**

   ```sh
   uv build
   ```

10. **Run the example skeleton script**

    ```sh
    uv run -m eloquia.backend.skeleton 42
    ```

### Running Tests

**Backend tests:**

```sh
cd apps/backend
uv pip install -e ".[testing]"
uv run pytest
```

**Linting and formatting (Backend):**

```sh
# Check code style
uvx ruff check

# Format code
uvx ruff format
```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Development Notes

### Mobile App Design

The mobile application's theme was built using [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/).

**Design Choices:**

- **Primary Color**: Sea Green
- **Typography**:
  - Display Font: **Outfit**
  - Body Font: **Inter**
- **Design System**: Material Design 3

### Known Issues & Tips

> [!CAUTION]
> **Large dependency downloads**: The amount of libraries required for KMP itself and all complementary ones for Android and iOS are HUGE. Loading the project for the first time will take a considerable amount of time. Be prepared and patient during the initial setup.

> [!NOTE]
> **ANDROID_HOME**: If necessary, the `ANDROID_HOME` environment variable needs to be set in the project structure for the KMP mobile frontend. However, IntelliJ IDEA is usually intelligent enough to detect this automatically.

**Virtual Device Manager**: Android Studio includes a device manager for spinning up virtual phones. Setting up a virtual device is extremely simple and fast - just select your desired phone model and API level.

### Backend Setup Details

The backend was bootstrapped using PyScaffold with the following command:

```sh
putup --pre-commit --github-actions --cirrus --license=MIT --namespace eloquia backend --force
```

The license was later changed from MIT to **Apache 2.0** to match the frontend.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## License

Distributed under the Apache License 2.0. See `LICENSE` for more information.

Copyright [2025] [Group 3 developing the Stutter-Journal]

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Acknowledgments

- [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/) - For the beautiful design system
- [PyScaffold](https://pyscaffold.org/) - Python project scaffolding
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) - Cross-platform development
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern Android UI
- [Best-README-Template](https://github.com/othneildrew/Best-README-Template) - README structure inspiration

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

<!-- MARKDOWN LINKS & IMAGES -->
[contributors-shield]: https://img.shields.io/github/contributors/joyalissa13/Stutter-Journal.svg?style=for-the-badge
[contributors-url]: https://github.com/joyalissa13/Stutter-Journal/graphs/contributors
[stars-shield]: https://img.shields.io/github/stars/joyalissa13/Stutter-Journal.svg?style=for-the-badge
[stars-url]: https://github.com/joyalissa13/Stutter-Journal/stargazers
[issues-shield]: https://img.shields.io/github/issues/joyalissa13/Stutter-Journal.svg?style=for-the-badge
[issues-url]: https://github.com/joyalissa13/Stutter-Journal/issues
[license-shield]: https://img.shields.io/github/license/joyalissa13/Stutter-Journal.svg?style=for-the-badge
[license-url]: https://github.com/joyalissa13/Stutter-Journal/blob/main/LICENSE

<!-- Technology Badges -->
[Kotlin]: https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white
[Kotlin-url]: https://kotlinlang.org/
[Compose]: https://img.shields.io/badge/Compose%20Multiplatform-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white
[Compose-url]: https://www.jetbrains.com/lp/compose-multiplatform/
[Android]: https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white
[Android-url]: https://developer.android.com/
[iOS]: https://img.shields.io/badge/iOS-000000?style=for-the-badge&logo=apple&logoColor=white
[iOS-url]: https://developer.apple.com/ios/
[Python]: https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white
[Python-url]: https://www.python.org/
[pytest]: https://img.shields.io/badge/pytest-0A9EDC?style=for-the-badge&logo=pytest&logoColor=white
[pytest-url]: https://pytest.org/

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
        <li><a href="#desktop-web-setup-portal--bff">Desktop Web Setup (Portal + BFF)</a></li>
        <li><a href="#backend-api-setup-go">Backend API Setup (Go)</a></li>
        <li><a href="#analyzer-setup-python">Analyzer Setup (Python)</a></li>
        <li><a href="#mobile-app-setup">Mobile App Setup</a></li>
      </ul>
    </li>
    <li><a href="#development-notes">Development Notes</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
  </ol>
</details>

## About The Project

**Eloquia - Stutter Journal** is a multi-platform journaling system that helps individuals track, manage, and understand stuttering patterns. The project includes:

- A **Kotlin Multiplatform** mobile app for Android and iOS.
- A **web portal** (Angular + Nx) for clinicians and admins.
- A **BFF service** (NestJS) that powers the web portal.
- A **Go backend API** with PostgreSQL for data storage.
- A **Python analyzer** for data processing workflows.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Built With

### Mobile App

- [![Kotlin][Kotlin]][Kotlin-url]
- [![Compose Multiplatform][Compose]][Compose-url]
- [![Android][Android]][Android-url]
- [![iOS][iOS]][iOS-url]

**Key Technologies:**

- **Kotlin Multiplatform** - Shared business logic across platforms
- **Compose Multiplatform** - Declarative UI framework
- **Ktor** - Networking client
- **Koin** - Dependency injection
- **Coil** - Image loading
- **Kotlinx Serialization** - JSON serialization

### Desktop Web (Portal + BFF)

- [![Angular][Angular]][Angular-url]
- [![Nx][Nx]][Nx-url]
- [![Node][Node]][Node-url]

**Key Technologies:**

- **Angular** - Web application framework
- **Nx** - Monorepo tooling
- **NestJS** - BFF service framework
- **Tailwind CSS** - UI styling utilities

### Backend API

- [![Go][Go]][Go-url]
- [![Postgres][Postgres]][Postgres-url]

**Key Technologies:**

- **Go** - API service runtime
- **PostgreSQL** - Primary database
- **Ent** - ORM and schema tooling
- **Atlas** - Database migrations

### Analyzer

- [![Python][Python]][Python-url]
- [![pytest][pytest]][pytest-url]

**Key Technologies:**

- **Python** - Data analysis workflows
- **pytest** - Testing framework
- **Ruff** - Linting and formatting

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Project Structure

```
Stutter-Journal/
├── apps/
│   ├── mobile/               # Kotlin Multiplatform mobile app
│   │   ├── composeApp/        # Shared UI + business logic
│   │   └── iosApp/            # iOS wrapper
│   ├── desktop/              # Nx workspace (Angular + NestJS)
│   │   ├── apps/portal        # Web portal (Angular SSR)
│   │   ├── apps/bff           # BFF service (NestJS)
│   │   └── libs/              # Shared UI + feature libs
│   ├── backend/              # Go API service
│   │   ├── cmd/api            # API entrypoint
│   │   ├── ent/               # Ent schemas & migrations
│   │   └── internal/          # Core services
│   └── analyzer/             # Python analysis tooling
│       ├── src/              # Analyzer package
│       └── tests/            # Test suite
└── LICENSE                   # Apache 2.0 License
```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Getting Started

Follow these instructions to set up the project locally for development.

### Prerequisites

- **Node.js** 20+ and **pnpm** 10 (desktop web)
- **Go** 1.25+ (backend API)
- **Docker Desktop** (PostgreSQL for backend)
- **Python** 3.13+ and **uv** (analyzer)
- **Android Studio** and **Xcode** (mobile app)

### Desktop Web Setup (Portal + BFF)

1. **Install dependencies**

  ```sh
  cd apps/desktop
  pnpm install --config.confirmModulesPurge=false
  ```

2. **Run the portal (starts the BFF automatically)**

  ```sh
  pnpm nx serve portal
  ```

3. **Open the portal**

  ```sh
  http://localhost:4200
  ```

**Optional:** Run the BFF alone

```sh
pnpm nx serve bff
```

### Backend API Setup (Go)

1. **Review environment variables**

  The backend expects database settings in `apps/backend/.env`.

2. **Start the database**

  ```sh
  cd apps/backend
  make db-up
  ```

3. **Run migrations**

  ```sh
  make migrate
  ```

4. **Run the API**

  ```sh
  make run
  ```

  The API starts on `http://localhost:8080` with health endpoints at `/health` and `/ready`.

### Analyzer Setup (Python)

1. **Create and sync the environment**

  ```sh
  cd apps/analyzer
  uv venv
  uv sync
  ```

2. **Run tests**

  ```sh
  uv run pytest
  ```

### Mobile App Setup

> [!IMPORTANT]
> First-time setup downloads a large number of Kotlin, Android, and iOS dependencies.

1. **Open the project**

  - Android Studio → Open → `apps/mobile`
  - Wait for Gradle sync to complete

2. **Run the app**

  **Android:** select a device/emulator and run.

  **iOS (macOS only):** open `apps/mobile/iosApp/iosApp.xcodeproj` in Xcode and run.

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

Generating new migrations, and forcing atlas to migrate with new diffs is a pain, as it forces you to first generate the actual migration using ent (which is totally fine), but then you have to execute something like this:

```bash
atlas migrate diff remove_logo_url --to "ent://ent/schema" --dev-url "postgres://melkey:password1234@localhost:5432/blueprint?sslmode=disable" --dir "file://ent/migrate/migrations"
```

every single time, which is cumbersome. I have to think of something better, TODO for later me.

### Container Images (GHCR)

The GitHub Actions workflow `.github/workflows/build-images.yml` builds and pushes the
backend, migrate, and desktop images to GHCR.

**Permissions & secrets:**
- The workflow uses `GITHUB_TOKEN` with `packages: write` (already configured).
- If pushes fail (e.g. "installation does not exist"), add a repo secret `GHCR_TOKEN`
  that is a PAT with `write:packages` and `read:packages` (plus `repo` if the repo is private).
- If GHCR packages are private, your cluster will need an `imagePullSecret` in each namespace,
  or you can make the packages public.
- Flux image automation also needs GHCR credentials when packages are private. Create a
  docker-registry secret in `flux-system` and reference it as `ghcr-credentials`:

```bash
kubectl -n flux-system create secret docker-registry ghcr-credentials \
  --docker-server=ghcr.io \
  --docker-username=stutter-journal \
  --docker-password=YOUR_PAT_WITH_read:packages \
  --docker-email=you@example.com
```

- To make the same secret available in all app namespaces, annotate it for
  the Emberstack reflector (see `deploy/flux-image-automation/ghcr-credentials.template.yaml`):

```bash
kubectl -n flux-system annotate secret ghcr-credentials \
  reflector.v1.k8s.emberstack.com/reflection-allowed="true" \
  reflector.v1.k8s.emberstack.com/reflection-allowed-namespaces=".*" \
  reflector.v1.k8s.emberstack.com/reflection-auto-enabled="true" \
  reflector.v1.k8s.emberstack.com/reflection-auto-namespaces="eloquia-.*"
```

### Backend Migrations (Atlas)

Generating new migrations uses Atlas with Ent schemas. Example:

```bash
atlas migrate diff add_new_feature --to "ent://ent/schema" --dev-url "postgres://melkey:password1234@localhost:5432/blueprint?sslmode=disable" --dir "file://ent/migrate/migrations"
```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Contributing

Contributions are welcome. Please open an issue or submit a pull request with a clear description of the change.

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
[Angular]: https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white
[Angular-url]: https://angular.dev/
[Nx]: https://img.shields.io/badge/Nx-143055?style=for-the-badge&logo=nx&logoColor=white
[Nx-url]: https://nx.dev/
[Node]: https://img.shields.io/badge/Node.js-339933?style=for-the-badge&logo=nodedotjs&logoColor=white
[Node-url]: https://nodejs.org/
[Go]: https://img.shields.io/badge/Go-00ADD8?style=for-the-badge&logo=go&logoColor=white
[Go-url]: https://go.dev/
[Postgres]: https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white
[Postgres-url]: https://www.postgresql.org/

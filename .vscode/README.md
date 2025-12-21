# VS Code Configuration Guide

This directory contains comprehensive VS Code configurations for the Eloquia - Stutter Journal project.

## 📁 Files Overview

### `tasks.json`
Defines automated tasks for building, testing, and running both the backend and mobile applications.

### `launch.json`
Debug configurations for Python backend and Android/iOS mobile applications.

### `settings.json`
Workspace-specific settings for Python, Kotlin, Gradle, and general editor behavior.

### `extensions.json`
Recommended VS Code extensions for optimal development experience.

---

## 🎯 Quick Start

### First Time Setup

1. **Open the workspace in VS Code**
   ```bash
   code /path/to/Stutter-Journal
   ```

2. **Install recommended extensions**
   - Press `Cmd+Shift+P` (macOS) or `Ctrl+Shift+P` (Windows/Linux)
   - Type "Extensions: Show Recommended Extensions"
   - Click "Install All"

3. **Run the full setup task**
   - Press `Cmd+Shift+P` / `Ctrl+Shift+P`
   - Type "Tasks: Run Task"
   - Select "Full Setup: Backend and Mobile"

---

## 🐍 Backend Tasks

### Setup Tasks
- **Backend: Setup Virtual Environment** - Creates Python virtual environment
- **Backend: Install Dependencies** - Installs all backend dependencies
- **Backend: Install Testing Dependencies** - Installs pytest and testing tools
- **Backend: Install Pre-commit Hooks** - Sets up git pre-commit hooks

### Build Tasks
- **Backend: Build** - Builds the backend package using `uv build`

### Testing Tasks
- **Backend: Run Tests** ⭐ (Default test task)
- **Backend: Run Tests with Coverage** - Runs tests with HTML coverage report

### Code Quality Tasks
- **Backend: Lint (Ruff Check)** - Checks code style and errors
- **Backend: Format (Ruff Format)** - Auto-formats Python code
- **Backend: Lint and Format** - Runs both lint and format

### Pre-commit Tasks
- **Backend: Update Pre-commit Hooks** - Updates hooks to latest versions
- **Backend: Run Pre-commit on All Files** - Runs all pre-commit hooks

### Run Tasks
- **Backend: Run Skeleton Example** - Runs the example skeleton module

---

## 📱 Mobile Tasks

### Build Tasks
- **Mobile: Build All** ⭐ (Default build task)
- **Mobile: Build Android Debug APK** - Builds debug APK for Android
- **Mobile: Build Android Release APK** - Builds release APK for Android
- **Mobile: Build iOS Framework** - Builds iOS framework (macOS only)

### Clean Tasks
- **Mobile: Clean Build** - Cleans all build artifacts

### Testing Tasks
- **Mobile: Run Android Tests** - Runs Android unit tests

### Gradle Tasks
- **Mobile: Gradle Sync** - Refreshes Gradle dependencies
- **Mobile: List All Tasks** - Shows all available Gradle tasks
- **Mobile: Check Dependencies** - Shows dependency tree

### Installation Tasks
- **Mobile: Install Android Debug** - Installs debug APK to connected device/emulator

### iOS Tasks
- **Mobile: Run iOS Simulator** - Opens iOS Simulator (macOS only)

---

## 🔧 Composite Tasks

### Multi-Project Tasks
- **Full Setup: Backend and Mobile** - Complete first-time setup for both projects
- **Build: All Projects** - Builds both backend and mobile
- **Test: All Projects** - Runs tests for both projects
- **Clean: All Projects** - Cleans all build artifacts

---

## 🐛 Debug Configurations

### Python Backend Debugging

#### Available Configurations:
1. **Backend: Debug Skeleton Module**
   - Debugs the example skeleton module
   - Pre-launches dependency installation
   - Args: `[42]`

2. **Backend: Debug Current Python File**
   - Debugs the currently open Python file
   - Useful for quick script debugging

3. **Backend: Debug Tests**
   - Debugs all tests with coverage
   - Pre-installs testing dependencies

4. **Backend: Debug Current Test File**
   - Debugs only the currently open test file

5. **Backend: Debug Specific Test**
   - Debugs a specific test function
   - Example: `test_skeleton.py::test_fib`

6. **Backend: Attach to Python Process**
   - Attaches debugger to running Python process
   - Requires `debugpy` listening on port 5678

### Mobile Debugging

#### Available Configurations:
1. **Mobile: Launch Android App**
   - Launches and debugs Android app
   - Pre-builds debug APK

2. **Mobile: Attach Android Debugger**
   - Attaches to running Android app
   - Port: 5005

### Compound Configurations

1. **Full Stack: Backend + Mobile**
   - Launches both backend and mobile debuggers simultaneously
   - Pre-builds all projects

---

## ⌨️ Keyboard Shortcuts

### Running Tasks
- **Run Task**: `Cmd+Shift+P` → "Tasks: Run Task"
- **Run Build Task**: `Cmd+Shift+B` (macOS) / `Ctrl+Shift+B` (Windows/Linux)
- **Run Test Task**: `Cmd+Shift+T` (if configured)

### Debugging
- **Start Debugging**: `F5`
- **Start Without Debugging**: `Ctrl+F5`
- **Toggle Breakpoint**: `F9`
- **Step Over**: `F10`
- **Step Into**: `F11`
- **Step Out**: `Shift+F11`

---

## 🔍 Workspace Settings Highlights

### Python
- **Interpreter**: Automatically uses backend virtual environment
- **Linter**: Ruff (fast Python linter)
- **Formatter**: Ruff (fast Python formatter)
- **Testing**: pytest with coverage enabled
- **Format on Save**: Enabled

### Kotlin/Java
- **Gradle**: Auto-import enabled
- **Format on Save**: Enabled
- **Organize Imports**: Enabled on save

### Editor
- **Rulers**: 88 (Python PEP 8) and 120 characters
- **Trim Trailing Whitespace**: Enabled
- **Insert Final Newline**: Enabled

### Search Exclusions
Excludes common build artifacts and virtual environments:
- `.venv`, `venv`, `node_modules`
- `build`, `dist`, `.gradle`
- `__pycache__`, `.pytest_cache`, `.ruff_cache`

---

## 📦 Recommended Extensions

### Essential
- **ms-python.python** - Python language support
- **ms-python.vscode-pylance** - Fast Python language server
- **charliermarsh.ruff** - Ruff linter and formatter
- **fwcd.kotlin** - Kotlin language support
- **vscjava.vscode-gradle** - Gradle support

### Highly Recommended
- **eamodio.gitlens** - Enhanced Git capabilities
- **GitHub.copilot** - AI-powered code completion
- **EditorConfig.EditorConfig** - Consistent coding styles
- **usernamehw.errorlens** - Inline error highlighting

### Quality of Life
- **Gruntfuggly.todo-tree** - TODO/FIXME tracking
- **oderwat.indent-rainbow** - Colorful indentation
- **PKief.material-icon-theme** - Beautiful file icons

---

## 🚀 Common Workflows

### Starting Development

1. **First time setup**:
   ```
   Run Task → "Full Setup: Backend and Mobile"
   ```

2. **Daily development**:
   ```
   Run Task → "Backend: Install Dependencies" (if dependencies changed)
   Run Task → "Mobile: Gradle Sync" (if dependencies changed)
   ```

### Testing Workflow

1. **Backend tests**:
   ```
   Run Task → "Backend: Run Tests" (or press Cmd+Shift+B)
   ```

2. **Mobile tests**:
   ```
   Run Task → "Mobile: Run Android Tests"
   ```

3. **All tests**:
   ```
   Run Task → "Test: All Projects"
   ```

### Code Quality Workflow

1. **Lint backend code**:
   ```
   Run Task → "Backend: Lint (Ruff Check)"
   ```

2. **Format backend code**:
   ```
   Run Task → "Backend: Format (Ruff Format)"
   ```
   Or just save a Python file (format on save is enabled)

3. **Run pre-commit checks**:
   ```
   Run Task → "Backend: Run Pre-commit on All Files"
   ```

### Building Workflow

1. **Build everything**:
   ```
   Run Task → "Build: All Projects"
   ```

2. **Build Android APK**:
   ```
   Run Task → "Mobile: Build Android Debug APK"
   ```

3. **Build backend package**:
   ```
   Run Task → "Backend: Build"
   ```

### Debugging Workflow

1. **Debug Python backend**:
   - Set breakpoints in Python files
   - Press `F5` → Select "Backend: Debug Skeleton Module"

2. **Debug Android app**:
   - Set breakpoints in Kotlin files
   - Press `F5` → Select "Mobile: Launch Android App"

3. **Debug tests**:
   - Set breakpoints in test files
   - Press `F5` → Select "Backend: Debug Tests"

---

## 🔧 Troubleshooting

### Python virtual environment not found
```bash
cd apps/backend
uv venv
source .venv/bin/activate  # macOS/Linux
.venv\Scripts\activate     # Windows
```

### Gradle sync issues
```bash
cd apps/mobile
./gradlew --refresh-dependencies
./gradlew clean build
```

### Pre-commit not working
```bash
cd apps/backend
pre-commit install
pre-commit autoupdate
```

### Extensions not loading
- Reload VS Code window: `Cmd+Shift+P` → "Developer: Reload Window"
- Check extension compatibility with your VS Code version

---

## 📚 Additional Resources

- [VS Code Tasks Documentation](https://code.visualstudio.com/docs/editor/tasks)
- [VS Code Debugging Documentation](https://code.visualstudio.com/docs/editor/debugging)
- [Python in VS Code](https://code.visualstudio.com/docs/languages/python)
- [Kotlin in VS Code](https://marketplace.visualstudio.com/items?itemName=fwcd.kotlin)

---

## 💡 Tips

1. **Use the Command Palette** (`Cmd+Shift+P` / `Ctrl+Shift+P`) to quickly access tasks and commands
2. **Pin frequently used tasks** to the status bar for quick access
3. **Customize keyboard shortcuts** for your most-used tasks via Preferences → Keyboard Shortcuts
4. **Use workspace-specific settings** to override global settings per project
5. **Enable Auto Save** for a smoother development experience: File → Auto Save

---

*Last updated: November 2025*

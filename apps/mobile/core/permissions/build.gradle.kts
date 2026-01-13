plugins {
    alias(libs.plugins.eloquia.kmp.library.no.desktop)
    // This module declares @Composable helpers (moko permissions compose wrappers).
    alias(libs.plugins.eloquia.kmp.compose)
}

dependencies {
    // Compose runtime is needed because we call androidx.compose.runtime APIs in commonMain.
    commonMainImplementation(libs.jetbrains.compose.runtime)

    // Base API
    commonMainApi(libs.moko.permissions)

    // Compose Multiplatform: API + composable helpers (BindEffect, factory)
    commonMainApi(libs.moko.permissions.compose)

    // For PermissionsViewModel (commonMain ViewModel + viewModelScope)
    commonMainImplementation(libs.jetbrains.lifecycle.viewmodel)

    // Specific permissions support
    commonMainImplementation(libs.moko.permissions.bluetooth)
    commonMainImplementation(libs.moko.permissions.camera)
    commonMainImplementation(libs.moko.permissions.contacts)
    commonMainImplementation(libs.moko.permissions.gallery)
    commonMainImplementation(libs.moko.permissions.location)
    commonMainImplementation(libs.moko.permissions.microphone)
    commonMainImplementation(libs.moko.permissions.motion)
    commonMainImplementation(libs.moko.permissions.notifications)
    commonMainImplementation(libs.moko.permissions.storage)

    commonTestImplementation(libs.moko.permissions.test)
}

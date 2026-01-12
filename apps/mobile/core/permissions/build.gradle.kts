plugins {
    alias(libs.plugins.eloquia.kmp.library.no.desktop)
}

dependencies {
    // Base API
    commonMainApi(libs.moko.permissions)

    // Compose Multiplatform: API + composable helpers (BindEffect, factory)
    commonMainApi(libs.moko.permissions.compose)

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

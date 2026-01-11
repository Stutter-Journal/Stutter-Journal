plugins {
    alias(libs.plugins.eloquia.kmp.library)
    alias(libs.plugins.eloquia.kmp.room)
}

dependencies {
    commonMainImplementation(projects.core.logger)
    commonMainImplementation(projects.core.network.api)
}

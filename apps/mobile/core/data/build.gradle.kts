plugins {
    alias(libs.plugins.eloquia.kmp.library.no.desktop)
    alias(libs.plugins.eloquia.kmp.room)
    alias(libs.plugins.eloquia.openapi.models)
}

dependencies {
    commonMainImplementation(projects.core.domain)
    commonMainImplementation(projects.core.network.api)
    commonMainImplementation(projects.core.network.ktor)

    androidMainImplementation(libs.koin.android)
}

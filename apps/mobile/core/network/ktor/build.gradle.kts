plugins {
    alias(libs.plugins.eloquia.kmp.networking.ktor)
}

dependencies {
    commonMainImplementation(projects.core.network.api)
    commonMainImplementation(projects.core.domain)
}

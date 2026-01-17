plugins {
    alias(libs.plugins.eloquia.cmp.feature)
    alias(libs.plugins.kotlinxSerialization)
}

dependencies {
    commonMainImplementation(projects.core.network.api)
    commonMainImplementation(projects.core.network.ktor)
}

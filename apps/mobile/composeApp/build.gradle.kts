plugins {
    alias(libs.plugins.eloquia.kmp.application)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.theme)
            implementation(projects.features.entries)
            implementation(projects.features.progress)
            implementation(projects.features.support)
            implementation(libs.jetbrains.compose.material.icons.extended)
        }
    }
}

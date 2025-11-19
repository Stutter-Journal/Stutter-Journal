plugins {
    alias(libs.plugins.eloquia.kmp.application)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.theme)
        }
    }
}
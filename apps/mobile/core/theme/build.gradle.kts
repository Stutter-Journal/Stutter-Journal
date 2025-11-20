plugins {
    alias(libs.plugins.eloquia.cmp.library)
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.ui.text.google.fonts)
            implementation(libs.jetbrains.compose.ui.tooling.preview)
        }
    }
}

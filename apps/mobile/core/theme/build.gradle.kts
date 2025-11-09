plugins {
    alias(libs.plugins.eloquia.cmp.library)
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.ui.text.google.fonts)
        }
    }
}

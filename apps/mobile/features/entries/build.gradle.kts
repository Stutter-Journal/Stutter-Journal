plugins {
    alias(libs.plugins.eloquia.cmp.feature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.theme)
        }

        // Explicitly declare for IDE support
        androidMain.dependencies {
            implementation(libs.jetbrains.compose.material.icons.extended)
        }

        iosMain.dependencies {
            implementation(libs.jetbrains.compose.material.icons.extended)
        }

        val desktopMain = findByName("desktopMain")
        desktopMain?.dependencies {
            implementation(libs.jetbrains.compose.material.icons.extended)
        }
    }
}

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.compose.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpApplication") {
            id = "eloquia.kmp.application"
            implementationClass = "KmpApplicationConventionPlugin"
        }

        register("kmpLibrary") {
            id = "eloquia.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }

        register("androidCompose") {
            id = "eloquia.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
    }
}

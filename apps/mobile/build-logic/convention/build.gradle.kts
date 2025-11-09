plugins {
    `kotlin-dsl`
}

group = "at.isg.eloquia.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
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

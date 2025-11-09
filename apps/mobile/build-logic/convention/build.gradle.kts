plugins {
    `kotlin-dsl`
}

group = "at.isg.eloquia.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
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

        register("kmpCompose") {
            id = "eloquia.kmp.compose"
            implementationClass = "KmpComposeConventionPlugin"
        }

        register("iosFramework") {
            id = "eloquia.ios.framework"
            implementationClass = "IosFrameworkConventionPlugin"
        }

        register("kmpNetworking") {
            id = "eloquia.kmp.networking"
            implementationClass = "KmpNetworkingConventionPlugin"
        }

        register("kmpDependencyInjection") {
            id = "eloquia.kmp.di"
            implementationClass = "KmpDependencyInjectionConventionPlugin"
        }

        register("androidCompose") {
            id = "eloquia.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
    }
}

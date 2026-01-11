plugins {
    `kotlin-dsl`
}

group = "at.isg.eloquia.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
    implementation(libs.room.gradlePlugin)
    implementation(libs.spotless.gradlePlugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "eloquia.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }

        register("androidApplicationCompose") {
            id = "eloquia.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }

        register("cmpApplication") {
            id = "eloquia.cmp.application"
            implementationClass = "CmpApplicationConventionPlugin"
        }

        register("cmpLibrary") {
            id = "eloquia.cmp.library"
            implementationClass = "CmpLibraryConventionPlugin"
        }

        register("cmpFeature") {
            id = "eloquia.cmp.feature"
            implementationClass = "CmpFeatureConventionPlugin"
        }

        register("kmpLibrary") {
            id = "eloquia.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }

        register("kmpLibraryNoDesktop") {
            id = "eloquia.kmp.library.no-desktop"
            implementationClass = "KmpLibraryNoDesktopConventionPlugin"
        }

        // Keep old plugins for backward compatibility if needed
        register("kmpApplication") {
            id = "eloquia.kmp.application"
            implementationClass = "KmpApplicationConventionPlugin"
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

        register("kmpNetworkingApi") {
            id = "eloquia.kmp.networking.api"
            implementationClass = "KmpNetworkingApiConventionPlugin"
        }

        register("kmpNetworkingKtor") {
            id = "eloquia.kmp.networking.ktor"
            implementationClass = "KmpNetworkingKtorConventionPlugin"
        }

        register("kmpDependencyInjection") {
            id = "eloquia.kmp.di"
            implementationClass = "KmpDependencyInjectionConventionPlugin"
        }

        register("kmpRoom") {
            id = "eloquia.kmp.room"
            implementationClass = "KmpRoomConventionPlugin"
        }

        register("androidCompose") {
            id = "eloquia.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }

        register("architectureTests") {
            id = "eloquia.architecture.tests"
            implementationClass = "ArchitectureTestsConventionPlugin"
        }

        register("spotless") {
            id = "eloquia.spotless"
            implementationClass = "SpotlessConventionPlugin"
        }
    }
}

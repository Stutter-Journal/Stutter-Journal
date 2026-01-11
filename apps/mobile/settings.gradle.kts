@file:Suppress("UnstableApiUsage")

rootProject.name = "KMP-App-Template"

pluginManagement {
    includeBuild("build-logic")

    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":composeApp")
include(":core:data")
include(":core:domain")
include(":core:network:ktor")
include(":core:network:api")
include(":core:theme")
include(":features:auth")
include(":tests:architecture")
include(":features:entries")
include(":features:progress")
include(":features:support")

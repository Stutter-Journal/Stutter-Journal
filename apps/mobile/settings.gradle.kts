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
        google()
        mavenCentral()
        maven("https://repo.kotlin.link")
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":composeApp")
include(":core:data")
include(":core:domain")
include(":core:theme")
include(":features:entries")
include(":features:progress")
include(":features:support")
include(":tests:architecture")


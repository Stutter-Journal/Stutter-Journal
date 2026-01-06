@file:Suppress("UnstableApiUsage")

plugins {
    id("dev.panuszewski.typesafe-conventions") version "0.10.0"
}

rootProject.name = "build-logic"

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://repo.kotlin.link")
    }

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":convention")

@file:Suppress("UnstableApiUsage")

import at.isg.eloquia.convention.configureKotlinAndroid
import at.isg.eloquia.convention.configureKotlinMultiplatform
import at.isg.eloquia.convention.pathToResourcePrefix
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.kotlinMultiplatform.get().pluginId)
                apply(libs.plugins.androidLibrary.get().pluginId)
                apply("eloquia.spotless")
            }

            configureKotlinMultiplatform()

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)

                resourcePrefix = this@with.pathToResourcePrefix()

                // Required to make debug build of app run in iOS simulator
                experimentalProperties["android.experimental.kmp.enableAndroidResources"] = "true"
            }

            dependencies {
                "commonMainImplementation"(libs.kotlinx.coroutines.core)
                "commonMainImplementation"(libs.kotlinx.serialization.json)
                "commonMainImplementation"(libs.kotlinx.datetime)
                "commonMainImplementation"(libs.koin.core)
                "commonTestImplementation"(libs.kotlin.test)
            }
        }
    }
}

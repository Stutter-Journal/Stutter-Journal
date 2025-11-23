@file:Suppress("UnstableApiUsage")

import at.isg.eloquia.convention.configureKotlinAndroid
import at.isg.eloquia.convention.configureKotlinMultiplatformNoDesktop
import at.isg.eloquia.convention.pathToResourcePrefix
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention plugin for KMP libraries that don't include Desktop target.
 * Use this for data/domain modules that only support Android and iOS.
 */
class KmpLibraryNoDesktopConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.kotlinMultiplatform.get().pluginId)
                apply(libs.plugins.androidLibrary.get().pluginId)
                apply("eloquia.spotless")
            }

            configureKotlinMultiplatformNoDesktop()

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)

                resourcePrefix = this@with.pathToResourcePrefix()

                // Required to make debug build of app run in iOS simulator
                experimentalProperties["android.experimental.kmp.enableAndroidResources"] = "true"
            }

            dependencies {
                "commonMainImplementation"(libs.kotlinx.serialization.json)
                "commonTestImplementation"(libs.kotlin.test)
            }
        }
    }
}

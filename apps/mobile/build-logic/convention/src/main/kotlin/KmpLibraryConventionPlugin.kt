import at.isg.eloquia.convention.addComposeCommonDependencies
import at.isg.eloquia.convention.configureAndroidCommon
import at.isg.eloquia.convention.configureIosTargets
import at.isg.eloquia.convention.configureKotlinMultiplatform
import at.isg.eloquia.convention.libs
import at.isg.eloquia.convention.plugin
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugin("kotlinMultiplatform").get().pluginId)
                apply(libs.plugin("androidKotlinMultiplatformLibrary").get().pluginId)
                apply(libs.plugin("composeMultiplatform").get().pluginId)
                apply(libs.plugin("composeCompiler").get().pluginId)
            }

            extensions.configure<KotlinMultiplatformExtension> {
                configureKotlinMultiplatform(this)
                configureIosTargets()
            }

            addComposeCommonDependencies()

            extensions.configure<LibraryExtension> {
                configureAndroidCommon(this)
            }
        }
    }
}

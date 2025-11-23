import at.isg.eloquia.convention.configureIosTargets
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin that configures iOS framework targets for KMP projects.
 * This sets up iosArm64, iosX64, and iosSimulatorArm64 targets with a static framework.
 */
class IosFrameworkConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.kotlinMultiplatform.get().pluginId)
                apply("eloquia.spotless")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                configureIosTargets()
            }
        }
    }
}

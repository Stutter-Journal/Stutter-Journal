import at.isg.eloquia.convention.bundle
import at.isg.eloquia.convention.library
import at.isg.eloquia.convention.libs
import at.isg.eloquia.convention.plugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin that adds Ktor networking support to a KMP module.
 * This configures common Ktor dependencies and platform-specific HTTP clients.
 */
class KmpNetworkingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugin("kotlinxSerialization").get().pluginId)
            }

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.apply {
                    commonMain.dependencies {
                        implementation(libs.bundle("ktor-common"))
                    }

                    androidMain.dependencies {
                        implementation(libs.library("ktor-client-okhttp"))
                    }

                    iosMain.dependencies {
                        implementation(libs.library("ktor-client-darwin"))
                    }
                }
            }
        }
    }
}

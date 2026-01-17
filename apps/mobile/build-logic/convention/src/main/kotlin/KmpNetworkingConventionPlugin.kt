import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin that adds Ktor networking support to a KMP module.
 * This configures common Ktor dependencies and platform-specific HTTP clients.
 */
class KmpNetworkingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.kotlinxSerialization.get().pluginId)
            }

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.apply {
                    commonMain.dependencies {
                        implementation(libs.bundles.ktor.common)
                    }

                    androidMain.dependencies {
                        implementation(libs.ktor.client.okhttp)
                        implementation(libs.okhttp.logging.interceptor)
                    }

                    iosMain.dependencies {
                        implementation(libs.ktor.client.darwin)
                    }

                    // Desktop/JVM engine for the "desktop" target.
                    findByName("desktopMain")?.dependencies {
                        implementation(libs.ktor.client.cio)
                    }
                }
            }
        }
    }
}

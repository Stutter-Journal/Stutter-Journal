import androidx.room.gradle.RoomExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention plugin for adding Room database support to KMP modules.
 * Applies KSP and Room plugins, adds dependencies, and configures Room schema directory.
 */
class KmpRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // Apply KSP and Room plugins
            with(pluginManager) {
                apply(libs.plugins.ksp.get().pluginId)
                apply(libs.plugins.androidx.room.get().pluginId)
            }

            // Configure Room extension
            extensions.configure<RoomExtension> {
                // The schemas directory contains a schema file for each version of the Room database.
                // This is required to enable Room auto migrations.
                // See https://developer.android.com/reference/kotlin/androidx/room/AutoMigration.
                schemaDirectory("$projectDir/schemas")
            }

            // Add Room runtime dependencies
            dependencies {
                add("commonMainImplementation", libs.androidx.room.runtime)
                add("commonMainImplementation", libs.androidx.sqlite.bundled)
            }

            // Add KSP dependencies after project evaluation when all configurations exist
            afterEvaluate {
                dependencies {
                    // KSP dependencies for Room compiler
                    // We check if configurations exist because targets are defined by other plugins
                    listOf(
                        "kspCommonMainMetadata",
                        "kspAndroid",
                        "kspIosX64",
                        "kspIosArm64",
                        "kspIosSimulatorArm64"
                    ).forEach { configName ->
                        if (configurations.findByName(configName) != null) {
                            add(configName, libs.androidx.room.compiler)
                        }
                    }
                }
            }
        }
    }
}

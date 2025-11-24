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

            // Add Room runtime dependencies scoped to Android
            dependencies {
                add("androidMainImplementation", libs.androidx.room.runtime)
                add("androidMainImplementation", libs.androidx.room.ktx)
                add("androidMainImplementation", libs.androidx.sqlite.bundled)
            }

            // Add KSP dependencies after project evaluation when all configurations exist
            afterEvaluate {
                dependencies {
                    val staticKspConfigs = listOf(
                        "kspCommonMainMetadata",
                        "kspAndroid",
                        "kspIosX64",
                        "kspIosArm64",
                        "kspIosSimulatorArm64"
                    )

                    val variantAwareAndroidKspConfigs = configurations
                        .filter { it.name.startsWith("ksp", ignoreCase = true) }
                        .map { it.name }
                        .filter { name ->
                            val lower = name.lowercase()
                            // Match debug/release + any Android-specific configurations (tests, unit tests, etc.).
                            lower.contains("android") || lower.contains("debug") || lower.contains("release")
                        }

                    (staticKspConfigs + variantAwareAndroidKspConfigs)
                        .distinct()
                        .forEach { configName ->
                            if (configurations.findByName(configName) != null) {
                                add(configName, libs.androidx.room.compiler)
                            }
                        }
                }
            }
        }
    }
}

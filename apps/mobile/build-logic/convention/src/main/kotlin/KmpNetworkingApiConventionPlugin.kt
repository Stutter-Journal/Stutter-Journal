import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for the pure networking API module.
 *
 * This should stay dependency-light (no Ktor engines), but enables Kotlinx Serialization
 * so DTOs can use @Serializable.
 */
class KmpNetworkingApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("eloquia.kmp.library")
                apply(libs.plugins.kotlinxSerialization.get().pluginId)
            }
        }
    }
}

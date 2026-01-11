import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for the Ktor-backed networking implementation module.
 *
 * Applies the base KMP library setup and then adds Ktor client dependencies + platform engines.
 */
class KmpNetworkingKtorConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("eloquia.kmp.library")
                apply("eloquia.kmp.networking")
            }
        }
    }
}

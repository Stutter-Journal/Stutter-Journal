import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin that adds Compose Multiplatform support to a KMP module.
 * Apply this after kmp.library if you need Compose in a library module.
 */
class KmpComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.composeMultiplatform.get().pluginId)
                apply(libs.plugins.composeCompiler.get().pluginId)
            }
        }
    }
}

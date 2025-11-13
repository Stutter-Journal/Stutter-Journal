import at.isg.eloquia.convention.applyHierarchyTemplate
import at.isg.eloquia.convention.configureAndroidTarget
import at.isg.eloquia.convention.configureDesktopTarget
import at.isg.eloquia.convention.configureIosTargets
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class CmpApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("eloquia.android.application.compose")
                apply(libs.plugins.kotlinMultiplatform.get().pluginId)
                apply(libs.plugins.composeMultiplatform.get().pluginId)
                apply(libs.plugins.composeCompiler.get().pluginId)
                apply(libs.plugins.kotlinxSerialization.get().pluginId)
            }

            configureAndroidTarget()
            configureIosTargets()
            configureDesktopTarget()

            extensions.configure<KotlinMultiplatformExtension> {
                applyHierarchyTemplate()
            }

            dependencies {
                "debugImplementation"(libs.androidx.compose.ui.tooling)
            }
        }
    }
}

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class CmpLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("eloquia.kmp.library")
                apply(libs.plugins.composeCompiler.get().pluginId)
                apply(libs.plugins.composeMultiplatform.get().pluginId)
            }

            val compose = extensions.getByType<ComposeExtension>()

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.apply {
                    commonMain.dependencies {
                        implementation(compose.dependencies.runtime)
                        implementation(compose.dependencies.foundation)
                        implementation(compose.dependencies.material3)
                        implementation(compose.dependencies.ui)
                        implementation(compose.dependencies.components.resources)
                        implementation(libs.kotlinx.datetime)
                    }

                    androidMain.dependencies {
                        implementation(libs.jetbrains.compose.material.icons.extended)
                    }
                }
            }

            dependencies {
                "debugImplementation"(libs.androidx.compose.ui.tooling)
            }
        }
    }
}

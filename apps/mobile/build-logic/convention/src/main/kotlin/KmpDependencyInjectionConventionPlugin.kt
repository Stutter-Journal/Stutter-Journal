import at.isg.eloquia.convention.bundle
import at.isg.eloquia.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin that adds Koin dependency injection support to a KMP module.
 */
class KmpDependencyInjectionConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.apply {
                    commonMain.dependencies {
                        implementation(libs.bundle("koin"))
                    }
                }
            }
        }
    }
}

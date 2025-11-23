import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

class ArchitectureTestsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.kotlinJvm.get().pluginId)
                apply("eloquia.spotless")
            }

            dependencies {
                "implementation"(libs.kotlin.stdlib)

                "testImplementation"(libs.junit.jupiter)
                "testRuntimeOnly"(libs.junit.jupiter.engine)
                "testRuntimeOnly"(libs.junit.platform.launcher)
                "testImplementation"(libs.mockk)
                "testImplementation"(libs.kotest.assertions.core)
                "testImplementation"(libs.konsist)

                "testImplementation"(project(":composeApp"))
                "testImplementation"(project(":core:theme"))
                "testImplementation"(project(":features:entries"))
                "testImplementation"(project(":features:progress"))
                "testImplementation"(project(":features:support"))
            }

            tasks.withType<Test>().configureEach {
                useJUnitPlatform()
            }
        }
    }
}

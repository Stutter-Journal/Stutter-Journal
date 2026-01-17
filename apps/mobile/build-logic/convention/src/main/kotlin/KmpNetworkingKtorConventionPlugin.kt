import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

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

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.apply {
                    commonMain.dependencies {
                        implementation(libs.ktor.client.logging)
                        implementation(libs.ktor.client.auth)
                    }

                    commonTest.dependencies {
                        implementation(libs.ktor.client.mock)
                        implementation(libs.kotlinx.coroutines.test)
                        implementation(libs.kotest.assertions.core)
                    }

                    // KMP desktop tests are JVM tests, so we can use JUnit5 here.
                    findByName("desktopTest")?.dependencies {
                        implementation(libs.junit.jupiter)
                        runtimeOnly(libs.junit.jupiter.engine)
                        runtimeOnly(libs.junit.platform.launcher)
                        implementation(libs.mockk)
                    }
                }
            }

            tasks.withType<Test>().configureEach {
                useJUnitPlatform()
            }
        }
    }
}

import at.isg.eloquia.convention.applyHierarchyTemplate
import at.isg.eloquia.convention.configureAndroidTarget
import at.isg.eloquia.convention.configureDesktopTarget
import at.isg.eloquia.convention.configureIosTargets
import at.isg.eloquia.convention.configureKotlinAndroid
import at.isg.eloquia.convention.libs
import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.multiplatform")
                apply("org.jetbrains.compose")
                apply("org.jetbrains.kotlin.plugin.compose")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            configureAndroidTarget()
            configureIosTargets()
            configureDesktopTarget()

            val compose = extensions.getByType<ComposeExtension>()

            extensions.configure<KotlinMultiplatformExtension> {
                applyHierarchyTemplate()

                sourceSets.apply {
                    androidMain.dependencies {
                        implementation(libs.findLibrary("androidx-compose-ui-tooling-preview").get())
                        implementation(libs.findLibrary("androidx-activity-compose").get())
                        implementation(libs.findLibrary("ktor-client-okhttp").get())
                    }

                    iosMain.dependencies {
                        implementation(libs.findLibrary("ktor-client-darwin").get())
                    }

                    commonMain.dependencies {
                        implementation(compose.dependencies.runtime)
                        implementation(compose.dependencies.foundation)
                        implementation(compose.dependencies.material3)
                        implementation(compose.dependencies.ui)
                        implementation(compose.dependencies.components.resources)

                        implementation(libs.findBundle("navigation-lifecycle").get())
                        implementation(libs.findLibrary("material-icons-core").get())
                        implementation(libs.findBundle("ktor-common").get())
                        implementation(libs.findBundle("coil").get())
                        implementation(libs.findBundle("koin").get())
                    }
                }
            }

            extensions.configure<ApplicationExtension> {
                namespace = "at.isg.eloquia"

                defaultConfig {
                    applicationId = libs.findVersion("projectApplicationId").get().toString()
                    targetSdk = libs.findVersion("projectTargetSdkVersion").get().toString().toInt()
                    versionCode = libs.findVersion("projectVersionCode").get().toString().toInt()
                    versionName = libs.findVersion("projectVersionName").get().toString()
                }

                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }

                buildTypes {
                    getByName("release") {
                        // Minification can be configured here if needed
                    }
                }

                configureKotlinAndroid(this)
            }

            dependencies {
                "debugImplementation"(libs.findLibrary("androidx-compose-ui-tooling").get())
            }
        }
    }
}

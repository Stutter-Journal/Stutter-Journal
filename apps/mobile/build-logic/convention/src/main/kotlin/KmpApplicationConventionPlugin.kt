import at.isg.eloquia.convention.ProjectConfig
import at.isg.eloquia.convention.applyHierarchyTemplate
import at.isg.eloquia.convention.configureAndroidTarget
import at.isg.eloquia.convention.configureDesktopTarget
import at.isg.eloquia.convention.configureIosTargets
import at.isg.eloquia.convention.configureKotlinAndroid
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
                apply(libs.plugins.androidApplication.get().pluginId)
                apply(libs.plugins.kotlinMultiplatform.get().pluginId)
                apply(libs.plugins.composeMultiplatform.get().pluginId)
                apply(libs.plugins.composeCompiler.get().pluginId)
                apply(libs.plugins.kotlinxSerialization.get().pluginId)
                apply("eloquia.spotless")
            }

            configureAndroidTarget()
            configureIosTargets()
            configureDesktopTarget()

            val compose = extensions.getByType<ComposeExtension>()

            extensions.configure<KotlinMultiplatformExtension> {
                applyHierarchyTemplate()

                sourceSets.apply {
                    androidMain.dependencies {
                        implementation(libs.jetbrains.compose.ui.tooling.preview)
                        implementation(libs.androidx.activity.compose)
                        implementation(libs.ktor.client.okhttp)
                        runtimeOnly(libs.slf4j.simple)
                        implementation(project(":core:data"))
                        implementation(project(":core:network:ktor"))
                    }

                    iosMain.dependencies {
                        implementation(libs.ktor.client.darwin)
                        implementation(project(":core:data"))
                        implementation(project(":core:network:ktor"))
                    }

                    commonMain.dependencies {
                        implementation(compose.dependencies.runtime)
                        implementation(compose.dependencies.foundation)
                        implementation(compose.dependencies.material3)
                        implementation(compose.dependencies.ui)
                        implementation(compose.dependencies.components.resources)

                        implementation(libs.qr.kit)

                        implementation(libs.bundles.navigation.lifecycle)
                        implementation(libs.jetbrains.compose.material.icons.extended)
                        implementation(libs.bundles.ktor.common)
                        implementation(libs.bundles.coil)
                        implementation(libs.bundles.koin)
                        implementation(libs.napier)

                        implementation(project(":core:theme"))
                        implementation(project(":core:domain"))
                        implementation(project(":core:permissions"))
                        implementation(project(":features:entries"))
                        implementation(project(":features:auth"))
                        implementation(project(":features:progress"))
                        implementation(project(":features:support"))
                        implementation(project(":features:therapist"))
                    }
                }
            }

            extensions.configure<ApplicationExtension> {
                namespace = ProjectConfig.NAMESPACE

                defaultConfig {
                    applicationId = ProjectConfig.NAMESPACE
                    targetSdk = ProjectConfig.TARGET_SDK
                    versionCode = ProjectConfig.VERSION_CODE
                    versionName = ProjectConfig.VERSION_NAME
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
                "debugImplementation"(libs.androidx.compose.ui.tooling)
            }
        }
    }
}

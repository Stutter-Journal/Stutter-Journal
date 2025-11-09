import at.isg.eloquia.convention.ProjectConfig
import at.isg.eloquia.convention.addComposeCommonDependencies
import at.isg.eloquia.convention.addComposeUiTooling
import at.isg.eloquia.convention.bundle
import at.isg.eloquia.convention.configureAndroidCommon
import at.isg.eloquia.convention.configureIosTargets
import at.isg.eloquia.convention.configureKotlinMultiplatform
import at.isg.eloquia.convention.library
import at.isg.eloquia.convention.libs
import at.isg.eloquia.convention.plugin
import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugin("kotlinMultiplatform").get().pluginId)
                apply(libs.plugin("androidApplication").get().pluginId)
                apply(libs.plugin("composeMultiplatform").get().pluginId)
                apply(libs.plugin("composeCompiler").get().pluginId)
                apply(libs.plugin("kotlinxSerialization").get().pluginId)
            }

            extensions.configure<KotlinMultiplatformExtension> {
                configureKotlinMultiplatform(this)
                configureIosTargets()

                sourceSets.apply {
                    androidMain.dependencies {
                        implementation(libs.library("androidx-compose-ui-tooling-preview"))
                        implementation(libs.library("androidx-activity-compose"))
                        implementation(libs.library("ktor-client-okhttp"))
                    }

                    iosMain.dependencies {
                        implementation(libs.library("ktor-client-darwin"))
                    }

                    commonMain.dependencies {
                        implementation(libs.bundle("navigation-lifecycle"))
                        implementation(libs.library("material-icons-core"))
                        implementation(libs.bundle("ktor-common"))
                        implementation(libs.bundle("coil"))
                        implementation(libs.bundle("koin"))
                    }
                }
            }

            addComposeCommonDependencies()
            addComposeUiTooling()

            extensions.configure<ApplicationExtension> {
                configureAndroidCommon(this)

                namespace = ProjectConfig.NAMESPACE

                defaultConfig {
                    applicationId = ProjectConfig.NAMESPACE
                    targetSdk = ProjectConfig.TARGET_SDK
                    versionCode = ProjectConfig.VERSION_CODE
                    versionName = ProjectConfig.VERSION_NAME
                }

                buildTypes {
                    getByName("release") {
                        isMinifyEnabled = false
                    }
                }
            }
        }
    }
}

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.kotlinMultiplatform.get().pluginId)
                apply(libs.androidApplication.get().pluginId)
                apply(libs.composeMultiplatform.get().pluginId)
                apply(libs.composeCompiler.get().pluginId)
                apply(libs.kotlinxSerialization.get().pluginId)
            }

            val composeExt = extensions.getByType<ComposeExtension>()

            extensions.configure<KotlinMultiplatformExtension> {
                androidTarget {
                    @OptIn(ExperimentalKotlinGradlePluginApi::class) compilerOptions {
                        jvmTarget.set(ProjectConfig.JVM_TARGET)
                        freeCompilerArgs.addAll(
                            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                        )
                    }
                }

                listOf(
                    iosX64(), iosArm64(), iosSimulatorArm64()
                ).forEach { iosTarget ->
                    iosTarget.binaries.framework {
                        baseName = "ComposeApp"
                        isStatic = true
                        binaryOption("bundleId", ProjectConfig.IOS_FRAMEWORK_BUNDLE_ID)
                    }
                }

                sourceSets.apply {
                    androidMain.dependencies {
                        implementation(libs.androidxComposeUiToolingPreview)
                        implementation(libs.androidxActivityCompose)
                        implementation(libs.ktorClientOkhttp)
                    }

                    iosMain.dependencies {
                        implementation(libs.ktorClientDarwin)
                    }

                    commonMain.dependencies {
                        implementation(composeExt.dependencies.runtime)
                        implementation(composeExt.dependencies.foundation)
                        implementation(composeExt.dependencies.material3)
                        implementation(composeExt.dependencies.ui)
                        implementation(composeExt.dependencies.components.resources)
                        implementation(composeExt.dependencies.components.uiToolingPreview)

                        implementation(libs.navigationLifecycle)
                        implementation(libs.materialIconsCore)
                        implementation(libs.ktorCommon)
                        implementation(libs.coilBundle)
                        implementation(libs.koinBundle)
                    }
                }
            }

            extensions.configure<ApplicationExtension> {
                namespace = ProjectConfig.NAMESPACE
                compileSdk = ProjectConfig.COMPILE_SDK

                defaultConfig {
                    applicationId = ProjectConfig.NAMESPACE
                    targetSdk = ProjectConfig.TARGET_SDK
                    minSdk = ProjectConfig.MIN_SDK

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
                        isMinifyEnabled = false
                    }
                }

                compileOptions {
                    sourceCompatibility = ProjectConfig.JAVA_VERSION
                    targetCompatibility = ProjectConfig.JAVA_VERSION
                }
            }

            dependencies {
                add("debugImplementation", libs.androidxComposeUiTooling)
            }
        }
    }
}

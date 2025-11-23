import at.isg.eloquia.convention.ProjectConfig
import at.isg.eloquia.convention.configureKotlinAndroid
import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.androidApplication.get().pluginId)
                apply("eloquia.spotless")
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
        }
    }
}

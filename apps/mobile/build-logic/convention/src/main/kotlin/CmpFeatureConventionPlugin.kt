import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class CmpFeatureConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("eloquia.cmp.library")
            }

            dependencies {
                "commonMainImplementation"(platform(libs.koin.bom))
                "androidMainImplementation"(platform(libs.koin.bom))

                "commonMainImplementation"(libs.koin.compose)
                "commonMainImplementation"(libs.koin.compose.viewmodel)

                "commonMainImplementation"(libs.jetbrains.compose.runtime)
                "commonMainImplementation"(libs.jetbrains.compose.viewmodel)
                "commonMainImplementation"(libs.jetbrains.lifecycle.viewmodel)
                "commonMainImplementation"(libs.jetbrains.lifecycle.runtime.compose)

                "commonMainImplementation"(libs.jetbrains.lifecycle.viewmodel.savedstate)
                "commonMainImplementation"(libs.jetbrains.savedstate)
                "commonMainImplementation"(libs.jetbrains.bundle)
                "commonMainImplementation"(libs.jetbrains.navigation.compose)
                "commonMainImplementation"(libs.jetbrains.compose.material3)
                "commonMainImplementation"(libs.material.icons.core)

                "androidMainImplementation"(libs.koin.android)
                "androidMainImplementation"(libs.koin.androidx.compose)
                "androidMainImplementation"(libs.koin.androidx.navigation)
                "androidMainImplementation"(libs.koin.core.viewmodel)

                "commonMainImplementation"(project(":core:theme"))
            }
        }
    }
}

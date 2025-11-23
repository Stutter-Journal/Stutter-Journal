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
                "commonMainImplementation"(libs.jetbrains.compose.material.icons.extended)

                "androidMainImplementation"(libs.koin.android)

                "commonMainImplementation"(project(":core:theme"))
                "commonMainImplementation"(project(":core:domain"))
            }
        }
    }
}

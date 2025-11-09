package at.isg.eloquia.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun Project.addComposeCommonDependencies() {
    val composeExt = extensions.getByType<ComposeExtension>()

    extensions.configure<KotlinMultiplatformExtension> {
        sourceSets.apply {
            commonMain.dependencies {
                implementation(composeExt.dependencies.runtime)
                implementation(composeExt.dependencies.foundation)
                implementation(composeExt.dependencies.material3)
                implementation(composeExt.dependencies.ui)
                implementation(composeExt.dependencies.components.resources)
            }
        }
    }
}

fun Project.addComposeUiTooling() {
    val composeExt = extensions.getByType<ComposeExtension>()

    extensions.configure<KotlinMultiplatformExtension> {
        sourceSets.apply {
            commonMain.dependencies {
                implementation(composeExt.dependencies.components.uiToolingPreview)
            }
        }
    }

    dependencies {
        "debugImplementation"(libs.library("androidx-compose-ui-tooling"))
    }
}

package at.isg.eloquia.convention

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun configureKotlinMultiplatform(
    extension: KotlinMultiplatformExtension
) {
    extension.apply {
        androidTarget {
            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            compilerOptions {
                jvmTarget.set(ProjectConfig.JVM_TARGET)
                freeCompilerArgs.addAll(
                    "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                )
            }
        }
    }
}

internal fun KotlinMultiplatformExtension.configureIosTargets(
    frameworkBaseName: String = "ComposeApp"
) {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = frameworkBaseName
            isStatic = true
            binaryOption("bundleId", ProjectConfig.IOS_FRAMEWORK_BUNDLE_ID)
        }
    }
}

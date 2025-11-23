package at.isg.eloquia.convention

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Configures Kotlin Multiplatform for Android and iOS targets only (no Desktop/JVM).
 * Use this for modules that don't have Desktop actual implementations.
 */
internal fun Project.configureKotlinMultiplatformNoDesktop() {
    extensions.configure<LibraryExtension> {
        namespace = this@configureKotlinMultiplatformNoDesktop.pathToPackageName()
    }

    configureAndroidTarget()

    extensions.configure<KotlinMultiplatformExtension> {
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = this@configureKotlinMultiplatformNoDesktop.pathToFrameworkName()
            }
        }

        applyHierarchyTemplate()

        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
            freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
        }
    }
}

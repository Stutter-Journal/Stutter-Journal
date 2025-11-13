package at.isg.eloquia.convention

import com.android.build.api.dsl.CommonExtension
import libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension<*, *, *, *, *, *>
) {
    with(commonExtension) {
        buildFeatures {
            compose = true
        }

        dependencies {
            val bom = libs.androidx.compose.bom
            "implementation"(platform(bom))
            "testImplementation"(platform(bom))
            "debugImplementation"(libs.androidx.compose.ui.tooling.preview)
            "debugImplementation"(libs.androidx.compose.ui.tooling)
        }
    }
}

package at.isg.eloquia.convention

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

object ProjectConfig {
    const val COMPILE_SDK = 36
    const val TARGET_SDK = 35
    const val MIN_SDK = 24

    const val VERSION_CODE = 1
    const val VERSION_NAME = "1.0"

    val JAVA_VERSION = JavaVersion.VERSION_23
    val JVM_TARGET = JvmTarget.JVM_23

    const val NAMESPACE = "at.isg.eloquia"
    const val IOS_FRAMEWORK_BUNDLE_ID = "at.isg.eloquia"
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.androidLint) apply false
    alias(libs.plugins.openapiGenerator) apply false

    // Convention plugins
    alias(libs.plugins.eloquia.kmp.application) apply false
    alias(libs.plugins.eloquia.kmp.library) apply false
    alias(libs.plugins.eloquia.android.compose) apply false
}

plugins {
    alias(libs.plugins.eloquia.kmp.application)
}

android {
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        // TODO: Make this environment-specific when needed (debug/release can diverge).
        buildConfigField("String", "BASE_URL", "\"http://eloquia.backend:8080/\"")
    }
}

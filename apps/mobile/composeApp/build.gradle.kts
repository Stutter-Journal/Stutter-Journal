plugins {
    alias(libs.plugins.eloquia.kmp.application)
}

android {
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        getByName("debug") {
            // INFO: 10.0.2.2 is the hosts loopback interface, useful when working with the android emulator
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\"")
        }

        getByName("release") {
            signingConfig = signingConfigs.getByName("debug")

            buildConfigField("String", "BASE_URL", "\"http://api.eloquia.test/\"")
        }
    }
}

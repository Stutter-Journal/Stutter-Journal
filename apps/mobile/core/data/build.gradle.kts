plugins {
    alias(libs.plugins.eloquia.kmp.library.no.desktop)
    alias(libs.plugins.eloquia.kmp.room)
}

// TODO: This must be moved into some gradle plugin in the foreseeable future, this is
//  unacceptable - oh wait, I wrote this crap
dependencies {
    commonMainImplementation(project(":core:domain"))
    androidMainImplementation(libs.koin.android)
}

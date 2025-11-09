package at.isg.eloquia.convention

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.gradle.plugin.use.PluginDependency

val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun VersionCatalog.plugin(alias: String): Provider<PluginDependency> = findPlugin(alias).get()

fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> = findLibrary(alias).get()

fun VersionCatalog.bundle(alias: String): Provider<ExternalModuleDependencyBundle> = findBundle(alias).get()

fun VersionCatalog.version(alias: String): String = findVersion(alias).get().toString()

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.gradle.plugin.use.PluginDependency
import org.jetbrains.compose.ComposePlugin

// TODO: Hideous solution, because everytime I add a new library to the gradle catalogue, I have to update this
//  catalogue as well, but there's right now so viable solution to introduce type safe project accessors to
//  Kotlin native convention plugins

val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

val Project.compose: ComposePlugin.Dependencies
    get() = extensions.getByType()

fun VersionCatalog.plugin(alias: String): Provider<PluginDependency> = findPlugin(alias).get()

fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> = findLibrary(alias).get()

fun VersionCatalog.bundle(alias: String): Provider<ExternalModuleDependencyBundle> = findBundle(alias).get()

val VersionCatalog.kotlinMultiplatform: Provider<PluginDependency>
    get() = plugin("kotlinMultiplatform")

val VersionCatalog.androidApplication: Provider<PluginDependency>
    get() = plugin("androidApplication")

val VersionCatalog.composeMultiplatform: Provider<PluginDependency>
    get() = plugin("composeMultiplatform")

val VersionCatalog.composeCompiler: Provider<PluginDependency>
    get() = plugin("composeCompiler")

val VersionCatalog.kotlinxSerialization: Provider<PluginDependency>
    get() = plugin("kotlinxSerialization")

val VersionCatalog.androidxComposeUiToolingPreview: Provider<MinimalExternalModuleDependency>
    get() = library("androidx-compose-ui-tooling-preview")

val VersionCatalog.androidxActivityCompose: Provider<MinimalExternalModuleDependency>
    get() = library("androidx-activity-compose")

val VersionCatalog.androidxComposeUiTooling: Provider<MinimalExternalModuleDependency>
    get() = library("androidx-compose-ui-tooling")

val VersionCatalog.ktorClientOkhttp: Provider<MinimalExternalModuleDependency>
    get() = library("ktor-client-okhttp")

val VersionCatalog.ktorClientDarwin: Provider<MinimalExternalModuleDependency>
    get() = library("ktor-client-darwin")

val VersionCatalog.materialIconsCore: Provider<MinimalExternalModuleDependency>
    get() = library("material-icons-core")

val VersionCatalog.navigationLifecycle: Provider<ExternalModuleDependencyBundle>
    get() = bundle("navigation-lifecycle")

val VersionCatalog.ktorCommon: Provider<ExternalModuleDependencyBundle>
    get() = bundle("ktor-common")

val VersionCatalog.coilBundle: Provider<ExternalModuleDependencyBundle>
    get() = bundle("coil")

val VersionCatalog.koinBundle: Provider<ExternalModuleDependencyBundle>
    get() = bundle("koin")

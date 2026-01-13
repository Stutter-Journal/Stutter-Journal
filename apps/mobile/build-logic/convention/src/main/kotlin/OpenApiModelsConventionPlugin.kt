@file:Suppress("UnstableApiUsage")

import at.isg.eloquia.convention.ProjectConfig
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import java.net.URI

/**
 * Convention plugin that wires OpenAPI model generation for the mobile KMP codebase.
 *
 * It intentionally generates *models only* (no API clients) into `generated/openapi/` so:
 * - the DTOs can be checked in (not under build/)
 * - networking concerns remain in core:network
 *
 * Configuration-cache friendly:
 * - no doLast {} actions that capture Project
 * - custom tasks use only task properties in @TaskAction
 */
class OpenApiModelsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        // Apply OpenAPI Generator plugin
        pluginManager.apply(libs.plugins.openapiGenerator.get().pluginId)

        // Providers (keep lazy; do NOT call .get() during configuration)
        val openApiSpecUrlProvider =
            providers.gradleProperty("openApiSpecUrl").orElse("http://localhost:8080/docs/doc.json")

        val openApiSpecPathProvider = providers.gradleProperty("openApiSpecPath")

        // Locations
        val openApiSpecFileProvider = layout.buildDirectory.file("openapi/doc.json")

        // IMPORTANT: Keep generated sources out of build/ so they are not ignored by git.
        val openApiOutDirProvider = layout.projectDirectory.dir("generated/openapi")
        val openApiGeneratedCommonDirProvider = openApiOutDirProvider.dir("src/commonMain/kotlin")

        // 1) Download spec
        val downloadOpenApiSpec = tasks.register<DownloadOpenApiSpecTask>("downloadOpenApiSpec") {
            group = "openapi"
            description = "Downloads OpenAPI spec (doc.json) for codegen."

            openApiSpecUrl.set(openApiSpecUrlProvider)
            openApiSpecPath.set(openApiSpecPathProvider)
            outputFile.set(openApiSpecFileProvider)
        }

        // 2) Generate models (OpenAPI Generator task)
        val generateOpenApiModels = tasks.register<GenerateTask>("generateOpenApiModels") {
            group = "openapi"
            description =
                "Generates Kotlin Multiplatform models from OpenAPI doc.json into generated/openapi."

            dependsOn(downloadOpenApiSpec)

            generatorName.set("kotlin")

            // Wire inputSpec lazily from the download task output
            inputSpec.set(downloadOpenApiSpec.flatMap { it.outputFile }
                .map { it.asFile.absolutePath })

            // OpenAPI generator wants a String path here; this is stable at configuration time
            outputDir.set(openApiOutDirProvider.asFile.absolutePath)

            // Generate DTOs only (no API clients/supporting files)
            globalProperties.set(
                mapOf(
                    "models" to "",
                    "apis" to "false",
                    "supportingFiles" to "false",
                    "modelDocs" to "false",
                    "modelTests" to "false",
                )
            )

            packageName.set("${ProjectConfig.NAMESPACE}.core.data.openapi")
            modelPackage.set("${ProjectConfig.NAMESPACE}.core.data.openapi.model")

            configOptions.set(
                mapOf(
                    "library" to "multiplatform",
                    "sourceFolder" to "src/commonMain/kotlin",
                    "serializationLibrary" to "kotlinx_serialization",
                    "dateLibrary" to "kotlinx-datetime",
                    "enumPropertyNaming" to "UPPERCASE",
                )
            )
        }

        // 3) Sanitize generated Kotlin sources
        val sanitizeOpenApiModels =
            tasks.register<SanitizeOpenApiModelsTask>("sanitizeOpenApiModels") {
                group = "openapi"
                description =
                    "Cleans up OpenAPI generator output to keep compilation stable (e.g. duplicate @Serializable)."

                dependsOn(generateOpenApiModels)

                // Provider<Directory> -> DirectoryProperty (correct overload)
                openApiOutDir.set(openApiOutDirProvider)
            }

        // Ensure generated sources exist when compiling (clean builds).
        tasks.matching { it.name.startsWith("compile") && it.name.contains("Kotlin") }
            .configureEach {
                dependsOn(sanitizeOpenApiModels)
            }

        // KSP tasks also compile against source sets and need the generated sources present.
        tasks.matching { it.name.startsWith("ksp") }.configureEach {
            dependsOn(sanitizeOpenApiModels)
        }

        // Only configure source sets once KMP is present.
        pluginManager.withPlugin(libs.plugins.kotlinMultiplatform.get().pluginId) {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.getByName("commonMain").kotlin.srcDir(openApiGeneratedCommonDirProvider.asFile)
            }
        }
    }
}

/**
 * Downloads OpenAPI spec (doc.json) either from a local file path (openApiSpecPath)
 * or over HTTP (openApiSpecUrl).
 *
 * Configuration-cache safe: task action uses only task properties (no Project).
 */
abstract class DownloadOpenApiSpecTask : DefaultTask() {

    @get:Input
    abstract val openApiSpecUrl: Property<String>

    @get:Input
    @get:Optional
    abstract val openApiSpecPath: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun run() {
        val targetFile = outputFile.get().asFile
        targetFile.parentFile.mkdirs()

        val localPath = openApiSpecPath.orNull
        if (!localPath.isNullOrBlank()) {
            val source = java.io.File(localPath)
            require(source.exists()) {
                "openApiSpecPath points to a missing file: $localPath"
            }
            source.copyTo(targetFile, overwrite = true)
            return
        }

        val url = URI(openApiSpecUrl.get()).toURL()
        val connection = url.openConnection().apply {
            connectTimeout = 5_000
            readTimeout = 15_000
        }

        connection.getInputStream().use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}

/**
 * Post-processes generated Kotlin files to avoid compilation issues (e.g. duplicate @Serializable).
 *
 * Configuration-cache safe: uses DirectoryProperty.asFileTree (no Project.fileTree).
 */
abstract class SanitizeOpenApiModelsTask : DefaultTask() {

    @get:InputDirectory
    abstract val openApiOutDir: DirectoryProperty

    @TaskAction
    fun run() {
        openApiOutDir.asFileTree.matching { include("**/*.kt") }.forEach { file ->
            val original = file.readText()
            val sanitized = original.replace("@Serializable@Serializable", "@Serializable")
                .replace("@Serializable @Serializable", "@Serializable")

            if (sanitized != original) {
                file.writeText(sanitized)
            }
        }
    }
}
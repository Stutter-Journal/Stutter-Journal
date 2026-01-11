@file:Suppress("UnstableApiUsage")

import at.isg.eloquia.convention.ProjectConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.net.URI

/**
 * Convention plugin that wires OpenAPI model generation for the mobile KMP codebase.
 *
 * It intentionally generates *models only* (no API clients) into `generated/openapi/` so:
 * - the DTOs can be checked in (not under build/)
 * - networking concerns remain in core:network
 */
class OpenApiModelsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.openapiGenerator.get().pluginId)
            }

            val openApiSpecUrl = providers
                .gradleProperty("openApiSpecUrl")
                .orElse("http://localhost:8080/docs/doc.json")

            val openApiSpecPath = providers.gradleProperty("openApiSpecPath")

            val openApiSpecFile = layout.buildDirectory.file("openapi/doc.json")

            // IMPORTANT: Keep generated sources out of build/ so they are not ignored by git.
            // This allows checking in generated DTOs when desired.
            val openApiOutDir = layout.projectDirectory.dir("generated/openapi")
            val openApiGeneratedCommonDir = openApiOutDir.dir("src/commonMain/kotlin")

            tasks.register("downloadOpenApiSpec") {
                group = "openapi"
                description = "Downloads OpenAPI spec (doc.json) for codegen."
                outputs.file(openApiSpecFile)

                doLast {
                    val targetFile = openApiSpecFile.get().asFile
                    targetFile.parentFile.mkdirs()

                    openApiSpecPath.orNull
                        ?.let { localPath ->
                            val source = file(localPath)
                            require(source.exists()) {
                                "openApiSpecPath points to a missing file: $localPath"
                            }
                            source.copyTo(targetFile, overwrite = true)
                        }
                        ?: run {
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
            }

            tasks.register<GenerateTask>("generateOpenApiModels") {
                group = "openapi"
                description = "Generates Kotlin Multiplatform models from OpenAPI doc.json into generated/openapi."

                dependsOn("downloadOpenApiSpec")

                generatorName.set("kotlin")
                inputSpec.set(openApiSpecFile.map { it.asFile.absolutePath })
                outputDir.set(openApiOutDir.asFile.absolutePath)

                // Generate DTOs only (keep Ktor usage inside core:network, not in generated code).
                globalProperties.set(
                    mapOf(
                        "models" to "",
                        "apis" to "false",
                        "supportingFiles" to "false",
                        "modelDocs" to "false",
                        "modelTests" to "false",
                    ),
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
                    ),
                )
            }

            val sanitizeOpenApiModels = tasks.register("sanitizeOpenApiModels") {
                group = "openapi"
                description = "Cleans up OpenAPI generator output to keep compilation stable (e.g. duplicate @Serializable)."
                dependsOn("generateOpenApiModels")

                doLast {
                    fileTree(openApiOutDir.asFile).matching { include("**/*.kt") }.forEach { file ->
                        val original = file.readText()
                        val sanitized = original
                            .replace("@Serializable@Serializable", "@Serializable")
                            .replace("@Serializable @Serializable", "@Serializable")

                        if (sanitized != original) {
                            file.writeText(sanitized)
                        }
                    }
                }
            }

            // Ensure generated sources exist when compiling (clean builds).
            tasks.matching { it.name.startsWith("compile") && it.name.contains("Kotlin") }.configureEach {
                dependsOn(sanitizeOpenApiModels)
            }

            // KSP tasks also compile against source sets and need the generated sources present.
            tasks.matching { it.name.startsWith("ksp") }.configureEach {
                dependsOn(sanitizeOpenApiModels)
            }

            // Only configure source sets once KMP is present.
            pluginManager.withPlugin(libs.plugins.kotlinMultiplatform.get().pluginId) {
                extensions.configure<KotlinMultiplatformExtension> {
                    sourceSets.getByName("commonMain").kotlin.srcDir(openApiGeneratedCommonDir.asFile)
                }
            }
        }
    }
}

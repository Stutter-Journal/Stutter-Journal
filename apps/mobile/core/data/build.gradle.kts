import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import java.net.URI
import java.net.URL

plugins {
    alias(libs.plugins.eloquia.kmp.library.no.desktop)
    alias(libs.plugins.eloquia.kmp.room)
    alias(libs.plugins.openapiGenerator)
}

// TODO: This must be moved into some gradle plugin in the foreseeable future, this is
//  unacceptable - oh wait, I wrote this crap
dependencies {
    commonMainImplementation(project(":core:domain"))
    androidMainImplementation(libs.koin.android)
}

val openApiSpecUrl = providers
    .gradleProperty("openApiSpecUrl")
    .orElse("http://192.168.0.77:8080/docs/doc.json")

val openApiSpecPath = providers.gradleProperty("openApiSpecPath")

val openApiSpecFile = layout.buildDirectory.file("openapi/doc.json")
val openApiOutDir = layout.buildDirectory.dir("generated/openapi")
val openApiGeneratedCommonDir = openApiOutDir.map { it.dir("src/commonMain/kotlin") }

tasks.register("downloadOpenApiSpec") {
    group = "openapi"
    description = "Downloads OpenAPI spec (doc.json) for codegen."
    outputs.file(openApiSpecFile)

    doLast {
        val target = openApiSpecFile.get().asFile
        target.parentFile.mkdirs()

        openApiSpecPath.orNull
            ?.let { localPath ->
                val source = file(localPath)
                require(source.exists()) { "openApiSpecPath points to a missing file: $localPath" }
                source.copyTo(target, overwrite = true)
            }
            ?: run {
                val url = URI(openApiSpecUrl.get()).toURL()
                val connection = url.openConnection().apply {
                    connectTimeout = 5_000
                    readTimeout = 15_000
                }
                connection.getInputStream().use { input ->
                    target.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
    }
}

tasks.register<GenerateTask>("generateOpenApiModels") {
    group = "openapi"
    description = "Generates Kotlin Multiplatform models from OpenAPI doc.json into build/generated/openapi."

    dependsOn("downloadOpenApiSpec")

    generatorName.set("kotlin-multiplatform")
    inputSpec.set(openApiSpecFile.map { it.asFile.absolutePath })
    outputDir.set(openApiOutDir.map { it.asFile.absolutePath })

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

    packageName.set("at.isg.eloquia.core.data.openapi")
    modelPackage.set("at.isg.eloquia.core.data.openapi.model")

    configOptions.set(
        mapOf(
            "serializationLibrary" to "kotlinx_serialization",
            "dateLibrary" to "kotlinx-datetime",
            "enumPropertyNaming" to "UPPERCASE",
        ),
    )
}

extensions.configure<KotlinMultiplatformExtension> {
    sourceSets.getByName("commonMain").kotlin.srcDir(openApiGeneratedCommonDir)
}

package at.isg.eloquia.convention

import org.gradle.api.Project
import java.util.*

fun Project.pathToPackageName(): String {
    val relativePackageName = path
        .replace(':', '.')
        .lowercase()

    return "at.isg.eloquia$relativePackageName"
}

fun Project.pathToResourcePrefix(): String {
    return path
        .replace(':', '_')
        .lowercase()
        .drop(1) + "_"
}

fun Project.pathToFrameworkName(): String {
    val parts = this.path.split(":", "-", "_", " ")
    return parts.joinToString("") { part ->
        part.replaceFirstChar {
            it.titlecase(Locale.ROOT)
        }
    }
}

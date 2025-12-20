package at.isg.eloquia.features.entries.presentation.list

import at.isg.eloquia.core.domain.entries.model.JournalEntry
import kotlin.comparisons.compareBy

internal val labelCategoryOrder = listOf(
    EntryLabelCategory.Method,
    EntryLabelCategory.StutterForm,
    EntryLabelCategory.Trigger,
    EntryLabelCategory.Other,
)

internal val entryLabelComparator = compareBy<EntryLabel> { labelCategoryOrder.indexOf(it.category) }
    .thenBy { it.value.lowercase() }

internal val labelEntryComparator = compareBy<Map.Entry<EntryLabel, *>> { labelCategoryOrder.indexOf(it.key.category) }
    .thenBy { it.key.value.lowercase() }

internal fun JournalEntry.extractLabels(): List<EntryLabel> = tags.mapNotNull(::parseEntryLabel)

internal fun parseEntryLabel(tag: String): EntryLabel? {
    val normalized = tag.trim()
    if (normalized.startsWith("intensity", ignoreCase = true)) return null

    val prefix = normalized.substringBefore(":", missingDelimiterValue = normalized).lowercase()
    val value = normalized.substringAfter(":", missingDelimiterValue = normalized).trim().ifBlank { normalized }

    val category = when (prefix) {
        "method" -> EntryLabelCategory.Method
        "stutterform" -> EntryLabelCategory.StutterForm
        "trigger" -> EntryLabelCategory.Trigger
        else -> EntryLabelCategory.Other
    }

    return EntryLabel(value = value, category = category)
}

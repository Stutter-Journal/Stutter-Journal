package at.isg.eloquia.core.domain.util

import java.util.UUID

actual fun generateEntryId(): String = UUID.randomUUID().toString()

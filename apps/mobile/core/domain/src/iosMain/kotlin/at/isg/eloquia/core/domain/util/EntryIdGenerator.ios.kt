package at.isg.eloquia.core.domain.util

import platform.Foundation.NSUUID

actual fun generateEntryId(): String = NSUUID().UUIDString()

package at.isg.eloquia.core.logger

actual object AppLogger {
    actual fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            System.err.println("ERROR: [$tag] $message")
            throwable.printStackTrace()
        } else {
            System.err.println("ERROR: [$tag] $message")
        }
    }

    actual fun d(tag: String, message: String) {
        println("DEBUG: [$tag] $message")
    }

    actual fun i(tag: String, message: String) {
        println("INFO: [$tag] $message")
    }
}

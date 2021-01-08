package ua.turskyi.travelling.utils

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 */
open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        /* Allow external read but not write */
        private set

    /**
     * Returns the content and prevents its use again.
     */
    fun getMessageIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
}

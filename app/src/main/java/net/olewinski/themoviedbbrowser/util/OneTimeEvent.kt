package net.olewinski.themoviedbbrowser.util

/**
 * Small utility class that represents one-time event: the object that has content which can be read
 * just once. Needed to use observable LiveData object as a source of one-time events (normally,
 * LiveData gives data to each observer that subscribes).
 *
 * NOTE!!! This is designed to be used in single-thread environment, so it's not thread-safe.
 *
 * @param content   Content that can be retrieved only once.
 */
open class OneTimeEvent<out T>(private val content: T) {

    private var hasBeenHandled = false

    /**
     * Returns content (if it hasn't been consumed yet) or null.
     */
    fun getContent(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
}

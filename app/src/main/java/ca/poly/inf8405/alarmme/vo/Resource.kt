package ca.poly.inf8405.alarmme.vo

import ca.poly.inf8405.alarmme.vo.Status.*

/**
 * A generic class that holds a value with its loading status
 *
 * @param <T> the type of the resource object
 */
data class Resource<out T>(val status: Status, val data: T?, val message: String?) {
  
  companion object {
    /**
     * Creates [Resource] object with `SUCCESS` status and [data].
     *
     * Indicates that the resource was fetched successfully
     */
    fun <T> success(data: T): Resource<T> = Resource(SUCCESS, data, null)
  
    /**
     * Creates [Resource] object with `LOADING` status to notify
     * the UI to showing loading.
     *
     * Indicates that the resource is still loading
     */
    fun <T> loading(data: T?): Resource<T> = Resource(LOADING, data, null)
  
    /**
     * Creates [Resource] object with `ERROR` status and [message].
     *
     * Indicates that an error occurred while fetching the resource
     */
    fun <T> error(msg: String, data: T?): Resource<T> = Resource(ERROR, data, msg)
  }
}
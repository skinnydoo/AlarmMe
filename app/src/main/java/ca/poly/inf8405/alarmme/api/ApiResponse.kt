package ca.poly.inf8405.alarmme.api

import retrofit2.Response


/**
 * Common class used by API responses.
 * @param <T> the type of the response object
 */
sealed class ApiResponse<T> {
  companion object {
    fun <T> create(response: Response<T>): ApiResponse<T> =
      if (response.isSuccessful) {
        val body = response.body()
        if (body == null || response.code() == 204) ApiEmptyResponse() else ApiSuccessResponse(body)
      } else { // Unsuccessful response
        val message = response.errorBody()?.string()
        val errorMessage = if (message.isNullOrEmpty()) response.message() else message
        ApiErrorResponse(errorMessage ?: "unknown error")
      }
  
    fun <T> create(error: Throwable): ApiErrorResponse<T> =
      ApiErrorResponse(error.message ?: "unknown error")
  }
}

data class ApiSuccessResponse<T>(val body: T) : ApiResponse<T>()

data class ApiErrorResponse<T>(val errorMessage: String): ApiResponse<T>()

/**
 * separate class for HTTP 204 responses so that we can make ApiSuccessResponse's body non-null.
 */
class ApiEmptyResponse<T>: ApiResponse<T>()
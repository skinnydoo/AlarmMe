package ca.poly.inf8405.alarmme.utils

import androidx.lifecycle.LiveData
import ca.poly.inf8405.alarmme.api.ApiResponse
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A Retrofit adapter that converts the Call into a LiveData of ApiResponse.
 * @param T
*/
class LiveDataCallAdapter<T>(
  private val responseType: Type
): CallAdapter<T, LiveData<ApiResponse<T>>> {
  
  override fun adapt(call: Call<T>): LiveData<ApiResponse<T>> {
    return object : LiveData<ApiResponse<T>>() {
      private var started = AtomicBoolean(false)
      override fun onActive() {
        super.onActive()
        if (started.compareAndSet(false, true)) {
          call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
              postValue(ApiResponse.create(response))
            }
  
            override fun onFailure(call: Call<T>, t: Throwable) {
              postValue(ApiResponse.create(t))
            }
          })
        }
      }
    }
  }
  
  override fun responseType(): Type = responseType
}
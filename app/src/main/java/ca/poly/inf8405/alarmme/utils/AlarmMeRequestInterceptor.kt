package ca.poly.inf8405.alarmme.utils

import okhttp3.Interceptor
import okhttp3.Response

class AlarmMeRequestInterceptor(
  private val apiKey: String,
  private val cacheDuration: Int
) : Interceptor {
  
  override fun intercept(chain: Interceptor.Chain): Response {
    val original = chain.request()
    
    val url =
      original.url()
        .newBuilder()
        .addQueryParameter("appid", apiKey)
        .build()
    
    val newRequest =
      original.newBuilder()
        .url(url)
        .addHeader("Cache-Control", "public, max-age=$cacheDuration")
        .build()
    
    return chain.proceed(newRequest)
  }
}
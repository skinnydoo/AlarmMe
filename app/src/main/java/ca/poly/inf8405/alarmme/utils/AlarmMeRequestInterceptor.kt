package ca.poly.inf8405.alarmme.utils

import ca.poly.inf8405.alarmme.di.WEATHER_KEY
import ca.poly.inf8405.alarmme.di.qualifier.ApiKey
import ca.poly.inf8405.alarmme.di.qualifier.CacheDuration
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmMeRequestInterceptor @Inject constructor (
  @ApiKey(WEATHER_KEY) private val apiKey: String,
  @CacheDuration private val cacheDuration: Int
) : Interceptor {
  
  override fun intercept(chain: Interceptor.Chain): Response {
    val original = chain.request()
    
    val url =
      original.url()
        .newBuilder()
        .addQueryParameter("appid", apiKey)
        .addQueryParameter("units", "metric") //TODO: add metric change capabilities in App settings
        .build()
    
    val newRequest =
      original.newBuilder()
        .url(url)
        .addHeader("Cache-Control", "public, max-age=$cacheDuration")
        .build()
    
    return chain.proceed(newRequest)
  }
}
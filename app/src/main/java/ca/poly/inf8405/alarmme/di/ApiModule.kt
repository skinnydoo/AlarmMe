package ca.poly.inf8405.alarmme.di

import android.content.Context
import android.util.Log
import ca.poly.inf8405.alarmme.BuildConfig
import ca.poly.inf8405.alarmme.R
import ca.poly.inf8405.alarmme.api.WeatherService
import ca.poly.inf8405.alarmme.di.qualifier.ApiKey
import ca.poly.inf8405.alarmme.di.qualifier.ApplicationContext
import ca.poly.inf8405.alarmme.di.qualifier.CacheDuration
import ca.poly.inf8405.alarmme.utils.AlarmMeRequestInterceptor
import ca.poly.inf8405.alarmme.utils.DateTimeConverter
import ca.poly.inf8405.alarmme.utils.LiveDataCallAdapterFactory
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import org.joda.time.DateTime
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

const val WEATHER_KEY= "weather"
const val MAP_KEY= "map"

@Module
class ApiModule {
  
  @Singleton
  @Provides
  @ApiKey(WEATHER_KEY)
  fun provideWeatherApiKey(
    @ApplicationContext context: Context
  ): String = context.getString(R.string.open_weather_api_key)
  
  @Singleton
  @Provides
  @ApiKey(MAP_KEY)
  fun provideGoogleMapApiKey(
    @ApplicationContext context: Context
  ): String = context.getString(R.string.google_maps_api_key)
  
  @Singleton
  @Provides
  fun provideCache(@ApplicationContext context: Context) : Cache =
    Cache(context.cacheDir, 10 * 1024 * 1024L)
  
  @Singleton
  @Provides
  @CacheDuration
  fun provideCacheDuration(@ApplicationContext context: Context): Int = context.resources
    .getInteger(R.integer.cache_duration)
  
  /*@Singleton
  @Provides
  fun provideRequestInterceptor(
    @ApiKey(WEATHER_KEY) apiKey: String,
    @CacheDuration cacheDuration: Int
  ): AlarmMeRequestInterceptor = AlarmMeRequestInterceptor(apiKey, cacheDuration)*/
  
  @Singleton
  @Provides
  fun provideOkHttpClient(cache: Cache, requestInterceptor: AlarmMeRequestInterceptor): OkHttpClient =
    OkHttpClient().newBuilder()
      .cache(cache)
      .addInterceptor(requestInterceptor)
      .addInterceptor(HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
        Log.d("API", it)
      }).apply {
        level = if (BuildConfig.DEBUG) Level.BODY else Level.NONE
      })
      .build()
    
  @Singleton
  @Provides
  fun provideRetrofit(httpClient: OkHttpClient): Retrofit {
    
    val gson = GsonBuilder()
      .registerTypeAdapter(DateTime::class.java, DateTimeConverter())
      .setPrettyPrinting()
      .create()
    
    return Retrofit.Builder()
      .baseUrl(BASE_URL)
      .client(httpClient)
      .addConverterFactory(GsonConverterFactory.create(gson))
      .addCallAdapterFactory(LiveDataCallAdapterFactory())
      .build()
  }
  
  @Singleton
  @Provides
  fun provideWeatherService(retrofit: Retrofit): WeatherService =
    retrofit.create(WeatherService::class.java)
  
  companion object {
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
  }
  
}
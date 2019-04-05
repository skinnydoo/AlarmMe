package ca.poly.inf8405.alarmme.api

import androidx.lifecycle.LiveData
import ca.poly.inf8405.alarmme.vo.Weather
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * REST API endpoints
 */
interface WeatherService {
  /**
   * Gets the current weather by geographic coordinates.
   *
   * @param latitude the coordinate latitude
   * @param longitude the coordinate longitude
   *
   * @GET declares an HTTP GET request.
   *
   * @Query("lat") annotation on the [latitude] parameter marks it as a
   * parameter name in the @GET path.
   *
   * @Query("lon") annotation on the [longitude] parameter marks it as a
   * parameter name in the @GET path.
   */
  @GET(value = "weather")
  fun getCurrentWeather(
    @Query("lat") latitude: Double,
    @Query("lon") longitude: Double
  ): LiveData<ApiResponse<Weather>>
  
  /**
   * Get the current weather by city name.
   *
   * @param cityName The name of the city
   *
   * @GET declares an HTTP GET request.
   * @Query("q") annotation on the [cityName] parameter marks it as a
   * parameter name in the @GET path.
   * @Query("appid") annotation on the [apiKey] parameter marks it as a
   * parameter name in the @GET path
   */
  @GET(value = "weather")
  fun getCurrentWeather(
    @Query(value = "q") cityName: String
  ): LiveData<ApiResponse<Weather>>
  
}
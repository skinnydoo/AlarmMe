package ca.poly.inf8405.alarmme.api

import androidx.lifecycle.LiveData
import ca.poly.inf8405.alarmme.vo.CheckPoint
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
  ): LiveData<ApiResponse<CheckPoint.Weather>>
  
  /**
   * Get the current weather by city id.
   *
   * @param cityId The id of the city
   *
   * @GET declares an HTTP GET request.
   * @Query("id") annotation on the [cityId] parameter marks it as a
   * parameter name in the @GET path.
   */
  @GET(value = "weather")
  fun getCurrentWeather(
    @Query(value = "id") cityId: Long
  ): LiveData<ApiResponse<CheckPoint.Weather>>
  
}
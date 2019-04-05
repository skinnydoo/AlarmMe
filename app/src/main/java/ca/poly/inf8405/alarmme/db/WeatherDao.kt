package ca.poly.inf8405.alarmme.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import ca.poly.inf8405.alarmme.vo.Weather

/**
 * Interface for database access for Weather related operations.
 *
 * All queries must be executed on a separate thread.
 * Room uses the DAO to create a clean API for the code.
 */
@Dao
interface WeatherDao: BaseDao<Weather> {
  // Find current weather city id
  @Query("SELECT * FROM weather_table WHERE id = :cityId")
  fun findByCityId(cityId: Long): LiveData<Weather>
  
  // Find current weather by city name
  @Query(value = "SELECT * FROM weather_table WHERE city_name = :name")
  fun findCityName(name: String): LiveData<Weather>
  
  // Find current weather by latitude and longitude
  @Query("SELECT * FROM weather_table WHERE coord_latitude = :lat AND coord_longitude = :lng")
  fun findByLatLng(lat: Double, lng: Double) : LiveData<Weather>
}
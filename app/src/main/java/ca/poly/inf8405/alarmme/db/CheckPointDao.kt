package ca.poly.inf8405.alarmme.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import ca.poly.inf8405.alarmme.vo.CheckPoint

/**
 * Interface for database access for Weather related operations.
 *
 * All queries must be executed on a separate thread.
 * Room uses the DAO to create a clean API for the code.
 */
@Dao
interface CheckPointDao: BaseDao<CheckPoint> {
  // Query all Check points
  @Query(value = "SELECT * FROM checkpoint_table")
  fun getAllCheckPoints(): LiveData<List<CheckPoint>>
  
  // Query all active checkpoints
  @Query("SELECT * FROM checkpoint_table WHERE active = 1")
  fun findAllActiveCheckPoints() : LiveData<List<CheckPoint>>
  
  // Query the number of check points
  @Query(value = "SELECT COUNT(*) FROM checkpoint_table")
  fun getCheckPointCount(): LiveData<Int>
  
  // Find checkpoint current weather city id
  @Query("SELECT * FROM checkpoint_table WHERE weather_id = :cityId")
  fun findCheckPointByWeatherId(cityId: Long): LiveData<CheckPoint>
  
  // Find checkpoint current weather by city name
  @Query(value = "SELECT * FROM checkpoint_table WHERE weather_city_name = :name")
  fun findCheckPointByCityName(name: String): LiveData<CheckPoint>
  
  // Find checkpoint by latitude and longitude
  @Query("SELECT * FROM checkpoint_table WHERE latitude = :lat AND longitude = :lng")
  fun findCheckPointByLatLng(lat: Double, lng: Double) : LiveData<CheckPoint>
  
  // Delete all Check points
  @Query(value = "DELETE FROM checkpoint_table")
  fun deleteAll()
}
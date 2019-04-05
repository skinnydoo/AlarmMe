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
  
  // Query the number of check points
  @Query(value = "SELECT COUNT(*) FROM checkpoint_table")
  fun getCheckPointCount(): LiveData<Int>
  
  // Delete all Check points
  @Query(value = "DELETE FROM checkpoint_table")
  fun deleteAll()
}
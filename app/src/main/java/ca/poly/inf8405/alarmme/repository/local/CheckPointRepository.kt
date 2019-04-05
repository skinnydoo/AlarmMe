package ca.poly.inf8405.alarmme.repository.local

import androidx.lifecycle.LiveData
import ca.poly.inf8405.alarmme.db.CheckPointDao
import ca.poly.inf8405.alarmme.diskIOThread
import ca.poly.inf8405.alarmme.vo.CheckPoint
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class that abstracts access to CheckPoint data sources.
 * It handles data operations and provides a clean API to the rest of the app for app data.
 */
@Singleton
class CheckPointRepository @Inject constructor(
  private val checkPointDao: CheckPointDao
) {
  
  // region database LiveData query
  
  /**
   * Get all CheckPoints
   */
  val allCheckPoints: LiveData<List<CheckPoint>>
    get() = checkPointDao.getAllCheckPoints()
  
  // endregion
  
  // region database insertion
  
  /**
   * Insert a CheckPoint item
   * This method will be called on a non-UI thread cause other else the app will crash.
   */
  fun insert(checkPoint: CheckPoint) = diskIOThread {
    checkPointDao.insert(checkPoint)
  }
  
  /**
   * Insert a list of CheckPoints into the database
   * This method will be called on a non-UI thread cause other else the app will crash.
   */
  fun insert(checkPoints: List<CheckPoint>) = diskIOThread {
    checkPointDao.insertAll(checkPoints)
  }
  
  // endregion
  
  // region database deletion
  
  /**
   * Delete a CheckPoint from the database
   * It will be call on a non-UI thread cause other else the app will crash
   */
  fun delete(checkPoint: CheckPoint) = diskIOThread {
    checkPointDao.delete(checkPoint)
  }
  
  // endregion
}
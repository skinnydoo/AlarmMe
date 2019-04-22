package ca.poly.inf8405.alarmme.repository.local

import androidx.lifecycle.LiveData
import ca.poly.inf8405.alarmme.api.ApiResponse
import ca.poly.inf8405.alarmme.api.WeatherService
import ca.poly.inf8405.alarmme.db.CheckPointDao
import ca.poly.inf8405.alarmme.diskIOThread
import ca.poly.inf8405.alarmme.repository.NetworkBoundResource
import ca.poly.inf8405.alarmme.vo.CheckPoint
import ca.poly.inf8405.alarmme.vo.Resource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class that abstracts access to CheckPoint data sources.
 * It handles data operations and provides a clean API to the rest of the app for app data.
 */
@Singleton
class CheckPointRepository @Inject constructor(
  private val checkPointDao: CheckPointDao,
  private val weatherService: WeatherService
) {
  
  // region Database LiveData query
  /**
   * Get all CheckPoints
   */
  val allCheckPoints: LiveData<List<CheckPoint>>
    get() = checkPointDao.getAllCheckPoints()
  
  val allActiveCheckPoints: LiveData<List<CheckPoint>>
    get() = checkPointDao.findAllActiveCheckPoints()
  
  // endregion
  
  // region Network Bound Resource queries
  fun loadCurrentWeather(checkPoint: CheckPoint) : LiveData<Resource<CheckPoint>> {
    return object : NetworkBoundResource<CheckPoint, CheckPoint.Weather>() {
      override fun loadFromDb(): LiveData<CheckPoint> =
        checkPointDao.findCheckPointByLatLng(checkPoint.latitude, checkPoint.longitude)
      
      override fun shouldFetch(data: CheckPoint?): Boolean = data == null
      
      override fun createCall(): LiveData<ApiResponse<CheckPoint.Weather>> =
        weatherService.getCurrentWeather(checkPoint.latitude, checkPoint.longitude)
      
      override fun saveCallResult(item: CheckPoint.Weather) {
        checkPoint.weather = item
        checkPointDao.insert(checkPoint)
      }
      
    }.asLiveData()
  }
  
  // region Network Bound Resource queries
  fun loadUpdatedCurrentWeather(checkPoint: CheckPoint) :
    LiveData<Resource<CheckPoint>> {
    return object : NetworkBoundResource<CheckPoint, CheckPoint.Weather>() {
      override fun loadFromDb(): LiveData<CheckPoint> =
        checkPointDao.findCheckPointByWeatherId(checkPoint.weather!!.id)
      
      override fun shouldFetch(data: CheckPoint?): Boolean = true // always fetch
      
      override fun createCall(): LiveData<ApiResponse<CheckPoint.Weather>> =
        weatherService.getCurrentWeather(checkPoint.weather!!.id)
      
      override fun saveCallResult(item: CheckPoint.Weather) {
        checkPoint.weather = item
        checkPointDao.update(checkPoint)
      }
      
    }.asLiveData()
  }
  
  // region Database Insertion
  /**
   * Insert a CheckPoint item
   * This method will be called on a non-UI thread cause other else the app will crash.
   */
  fun insert(checkPoint: CheckPoint) = diskIOThread {
    checkPointDao.insert(checkPoint)
  }
  
  fun update(checkPoint: CheckPoint) = diskIOThread {
    checkPointDao.update(checkPoint)
  }
  /**
   * Insert a list of CheckPoints into the database
   * This method will be called on a non-UI thread cause other else the app will crash.
   */
  fun insert(checkPoints: List<CheckPoint>) = diskIOThread {
    checkPointDao.insertAll(checkPoints)
  }
  // endregion
  
  // region Database Deletion
  
  /**
   * Delete a CheckPoint from the database
   * It will be call on a non-UI thread cause other else the app will crash
   */
  fun delete(checkPoint: CheckPoint) = diskIOThread {
    checkPointDao.delete(checkPoint)
  }
  
  fun delete(name: String) = diskIOThread {
    checkPointDao.deleteByName(name)
  }
  // endregion
}
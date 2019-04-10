package ca.poly.inf8405.alarmme.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import ca.poly.inf8405.alarmme.repository.local.CheckPointRepository
import ca.poly.inf8405.alarmme.utils.AbsentLiveData
import ca.poly.inf8405.alarmme.vo.CheckPoint
import ca.poly.inf8405.alarmme.vo.Resource
import javax.inject.Inject

class CheckPointViewModel @Inject constructor(
  private val checkPointRepository: CheckPointRepository
) : ViewModel() {
  
  val allCheckPoints: LiveData<List<CheckPoint>> = checkPointRepository.allCheckPoints
  
  val activeCheckPoints: LiveData<List<CheckPoint>> = checkPointRepository.allActiveCheckPoints
  
  private val _checkPointCity = MutableLiveData<String>()
  val checkPointCity : LiveData<String>
    get() = _checkPointCity
  
  fun setCheckPointCity(checkPointCity: String) {
    if (_checkPointCity.value != checkPointCity) {
      _checkPointCity.value = checkPointCity
    }
  }
  
  // region Current weather LiveData by geographic coordinates
  private val _checkPoint = MutableLiveData<CheckPoint>()
  val checkPoint: LiveData<Resource<CheckPoint>> =
    Transformations.switchMap(_checkPoint) { checkPoint ->
      if (checkPoint == null){
        AbsentLiveData.create()
      } else {
        checkPointRepository.loadCurrentWeather(checkPoint)
      }
    }
  
  fun addCheckPoint(checkPoint: CheckPoint) {
    if (_checkPoint.value != checkPoint) {
      _checkPoint.value = checkPoint
    }
  }
  
  // endregion
  
  /**
   * Delete one checkpoint from the database
   */
  fun delete(checkPoint: CheckPoint): Unit = checkPointRepository.delete(checkPoint)
}
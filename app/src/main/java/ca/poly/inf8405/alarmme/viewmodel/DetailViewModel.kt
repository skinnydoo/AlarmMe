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


class DetailViewModel @Inject constructor(
  private val checkPointRepository: CheckPointRepository
): ViewModel() {
  
  private val _checkPoint = MutableLiveData<CheckPoint>()
  val checkPoint: LiveData<Resource<CheckPoint>> =
    Transformations.switchMap(_checkPoint) { checkPoint ->
      if (checkPoint == null){
        AbsentLiveData.create()
      } else {
        checkPointRepository.loadUpdatedCurrentWeather(checkPoint)
      }
    }
  
  fun setCheckPoint(checkPoint: CheckPoint) {
    if (_checkPoint.value != checkPoint) {
      _checkPoint.value = checkPoint
    }
  }
}
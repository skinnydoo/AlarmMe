package ca.poly.inf8405.alarmme.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ca.poly.inf8405.alarmme.repository.local.CheckPointRepository
import ca.poly.inf8405.alarmme.vo.CheckPoint
import javax.inject.Inject

class CheckPointViewModel @Inject constructor(
  private val checkPointRepository: CheckPointRepository
) : ViewModel() {

  val checkPoints: LiveData<List<CheckPoint>> = checkPointRepository.allCheckPoints
  
  /**
   * Insert one checkpoint into the database
   */
  fun insert(checkPoint: CheckPoint) : Unit = checkPointRepository.insert(checkPoint)
  
  /**
   * Delete one checkpoint from the database
   */
  fun delete(checkPoint: CheckPoint): Unit = checkPointRepository.delete(checkPoint)
}
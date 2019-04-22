package ca.poly.inf8405.alarmme.ui.alarmscreen

import androidx.recyclerview.widget.DiffUtil
import ca.poly.inf8405.alarmme.vo.CheckPoint

class AlarmDiffCallback: DiffUtil.ItemCallback<CheckPoint>() {
  
  override fun areItemsTheSame(oldItem: CheckPoint, newItem: CheckPoint): Boolean =
    oldItem.id == newItem.id
  
  override fun areContentsTheSame(oldItem: CheckPoint, newItem: CheckPoint): Boolean =
    oldItem.active == newItem.active
}

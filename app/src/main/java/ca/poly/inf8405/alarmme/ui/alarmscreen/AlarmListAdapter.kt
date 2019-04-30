package ca.poly.inf8405.alarmme.ui.alarmscreen

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ca.poly.inf8405.alarmme.R
import ca.poly.inf8405.alarmme.utils.LogWrapper
import ca.poly.inf8405.alarmme.utils.extensions.inflate
import ca.poly.inf8405.alarmme.vo.CheckPoint

class AlarmListAdapter(
  private val context: Context,
  private val listener: OnAlarmListItemClickListener
): RecyclerView.Adapter<AlarmListAdapter.ViewHolder>() {
  
  private var checkPointList: List<CheckPoint>? = null
  
  init {
    setHasStableIds(true)
  }
  
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = parent.inflate(R.layout.cardview_alarm_list)
    return ViewHolder(view)
  }
  
  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    checkPointList?.let { holder.bind(it[position], listener) }
  }
  
  override fun getItemCount(): Int = checkPointList?.size ?: 0
  
  override fun getItemId(position: Int): Long = position.toLong()
  
  override fun getItemViewType(position: Int): Int = position
  
  
  fun submitList(list: List<CheckPoint>) {
    checkPointList = list
    notifyDataSetChanged()
  }
  
  interface OnAlarmListItemClickListener {
    fun onAlarmSwithClicked(checkPoint: CheckPoint)
    fun onItemClicked(checkPoint: CheckPoint)
  }
  
  
  inner class ViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
    private val locationName = view.findViewById(R.id.alarm_location_name) as TextView
    private val locationMessage = view.findViewById(R.id.alarm_location_message) as TextView
    private val activeSwitch = view.findViewById(R.id.alarms_active_switch) as Switch
    
    fun bind(checkPoint: CheckPoint, listener: OnAlarmListItemClickListener) {
      locationName.text = checkPoint.name
      locationMessage.text = checkPoint.message
      activeSwitch.isChecked = checkPoint.active
      
      // TODO: make icon update work normally. Now it doesn't get updated when you click it
      if (checkPoint.active) {
       locationMessage.setCompoundDrawablesWithIntrinsicBounds(
         context.getDrawable(R.drawable.ic_notifications_active), null, null, null
       )
      } else {
        locationMessage.setCompoundDrawablesWithIntrinsicBounds(
          context.getDrawable(R.drawable.ic_notifications_not_active), null, null, null)
      }
      
      activeSwitch.setOnClickListener {
        val checked = (it as Switch).isChecked
        LogWrapper.d("isChecked -> $checked")
        checkPoint.active = checked
        listener.onAlarmSwithClicked(checkPoint)
      }
      view.setOnClickListener { listener.onItemClicked(checkPoint) }
    }
  }
  
}
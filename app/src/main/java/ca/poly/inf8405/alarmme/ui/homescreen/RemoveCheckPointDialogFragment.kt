package ca.poly.inf8405.alarmme.ui.homescreen

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import ca.poly.inf8405.alarmme.utils.LogWrapper
import ca.poly.inf8405.alarmme.vo.CheckPoint

private const val TITLE_ID = "title"
private const val MESSAGE_ID = "message"
private const val CHECK_POINT = "checkpoint"

class RemoveCheckPointDialogFragment : DialogFragment() {
  private var listener: OnRemoveCheckPointDialogListener? = null
  private lateinit var title: String
  private lateinit var message: String
  private lateinit var checkPoint: CheckPoint
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // get supplied title and message body
    arguments?.let {
      title = it.getString(TITLE_ID) as String // supply default text if no argument were set
      message = it.getString(MESSAGE_ID) as String  // supply default text if no argument were set
      checkPoint = it.getParcelable(CHECK_POINT) as CheckPoint
    }
  }
  
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return activity?.let { activity ->
      with(AlertDialog.Builder(activity)) {
        setTitle(title)
        setMessage(message)
        setPositiveButton(android.R.string.ok) { _, _ ->
          listener?.onRemoveCheckPoint(checkPoint) ?: LogWrapper.w("Listener not set")
        }
        setNegativeButton(android.R.string.cancel, null)
        create()
      }
    } ?: throw IllegalStateException("Activity cannot be null")
  }
  
  fun setRemoveCheckPointListener(listener: OnRemoveCheckPointDialogListener) {
    this.listener = listener
  }
  
  interface OnRemoveCheckPointDialogListener {
    fun onRemoveCheckPoint(checkPoint: CheckPoint)
  }
  
  companion object {
    fun newInstance(title: String, message: String, checkPoint: CheckPoint) =
      RemoveCheckPointDialogFragment().apply {
        arguments = Bundle().apply {
          putString(TITLE_ID, title)
          putString(MESSAGE_ID, message)
          putParcelable(CHECK_POINT, checkPoint)
        }
      }
  }
}
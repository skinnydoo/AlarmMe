package ca.poly.inf8405.alarmme.ui.homescreen

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import ca.poly.inf8405.alarmme.utils.LogWrapper

private const val TITLE_ID = "title"
private const val MESSAGE_ID = "message"
private const val CHECK_POINT_NAME = "checkpoint_name"

class RemoveCheckPointDialogFragment : DialogFragment() {
  private var listener: OnRemoveCheckPointDialogListener? = null
  private lateinit var title: String
  private lateinit var message: String
  private lateinit var checkPointName: String
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // get supplied title and message body
    arguments?.let {
      title = it.getString(TITLE_ID) ?: "" // supply default text if no argument were set
      message = it.getString(MESSAGE_ID) ?: "" // supply default text if no argument were set
      checkPointName = it.getString(CHECK_POINT_NAME) ?: ""
    }
  }
  
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return activity?.let { activity ->
      with(AlertDialog.Builder(activity)) {
        setTitle(title)
        setMessage(message)
        setPositiveButton(android.R.string.ok) { _, _ ->
          listener?.onRemoveCheckPoint(checkPointName) ?: LogWrapper.w("Listener not set")
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
    fun onRemoveCheckPoint(checkPointName: String)
  }
  
  companion object {
    fun newInstance(title: String, message: String, checkPointName: String) =
      RemoveCheckPointDialogFragment().apply {
        arguments = Bundle().apply {
          putString(TITLE_ID, title)
          putString(MESSAGE_ID, message)
          putString(CHECK_POINT_NAME, checkPointName)
        }
      }
  }
}
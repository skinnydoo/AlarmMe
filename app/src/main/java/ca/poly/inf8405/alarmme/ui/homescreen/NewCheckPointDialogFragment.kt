package ca.poly.inf8405.alarmme.ui.homescreen

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import ca.poly.inf8405.alarmme.R
import ca.poly.inf8405.alarmme.di.Injectable
import ca.poly.inf8405.alarmme.service.Constants.DEFAULT_RADIUS
import ca.poly.inf8405.alarmme.service.Constants.RADIUS_MIN
import ca.poly.inf8405.alarmme.service.Constants.RADIUS_STEP
import ca.poly.inf8405.alarmme.ui.MainActivity
import ca.poly.inf8405.alarmme.utils.LogWrapper
import ca.poly.inf8405.alarmme.utils.Validator
import ca.poly.inf8405.alarmme.viewmodel.CheckPointViewModel
import com.google.android.gms.maps.model.LatLng

private const val LAT_LNG = "lat_lng"

class NewCheckPointDialogFragment: DialogFragment(), Injectable, SeekBar.OnSeekBarChangeListener {
  
  private val checkPointViewModel: CheckPointViewModel by lazy { (activity as MainActivity).obtainCheckPointViewModel() }
  
  private var radius = DEFAULT_RADIUS
  private var listener: OnNewCheckPointDialogListener? = null
  
  private lateinit var latLng: LatLng
  
  private lateinit var radiusTextView: TextView
  private lateinit var cityNameTextView: TextView
  private lateinit var locationNameEditText: EditText
  private lateinit var messageEditText: EditText
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.let {
      latLng = it.getParcelable(LAT_LNG) as LatLng
    }
  }
  
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return activity?.let { activity ->
      
      val dialogView = activity.layoutInflater.inflate(R.layout.dialog_add_checkpoint, null)
  
      locationNameEditText = dialogView.findViewById(R.id.location_edit_text) as EditText
      messageEditText = dialogView.findViewById(R.id.message_edit_text) as EditText
  
      cityNameTextView = dialogView.findViewById(R.id.city_tv) as TextView
  
      radiusTextView = dialogView.findViewById(R.id.radius_tv) as TextView
      val radiusSeekBar = dialogView.findViewById(R.id.radius_sb) as SeekBar
      radiusSeekBar.setOnSeekBarChangeListener(this)
  
      subscribeToModel()
      
      val dialog = AlertDialog.Builder(activity)
        .setView(dialogView) // Add GUI to dialog
        .setPositiveButton(R.string.add, null)
        .setNegativeButton(R.string.cancel, null)
        .create()
      
      dialog.setOnShowListener {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
          attemptAddCheckPoint(dialog, latLng)
        }
      }
      
      return dialog
    } ?: throw IllegalStateException("Activity cannot be null")
  }
  
  override fun onDetach() {
    super.onDetach()
    listener = null
  }
  
  private fun attemptAddCheckPoint(dialog: AlertDialog, latLng: LatLng ) {
    // Reset errors
    locationNameEditText.error = null
    messageEditText.error = null
  
    val locationName = locationNameEditText.text.toString()
    val message = messageEditText.text.toString()
    
    var cancel = false
    var focusView: View? = null
    
    // Check for valid location name
    if (Validator.isFieldEmpty(locationNameEditText, getString(R.string.error_field_required)) &&
      !Validator.isNameValid(locationNameEditText, getString(R.string.error_location_name_validation_msg))) {
      
      cancel = true
      focusView = locationNameEditText
    }
    
    // Check for valid alarm message
    if (Validator.isFieldEmpty(messageEditText, getString(R.string.error_field_required)) &&
      !Validator.isNameValid(messageEditText, getString(R.string.error_message_validation_msg))) {
      cancel = true
      focusView = messageEditText
    }
    
    if (cancel) {
      // There was an error; don't attempt add checkpoint and focus the first
      // form field with an error.
      focusView?.requestFocus()
    } else {
      listener?.onAddCheckPoint(locationName, message, latLng)
      dialog.dismiss()
    }
  }
  
  override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
    val correctedProgress = RADIUS_MIN + (progress * RADIUS_STEP)
  
    radiusTextView.text = when {
      correctedProgress >= 1000 -> String.format("%.1fkm", correctedProgress * 0.001)
      else -> String.format("%sm", correctedProgress)
    }
  }
  
  override fun onStartTrackingTouch(seekBar: SeekBar) {}
  
  override fun onStopTrackingTouch(seekBar: SeekBar) {
    radius = RADIUS_MIN + (seekBar.progress * RADIUS_STEP)
    LogWrapper.d("Set Radius -> $radius")
  }
  
  private fun subscribeToModel() {
    checkPointViewModel.checkPointCity.observe(this, Observer {
      LogWrapper.d("City -> $it")
      cityNameTextView.text = it
    })
  }
  
  fun setOnNewCheckPointListener(listener: OnNewCheckPointDialogListener) {
    this.listener = listener
  }
  
  interface OnNewCheckPointDialogListener {
    fun onAddCheckPoint(checkPointName: String, alarmMessage: String,
                        latLng: LatLng, radius: Int = DEFAULT_RADIUS)
  }
  
  companion object {
    
    fun newInstance(latLng: LatLng) =
      NewCheckPointDialogFragment().apply {
        arguments = Bundle().apply {
          putParcelable(LAT_LNG, latLng)
        }
      }
  }
}
package ca.poly.inf8405.alarmme.ui.homescreen

import android.app.Dialog
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import ca.poly.inf8405.alarmme.R
import ca.poly.inf8405.alarmme.di.Injectable
import ca.poly.inf8405.alarmme.ui.MainActivity
import ca.poly.inf8405.alarmme.utils.LogWrapper
import ca.poly.inf8405.alarmme.viewmodel.CheckPointViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.textfield.TextInputLayout

private const val CITY_NAME = "city_name"
private const val LAT_LNG = "lat_lng"
private const val DEFAULT_RADIUS = 600
private const val RADIUS_MIN = 200
private const val RADIUS_MAX = 1000
private const val RADIUS_STEP = 200

class NewCheckPointDialogFragment: DialogFragment(), Injectable, SeekBar.OnSeekBarChangeListener {
  
  private val checkPointViewModel: CheckPointViewModel by lazy { (activity as MainActivity).obtainCheckPointViewModel() }
  
  var listener: OnNewCheckPointDialogListener? = null
  private var cityName: String? = null
  private var latLng: LatLng? = null
  
  private var radiusTextView: TextView? = null
  private var cityNameTextView: TextView? = null
  private var radius = DEFAULT_RADIUS
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.let {
      cityName = it.getString(CITY_NAME)
      latLng = it.getParcelable(LAT_LNG)
    }
  }
  
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return activity?.let { activity ->
      
      val dialogView = activity.layoutInflater.inflate(R.layout.dialog_add_checkpoint, null)
  
      val nameTextInputLayout = dialogView.findViewById(R.id.name_til) as TextInputLayout
      val nameEditText = nameTextInputLayout.editText
      
      val latitudeTextView = dialogView.findViewById(R.id.latitude_tv) as TextView
      val longitudeTextView = dialogView.findViewById(R.id.longitude_tv) as TextView
      latLng?.let {
        latitudeTextView.text = it.latitude.toString()
        longitudeTextView.text = it.longitude.toString()
      }
  
      cityNameTextView = dialogView.findViewById(R.id.city_tv) as TextView
      //cityName?.let { cityNameTextView.text = it }
      
      val radiusSeekBar = dialogView.findViewById(R.id.radius_sb) as SeekBar
      radiusSeekBar.setOnSeekBarChangeListener(this)
  
      radiusTextView = dialogView.findViewById(R.id.radius_tv) as TextView
      subscribeToModel()
      
      val dialog = AlertDialog.Builder(activity)
        .setView(dialogView) // Add GUI to dialog
        .setPositiveButton(R.string.add, null)
        .setNegativeButton(R.string.cancel, null)
        .create()
      
      dialog.setOnShowListener {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
  
          // TODO sanity check on the latlng..shake animation when null with explanation to user
          latLng?.let {
            val enteredName = nameEditText?.text.toString()
            if (enteredName.length >= 4){
              listener?.onAddCheckPoint(enteredName, it, radius)
              dialog.dismiss()
            } else {
              nameEditText?.error = getString(R.string.name_length_error)
            }
          }
        }
      }
      
      return dialog
    } ?: throw IllegalStateException("Activity cannot be null")
  }
  
  
  override fun onDetach() {
    super.onDetach()
    listener = null
  }
  
  override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
    val correctedProgress = RADIUS_MIN + (progress * RADIUS_STEP)
  
    radiusTextView?.text = when {
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
      cityNameTextView?.text = it
    })
  }
  
  interface OnNewCheckPointDialogListener {
    fun onAddCheckPoint(checkPointName: String, latLng: LatLng, radius: Int = DEFAULT_RADIUS)
  }
  
  companion object {
    
    fun newInstance(city: String, latLng: LatLng) =
      NewCheckPointDialogFragment().apply {
        arguments = Bundle().apply {
          putString(CITY_NAME, city)
          putParcelable(LAT_LNG, latLng)
        }
      }
  }
}
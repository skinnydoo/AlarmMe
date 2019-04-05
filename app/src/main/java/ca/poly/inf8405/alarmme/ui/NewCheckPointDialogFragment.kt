package ca.poly.inf8405.alarmme.ui

import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import ca.poly.inf8405.alarmme.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.textfield.TextInputLayout

private const val CITY_NAME = "city_name"
private const val LAT_LNG = "lat_lng"

class NewCheckPointDialogFragment: DialogFragment() {
  var listener: OnNewCheckPointDialogListener? = null
  private var cityName: String? = null
  private var latLng: LatLng? = null
  
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    require(listener != null) {"Class member `listener` should not be null"}
    arguments?.let {
      cityName = it.getString(CITY_NAME)
      latLng = it.getParcelable(LAT_LNG)
    }
  }
  
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return activity?.let { activity ->
  
      val builder = AlertDialog.Builder(activity)
      val dialogView = activity.layoutInflater.inflate(R.layout.dialog_add_checkpoint, null)
  
      val latitudeTextView = dialogView.findViewById(R.id.latitude_tv) as TextView
      val longitudeTextView = dialogView.findViewById(R.id.longitude_tv) as TextView
      latLng?.let {
        latitudeTextView.text = it.latitude.toString()
        longitudeTextView.text = it.longitude.toString()
      }
  
      /*val cityNameTextView = dialogView.findViewById(R.id.city_tv) as TextView
      cityName?.let { cityNameTextView.text = it }*/
  
      val nameTextInputLayout = dialogView.findViewById(R.id.name_til) as TextInputLayout
      val nameEditText = nameTextInputLayout.editText
  
      // Add GUI to dialog
      builder.setView(dialogView)
      /*.setPositiveButton(R.string.add) { _, _ ->
        // TODO sanity check on the latlng..shake animation when null with explanation to user
        latLng?.let { listener?.onAddCheckPoint(nameEditText?.text.toString(), it) }
      }
      .setNegativeButton(R.string.cancel, null)*/
  
      builder.create()
    } ?: throw IllegalStateException("Activity cannot be null")
  }
  
  override fun onDetach() {
    super.onDetach()
    listener = null
  }
  
  interface OnNewCheckPointDialogListener {
    fun onAddCheckPoint(checkPointName: String, latLng: LatLng)
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
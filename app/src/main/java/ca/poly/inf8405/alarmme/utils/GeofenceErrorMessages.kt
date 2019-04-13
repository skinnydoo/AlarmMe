package ca.poly.inf8405.alarmme.utils

import android.content.Context
import ca.poly.inf8405.alarmme.R
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.GeofenceStatusCodes

object GeofenceErrorMessages {
  
  /**
   * Returns the error string for a geofencing exception.
   */
  fun getErrorString(context: Context, ex: Exception) : String = when (ex) {
      is ApiException -> getErrorString(context, ex.statusCode)
      else -> context.resources.getString(R.string.error_unknown_geofence_error)
  }
  
  /**
   * Returns the error string for a geofencing error code.
   */
  fun getErrorString(context: Context, errorCode: Int): String {
    val resources = context.resources
    return when(errorCode) {
      GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE ->
        resources.getString(R.string.error_geofence_not_available)
      GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES ->
        resources.getString(R.string.error_geofence_too_many_geofences)
      GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS ->
        resources.getString(R.string.error_geofence_too_many_pending_intents)
      else -> resources.getString(R.string.error_unknown_geofence_error)
    }
  }
}
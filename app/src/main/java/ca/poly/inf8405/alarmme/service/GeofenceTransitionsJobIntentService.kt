package ca.poly.inf8405.alarmme.service

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import ca.poly.inf8405.alarmme.R
import ca.poly.inf8405.alarmme.ui.NotificationHandler
import ca.poly.inf8405.alarmme.utils.GeofenceErrorMessages
import ca.poly.inf8405.alarmme.utils.LogWrapper
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import dagger.android.AndroidInjection
import javax.inject.Inject

class GeofenceTransitionsJobIntentService : JobIntentService() {
  
  @Inject
  lateinit var notificationHandler: NotificationHandler
  
  override fun onCreate() {
    AndroidInjection.inject(this)
    super.onCreate()
  }
  
  /**
   * Handles incoming intents.
   * @param intent sent by Location Services. This Intent is provided to Location
   *               Services (inside a PendingIntent) when addGeofences() is called.
   */
  override fun onHandleWork(intent: Intent) {
    val geofencingEvent = GeofencingEvent.fromIntent(intent)
    
    if (geofencingEvent.hasError()) {
      val errorMessage = GeofenceErrorMessages.getErrorString(this, geofencingEvent.errorCode)
      LogWrapper.e(errorMessage)
      //deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage)
      return
    }
    handleEvent(geofencingEvent)
  }
  
  private fun handleEvent(geofencingEvent: GeofencingEvent?) {
    geofencingEvent?.let {
      // Get the transition type
      when (val geofenceTransition = geofencingEvent.geofenceTransition) {
        Geofence.GEOFENCE_TRANSITION_ENTER -> {
          
          // Get the geofences that were triggered.
          val triggeringGeofences = geofencingEvent.triggeringGeofences
          
          // Get the first geofence id
          val geofenceId = triggeringGeofences[0].requestId
          LogWrapper.d("Entered Geofence -> id = $geofenceId")
          notificationHandler.sendNotification("", geofenceId)
        }
        
        else -> LogWrapper.d(
          getString(
            R.string.error_geofence_transition_invalid_type,
            geofenceTransition
          )
        )
      }
    }
  }
  
  companion object {
    
    private const val JOB_ID = 586
    
    /**
     * Convenience method for enqueuing work into this service
     */
    @JvmStatic
    fun enqueueWork(context: Context, intent: Intent) {
      enqueueWork(context, GeofenceTransitionsJobIntentService::class.java, JOB_ID, intent)
    }
  }
}

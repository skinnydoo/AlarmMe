package ca.poly.inf8405.alarmme.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import ca.poly.inf8405.alarmme.di.qualifier.ApplicationContext
import ca.poly.inf8405.alarmme.service.Constants
import ca.poly.inf8405.alarmme.utils.extensions.newIntent
import javax.inject.Inject
import javax.inject.Singleton



private const val CHANNEL_ID = "channel_3251"
private const val NOTIFICATION_ID = 0

@Singleton
class NotificationHandler @Inject constructor(
  @ApplicationContext private val context: Context
) {
  
  fun sendNotification(locationName: String, message: String) {
    createNotificationChannel()
    
    // Create an explicit intent that starts MainActivity
    val notificationIntent = newIntent<MainActivity>(context)
    
    // Construct a task stack
    val stackBuilder = TaskStackBuilder.create(context).apply {
      // add MainActivity to the task stack as the Parent
      addParentStack(MainActivity::class.java)
      
      // Push the intent onto the stack
      addNextIntent(notificationIntent)
    }
    
    // Get a PendingIntent containing the entire back stack
    val notificationPendingIntent =
      stackBuilder.getPendingIntent(Constants.GEOFENCE_INTENT_REQUEST_ID, PendingIntent.FLAG_UPDATE_CURRENT)
    
    // Create a notification
    val notification = NotificationCompat.Builder(context, CHANNEL_ID).apply {
      // Define the notification settings
      setSmallIcon(ca.poly.inf8405.alarmme.R.drawable.ic_notification_white)
      setContentTitle(locationName)
      setContentText(message)
      setContentIntent(notificationPendingIntent)
      setShowWhen(true)
      setDefaults(NotificationCompat.DEFAULT_ALL)
      //setCategory(NotificationCompat.CATEGORY_REMINDER)
      color = ContextCompat.getColor(context, ca.poly.inf8405.alarmme.R.color.colorAccent)
      setAutoCancel(true) // Dismiss notification once the user touches it.
    }.build()
    
    // Issue the notification
    with(NotificationManagerCompat.from(context)) {
      notify(NOTIFICATION_ID, notification)
    }
    
  }
  
  private fun createNotificationChannel() {
    // Android O requires a Notification channel
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val name = context.getString(ca.poly.inf8405.alarmme.R.string.app_name)
      val importance = NotificationManager.IMPORTANCE_HIGH
      // Create the channel for the notification
      val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
        enableLights(true)
        enableVibration(true)
        //vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
      }
      // Get an instance of the Notification manager
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as
        NotificationManager
      // Register the channel with the system
      notificationManager.createNotificationChannel(channel)
    }
  }
}
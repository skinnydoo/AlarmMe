package ca.poly.inf8405.alarmme.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import ca.poly.inf8405.alarmme.BuildConfig
import ca.poly.inf8405.alarmme.R
import ca.poly.inf8405.alarmme.di.Injectable
import ca.poly.inf8405.alarmme.utils.LogWrapper
import ca.poly.inf8405.alarmme.utils.extensions.action
import ca.poly.inf8405.alarmme.utils.extensions.showToast
import ca.poly.inf8405.alarmme.utils.extensions.snackBar
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

abstract class BaseFragment: Fragment(), Injectable {
  
  // Provides access to the Fused Location Provider API
  @Inject
  lateinit var fusedLocationClient: FusedLocationProviderClient
  
  // Provides access tot the Location Settings API
  @Inject
  lateinit var settingsClient: SettingsClient
  
  // Stores parameters for requests tot the FusedLocationProviderApo
  @Inject
  lateinit var locationRequest: LocationRequest
  
  // Used to determine if the devices has optimal location settings
  @Inject
  lateinit var locationSettingsRequest: LocationSettingsRequest
  
  /**
   * Provides access to the Geofencing API
   */
  @Inject
  lateinit var geofencingClient: GeofencingClient
  
  // Provides access to the Places Api Client
  @Inject
  lateinit var placesClient: PlacesClient
  
  // Callback for location events
  private lateinit var locationCallback: LocationCallback
  
  // To display snackBars
  private var rootView: View? = null
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Get a reference to the rootView
    rootView = activity?.findViewById(android.R.id.content)
  }
  
  override fun onStart() {
    super.onStart()
    
    if (!checkPermissions()) {
      requestPermissions()
    } else {
      startLocationUpdates()
      onPermissionGranted()
    }
  }
  
  override fun onPause() {
    super.onPause()
    // Remove location updates to save battery
    stopLocationUpdates()
  }
  
  /**
   * Create a callback for receiving location events
   */
  protected fun createLocationCallback(fn: (Location) -> Unit) {
    LogWrapper.d("Configuring Location Callback...")
    locationCallback = object : LocationCallback() {
      override fun onLocationResult(locationResult: LocationResult) {
        fn(locationResult.lastLocation)
      }
    }
    LogWrapper.d("Exit")
  }
  
  /**
   * Return the current state of the permissions needed
   */
  private fun checkPermissions(): Boolean {
    val permissionState =
      ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
    return permissionState == PackageManager.PERMISSION_GRANTED
  }
  
  private fun requestPermissions() {
    LogWrapper.d("Requesting location permission from the user...")
    val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
      requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
    
    // Provide an additional rationale to the user. This would happen if the user denied the
    // request previously, but didn't check the "Don't ask again" checkbox.
    if(shouldProvideRationale) {
      LogWrapper.d("Displaying permission rationale to provide additional context.")
      rootView?.snackBar(R.string.error_permission_rationale, Snackbar.LENGTH_INDEFINITE) {
        action(android.R.string.ok) {
          // Request permission
          requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
          )
        }
      }
    } else {
      LogWrapper.d("Requesting permission...")
      // Request permission. It's possible this can be auto answered if device policy
      // sets the permission in a given state or the user denied the permission
      // previously and checked "Never ask again".
      // Request permission
      requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
        REQUEST_PERMISSIONS_REQUEST_CODE
      )
      
    }
    LogWrapper.d("Exit")
  }
  
  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    
    LogWrapper.d("Permission request completed. RequestCode -> $requestCode")
    if (requestCode != REQUEST_PERMISSIONS_REQUEST_CODE) return
    
    when {
      grantResults.isEmpty() ->
        // If user interaction was interrupted, the permission request is cancelled and you
        // receive empty arrays.
        LogWrapper.w("User interaction was cancelled")
      
      grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
        // Permission granted...start location updates
        startLocationUpdates()
        onPermissionGranted()
      }
      
      else -> // Permission denied
        // Notify the user via a SnackBar that they have rejected a core permission for the
        // app, which makes the Activity useless.
        rootView?.snackBar(R.string.error_permission_denied_explanation, Snackbar.LENGTH_INDEFINITE) {
          action(R.string.action_settings) {
            try {
              // Build intent that displays the App settings screen.
              val intent = Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
              }
              startActivity(intent)
            } catch (e: ActivityNotFoundException) {
              
              val alternateIntent =  Intent().apply {
                action = Settings.ACTION_SETTINGS
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
              }
              startActivity(alternateIntent)
            }
          }
        }
    }
  }
  
  /**
   * Request location updates from the FusedLocationApi.
   * Note: This is not called unless location runtime permission has been granted
   */
  @SuppressLint("MissingPermission")
  protected fun startLocationUpdates() {
    LogWrapper.d("Starting location updates...")
    LogWrapper.d("Checking if the device has the necessary location settings...")
    settingsClient.checkLocationSettings(locationSettingsRequest)
      .addOnSuccessListener {
        
        LogWrapper.d("Settings OK. Requesting location update...")
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        LogWrapper.d("Location update requested.")
      }
      .addOnFailureListener {
        LogWrapper.w("Device has the wrong location settings...")
        if (it !is ApiException) return@addOnFailureListener
        
        when(it.statusCode) {
          LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
            LogWrapper.d("Attempting to upgrade location settings")
            // Attempting to upgrade location settings
            try {
              // Show the dialog by calling startResolutionForResult(), and
              // check the result in onActivityResult()
              val rae = it as ResolvableApiException
              rae.startResolutionForResult(activity,
                REQUEST_CHECK_SETTINGS
              )
            } catch (ex: IntentSender.SendIntentException) {
              LogWrapper.e("PendingIntent unable to execute request", ex)
            }
          }
          
          LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
            LogWrapper.e(getString(R.string.error_location_settings_inadequate))
            activity?.showToast(R.string.error_location_settings_inadequate, Toast.LENGTH_LONG)
          }
        }
      }
  }
  
  /**
   * Removes location updates from the FusedLocationApi.
   * It is good practice to remove location request when the activity is in a paused or stopped
   * state. Doing so helps battery performance.
   */
  private fun stopLocationUpdates() {
    LogWrapper.d("Stopping location updates...")
    fusedLocationClient.removeLocationUpdates(locationCallback)
      .addOnCompleteListener {
        LogWrapper.d("Location updates stopped")
      }
    
    LogWrapper.d("Exit")
  }
  
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    LogWrapper.d("Activity result completed. RequestCode -> $requestCode")
    if (requestCode != REQUEST_CHECK_SETTINGS) return
    
    when (resultCode) {
      
      AppCompatActivity.RESULT_OK -> // Nothing to do. startLocationUpdates gets called in
        // onStart again
        LogWrapper.d("User agreed to make required location settings changes")
      
      AppCompatActivity.RESULT_CANCELED -> {
        // User chose not to make the required location settings changes
        LogWrapper.d("User chose not to make required location settings changes")
      }
      
    }
  }
  
  abstract fun onPermissionGranted()
  
  companion object {
    //region Permission request code
    
    // constant used in requesting runtime permissions
    private const val REQUEST_PERMISSIONS_REQUEST_CODE = 100
    
    // constant used in the location settings dialog
    private const val REQUEST_CHECK_SETTINGS = 101
    
    //endregion
  }
}
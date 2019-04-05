package ca.poly.inf8405.alarmme.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import ca.poly.inf8405.alarmme.BuildConfig
import ca.poly.inf8405.alarmme.R
import ca.poly.inf8405.alarmme.di.Injectable
import ca.poly.inf8405.alarmme.utils.LogWrapper
import ca.poly.inf8405.alarmme.utils.extensions.action
import ca.poly.inf8405.alarmme.utils.extensions.showToast
import ca.poly.inf8405.alarmme.utils.extensions.snackBar
import ca.poly.inf8405.alarmme.viewmodel.CheckPointViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_map.*
import org.joda.time.DateTime
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MapFragment.OnMapFragmentInteractionListener] interface
 * to handle interaction events.
 *
 */
class MapFragment : Fragment(),
  Injectable,
  NewCheckPointDialogFragment.OnNewCheckPointDialogListener,
  OnMapReadyCallback {
  
  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory
  
  
  // Provides access to the Fused Location Provider API
  @Inject
  lateinit var fusedLocationClient: FusedLocationProviderClient
  
  // Provides access tot eh Location Settings API
  @Inject
  lateinit var settingsClient: SettingsClient
  
  // Stores parameters for requests tot the FusedLocationProviderApo
  @Inject
  lateinit var locationRequest: LocationRequest

  // Used to determine if the devices has optimal location settings
  @Inject
  lateinit var locationSettingsRequest: LocationSettingsRequest
  
  // Provides access to the Places Api Client
  @Inject
  lateinit var placesClient: PlacesClient
  
  private lateinit var checkPointViewModel: CheckPointViewModel
  
  // Callback for location events
  private lateinit var locationCallback: LocationCallback
  
  // Stores a geographical location
  private var currentLocation: Location? = null
  
  // To manipulate the Map SDK methods
  lateinit var map: GoogleMap
  
  // To display snackBars
  private var rootView: View? = null
  
  private var allowLocationUpdates = true
  
  private var allowCameraUpdates = true
  
  
  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_map, container, false)
  }
  
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    mapView?.run {
      isHapticFeedbackEnabled = true
      onCreate(savedInstanceState)
      getMapAsync(this@MapFragment) // get notified when the map is ready to use
    }
  
    // Get a reference to the rootView
    rootView = activity?.findViewById(android.R.id.content)
    
    checkPointViewModel = ViewModelProviders.of(this, viewModelFactory)[CheckPointViewModel::class.java]
    subscribeToModel()
    // Kick off the process of building the LocationCallback
    createLocationCallback()
    
  }
  
  private fun subscribeToModel() {
    checkPointViewModel.checkPoints.observe(viewLifecycleOwner, Observer {
      // TODO: show checkpoints on screen
    })
  }
  
  
  override fun onStart() {
    super.onStart()
    mapView?.onStart()
    
    if (!checkPermissions()) {
      requestPermissions()
    } else if (allowLocationUpdates) {
      startLocationUpdates()
    }
  }
  
  override fun onResume() {
    super.onResume()
    mapView?.onResume() // displays the map
  }
  
  override fun onPause() {
    super.onPause()
    mapView?.onPause()
    
    // Remove location updates to save battery
    stopLocationUpdates()
  }
  
  override fun onStop() {
    super.onStop()
    mapView?.onStop()
  }
  
  override fun onDestroy() {
    super.onDestroy()
    mapView?.onDestroy()
  }
  
  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    mapView?.onSaveInstanceState(outState)
  }
  
  
  override fun onLowMemory() {
    super.onLowMemory()
    mapView?.onLowMemory()
  }
  
  
  /**
   * Manipulates the map once available.
   * This callback is triggered when the map is ready to be used.
   * This is where we can add markers or lines, add listeners or move the camera. In this case,
   * we just add a marker near Sydney, Australia.
   * If Google Play services is not installed on the device, the user will be prompted to install
   * it inside the SupportMapFragment. This method will only be triggered once the user has
   * installed Google Play services and returned to the app.
   */
  override fun onMapReady(googleMap: GoogleMap?) {
    // return early if the map was not initialised properly
    map = googleMap ?: return
    LogWrapper.d("Map is ready...making initial setup")
    with(map) {
      mapType = GoogleMap.MAP_TYPE_NORMAL
      moveCamera(CameraUpdateFactory.zoomTo(10F))
      setOnMapLongClickListener {
        handleMapLongClick(it)
      }
    }
    LogWrapper.d("Exit")
  }
  
  private fun handleMapLongClick(latLng: LatLng) {
    LogWrapper.d("Map Clicked at position -> $latLng")
    mapView.performHapticFeedback(
      HapticFeedbackConstants.VIRTUAL_KEY,
      HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
    
    showAddCheckPointDialog(latLng)
  }
  
  private fun showAddCheckPointDialog(latLng: LatLng) {
    val newCheckPointDialog =
      NewCheckPointDialogFragment.newInstance("", latLng)
    newCheckPointDialog.listener = this
    newCheckPointDialog.show(activity?.supportFragmentManager, null)
  
  }
  
  override fun onAddCheckPoint(checkPointName: String, latLng: LatLng) {
  
    map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
  
    map.addMarker(
      MarkerOptions()
        .position(latLng)
        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
    )
  }
  
  /**
   * Create a callback for receiving location events
   */
  private fun createLocationCallback() {
    LogWrapper.d("Configuring Location Callback...")
    locationCallback = object : LocationCallback() {
      
      override fun onLocationResult(locationResult: LocationResult) {
        currentLocation = locationResult.lastLocation
        currentLocation?.let {
          val latLng = LatLng(it.latitude, it.longitude)
          if (allowCameraUpdates) {
            map.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            allowCameraUpdates = false
          }
  
          val dateTime = DateTime.now().toString("EEE, MMM d ''yy, HH:mm:ss")
          LogWrapper.d("Current Location -> $latLng, Last update time -> $dateTime")
        }
      }
    }
    
    LogWrapper.d("Exit")
  }
  
  
  /**
   * Return the current state of the permissions needed
   */
  private fun checkPermissions(): Boolean {
    val permissionState =
      ActivityCompat.checkSelfPermission(activity as Context, Manifest.permission.ACCESS_FINE_LOCATION)
    return permissionState == PackageManager.PERMISSION_GRANTED
  }
  
  private fun requestPermissions() {
    LogWrapper.d("Requesting location permission from the user...")
    val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
      activity as FragmentActivity, Manifest.permission.ACCESS_FINE_LOCATION)
    
    // Provide an additional rationale to the user. This would happen if the user denied the
    // request previously, but didn't check the "Don't ask again" checkbox.
    if(shouldProvideRationale) {
      LogWrapper.d("Displaying permission rationale to provide additional context.")
      rootView?.snackBar(R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE) {
        action(android.R.string.ok) {
          // Request permission
          requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSIONS_REQUEST_CODE)
        }
      }
    } else {
      LogWrapper.d("Requesting permission...")
      // Request permission. It's possible this can be auto answered if device policy
      // sets the permission in a given state or the user denied the permission
      // previously and checked "Never ask again".
      // Request permission
      requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSIONS_REQUEST_CODE)
      
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
      }
      
      else -> // Permission denied
        // Notify the user via a SnackBar that they have rejected a core permission for the
        // app, which makes the Activity useless.
        rootView?.snackBar(R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE) {
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
                action = android.provider.Settings.ACTION_SETTINGS
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
  private fun startLocationUpdates() {
    LogWrapper.d("Checking if the device has the necessary location settings...")
    settingsClient.checkLocationSettings(locationSettingsRequest)
      .addOnSuccessListener {
        LogWrapper.d("Settings OK. Requesting location update...")
        //map?.isMyLocationEnabled = true
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
              rae.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS)
            } catch (ex: IntentSender.SendIntentException) {
              LogWrapper.e("PendingIntent unable to execute request", ex)
            }
          }
          
          LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
            LogWrapper.e(getString(R.string.location_settings_inadequate))
            activity?.showToast(R.string.location_settings_inadequate, Toast.LENGTH_LONG)
            allowLocationUpdates = false
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
    if (!allowLocationUpdates) {
      LogWrapper.d("No-op. Updates never requested")
      return
    }
    
    fusedLocationClient.removeLocationUpdates(locationCallback)
      .addOnCompleteListener {
        allowLocationUpdates = false
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
        allowLocationUpdates = false
      }
      
    }
  }
  
  
  companion object {
    //region Permission request code
    
    // constant used in requesting runtime permissions
    private const val REQUEST_PERMISSIONS_REQUEST_CODE = 100
    
    // constant used in the location settings dialog
    private const val REQUEST_CHECK_SETTINGS = 101
    
    //endregion
    
  }
}

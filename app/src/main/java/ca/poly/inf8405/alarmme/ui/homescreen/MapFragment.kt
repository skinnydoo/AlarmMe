package ca.poly.inf8405.alarmme.ui.homescreen

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import ca.poly.inf8405.alarmme.R
import ca.poly.inf8405.alarmme.di.Injectable
import ca.poly.inf8405.alarmme.service.Constants
import ca.poly.inf8405.alarmme.service.FetchAddressIntentService
import ca.poly.inf8405.alarmme.service.GeofenceTransitionsJobIntentService
import ca.poly.inf8405.alarmme.ui.BaseFragment
import ca.poly.inf8405.alarmme.ui.MainActivity
import ca.poly.inf8405.alarmme.utils.GeofenceErrorMessages
import ca.poly.inf8405.alarmme.utils.LogWrapper
import ca.poly.inf8405.alarmme.utils.extensions.*
import ca.poly.inf8405.alarmme.viewmodel.CheckPointViewModel
import ca.poly.inf8405.alarmme.vo.CheckPoint
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_map.*
import org.joda.time.DateTime

private const val DEFAULT_ZOOM = 15f

private data class MarkerPair(val marker: Marker, val circle: Circle)

class MapFragment : BaseFragment(), Injectable,
  NewCheckPointDialogFragment.OnNewCheckPointDialogListener,
  RemoveCheckPointDialogFragment.OnRemoveCheckPointDialogListener,
  OnMapReadyCallback {
  
  private val checkPointViewModel: CheckPointViewModel by lazy { (activity as MainActivity).obtainCheckPointViewModel() }
  
  // To manipulate the Map SDK methods
  private var map: GoogleMap? = null
  private val markers = hashMapOf<String, MarkerPair>()
  private var allowCameraUpdates = true
  
  // Stores a geographical location
  private var currentLocation: Location? = null
  
  private var autocompleteSearchBar: AutocompleteSupportFragment? = null
  
  // Receiver registered with this fragment to get the response from FetchAddressIntentService.
  private lateinit var addressResultReceiver: AddressResultReceiver
  
  private val geofencePendingIntent: PendingIntent by lazy {
    val intent = newIntent<GeofenceBroadcastReceiver>(requireContext())
    // Using the FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
    // addGeofences() and removeGeofences().
    PendingIntent.getBroadcast(
      requireContext(),
      Constants.GEOFENCE_INTENT_REQUEST_ID,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT
    )
  }
  
  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_map, container, false)
  }
  
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    allowLocationUpdates = true
    allowCameraUpdates = true
    addressResultReceiver = AddressResultReceiver(Handler())
    
    autocompleteSearchBar = requireActivity().supportFragmentManager
      .findFragmentById(R.id.places_autocomplete_fragment) as? AutocompleteSupportFragment
    LogWrapper.d("Search bar -> $autocompleteSearchBar")
    
    initView(savedInstanceState)
    subscribeToModel()
    // Kick off the process of building the LocationCallback
    createLocationCallback {
      locationCallback(it)
    }
    
  }
  
  private fun initView(savedInstanceState: Bundle?) {
    mapView?.run {
      isHapticFeedbackEnabled = true
      onCreate(savedInstanceState)
      getMapAsync(this@MapFragment) // get notified when the map is ready to use
    }
    
    activity?.currentLocationFab?.hide()
    
    // Remove the provided search icon from the search bar
    (autocompleteSearchBar?.view as? LinearLayout)?.getChildAt(0)?.visibility = View.GONE
    // Add a custom hint
    autocompleteSearchBar?.setHint(getString(R.string.where_to))
    autocompleteSearchBar?.a?.setPadding(4.toPx().toInt(), 0, 0, 0)
    val params = LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.MATCH_PARENT,
      LinearLayout.LayoutParams.MATCH_PARENT
    )
    params.marginStart = 28.toPx().toInt()
    autocompleteSearchBar?.a?.layoutParams = params
    
    // Specify the types of place data to return.
    autocompleteSearchBar?.setPlaceFields(
      listOf(
        Place.Field.ID,
        Place.Field.NAME,
        Place.Field.LAT_LNG,
        Place.Field.ADDRESS
      )
    )
    bindEvents()
  }
  
  private fun bindEvents() {
    // Set up a PlaceSelectionListener to handle the response.
    autocompleteSearchBar?.placeSelected(
      onSuccess = {
        LogWrapper.d("Selected place ->$name & Lat/Lng -> $latLng")
        latLng?.let {
          showAddCheckPointDialog(it)
        }
      },
      
      onError = {
        LogWrapper.d("An Error occurred -> $status")
      }
    )
    
    activity?.searchFab?.setOnClickListener { autocompleteSearchBar?.a?.performClick() }
  }
  
  override fun onStart() {
    super.onStart()
    mapView?.onStart()
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
    map?.apply {
      mapType = GoogleMap.MAP_TYPE_NORMAL
      uiSettings.isMyLocationButtonEnabled = false
      uiSettings.isMapToolbarEnabled = false
      setOnMapLongClickListener {
        handleMapLongClick(it)
      }
      setOnMarkerClickListener {
        handleMarkerClick(it)
      }
    }
    onPermissionGranted()
    LogWrapper.d("Exit")
  }
  
  @SuppressLint("MissingPermission")
  override fun onPermissionGranted() {
    map?.let { map ->
      map.isMyLocationEnabled = true
      activity?.currentLocationFab?.show()
      activity?.currentLocationFab?.setOnClickListener {
        currentLocation?.let { location ->
          map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
              LatLng(location.latitude, location.longitude), DEFAULT_ZOOM
            )
          )
        }
      }
    }
  }
  
  private fun locationCallback(location: Location) {
    currentLocation = location
    LogWrapper.d("Current Location : ${location.latitude}/${location.longitude}, " +
      "Last update time -> ${DateTime.now().toString("EEE, MMM d ''yy, HH:mm:ss")}")
  }
  
  private fun handleMapLongClick(latLng: LatLng?) {
    LogWrapper.d("Map Clicked at position -> $latLng")
    mapView.performHapticFeedback(
      HapticFeedbackConstants.VIRTUAL_KEY,
      HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
    )
    
    latLng?.let {
      showAddCheckPointDialog(it)
      FetchAddressIntentService.startActionFetchAddress(requireContext(), it, addressResultReceiver)
    }
  }
  
  private fun handleMarkerClick(marker: Marker?): Boolean {
    
    marker?.let {
      RemoveCheckPointDialogFragment.newInstance(
        title = getString(R.string.remove_check_point),
        message = getString(R.string.confirm_remove_checkpoint),
        checkPointName = it.title
      ).also { dialog ->
        dialog.setRemoveCheckPointListener(this)
      }.show(fragmentManager, null)
    }
    return true
  }
  
  private fun buildGeofence(checkPoint: CheckPoint): Geofence = Geofence.Builder().apply {
    // Set a request ID of the geofence. This is a string to identify this
    // geofence.
    setRequestId(checkPoint.name)
    
    // Set the circular region of this geofence.
    setCircularRegion(
      checkPoint.latitude,
      checkPoint.longitude,
      checkPoint.radius.toFloat()
    )
    
    // Set the transition types of interest. Alerts are only generated for these
    // transition. We are only interested in entry transitions.
    setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
    
    // Set the expiration duration of the geofence. This geofence will exist until
    // the user removes it
    setExpirationDuration(Geofence.NEVER_EXPIRE)
    
  }.build() // Create the geofence.
  
  private fun createGeofenceRequest(geofence: Geofence): GeofencingRequest =
    GeofencingRequest.Builder().apply {
      // Trigger a  GEOFENCE_TRANSITION_ENTER notification if the device
      // is already inside that geofence.
      setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
      
      // Add the geofence to be monitored by geofencing service.
      addGeofences(arrayListOf(geofence))
      
    }.build() // Create the request
  
  /**
   * Adds geofences. This method should be called after the user has granted the location
   * permission.
   */
  @SuppressLint("MissingPermission")
  private fun addGeofences(geofence: Geofence) {
    geofencingClient
      .addGeofences(createGeofenceRequest(geofence), geofencePendingIntent)
      .run {
        addOnSuccessListener {
          // Geofence added
          LogWrapper.d(getString(R.string.geofences_added))
        }
        
        addOnFailureListener {
          // Failed to add geofences
          val errorMessage = GeofenceErrorMessages.getErrorString(requireContext(), it)
          LogWrapper.e(errorMessage)
        }
      }
    
  }
  
  private fun removeGeofences(checkPointName: String) {
    geofencingClient.removeGeofences(arrayListOf(checkPointName)).run {
      addOnSuccessListener {
        LogWrapper.d(getString(R.string.geofences_removed))
      }
      
      addOnFailureListener {
        val errorMessage = GeofenceErrorMessages.getErrorString(requireContext(), it)
        LogWrapper.e(errorMessage)
      }
    }
  }
  
  private fun showAddCheckPointDialog(latLng: LatLng) {
    NewCheckPointDialogFragment
      .newInstance(latLng)
      .also { it.setOnNewCheckPointListener(this) }
      .show(fragmentManager, null)
  }
  
  override fun onAddCheckPoint(checkPointName: String, latLng: LatLng, radius: Int) {
    currentLocation?.let {
      val results = FloatArray(3)
      Location.distanceBetween(
        it.latitude,
        it.longitude,
        latLng.latitude,
        latLng.longitude,
        results
      )
      if (results[0] < Constants.RADIUS_MIN) {
        requireContext().showToast(R.string.msg_already_near_location, Toast.LENGTH_LONG)
      } else {
        val checkPoint = CheckPoint(
          checkPointName, latLng.latitude, latLng.longitude, radius, active = true
        )
        checkPointViewModel.setCheckPoint(checkPoint)
      }
      
    }
    
  }
  
  override fun onRemoveCheckPoint(checkPointName: String) {
    LogWrapper.d("Removing checkpoint ->$checkPointName")
    removeGeofences(checkPointName)
    checkPointViewModel.delete(checkPointName)
    removeMarkerOnMap(checkPointName)
  }
  
  
  private fun subscribeToModel() {
    
    checkPointViewModel.activeCheckPoints.observeOnce(viewLifecycleOwner, Observer {
      LogWrapper.d("Loading all CheckPoint...")
      it.forEach { checkPoint -> addMarkerOnMap(checkPoint) }
    })
    
    checkPointViewModel.checkPoint.observe(viewLifecycleOwner, Observer {
      it.data?.let { checkPoint ->
        val geofence = buildGeofence(checkPoint)
        addGeofences(geofence)
        
        LogWrapper.d("Adding Checkpoint -> $checkPoint <- on the map ")
        addMarkerOnMap(checkPoint)
      }
    })
  }
  
  private fun addMarkerOnMap(checkPoint: CheckPoint) {
    LogWrapper.d("Adding active Checkpoints on the map")
    map?.apply {
      val latLng = LatLng(checkPoint.latitude, checkPoint.longitude)
      
      animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
      val marker = addMarker(
        MarkerOptions()
          .position(latLng)
          .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
          .draggable(false)
          .title(checkPoint.name)
      )
      val circle = addCircle(
        CircleOptions()
          .center(latLng)
          .radius(checkPoint.radius.toDouble())
          .strokeColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
          .fillColor(ContextCompat.getColor(requireContext(), R.color.light_orange))
      )
      markers[checkPoint.name] = MarkerPair(marker, circle)
    }
  }
  
  private fun removeMarkerOnMap(checkpointName: String) {
    markers[checkpointName]?.let {
      it.marker.remove()
      it.circle.remove()
    }
  }
  
  /**
   * Receiver for geofence transition changes.
   * <p>
   * Receives geofence transition events from Location Services in the form of an Intent containing
   * the transition type and geofence id(s) that triggered the transition. Creates a JobIntentService
   * that will handle the intent in the background.
   */
  class GeofenceBroadcastReceiver : BroadcastReceiver() {
    
    /**
     * Receives incoming intents.
     *
     * @param context the application context.
     * @param intent  sent by Location Services. This Intent is provided to Location
     *                Services (inside a PendingIntent) when addGeofences() is called.
     */
    override fun onReceive(context: Context, intent: Intent) {
      // Enqueues a JobIntentService passing the context and intent as parameters
      LogWrapper.d("Enqueuing Geofence transition job...")
      GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
    }
  }
  
  /**
   * Receiver for data sent from FetchAddressIntentService
   */
  private inner class AddressResultReceiver(handler: Handler) : ResultReceiver(handler) {
    
    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
      // Display the address string or an error message sent from the intent service.
      val checkPointAddress = resultData.getString(Constants.RESULT_DATA_KEY) as String
      LogWrapper.d("Address delivered -> $checkPointAddress")
      checkPointViewModel.setCheckPointCity(checkPointAddress)
    }
  }
  
}

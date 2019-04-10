package ca.poly.inf8405.alarmme.ui.homescreen

import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import ca.poly.inf8405.alarmme.R
import ca.poly.inf8405.alarmme.ui.BaseFragment
import ca.poly.inf8405.alarmme.ui.MainActivity
import ca.poly.inf8405.alarmme.ui.service.Constants
import ca.poly.inf8405.alarmme.ui.service.FetchAddressIntentService
import ca.poly.inf8405.alarmme.utils.LogWrapper
import ca.poly.inf8405.alarmme.utils.extensions.observeOnce
import ca.poly.inf8405.alarmme.viewmodel.CheckPointViewModel
import ca.poly.inf8405.alarmme.vo.CheckPoint
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_map.*
import org.joda.time.DateTime


class MapFragment : BaseFragment(),
  NewCheckPointDialogFragment.OnNewCheckPointDialogListener,
  OnMapReadyCallback {
  
  private val checkPointViewModel: CheckPointViewModel by lazy { (activity as MainActivity).obtainCheckPointViewModel() }
  
  // To manipulate the Map SDK methods
  private lateinit var map: GoogleMap
  private val markers = mutableListOf<Marker>()
  private var allowCameraUpdates = true
  
  // Receiver registered with this fragment to get the response from FetchAddressIntentService.
  private lateinit var resultReceiver: AddressResultReceiver
  
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
    resultReceiver = AddressResultReceiver(Handler())
    
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
      moveCamera(CameraUpdateFactory.zoomTo(12F))
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
      HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
    )
    
    showAddCheckPointDialog(latLng)
    FetchAddressIntentService.startActionFetchAddress(requireContext(), latLng, resultReceiver)
  }
  
  private fun showAddCheckPointDialog(latLng: LatLng) {
    val newCheckPointDialog =
      NewCheckPointDialogFragment.newInstance("", latLng)
    newCheckPointDialog.listener = this
    activity?.let { newCheckPointDialog.show(it.supportFragmentManager, null) }
  }
  
  override fun onAddCheckPoint(checkPointName: String, latLng: LatLng, radius: Int) {
    val checkPoint = CheckPoint(
      checkPointName, latLng.latitude, latLng.longitude, radius, active = true
    )
    checkPointViewModel.addCheckPoint(checkPoint)
  }
  
  private fun locationCallback(latLng: LatLng) {
    if (allowCameraUpdates) {
      map.moveCamera(CameraUpdateFactory.newLatLng(latLng))
      allowCameraUpdates = false
    }
    
    val dateTime = DateTime.now().toString("EEE, MMM d ''yy, HH:mm:ss")
    LogWrapper.d("Current Location -> $latLng, Last update time -> $dateTime")
  }
  
  private fun subscribeToModel() {
    
    checkPointViewModel.activeCheckPoints.observeOnce(viewLifecycleOwner, Observer {
      // clear the markers that are on the map if any
      markers.forEach { marker -> marker.remove() }
      LogWrapper.d("Adding active Checkpoints on the map")
      it.forEach { checkPoint -> addMarkerOnMap(checkPoint) }
    })
    
    checkPointViewModel.checkPoint.observe(viewLifecycleOwner, Observer {
      it.data?.let { checkPoint ->
        LogWrapper.d("Adding Checkpoint -> $checkPoint <- on the map ")
        addMarkerOnMap(checkPoint) }
    })
  }
  
  private fun addMarkerOnMap(checkPoint: CheckPoint) {
    
    map.apply {
      val latLng = LatLng(checkPoint.latitude, checkPoint.longitude)
      
      animateCamera(CameraUpdateFactory.zoomTo(15f))
      animateCamera(CameraUpdateFactory.newLatLng(latLng))
      val marker = addMarker(
        MarkerOptions()
          .position(latLng)
          .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
          .draggable(false)
          .title(checkPoint.name)
      )
      markers.add(marker)
      addCircle(
        CircleOptions()
          .center(latLng)
          .radius(checkPoint.radius.toDouble())
          .strokeColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
          .fillColor(ContextCompat.getColor(requireContext(), R.color.light_orange))
      )
    }
  }
  
  private fun removeMarkerOnMap(checkPoint: CheckPoint) {
  
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

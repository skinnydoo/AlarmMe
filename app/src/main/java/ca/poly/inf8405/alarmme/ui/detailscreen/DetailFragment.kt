package ca.poly.inf8405.alarmme.ui.detailscreen


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import ca.poly.inf8405.alarmme.R
import ca.poly.inf8405.alarmme.di.GlideApp
import ca.poly.inf8405.alarmme.ui.BaseFragment
import ca.poly.inf8405.alarmme.ui.homescreen.OnMapAndViewReadyListener
import ca.poly.inf8405.alarmme.utils.LogWrapper
import ca.poly.inf8405.alarmme.viewmodel.DetailViewModel
import ca.poly.inf8405.alarmme.vo.CheckPoint
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_detail.*
import java.util.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 *
 */
class DetailFragment : BaseFragment(), OnMapAndViewReadyListener.OnGlobalLayoutAndMapReadyListener {
  
  private val args: DetailFragmentArgs by navArgs()
  
  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory
  
  private lateinit var detailViewModel: DetailViewModel
  
  // To manipulate the Map SDK methods
  lateinit var map: GoogleMap
  
  private val checkPoint: CheckPoint by lazy { args.checkPoint }
  
  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_detail, container, false)
  }
  
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    detailViewModel = ViewModelProviders.of(this, viewModelFactory)[DetailViewModel::class.java]
    detailViewModel.setCheckPoint(checkPoint)
    initView(savedInstanceState)
    subscribeToModel()
  }
  
  private fun initView(savedInstanceState: Bundle?) {
    map_lite?.run {
      onCreate(savedInstanceState)
      OnMapAndViewReadyListener(this, this@DetailFragment) // get notified when the map is ready to use
    }
  }
  
  private fun subscribeToModel() {
    detailViewModel.checkPoint.observe(viewLifecycleOwner, Observer {
      it.data?.let { checkPoint -> bindDetailFragmentViews(checkPoint) }
    })
  }
  
  private fun bindDetailFragmentViews(checkPoint: CheckPoint) {
    
    
    check_point_name.text = checkPoint.name
    check_point_message.text = checkPoint.message
    check_point_status.text = if (checkPoint.active)
      getString(R.string.active) else getString(R.string.not_active)
    
    checkPoint.weather?.let {
  
      GlideApp.with(requireContext())
        .load("https://openweathermap.org/img/w/${it.description[0].iconUrl}.png")
        .fitCenter()
        .into(check_point_weather_image)
      
      check_point_city_name.text = it.name
      
      check_point_weather_desc.text = getString(
        R.string.day_weather_description,
        it.dateTime.dayOfWeek().getAsText(Locale.getDefault()),
        it.description[0].description)
     
      check_point_weather_temp.text = getString(R.string.temp, it.weatherInfo.temperature)
      
      check_point_weather_min_temp.text = getString(R.string.min_temp, it.weatherInfo.minTemp)
     
      check_point_weather_max_temp.text = getString(R.string.max_temp, it.weatherInfo.maxTemp)
      
      check_point_weather_pressure.text = getString(R.string.pressure, it.weatherInfo.pressure)
      
      check_point_weather_humidity.text = getString(R.string.humidity, it.weatherInfo.humidity, "%")
      
      check_point_sunrise.text = getString(R.string.sunrise, it.city.sunrise.toString("HH:mm"))
      check_point_sunset.text = getString(R.string.sunset, it.city.sunset.toString("HH:mm"))
    }
   
  }
  
  override fun onResume() {
    super.onResume()
    listener?.onFragmentDisplayed(hideSearchBar = true, hideCurrentLocationFab = true)
  }
  
  override fun onMapReady(googleMap: GoogleMap?) {
    // return early if the map was not initialised properly
    map = googleMap ?: return
    LogWrapper.d("Map Lite is ready...making initial setup")
    with(map) {
      mapType = GoogleMap.MAP_TYPE_NORMAL
      animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(checkPoint.latitude, checkPoint
        .longitude), 16f))
    }
  }
}

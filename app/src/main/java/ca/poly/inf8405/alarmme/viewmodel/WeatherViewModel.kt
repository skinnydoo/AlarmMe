package ca.poly.inf8405.alarmme.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import ca.poly.inf8405.alarmme.repository.remote.WeatherRepository
import ca.poly.inf8405.alarmme.utils.AbsentLiveData
import ca.poly.inf8405.alarmme.vo.Resource
import ca.poly.inf8405.alarmme.vo.Weather
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject

class WeatherViewModel @Inject constructor(
  weatherRepository: WeatherRepository
) : ViewModel() {
  
  // region Current weather LiveData by geographic coordinates
  private val _latLng = MutableLiveData<LatLng>()
  val latLng: LiveData<LatLng>
    get() = _latLng
  
  val currentWeatherByLatLng: LiveData<Resource<Weather>> =
    Transformations.switchMap(_latLng) { latLng ->
      if (latLng == null){
        AbsentLiveData.create()
      } else {
        weatherRepository.loadCurrentWeather(latLng)
      }
    }
  
  fun setLatLng(latLng: LatLng?) {
    if (_latLng.value != latLng) {
      _latLng.value = latLng
    }
  }
  
  // endregion
  
  // region Current weather by city name
  private val _cityName = MutableLiveData<String>()
  val cityName: LiveData<String>
    get() = _cityName
  
  
  val currentWeatherByCityName: LiveData<Resource<Weather>> =
    Transformations.switchMap(_cityName) { cityName ->
      if (cityName == null) {
        AbsentLiveData.create()
      } else {
        weatherRepository.loadCurrentWeather(cityName)
      }
    }
  
  fun setCityName(name: String?) {
    if (_cityName.value != name) {
      _cityName.value = name
    }
  }
  
  
  // endregion
}
package ca.poly.inf8405.alarmme.repository.remote

import androidx.lifecycle.LiveData
import ca.poly.inf8405.alarmme.api.ApiResponse
import ca.poly.inf8405.alarmme.api.WeatherService
import ca.poly.inf8405.alarmme.db.WeatherDao
import ca.poly.inf8405.alarmme.repository.NetworkBoundResource
import ca.poly.inf8405.alarmme.vo.Resource
import ca.poly.inf8405.alarmme.vo.Weather
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that abstract access to Weather data sources
 */
@Singleton
class WeatherRepository @Inject constructor(
  private val weatherDao: WeatherDao,
  private val weatherService: WeatherService
) {
  
  fun loadCurrentWeather(latLng: LatLng) : LiveData<Resource<Weather>> {
    return object : NetworkBoundResource<Weather, Weather>() {
      override fun loadFromDb(): LiveData<Weather> =
        weatherDao.findByLatLng(latLng.latitude, latLng.longitude)
  
      override fun shouldFetch(data: Weather?): Boolean = data == null
  
      override fun createCall(): LiveData<ApiResponse<Weather>> =
        weatherService.getCurrentWeather(latLng.latitude, latLng.longitude)
  
      override fun saveCallResult(item: Weather): Unit = weatherDao.insert(item)
      
    }.asLiveData()
  }
  
  fun loadCurrentWeather(cityName: String) : LiveData<Resource<Weather>> {
    return object : NetworkBoundResource<Weather, Weather>() {
      override fun loadFromDb(): LiveData<Weather> = weatherDao.findCityName(cityName)
  
      override fun shouldFetch(data: Weather?): Boolean = data == null
      
      override fun createCall(): LiveData<ApiResponse<Weather>> =
        weatherService.getCurrentWeather(cityName)
  
      override fun saveCallResult(item: Weather) = weatherDao.insert(item)
    }.asLiveData()
  }
}
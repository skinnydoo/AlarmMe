package ca.poly.inf8405.alarmme.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ca.poly.inf8405.alarmme.di.qualifier.ViewModelKey
import ca.poly.inf8405.alarmme.viewmodel.CheckPointViewModel
import ca.poly.inf8405.alarmme.viewmodel.ViewModelFactory
import ca.poly.inf8405.alarmme.viewmodel.WeatherViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
  @Binds
  @IntoMap
  @ViewModelKey(CheckPointViewModel::class)
  abstract fun bindCheckPointViewModel(checkPointViewModel: CheckPointViewModel): ViewModel
  
  @Binds
  @IntoMap
  @ViewModelKey(WeatherViewModel::class)
  abstract fun checkWeatherViewModel(weatherViewModel: WeatherViewModel): ViewModel
  
  @Binds
  abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
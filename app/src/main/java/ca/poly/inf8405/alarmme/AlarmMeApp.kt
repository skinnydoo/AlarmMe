package ca.poly.inf8405.alarmme

import android.app.Activity
import android.app.Application
import android.app.Service
import ca.poly.inf8405.alarmme.di.AppInjector
import ca.poly.inf8405.alarmme.utils.LogWrapper
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import javax.inject.Inject

class AlarmMeApp: Application(), HasActivityInjector, HasServiceInjector {
  
  @Inject
  lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>
  
  @Inject
  lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>
  
  override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG){
      LogWrapper.DEBUG = true
    }
    AppInjector.init(this)
  }
  
  override fun activityInjector() = dispatchingActivityInjector
  
  override fun serviceInjector() = dispatchingServiceInjector
}
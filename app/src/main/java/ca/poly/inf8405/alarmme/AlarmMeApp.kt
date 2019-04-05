package ca.poly.inf8405.alarmme

import android.app.Activity
import android.app.Application
import ca.poly.inf8405.alarmme.di.AppInjector
import ca.poly.inf8405.alarmme.utils.LogWrapper
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

class AlarmMeApp: Application(), HasActivityInjector {
  
  @Inject
  lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>
  
  override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG){
      LogWrapper.debug = true
    }
    AppInjector.init(this)
  }
  
  override fun activityInjector() = dispatchingAndroidInjector
}
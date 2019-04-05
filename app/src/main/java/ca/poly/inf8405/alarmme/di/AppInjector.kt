package ca.poly.inf8405.alarmme.di

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import ca.poly.inf8405.alarmme.AlarmMeApp
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector

/**
 * Helper class to automatically inject fragments if they implement [Injectable].
 */
object AppInjector {
  
  fun init(alarmMeApp: AlarmMeApp) {
    DaggerAppComponent
      .builder()
      .application(alarmMeApp) // bind our application instance to our Dagger graph
      .build()
      .inject(alarmMeApp)
    
    alarmMeApp.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
  
      override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        handleActivity(activity)
      }
  
      override fun onActivityStarted(activity: Activity) {
    
      }
  
      override fun onActivityResumed(activity: Activity) {
      
      }
  
      override fun onActivityPaused(activity: Activity) {
    
      }
  
      override fun onActivityStopped(activity: Activity) {
    
      }
  
      override fun onActivityDestroyed(activity: Activity) {
      
      }
  
      override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {
      
      }
    })
  }
  
  private fun handleActivity(activity: Activity) {
    if (activity is HasSupportFragmentInjector) {
      AndroidInjection.inject(activity)
    }
    
    if (activity is FragmentActivity) {
      activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
        fragmentLifeCycleCallback, true)
    }
    
  }
  
  /**
   * Callback interface for listening to fragment state changes that happen
   *  a given FragmentManager.
   */
  private val fragmentLifeCycleCallback =
      object : FragmentManager.FragmentLifecycleCallbacks() {
  
        /**
         * Called after the fragment has returned from the FragmentManager's
         * call to onCreate(Bundle)
         */
        override fun onFragmentCreated(
        fm: FragmentManager,
        f: Fragment,
        savedInstanceState: Bundle?
      ) {
        if (f is Injectable) {
          AndroidSupportInjection.inject(f)
        }
      }
    }
}
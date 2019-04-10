package ca.poly.inf8405.alarmme.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import ca.poly.inf8405.alarmme.R
import ca.poly.inf8405.alarmme.viewmodel.CheckPointViewModel
import com.google.android.libraries.places.api.Places
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class MainActivity : AppCompatActivity(),
  HasSupportFragmentInjector {
  
  @Inject
  lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
  
  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    setContentView(R.layout.activity_main)
    
    val apiKey = getString(R.string.google_maps_api_key)
    
    // Initialize Places
    if(!Places.isInitialized()) {
      Places.initialize(applicationContext, apiKey)
    }
  }
  
  override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector
  
  
  fun obtainCheckPointViewModel(): CheckPointViewModel =
    ViewModelProviders.of(this, viewModelFactory)[CheckPointViewModel::class.java]
}

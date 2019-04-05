package ca.poly.inf8405.alarmme.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import ca.poly.inf8405.alarmme.R
import com.google.android.libraries.places.api.Places
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class MainActivity : AppCompatActivity(),
  HasSupportFragmentInjector {
  
  @Inject
  lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    
    val apiKey = getString(R.string.google_maps_api_key)
    
    // Initialize Places
    if(!Places.isInitialized()) {
      Places.initialize(applicationContext, apiKey)
    }
  }
  
  override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector
  
}

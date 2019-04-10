package ca.poly.inf8405.alarmme.ui

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import ca.poly.inf8405.alarmme.R
import ca.poly.inf8405.alarmme.utils.LogWrapper
import ca.poly.inf8405.alarmme.utils.extensions.placeSelected
import ca.poly.inf8405.alarmme.utils.extensions.toPx
import ca.poly.inf8405.alarmme.viewmodel.CheckPointViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_main.*
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
    
    // Initialize Places
    val apiKey = getString(R.string.google_maps_api_key)
    if(!Places.isInitialized()) {
      Places.initialize(applicationContext, apiKey)
    }
    
  }
  
  override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector

  
  fun obtainCheckPointViewModel(): CheckPointViewModel =
    ViewModelProviders.of(this, viewModelFactory)[CheckPointViewModel::class.java]
}

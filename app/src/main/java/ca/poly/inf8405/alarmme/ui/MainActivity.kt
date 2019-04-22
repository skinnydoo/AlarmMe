package ca.poly.inf8405.alarmme.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.os.BatteryManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import ca.poly.inf8405.alarmme.R
import ca.poly.inf8405.alarmme.utils.LogWrapper
import ca.poly.inf8405.alarmme.viewmodel.CheckPointViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(),
  HasSupportFragmentInjector,
  BaseFragment.OnFragmentEventListener {
  
  @Inject
  lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
  
  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory
  
  private val batteryInfoReceiver = BatteryInfoReceiver()
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    setContentView(R.layout.activity_main)
  
    val host: NavHostFragment = supportFragmentManager
      .findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return
  
    val navController = host.navController
    
    setupBottomNav(navController)
  
    navController.addOnDestinationChangedListener { _, destination, _ ->
      val dest: String = try {
        resources.getResourceName(destination.id)
      } catch (e: Resources.NotFoundException) {
        Integer.toString(destination.id)
      }
      LogWrapper.d("Navigated to $dest")
    }
    
    // Initialize Places
    /*val apiKey = getString(R.string.google_maps_api_key)
    if(!Places.isInitialized()) {
      Places.initialize(applicationContext, apiKey)
    }*/
    
    registerReceiver(batteryInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    
  }
  
  override fun onDestroy() {
    super.onDestroy()
    unregisterReceiver(batteryInfoReceiver)
  }
  
  override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector
  
  override fun onFragmentDisplayed(hideSearchBar: Boolean, hideCurrentLocationFab: Boolean) {
    search_bar_holder.visibility = if (hideSearchBar) View.GONE else View.VISIBLE
    if (hideCurrentLocationFab) currentLocationFab.hide() else currentLocationFab.show()
  }
  
  fun obtainCheckPointViewModel(): CheckPointViewModel =
    ViewModelProviders.of(this, viewModelFactory)[CheckPointViewModel::class.java]
  
  private fun setupBottomNav(navController: NavController) {
    val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
    bottomNav?.setupWithNavController(navController)
  }
  
  /**
   * Needed so Fragment's onActivityResult can be called
   */
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    LogWrapper.d("requestCode -> $requestCode, resultCode -> $resultCode")
    super.onActivityResult(requestCode, resultCode, data)
  }
  
  
  private inner class BatteryInfoReceiver: BroadcastReceiver() {
  
    override fun onReceive(context: Context?, intent: Intent?) {
      val batteryPct = intent?.let {
        val level: Int = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale: Int = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        level / scale.toFloat() * 100
        
      }
  
      LogWrapper.d("Battery percent -> ${batteryPct?.toInt()}%")
    }
  }
}

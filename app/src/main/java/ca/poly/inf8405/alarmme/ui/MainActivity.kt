package ca.poly.inf8405.alarmme.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ca.poly.inf8405.alarmme.R

class MainActivity : AppCompatActivity(), MapFragment.OnMapFragmentInteractionListener {
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    //requestWindowFeature(Window.FEATURE_NO_TITLE)
    /*with(window) {
      addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
      addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
      requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
      enterTransition = Explode()
      exitTransition = Slide()
    }*/
    setContentView(R.layout.activity_main)
  }
  
  
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
  }
  
  override fun onFragmentInteraction(uri: Uri) {
  
  }
  
}

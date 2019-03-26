package ca.poly.inf8405.alarmme.ui

import android.net.Uri
import android.os.Bundle
import android.transition.Explode
import android.transition.Slide
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import ca.poly.inf8405.alarmme.R

class MainActivity : AppCompatActivity(), MapFragment.OnMapFragmentInteractionListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        with(window) {
            setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            enterTransition = Explode()
            exitTransition = Slide()
        }
        setContentView(R.layout.activity_main)

    }

    override fun onFragmentInteraction(uri: Uri) {

    }
}

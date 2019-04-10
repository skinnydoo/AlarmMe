package ca.poly.inf8405.alarmme.utils.extensions

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.internal.fn
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

/**
 * Extensions for simpler adding fragments
 */
inline fun FragmentManager.inTransaction(fn: FragmentTransaction.() -> FragmentTransaction) {
  beginTransaction().fn().commit()
}

inline fun FragmentManager.inTransactionWithBackStack(fn: FragmentTransaction.() -> FragmentTransaction) {
  beginTransaction().fn().addToBackStack(null).commit()
}

fun AppCompatActivity.addFragment(
  frameId: Int, fragment: Fragment, tag: String? = null, transition: Int = FragmentTransaction.TRANSIT_NONE
) {
  
  supportFragmentManager.inTransaction {
    add(frameId, fragment, tag)
    setTransition(transition)
  }
}

fun AppCompatActivity.replaceFragment(
  frameId: Int, fragment: Fragment, tag: String? = null, transition: Int = FragmentTransaction.TRANSIT_NONE
) {
  
  supportFragmentManager?.inTransaction {
    replace(frameId, fragment, tag)
    setTransition(transition)
  }
}

fun AppCompatActivity.addFragmentWithBackStack(
  frameId: Int, fragment: Fragment, tag: String? = null, transition: Int = FragmentTransaction.TRANSIT_NONE
) {
  
  supportFragmentManager.inTransactionWithBackStack { add(frameId, fragment, tag).setTransition(transition) }
}

fun AppCompatActivity.replaceFragmentWithBackStack(
  frameId: Int, fragment: Fragment, tag: String? = null, transition: Int = FragmentTransaction.TRANSIT_NONE
) {
  
  supportFragmentManager.inTransactionWithBackStack { replace(frameId, fragment, tag).setTransition(transition) }
}

fun Fragment.addChildFragment(
  frameId: Int, fragment: Fragment, tag: String? = null, transition: Int = FragmentTransaction.TRANSIT_NONE
) {
  
  childFragmentManager.inTransaction {
    add(frameId, fragment, tag)
    setTransition(transition)
  }
}

fun Fragment.replaceChildFragment(
  frameId: Int, fragment: Fragment, tag: String? = null, transition: Int = FragmentTransaction.TRANSIT_NONE
) {
  
  childFragmentManager.inTransaction {
    replace(frameId, fragment, tag)
    setTransition(transition)
  }
}

inline fun AutocompleteSupportFragment.placeSelected(
  crossinline onSuccess: Place.() -> Unit,
  crossinline onError: Status.() -> Unit
) {
  setOnPlaceSelectedListener(object : PlaceSelectionListener {
  
    override fun onPlaceSelected(place: Place) {
      onSuccess(place)
    }
  
    override fun onError(status: Status) {
      onError(status)
    }
  })
}
package ca.poly.inf8405.alarmme.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

inline fun View.snackBar(@StringRes messageRes: Int, length: Int = Snackbar.LENGTH_LONG, f: Snackbar.() -> Unit = {}) {
  snackBar(resources.getString(messageRes), length, f)
}

inline fun View.snackBar(message: String, length: Int = Snackbar.LENGTH_LONG, f: Snackbar.() -> Unit = {}) {
  val snack = Snackbar.make(this, message, length)
  snack.f()
  snack.show()
}

fun Snackbar.action(@StringRes actionRes: Int, color: Int? = null, listener: (View) -> Unit) {
  action(view.resources.getString(actionRes), color, listener)
}

fun Snackbar.action(action: String, color: Int? = null, listener: (View) -> Unit) {
  setAction(action, listener)
  color?.let { setActionTextColor(color) }
}

fun Context.showToast(@StringRes actionRes: Int, duration: Int = Toast.LENGTH_SHORT) {
  showToast(resources.getString(actionRes), duration)
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
  Toast.makeText(this, message, duration).show()
}
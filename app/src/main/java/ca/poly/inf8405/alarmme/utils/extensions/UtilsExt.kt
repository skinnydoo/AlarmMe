package ca.poly.inf8405.alarmme.utils.extensions

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import kotlin.math.round

fun Int.toPx(): Float = round(this * Resources.getSystem().displayMetrics.density)

inline fun <reified T : Any> newIntent(context: Context): Intent = Intent(context, T::class.java)
package ca.poly.inf8405.alarmme.utils.extensions

import android.content.res.Resources
import kotlin.math.round

fun Int.toPx(): Float = round(this * Resources.getSystem().displayMetrics.density)

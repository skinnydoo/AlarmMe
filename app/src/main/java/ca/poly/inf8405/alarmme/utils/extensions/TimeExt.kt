package ca.poly.inf8405.alarmme.utils.extensions

val Long.seconds: Long
  get() = (this / 1000) % 60
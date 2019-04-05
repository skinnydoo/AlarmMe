package ca.poly.inf8405.alarmme.di.qualifier

import javax.inject.Qualifier

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiKey(val value: String)
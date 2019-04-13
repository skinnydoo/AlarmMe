package ca.poly.inf8405.alarmme.di

import ca.poly.inf8405.alarmme.service.GeofenceTransitionsJobIntentService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilderModule {
  @ContributesAndroidInjector
  abstract fun contributeGeofenceTransitionService(): GeofenceTransitionsJobIntentService
}
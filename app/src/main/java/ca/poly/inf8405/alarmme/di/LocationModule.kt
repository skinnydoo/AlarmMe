package ca.poly.inf8405.alarmme.di

import android.content.Context
import ca.poly.inf8405.alarmme.di.qualifier.ApplicationContext
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class LocationModule {
  
  @Singleton
  @Provides
  fun provideFusedLocationClient(
    @ApplicationContext context: Context
  ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
  
  @Singleton
  @Provides
  fun provideSettingsClient(@ApplicationContext context: Context): SettingsClient =
    LocationServices.getSettingsClient(context)
  
  @Singleton
  @Provides
  fun providePlacesClient(
    @ApplicationContext context: Context
  ) : PlacesClient = Places.createClient(context)
  
  @Singleton
  @Provides
  fun provideGeofencingClient(@ApplicationContext context: Context) : GeofencingClient =
    LocationServices.getGeofencingClient(context)
  
  
  /**
   * Sets up the location request. This app uses ACCESS_FINE_LOCATION, as defined in
   * the AndroidManifest.xml.
   *
   * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
   * interval (5 seconds), the Fused Location Provider API returns location updates that are
   * accurate to within a few feet.
   */
  @Singleton
  @Provides
  fun provideLocationRequest() : LocationRequest =
    LocationRequest().apply {
      /**
       * Sets the desired interval for active location updates. This interval is inexact.
       * We may not receive updates at all if no location sources are available, or we
       * may receive them slower than requested. We may also receive update faster than
       * requested if other application are requesting location updates at a faster interval
       */
      interval = UPDATE_INTERVAL_IN_MILLISECONDS
    
      /**
       * Sets the fastest rate for active location updates. This interval is exact, and the
       * application will never receive update faster than this value
       */
      fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
      priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    
    }
  
  @Singleton
  @Provides
  fun provideLocationSettingsRequest(locationRequest: LocationRequest): LocationSettingsRequest =
    LocationSettingsRequest.Builder()
      .addLocationRequest(locationRequest)
      .build()
    
  
  
  companion object {
  
    // The desired interval for location updates. Inexact.
    // Updates may be more or less frequent.
    private const val UPDATE_INTERVAL_IN_MILLISECONDS = 10000L
  
    // The fastest rate for active location updates. Exact.
    // Updates will never be more frequent than this value
    private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
      UPDATE_INTERVAL_IN_MILLISECONDS / 2
  }
}
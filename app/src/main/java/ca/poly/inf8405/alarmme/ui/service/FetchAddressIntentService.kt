package ca.poly.inf8405.alarmme.ui.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.ResultReceiver
import ca.poly.inf8405.alarmme.R
import ca.poly.inf8405.alarmme.utils.LogWrapper
import com.google.android.gms.maps.model.LatLng
import java.io.IOException
import java.util.*

/**
 * Asynchronously handles an intent using a worker thread. Receives a ResultReceiver object and a
 * location through an intent. Tries to fetch the address for the location using a Geocoder, and
 * sends the result to the ResultReceiver.
 */
/**
 * This constructor is required, and calls the super IntentService(String)
 * constructor with the name for a worker thread.
 */
class FetchAddressIntentService : IntentService("FetchAddressIntentService") {
  /**
   * The receiver where results are forwarded from this service.
   */
  private var receiver: ResultReceiver? = null
  
  override fun onHandleIntent(intent: Intent?) {
    when (intent?.action) {
      Constants.ACTION_FETCH_ADDRESS -> {
        val latLng = intent.getParcelableExtra<LatLng>(Constants.EXTRA_LOCATION_DATA)
        receiver = intent.getParcelableExtra(Constants.EXTRA_FETCH_ADDRESS_RECEIVER)
        handleActionFetchAddress(latLng, receiver)
      }
       else -> LogWrapper.wtf(getString(R.string.no_receiver_received))
    }
  }
  
  /**
   * Handle action Foo in the provided background thread with the provided
   * parameters.
   */
  private fun handleActionFetchAddress(latLng: LatLng?, receiver: ResultReceiver?) {
    var errorMessage = ""
  
    // Check if receiver was properly registered
    receiver ?: return LogWrapper.d(getString(R.string.no_receiver_received))
    
    // Make sure that the latLng data was really sent over through an extra. If it wasn't,
    // send an error error message and return.
    latLng ?: return run {
      errorMessage = getString(R.string.no_location_data_provided)
      LogWrapper.d(errorMessage)
      deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage)
    }
    
    LogWrapper.d("Initializing Geocoder...")
    val geocoder = Geocoder(this, Locale.getDefault())
    
    // Address found using the Geocoder
    var addresses = emptyList<Address>()
    try {
      // Using getFromLocation() returns an array of Addresses for the area immediately
      // surrounding the given latitude and longitude. The results are a best guess and are
      // not guaranteed to be accurate.
      addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
    } catch (ex: IOException) {
      // Catch network or other IO problems
      errorMessage = getString(R.string.service_not_available)
      LogWrapper.e(errorMessage, ex)
    } catch (ex: IllegalArgumentException) {
      // Catch invalid latitude or longitude values
      errorMessage = getString(R.string.invalid_lat_long_used)
      LogWrapper.e("$errorMessage. Latitude -> ${latLng.latitude}, " +
        "Longitude -> ${latLng.longitude}", ex)
    }
    
    // Handle case where no address was found
    when {
      addresses.isEmpty() -> {
        if (errorMessage.isEmpty()) {
          errorMessage = getString(R.string.no_address_found)
          LogWrapper.e(errorMessage)
        }
        deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage)
      }
      
      else -> {
        val address = addresses[0]
        // Fetch the address lines using {@code getAddressLine},
        // join them, and send them to the thread. The {@link android.latLng.address}
        // class provides other options for fetching address details that you may prefer
        // to use. Here are some examples:
        // getLocality() ("Mountain View", for example)
        // getAdminArea() ("CA", for example)
        // getPostalCode() ("94043", for example)
        // getCountryCode() ("US", for example)
        // getCountryName() ("United States", for example)
        val result = "${address.locality}, ${address.countryCode}"
        LogWrapper.d("Address found -> $result")
        deliverResultToReceiver(Constants.SUCCESS_RESULT, result)
      }
    }
    
  }
  
  
  /**
   * Sends a resultCode and message to the receiver.
   */
  private fun deliverResultToReceiver(resultCode: Int, message: String) {
    val bundle = Bundle().apply { putString(Constants.RESULT_DATA_KEY, message) }
    receiver?.send(resultCode, bundle)
  }
  
  
  companion object {
    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    @JvmStatic
    fun startActionFetchAddress(context: Context,
                                latLng: LatLng,
                                receiver: ResultReceiver) {
      // Create an intent for passing to the intent service responsible for fetching the address.
      val intent = Intent(context, FetchAddressIntentService::class.java).apply {
        action = Constants.ACTION_FETCH_ADDRESS
  
        // Pass the result receiver as an extra to the service.
        putExtra(Constants.EXTRA_LOCATION_DATA, latLng)
  
        // Pass the location data as an extra to the service.
        putExtra(Constants.EXTRA_FETCH_ADDRESS_RECEIVER, receiver)
      }
  
      // Start the service. If the service isn't already running, it is instantiated and started
      // (creating a process for it if needed); if it is running then it remains running. The
      // service kills itself automatically once all intents are processed.
      context.startService(intent)
    }
  }
}

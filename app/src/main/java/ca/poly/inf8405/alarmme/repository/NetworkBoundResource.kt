package ca.poly.inf8405.alarmme.repository

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import ca.poly.inf8405.alarmme.api.ApiEmptyResponse
import ca.poly.inf8405.alarmme.api.ApiErrorResponse
import ca.poly.inf8405.alarmme.api.ApiResponse
import ca.poly.inf8405.alarmme.api.ApiSuccessResponse
import ca.poly.inf8405.alarmme.diskIOThread
import ca.poly.inf8405.alarmme.mainThread
import ca.poly.inf8405.alarmme.utils.LogWrapper
import ca.poly.inf8405.alarmme.vo.Resource

/**
 * A generic class that can provide a resource backed by both the sqlite database and the network.
 *
 * @param [ResultType]  Type for the Resource data.
 * @param [RequestType] Type for the API response.
*/
abstract class NetworkBoundResource<ResultType, RequestType> {
  
  /**
   * The final result LiveData
   */
  private val result = MediatorLiveData<Resource<ResultType>>()
  
  init {
    // STEP 1 - Send event for loading...
    LogWrapper.d("STEP 1 -> Sending event for loading...")
    result.value = Resource.loading(null)
    
    // STEP 2 - Fetch data from local database (NOTE: It can be empty)
    LogWrapper.d("STEP 2 -> Fetching data from local database...")
    @Suppress("LeakingThis")
    val dbSource = loadFromDb()
    result.addSource(dbSource) { data ->
      result.removeSource(dbSource)
  
      LogWrapper.d("STEP 3 -> Checking if we should fetch data from server...")
      // STEP 3 - Check if we should fetch data from api
      if (shouldFetch(data)) {
        LogWrapper.d("STEP 3 -> Cached data is out-of-data...Fetching new data from server...")
        fetchFromNetwork(dbSource) //True. Then perform the required network call and fetch the data
      } else { // False. Then send the data upstream from local database
        LogWrapper.d("STEP 3 -> Cached data is up-to-date...sending it upstream...")
        result.addSource(dbSource) { newData ->
          setValue(Resource.success(newData))
        }
      }
    }
  }
  
  /**
   * Fetches the data from network and persist into DB and then
   * send it back to UI.
   */
  private fun fetchFromNetwork(dbSource: LiveData<ResultType>) {
    val apiResponse = createCall()
    // Re-attach dbSource as a new source. It will dispatch its latest value quickly
    result.addSource(dbSource) { newData -> setValue(Resource.loading(newData)) }
    result.addSource(apiResponse) { response ->
      result.removeSource(apiResponse)
      result.removeSource(dbSource)
      
      // STEP 4 - On response, save the data into local database and send the data and the event
      // upstream
      LogWrapper.d("STEP 4 -> Server response -> $response\n\nSaving the data into local database" +
        " and sending it and the event upstream")
      when(response) {
        is ApiSuccessResponse -> {
          diskIOThread {
            saveCallResult(processResponse(response))
            mainThread {
              // we specially request a new live data,
              // otherwise we will get immediately last cached value,
              // which may not be updated with latest results received from network.
              result.addSource(loadFromDb()) { newData -> setValue(Resource.success(newData))}
            }
          }
        }
        
        is ApiEmptyResponse -> {
          mainThread {
            // Reload from disk whatever we had
            result.addSource(loadFromDb()) { newData -> setValue(Resource.success(newData))}
          }
        }
        
        is ApiErrorResponse -> {
          onFetchFailed()
          result.addSource(dbSource) { newData ->
            setValue(Resource.error(response.errorMessage, newData))
          }
        }
      }
    }
  }
  
  
  @MainThread
  private fun setValue(newValue: Resource<ResultType>) {
    if (result.value != newValue) result.value = newValue
  }
  
  
  // region NetworkBoundResource methods
  
  /**
   * Called when the fetch fails. The child class may want to reset components
   * like rate limiter.
   */
  protected open fun onFetchFailed() {}
  
  /**
   * Returns a LiveData object that represents the resource that's implemented
   * in the base class.
   */
  fun asLiveData() = result as LiveData<Resource<ResultType>>
  
  /**
   * Called to get the cached data from the database.
   */
  @MainThread
  protected abstract fun loadFromDb(): LiveData<ResultType>
  
  /**
   * Called with the data in the database to decide whether to fetch
   * potentially updated data from the network.
   */
  @MainThread
  protected abstract fun shouldFetch(data: ResultType?): Boolean
  
  /**
   * Called to create the API call.
   */
  @MainThread
  protected abstract fun createCall(): LiveData<ApiResponse<RequestType>>
  
  /**
   * Called to save the result of the API response into the database.
   */
  @WorkerThread
  protected abstract fun saveCallResult(item: RequestType)
  
  /**
   * Call to process the API call response
   */
  @WorkerThread
  protected open fun processResponse(response: ApiSuccessResponse<RequestType>) = response.body
  
  // endregion
  
}
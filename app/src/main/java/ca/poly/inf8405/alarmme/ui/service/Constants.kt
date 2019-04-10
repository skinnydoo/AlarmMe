package ca.poly.inf8405.alarmme.ui.service

object Constants {
  
  private const val PACKAGE_NAME = "ca.poly.inf8405.alarmme.ui"
  
  // ###########################################################
  // # FetchAddress Intent Service Constants
  // ###########################################################
  const val SUCCESS_RESULT = 0
  const val FAILURE_RESULT = 1
  const val ACTION_FETCH_ADDRESS = "$PACKAGE_NAME.ACTION_FETCH_ADDRESS"
  const val EXTRA_FETCH_ADDRESS_RECEIVER = "$PACKAGE_NAME.EXTRA_FETCH_ADDRESS_RECEIVER"
  const val EXTRA_LOCATION_DATA = "$PACKAGE_NAME.EXTRA_LOCATION_DATA"
  const val RESULT_DATA_KEY = "$PACKAGE_NAME.RESULT_DATA_KEY"
}
package ca.poly.inf8405.alarmme.utils

import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout

object Validator {

    fun isFieldEmpty(data: Any, errorMsg: String, updateUI: Boolean = true): Boolean {
        val str = getText(data)
        val empty = str.isBlank()

        if(updateUI) {
            val error = if (empty) errorMsg else null
            setError(data, error)
        }

        return empty
    }

    fun isNameValid(data: Any, errorMsg: String, updateUI: Boolean = true): Boolean {
        val str = getText(data)
        val valid = str.trim().length >= 4

        if(updateUI) {
            val error = if (valid) null else errorMsg
            setError(data, error)
        }

        return valid
    }

    /**
     * Retrieve string data from the parameter.
     * @param data - can be EditText or String
     * @return - String extracted from EditText or data if its data type is String.
     */
    private fun getText(data: Any): String {
        var str = ""
        if (data is EditText) {
            str = data.text.toString()
        } else if (data is String) {
            str = data
        }
        return str
    }

    /**
     * Sets error on EditText or TextInputLayout of the EditText.
     * @param data - Should be EditText
     * @param error - Message to be shown as error, can be null if no error is to be set
     */
    private fun setError(data: Any, error: String?) {
        if (data is EditText) {
            val til = data.parent.parent
            if (til is TextInputLayout) {
                til.error = error
            } else {
                data.error = error
            }
        }
    }
}
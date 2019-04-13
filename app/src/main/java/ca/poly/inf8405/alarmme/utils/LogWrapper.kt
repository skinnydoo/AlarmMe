package ca.poly.inf8405.alarmme.utils

import android.util.Log

/**
 * LogWrapper is used like android.util.Log but adds additional information like
 * method, file name, and line number
 * @author: Ralph S.
 */
object LogWrapper {

    /*
	 * DEBUG variable enables/disables all log messages to logcat
	 * Useful to disable prior to app store submission
	 */
    var DEBUG = false
    private const val stackTraceLevelsUp = 3

    /*
	 * trace
	 * Gathers the calling file, method, and line from the stack
	 * returns a string array with element 0 as file name and
	 * element 1 as method[line]
	 */
    private fun trace(e: Array<StackTraceElement>?, level: Int): Array<String>? {
        // Examine the method in which LogWrapper was used.

        val fileName: String
        val fullClassName: String
        val className: String
        val methodName: String
        val lineNumber: Int

        return if (e != null && e.size >= level) {

            val s = e[level]

            fileName = s.fileName
            fullClassName = s.className
            className = fileName.substring(0, fileName.indexOf("."))
            methodName = s.methodName
            lineNumber = s.lineNumber

            arrayOf(className, ".($fileName:$lineNumber) - $methodName: ")

        } else null
    }

    /*
	 * e method used to log passed error string and returns the
	 * calling file as the tag, method and line number prior
	 * to the string's message
	 */
    fun e(msg: String, thr: Throwable) {

        if (!DEBUG) return

        val t = trace(Thread.currentThread().stackTrace, stackTraceLevelsUp)
        Log.e(t!![0], t[1] + msg, thr)
    }

    /*
	 * e method used to log passed error string and returns the
	 * calling file as the tag, method and line number prior
	 * to the string's message
	 */
    fun e(msg: String) {

        if (!DEBUG) return

        val t = trace(Thread.currentThread().stackTrace, stackTraceLevelsUp)
        Log.e(t!![0], t[1] + msg)
    }

    /*
	 * d method used to log passed DEBUG string and returns the
	 * calling file as the tag, method and line number prior
	 * to the string's message
	 */
    fun d(msg: String) {

        if (!DEBUG) return

        val t = trace(Thread.currentThread().stackTrace, stackTraceLevelsUp)
        Log.d(t!![0], t[1] + msg)

    }

    /*
	 * d (tag, string)
	 * used to pass logging messages as normal but can be disabled
	 * when DEBUG == false
	 */
    fun d(TAG: String, msg: String) {

        if (!DEBUG) return

        Log.d(TAG, msg)
    }


    fun w(msg: String) {
        if (!DEBUG) return

        val t = trace(Thread.currentThread().stackTrace, stackTraceLevelsUp)
        Log.w(t!![0], t[1] + msg)
    }


    /*
	 * e method used to log passed error string and returns the
	 * calling file as the tag, method and line number prior
	 * to the string's message
	 */
    fun wtf(msg: String, thr: Throwable) {

        if (!DEBUG) return

        val t = trace(Thread.currentThread().stackTrace, stackTraceLevelsUp)
        Log.wtf(t!![0], t[1] + msg, thr)
    }

    /*
	 * e method used to log passed error string and returns the
	 * calling file as the tag, method and line number prior
	 * to the string's message
	 */
    fun wtf(msg: String) {

        if (!DEBUG) return

        val t = trace(Thread.currentThread().stackTrace, stackTraceLevelsUp)
        Log.wtf(t!![0], t[1] + msg)
    }
}
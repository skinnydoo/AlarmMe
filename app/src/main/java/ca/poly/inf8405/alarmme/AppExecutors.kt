package ca.poly.inf8405.alarmme

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Global executor pools for the whole application.
 * Grouping tasks like this avoids the effects of task starvation (e.g. disk reads don't wait behind
 * webservice requests).
 */
private val DISK_IO_EXECUTOR = Executors.newSingleThreadExecutor()
private val NETWORK_IO_EXECUTOR = Executors.newFixedThreadPool(3)


/**
 * Utility method to run blocks on a dedicated background thread, used for disk io/database work.
 */
fun diskIOThread(fn: () -> Unit) = DISK_IO_EXECUTOR.execute(fn)

/**
 * Utility method to run blocks on dedicated background threads, used for network IO
 */
fun networkIOThread(fn: () -> Unit) = NETWORK_IO_EXECUTOR.execute(fn)

/**
 * Utility method to run blocks on the UI thread
 */
fun mainThread(fn: () -> Unit) {
    val executor = MainThreadExecutor()
    executor.execute(fn)
}

private class MainThreadExecutor: Executor {
    private val mainThreadHandler: Handler = Handler(Looper.getMainLooper())

    override fun execute(command: Runnable?) {
        mainThreadHandler.post(command)
    }
}
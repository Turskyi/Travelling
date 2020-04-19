package ua.turskyi.travelling.utils

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor

internal class MainThreadExecutor : Executor {
    private val mHandler = Handler(Looper.getMainLooper())
    override fun execute(command: Runnable) {
        mHandler.post(command)
    }
}
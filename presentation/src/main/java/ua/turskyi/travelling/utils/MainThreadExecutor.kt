package ua.turskyi.visitedcountries.utils

import android.os.Handler
import java.util.concurrent.Executor
import android.os.Looper

internal class MainThreadExecutor : Executor {
    private val mHandler = Handler(Looper.getMainLooper())

    override fun execute(command: Runnable) {
        mHandler.post(command)
    }
}
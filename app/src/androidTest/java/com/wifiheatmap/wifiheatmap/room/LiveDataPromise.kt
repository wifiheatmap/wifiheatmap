package com.wifiheatmap.wifiheatmap.room

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import java.util.concurrent.CountDownLatch

class LiveDataPromise<T>(liveData: LiveData<T>) {
    var result: T? = null
    val latch = CountDownLatch(1)
    init {
        val handler = Handler(Looper.getMainLooper())
        handler.post(Runnable {
            liveData.observeForever {
                result = it
                latch.countDown()
            }
        })
    }

    fun await(): T? {
        latch.await()
        return result
    }
}
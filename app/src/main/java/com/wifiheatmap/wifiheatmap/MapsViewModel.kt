package com.wifiheatmap.wifiheatmap

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import java.util.*

class MapsViewModel(application: Application) : AndroidViewModel(application) {

    val isDarkModeEnabled = MutableLiveData<Boolean>(false)
    val isColorBlindModeEnabled = MutableLiveData<Boolean>(false)
    val startDate = MutableLiveData<Date>()
    val endDate = MutableLiveData<Date>()
    val refreshRate = MutableLiveData<Double>(5.0)
    val viewNetwork = MutableLiveData<String>("")
    val tileSize = MutableLiveData<Double>(0.5)

}

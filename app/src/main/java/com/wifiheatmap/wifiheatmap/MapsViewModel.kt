package com.wifiheatmap.wifiheatmap

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class MapsViewModel(application: Application) : AndroidViewModel(application) {

    val isDarkModeEnabled = MutableLiveData<Boolean>(false)
    val isColorBlindModeEnabled = MutableLiveData<Boolean>(false)
    val startDate = MutableLiveData<String>("")
    val endDate = MutableLiveData<String>("")
    val refreshRate = MutableLiveData<Double>(5.0)
    var viewNetwork = MutableLiveData<String>("")

}

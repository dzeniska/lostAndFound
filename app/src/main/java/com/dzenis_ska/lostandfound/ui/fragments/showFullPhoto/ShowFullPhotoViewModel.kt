package com.dzenis_ska.lostandfound.ui.fragments.showFullPhoto

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dzenis_ska.lostandfound.ui.fragments.BaseViewModel

class ShowFullPhotoViewModel(
    private val imageUrl: String
): BaseViewModel() {

    private val _photoUri = MutableLiveData<String>()
    val photoUri: LiveData<String> = _photoUri

    init {
        _photoUri.value = imageUrl
    }

}
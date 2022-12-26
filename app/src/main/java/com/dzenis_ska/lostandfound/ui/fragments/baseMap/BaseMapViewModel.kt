package com.dzenis_ska.lostandfound.ui.fragments.baseMap

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dzenis_ska.lostandfound.ui.db.firebase.FBAuth
import com.dzenis_ska.lostandfound.ui.db.firebase.FBDatabase
import com.dzenis_ska.lostandfound.ui.fragments.BaseViewModel
import com.dzenis_ska.lostandfound.ui.fragments.changeLocation.InstanceStateChangeLocationFragment
import com.dzenis_ska.lostandfound.ui.fragments.map.ViewModelState
import com.dzenis_ska.lostandfound.ui.utils.Event

class BaseMapViewModel(private val fbAuth: FBAuth, private val fbDatabase: FBDatabase) : BaseViewModel() {
}
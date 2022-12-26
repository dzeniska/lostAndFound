package com.dzenis_ska.lostandfound.ui.activity

import com.dzenis_ska.lostandfound.ui.db.firebase.FBAuth
import com.dzenis_ska.lostandfound.ui.db.firebase.FBDatabase
import com.dzenis_ska.lostandfound.ui.db.firebase.FBStorage
import com.dzenis_ska.lostandfound.ui.db.room.MainDataBase
import com.dzenis_ska.lostandfound.ui.fragments.BaseViewModel
import com.dzenis_ska.lostandfound.ui.fragments.baseMap.MapState

class MainActivityViewModel: BaseViewModel() {


    var stateMap: MapState? = null


//    fun setStateMapValue(mapState: MapState? = null) {
//        this.stateMap = mapState
//    }
//    fun getStateMapValue() = this.stateMap

    override fun onCleared() {
        super.onCleared()
    }

}



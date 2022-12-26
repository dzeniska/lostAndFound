package com.dzenis_ska.lostandfound

import android.app.Application
import com.dzenis_ska.lostandfound.ui.db.firebase.FBAuth
import com.dzenis_ska.lostandfound.ui.db.firebase.cloudFirestore.FBCloudFirestore
import com.dzenis_ska.lostandfound.ui.db.firebase.FBDatabase
import com.dzenis_ska.lostandfound.ui.db.firebase.FBStorage
import com.dzenis_ska.lostandfound.ui.db.room.MainDataBase
import com.dzenis_ska.lostandfound.ui.utils.networkState.NetworkRequest


class App : Application() {

    val network by lazy {
        NetworkRequest(this)
    }

    val database by lazy { MainDataBase.getDataBase(this)}

    val fbAuth by lazy {
        FBAuth()
    }
    val fbStorage by lazy {
        FBStorage()
    }
    val fbCLoudFirestore by lazy {
        FBCloudFirestore()
    }

    val fbDatabase by lazy {
        FBDatabase()
    }

}
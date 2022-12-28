package com.dzenis_ska.lostandfound.ui.fragments.chat

import android.util.Log
import com.dzenis_ska.lostandfound.R
import com.dzenis_ska.lostandfound.ui.db.firebase.FBAuth
import com.dzenis_ska.lostandfound.ui.db.firebase.cloudFirestore.FBCloudFirestore
import com.dzenis_ska.lostandfound.ui.db.firebase.FBDatabase
import com.dzenis_ska.lostandfound.ui.db.firebase.FBStorage
import com.dzenis_ska.lostandfound.ui.db.room.MainDataBase
import com.dzenis_ska.lostandfound.ui.fragments.BaseViewModel
import com.dzenis_ska.lostandfound.ui.fragments.toast
import com.dzenis_ska.lostandfound.ui.utils.Event
import com.dzenis_ska.lostandfound.ui.utils.map.ContextActions
import com.dzenis_ska.lostandfound.ui.utils.networkState.NetworkRequest

class ChatViewModel(
    private val network: NetworkRequest,
    private val fbAuth: FBAuth,
    private val fbDatabase: FBDatabase,
    private val fbStorage: FBStorage,
    private val fbCloudFirestore: FBCloudFirestore,
    private val roomDatabase: MainDataBase
) : BaseViewModel() {

    private val dao = roomDatabase.getChatDao()

    fun sendOnlyMessage(message: String) {
        Log.d("!!!sendOnlyMessage", "${message}")
        if (message.isEmpty())
        toast(R.string.message_is_empty)

    }
}


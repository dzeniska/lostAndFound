package com.dzenis_ska.lostandfound.ui.fragments.chat

import com.dzenis_ska.lostandfound.ui.db.firebase.FBAuth
import com.dzenis_ska.lostandfound.ui.db.firebase.cloudFirestore.FBCloudFirestore
import com.dzenis_ska.lostandfound.ui.db.firebase.FBDatabase
import com.dzenis_ska.lostandfound.ui.db.firebase.FBStorage
import com.dzenis_ska.lostandfound.ui.db.room.MainDataBase
import com.dzenis_ska.lostandfound.ui.fragments.BaseViewModel
import com.dzenis_ska.lostandfound.ui.utils.networkState.NetworkRequest

class ChatViewModel(
    private val network: NetworkRequest,
    private val fbAuth: FBAuth,
    private val fbDatabase: FBDatabase,
    private val fbStorage: FBStorage,
    private val fbCloudFirestore: FBCloudFirestore,
    private val roomDatabase: MainDataBase
): BaseViewModel() {
    private val dao = roomDatabase.getChatDao()
}
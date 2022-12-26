package com.dzenis_ska.lostandfound.ui.db.firebase

import android.util.Log
import com.dzenis_ska.lostandfound.ui.db.firebase.classes.RequestForInfo
import com.dzenis_ska.lostandfound.ui.db.firebase.classes.RequestForMarkerMap
import com.dzenis_ska.lostandfound.ui.db.firebase.classes.RequestFromBureau
import com.dzenis_ska.lostandfound.ui.db.firebase.classes.RequestToBureau
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FBDatabase {
    private val db = Firebase.database
    private val ref = db.getReference(MAIN_REQUESTS)
    private val refForMap = db.getReference(MARKERS_REQUEST)
    private val refForInfo = db.getReference(VIEW_COUNT_REQUEST)
    private var childEventListenerSAF: ChildEventListener? = null
    private var childInfoEventListenerSAF: ChildEventListener? = null
    private var valueEventListener: ValueEventListener? = null

    fun pushKey() = ref.push().key

    fun getInfoFlow(
        key: String,
        callbackInfo: (RequestForInfo) -> Unit
    ) {
        childInfoEventListenerSAF = refForInfo.orderByChild("/key").equalTo(key)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val requestForInfo = try {
                        snapshot.getValue(RequestForInfo::class.java)
                    } catch (e: DatabaseException) { e }
                    Log.d("!!!onChildAdded22", "_ ${requestForInfo}")
                    if (requestForInfo is RequestForInfo) callbackInfo(requestForInfo)
                }
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val requestForInfo = try {
                        snapshot.getValue(RequestForInfo::class.java)
                    } catch (e: DatabaseException) { e }
                    Log.d("!!!onChildChanged22", "_ ${requestForInfo}")
                    if (requestForInfo is RequestForInfo) callbackInfo(requestForInfo)
                }
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun getOneApplicationFlow(
        markerTag: String,
        startUpload: () -> Unit,
        eventResultChanged: (RequestFromBureau) -> Unit,
        eventResultRemoved: (String) -> Unit,
        errorMessage: (String) -> Unit
    ) {
        childEventListenerSAF = ref.orderByChild("/${FILTER_NODE}/key")
            .equalTo(markerTag)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d("!!!onChildChanged1", "${markerTag} _ ${snapshot.value}")
                    startUpload()
                    snapshot.children.forEach { snapshot2 ->
                        snapshot2.children.forEach { snapshot3 ->
                            var application =  try {
                                snapshot3.getValue(RequestFromBureau::class.java)
                            } catch (e: Exception){
                                Log.d("!!!onChildChanged2", "error : ${e.message}")
                                null
                            }
                            if (application != null) {
                                Log.d("!!!onChildChanged2", "${application}")
                                blockItem(snapshot) { blockMess ->
                                    Log.d("!!!onChildChangedBlocked", " _ ${blockMess}")
                                    blockMess?.let {
                                        application = application!!.copy(messBlocked = blockMess)
                                    }
                                }
                                eventResultChanged(application!!)
                            }
                            Log.d("!!!onCancelled", "${markerTag} _ ${application?.key}")
                        }
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    Log.d("!!!onChildRemoved", "${markerTag} _ ${snapshot.value}")
                    startUpload()
                    snapshot.children.forEach { dataSnapshot1 ->
                        dataSnapshot1.children.forEach {dataSnapshot2->
                            Log.d("!!!onChildRemoved2", "${dataSnapshot2.value}")
                            val requestFromBureau = try {
                                dataSnapshot2.getValue(RequestFromBureau::class.java)
                            } catch (e: Exception){
                                Log.d("!!!onChildRemoved2", "error : ${e.message}")
                                null
                            }
                            Log.d("!!!onChildChanged3", "${markerTag} _ ${requestFromBureau}")
                            if (requestFromBureau != null) eventResultRemoved(requestFromBureau.key)
                        }
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {
                    Log.d("!!!onCancelledCEL", "${error.message} _ ${error.code}")
                    errorMessage(error.message)
                }
            })
    }

    fun getOneApplication(
        markerTag: String,
        eventResult: (RequestFromBureau?, String) -> Unit,
        errorMessage: (String) -> Unit
    ) {
        ref.orderByChild("/${FILTER_NODE}/key")
            .equalTo(markerTag)
            .addListenerForSingleValueEvent( object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("!!!onCancelledonDataChange", "${markerTag} _ ${snapshot.value}")
                    if (snapshot.value == null) eventResult(null, markerTag)
                    snapshot.children.forEach { item ->
                        item.children.forEach { data ->
                            var application = data.child(APPLICATION).getValue(RequestFromBureau::class.java)
                            if (application != null) {

                                snapshot.children.forEach { item ->
                                    blockItem(item) { blockMess ->
                                        if (blockMess != null) application = application!!.copy(messBlocked = blockMess)
                                        Log.d("!!!currentUserUid", " _ ${application!!.uid}")
                                        eventResult(application, markerTag)
                                    }
                                }
                                addViews(application!!)
                            }
                            Log.d("!!!onCancelled", "${markerTag} _ ${application?.key}")
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.d("!!!onCancelled", error.message)
                    errorMessage(error.message)
                }
            }).runCatching {
                Log.d("!!!onCancelled", "error.message")
            }
    }

    fun addViews(application: RequestFromBureau) {
        refForInfo.child(application.key)
//            .child(INFO_NODE)
            .setValue(RequestForInfo(
                key = application.key,
                views = ServerValue.increment(1),
                calls = ServerValue.increment(0)
            ))
    }


    private fun blockItem(item: DataSnapshot, blockMess: (String?) -> Unit) {
        val blockCounter = item.child(BLOCK_ANN_NODE).childrenCount
        Log.d("!!!block1", "${blockCounter} _11")

        if (blockCounter > 0)
            item.child(BLOCK_ANN_NODE).children.forEach itemChild@{ mess ->
                if (mess.value.toString().isNotEmpty()) {
                    Log.d("!!!block2", "${blockCounter} _ ${mess.value}")
                    blockMess(mess.value.toString())
                    return@itemChild
                }
            }
        else
            blockMess(null)
    }

    fun readDataFromDB(
        query: Query? = null,
        eventResult: (ArrayList<RequestForMarkerMap>) -> Unit,
        result: (RequestForMarkerMap) -> Unit,
        errorMessage: (String) -> Unit
    ) {
        //remake ref to refForMap
        valueEventListener = ref/*ForMap*/.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("!!!DBonDataChange0", "${snapshot.value}")
                if (snapshot.value == null) return
                Log.d("!!!DBonDataChange1", "${snapshot.value}")
                val listRequestForMarkerMap = arrayListOf<RequestForMarkerMap>()
                snapshot.children.forEach { item ->
                    Log.d("!!!DBonDataChange11", "${item.value}")
                    item.children.forEach { data ->


                        val requestForMarkerMap = data.child(APPLICATION).getValue(
                            RequestForMarkerMap::class.java)

                        Log.d("!!!DBonDataChange112", "${data.child(APPLICATION).value} __ ")
                        Log.d("!!!DBonDataChange114", "__ ${requestForMarkerMap}")



                        if (requestForMarkerMap != null) {
                            listRequestForMarkerMap.add(requestForMarkerMap)
                        }
                    }
                }
//                load two time at one time
                Log.d("!!!DBonDataChange2", "${listRequestForMarkerMap.size}")
                if(listRequestForMarkerMap.isNotEmpty()) eventResult(listRequestForMarkerMap)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("!!!readDataFromDBError", "onCancelledS_ ${error.message}")
                Log.d("!!!readDataFromDBError", "onCancelledS_ ${error.code}")
                errorMessage(error.message)
            }
        })
    }

    fun addToDatabase(requestToBureau: RequestToBureau, childFirst: String, callback: (result: String)->Unit) {
//        val myRef = db.getReference("message")
        Log.d("!!!publish", "${childFirst}")
        //todo redo tables
        ref.child(childFirst)
            .child(requestToBureau.uid!!)
            .child(APPLICATION)
            .setValue(requestToBureau)
            .addOnSuccessListener {
                Log.d("!!!publish", "${requestToBureau}")

                ref.child(childFirst)
                    .child(FILTER_NODE)
                    .setValue(requestToBureau.createFilter())
                    .addOnSuccessListener {
                        refForMap.child(childFirst)
                            .setValue(requestToBureau.toRequestForMarkerMap())
                            .addOnSuccessListener {
                                //todo add ref for INFO
//                                refForInfo.child(requestToBureau.key.toString())
//                                    .setValue(RequestForInfo())
//                                    .addOnSuccessListener {
                                        callback(ADD_TO_DB_SUCCESS)
                                        //remove the block if there is one
                                        ref.child(childFirst)
                                            .child(BLOCK_ANN_NODE)
                                            .removeValue()
//                                    }.addOnFailureListener {
//                                        Log.d("!!!addOnFailureListener4", "${it.message}")
//                                        callback(ADD_TO_DB_FAILURE + " " + "${it.message}")
//                                    }
                            }
                            .addOnFailureListener {
                                Log.d("!!!addOnFailureListener3", "${it.message}")
                                callback(ADD_TO_DB_FAILURE + " " + "${it.message}")
                            }
                    }
                    .addOnFailureListener {
                        Log.d("!!!addOnFailureListener2", "${it.message}")
                        callback(ADD_TO_DB_FAILURE + " " + "${it.message}")
                    }
            }.addOnFailureListener {
                Log.d("!!!addOnFailureListener1", "${it.message}")
                callback(ADD_TO_DB_FAILURE + " " + "${it.message}")
            }
    }

    fun blockAnn(recFromBur: RequestFromBureau, myUid: String, mess: String, callback: (isBlock: Boolean) -> Unit) {
        val childFirst = "${recFromBur.email?.substringBefore('.')}_${recFromBur.key}"
        childFirst.let { key ->
            ref.child(key).child(BLOCK_ANN_NODE).child(myUid).setValue(mess)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                        callback(true)
                    else
                        callback(false)
                }.addOnFailureListener {
                    callback(false)
                }
        }
    }

    fun deleteApplication(recFromBur: RequestFromBureau, deleted: () -> Unit, error: (mess: String?) -> Unit) {
        val childFirst = "${recFromBur.email?.substringBefore('.')}_${recFromBur.key}"
        ref.child(childFirst)
            .child(recFromBur.uid!!)
            .removeValue()
            .addOnSuccessListener {
                ref.child(childFirst)
                    .child(FILTER_NODE)
                    .removeValue()
                    .addOnSuccessListener {
                        ref.child(childFirst)
                            .child(BLOCK_ANN_NODE)
                            .removeValue()
                            .addOnSuccessListener {
                                refForMap.child(childFirst)
                                    .removeValue()
                                    .addOnSuccessListener {
                                        refForInfo.child(recFromBur.key).removeValue()
                                    }.addOnFailureListener { error(it.message) }
                            }
                            .addOnFailureListener { error(it.message) }
                    }
                    .addOnFailureListener { error(it.message) }
            }
            .addOnFailureListener { error(it.message) }
    }

    fun removeListenersMap() {
        valueEventListener?.let { ref.removeEventListener(it) }

    }

    fun removeChildEventListenerShowApplication() {
        childEventListenerSAF?.let { ref.removeEventListener(it) }
        childInfoEventListenerSAF?.let { refForInfo.removeEventListener(it) }
    }




    companion object {
        const val MAIN_REQUESTS = "main_requests"
        const val MARKERS_REQUEST = "marker_request"
        const val VIEW_COUNT_REQUEST = "views_count_request"
        const val APPLICATION = "application"
        const val FILTER_NODE = "filter"
        const val BLOCK_ANN_NODE = "blocking"
        const val INFO_NODE = "info"

        const val ADD_TO_DB_SUCCESS = "ADD_TO_DB_SUCCESS"
        const val ADD_TO_DB_FAILURE = "ADD_TO_DB_FAILURE"
    }
}
package com.dzenis_ska.lostandfound.ui.fragments.showApplication

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.dzenis_ska.lostandfound.R
import com.dzenis_ska.lostandfound.ui.db.firebase.FBAuth
import com.dzenis_ska.lostandfound.ui.db.firebase.FBDatabase
import com.dzenis_ska.lostandfound.ui.db.firebase.FBStorage
import com.dzenis_ska.lostandfound.ui.db.firebase.classes.RequestForInfo
import com.dzenis_ska.lostandfound.ui.db.firebase.classes.RequestFromBureau
import com.dzenis_ska.lostandfound.ui.db.room.MainDataBase
import com.dzenis_ska.lostandfound.ui.db.room.marker.entities.RequestFromBureauEntity
import com.dzenis_ska.lostandfound.ui.fragments.BaseViewModel
import com.dzenis_ska.lostandfound.ui.fragments.LiveEvent
import com.dzenis_ska.lostandfound.ui.fragments.MutableLiveEvent
import com.dzenis_ska.lostandfound.ui.fragments.dialogWarning
import com.dzenis_ska.lostandfound.ui.utils.Event
import kotlinx.coroutines.*

class ShowApplicationViewModel(
    private val fbAuth: FBAuth,
    private val fbDatabase: FBDatabase,
    private val fbStorage: FBStorage,
    private val roomDatabase: MainDataBase,
    private val id: String,
) : BaseViewModel() {

    private val dao = roomDatabase.getDao()

    private val _uploadState = MutableLiveData<Boolean>(false)
    val uploadState: LiveData<Boolean> = _uploadState

    private val _requestForInfo = MutableLiveData<RequestForInfo>()
    val requestForInfo: LiveData<RequestForInfo> = _requestForInfo

    private val _requestFromBureauAndMyUid = MutableLiveData<RequestFromBureauAndMyUid>()
    val requestFromBureauAndMyUid: LiveData<RequestFromBureauAndMyUid> = _requestFromBureauAndMyUid

    private val _listPreloadPhotoAdapter = MutableLiveEvent<ShowPhotoPreload>()
    val listPreloadPhotoAdapter: LiveEvent<ShowPhotoPreload> = _listPreloadPhotoAdapter

    private val _popBackStackToMapFragment = MutableLiveEvent<Unit>()
    val popBackStackToMapFragment: LiveEvent<Unit> = _popBackStackToMapFragment

    private val _popBackStack = MutableLiveEvent<Unit>()
    val popBackStack: LiveEvent<Unit> = _popBackStack

    private val _onBlocked = MutableLiveEvent<Boolean>()
    val onBlocked: LiveEvent<Boolean> = _onBlocked

    init {
        initDao()
        requestOnExternalDB()
        requestForInfo()
    }

    fun goToChatFragment() {
        TODO("Not yet implemented")
    }

    private fun requestForInfo(){
        viewModelScope.launch {
            fbDatabase.getInfoFlow(key = id) {
                _requestForInfo.value = it
            }
        }
    }

    private fun requestOnExternalDB() {
        viewModelScope.launch {
            fbDatabase.getOneApplicationFlow(
                markerTag = id,
                startUpload = {
                    _uploadState.value = true
                },
                eventResultChanged = { requestFromBureau ->
                    updateRequestToRoom(requestFromBureau)
                },
                eventResultRemoved = {
                    removeRequestFromRoom(it)
                },
                errorMessage = {
                    toastEventBase.value = Event(it)
                }
            )
        }
    }

    private fun updateRequestToRoom(requestFromBureau: RequestFromBureau) = viewModelScope.launch {
        dao.updateRequestFromBureauEntity(RequestFromBureauEntity.fromRequestFromBureau(requestFromBureau))
    }

    private fun removeRequestFromRoom(requestToBureauId: String) = viewModelScope.launch {
        dao.deleteMarkerFromRequestFromBureau(requestToBureauId)
        dao.deleteMarker(requestToBureauId)
    }

    private fun initDao() {
        Log.d("!!!getOneRequestFromBureauFlow1", "${id}")
        val currentUserUid = fbAuth.uid() ?: return
        viewModelScope.launch {
            dao.getOneRequestFromBureauFlow(id).collect { requestFromBureauEntity ->
                Log.d("!!!getOneRequestFromBureauFlow11", "${requestFromBureauEntity}")
                if (requestFromBureauEntity != null) {
                    Log.d("!!!requestFromBureauEntityisNotBlank", "${requestFromBureauEntity.messBlocked.isNotBlank()} _ ${currentUserUid != requestFromBureauEntity.uid}")

                    if (requestFromBureauEntity.messBlocked.isNotBlank()) {
                        if (currentUserUid != requestFromBureauEntity.uid) {
                            _onBlocked.value = Event(false)
                            _popBackStackToMapFragment.value = Event(Unit)
                            dialogWarning(R.string.this_application_was_blocked)

                        } else {
                            dialogWarning(R.string.blocked_my_add, requestFromBureauEntity.messBlocked)
                        }
                        Log.d("!!!getOneRequestFromBureauFlow2232", "${requestFromBureauEntity.toRequestFromBureau()}")
                    } else {

                        _onBlocked.value = Event(false)
                        Log.d("!!!_onBlocked", "${_onBlocked.value}")
                    }
                    _requestFromBureauAndMyUid.value = RequestFromBureauAndMyUid(requestFromBureauEntity.toRequestFromBureau(), currentUserUid)
                    Log.d("!!!getOneRequestFromBureauFlow2", "${requestFromBureauEntity}")
                } else {
                    Log.d("!!!getOneRequestFromBureauFlow21231", "${requestFromBureauEntity}")
                    dialogWarning(R.string.this_application_was_deleted, "")
                    _requestFromBureauAndMyUid.value = RequestFromBureauAndMyUid(null, currentUserUid)
                    _popBackStackToMapFragment.value = Event(Unit)
                }
                delay(500)
                _uploadState.value = false
            }
        }
    }

    fun showPreloadPhoto() {
        Log.d("!!!showPreloadPhoto1", "${requestFromBureauAndMyUid.value?.requestFromBureau?.uriMarkerPhoto}")
        whenContextActive { context ->
            Log.d("!!!showPreloadPhoto2", "${requestFromBureauAndMyUid.value?.requestFromBureau?.uriMarkerPhoto}")
            CoroutineScope(Dispatchers.IO).launch {
                Log.d("!!!listLoadPhotoAdapter", "${requestFromBureauAndMyUid.value?.requestFromBureau?.uriMarkerPhoto}")
                    val bitmap: Bitmap? = try {
                        Glide.with(context)
                            .asBitmap()
                            .load(requestFromBureauAndMyUid.value?.requestFromBureau?.uriMarkerPhoto)
                            .transform()
                            .submit(7, 7)
                            .get()
                    } catch (e: Exception) {
                        Log.d("!!!listLoadPhotoAdapter", "${e.message} _ ")
                        null
                    }
                withContext(Dispatchers.Main) {
                    _listPreloadPhotoAdapter.value = Event(ShowPhotoPreload(
                        bitmap = bitmap,
                        listUri = requestFromBureauAndMyUid.value?.requestFromBureau?.photoUri?.map {
                            it.toUri()
                        } ?: emptyList()
                    ))
                }
            }
        }
    }

    fun blockAppStepOne(recFromBur: RequestFromBureau, mess: String) {
        fbAuth.uid() ?: return
        viewModelScope.launch {
            fbDatabase.getOneApplication(
                recFromBur.key,
                eventResult = { recToBur, _ ->
                    if (recToBur != null) blockAppStepTwo(recToBur, mess)
                    else _onBlocked.value = Event(false)
                },
                errorMessage = {}
            )
        }
    }
    private fun blockAppStepTwo(recFromBur: RequestFromBureau, mess: String) {
        viewModelScope.launch {
            Log.d("!!!blockAppStepTwo", "${fbAuth.uid()}")
            val myUid = fbAuth.uid() ?: return@launch
            fbDatabase.blockAnn(recFromBur, myUid, mess) { isBlock ->
                Log.d("!!!btn1", "${isBlock}")
                _onBlocked.value = Event(isBlock)
            }
        }
    }

    fun deleteApplication(requestFromBureau: RequestFromBureau) {
        viewModelScope.launch {
            deletePhotoFromStorage{
                Log.d("!!!deletePhotoWithoutListenerSAF10", "deletedApp")
                fbDatabase.deleteApplication(
                    recFromBur = requestFromBureau,
                    deleted = {
                        Log.d("!!!deletePhotoWithoutListenerSAF0", "deletedApp")
//                        _popBackStack.value = Event(Unit)
                    },
                    error = {
                        Log.d("ExceptionDeleteApplication", "${it}")
                        toastEventBase.value = Event(it ?: "ExceptionDeleteApplication")
                    }
                )
            }

        }
    }

    private fun deletePhotoFromStorage(deleted:()->Unit) {
        val request = requestFromBureauAndMyUid.value?.requestFromBureau ?: return
        Log.d("!!!deletePhotoWithoutListenerSAF1", "${request}")
        viewModelScope.launch {
            val listNewPhotoUri =
                arrayListOf(request.uriMarkerPhoto)
            listNewPhotoUri.addAll(request.photoUri)
            Log.d("!!!deletePhotoWithoutListenerSAF2", "${listNewPhotoUri}")
            listNewPhotoUri.filterNotNull().onEach {
                Log.d("!!!deletePhotoWithoutListenerSAF3", "${it}")
                fbStorage.deletePhotoWithoutListener(it)
            }
            deleted()
        }
    }

    override fun onCleared() {
        Log.d("!!!onCleared", "AAViewModel")
        fbDatabase.removeChildEventListenerShowApplication()
        super.onCleared()
    }


}

class ShowPhotoPreload(
    val bitmap: Bitmap? = null,
    val listUri: List<Uri> = emptyList()
)

class RequestFromBureauAndMyUid(
    val requestFromBureau: RequestFromBureau?,
    val uid: String
)




package com.dzenis_ska.lostandfound.ui.fragments.add_application

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dzenis_ska.lostandfound.R
import com.dzenis_ska.lostandfound.ui.db.firebase.FBAuth
import com.dzenis_ska.lostandfound.ui.db.firebase.FBDatabase
import com.dzenis_ska.lostandfound.ui.db.firebase.FBStorage
import com.dzenis_ska.lostandfound.ui.db.firebase.classes.RequestToBureau
import com.dzenis_ska.lostandfound.ui.db.room.MainDataBase
import com.dzenis_ska.lostandfound.ui.fragments.BaseViewModel
import com.dzenis_ska.lostandfound.ui.fragments.LiveEvent
import com.dzenis_ska.lostandfound.ui.fragments.MutableLiveEvent
import com.dzenis_ska.lostandfound.ui.utils.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AddApplicationViewModel(
    private val fbAuth: FBAuth,
    private val fbDatabase: FBDatabase,
    private val fbStorage: FBStorage,
    private val roomDatabase: MainDataBase
) : BaseViewModel() {

    private var requestToBureau: RequestToBureau? = null
    private var isEdit: Boolean = false
    private var listPhotoUriToDelete: ArrayList<String?> = arrayListOf()


    private val _viewModelStateAA = MutableLiveData<AAViewModelState>(AAViewModelState())
    val viewModelStateAA: LiveData<AAViewModelState> = _viewModelStateAA

    private val _getBiteArrayEvent = MutableLiveEvent<Unit>()
    val getBiteArrayEvent: LiveEvent<Unit> = _getBiteArrayEvent

    //Toast
//    private val _toastEvent = MutableLiveData<Event<String>>()
//    val toastEvent: LiveData<Event<String>> = _toastEvent

    fun getCurrentUserEmail() = fbAuth.currentUser?.email ?: "#+++..."

    fun launchPreparePhotoToPublish(
        ldd: LoadDialogDisplayClass? = null,
    ) {
        viewModelScope.launch {
            _viewModelStateAA.value = getVMState()?.copyVMState(
                isShowLoadApplicationDialog = true,
                displayLoadApplicationDialog = ldd
            )
            _getBiteArrayEvent.value = Event(Unit)
        }
    }

    fun publishPhotos(
        content: InstanceStateAddApplication,
        argsAAF: AddApplicationFragmentArguments
    ) {
        isEdit = argsAAF.isEditApplication
        //fill list to preDelete photo
        listPhotoUriToDelete.clear()
        listPhotoUriToDelete.add(argsAAF.uriMarkerPhoto)
        listPhotoUriToDelete.addAll(argsAAF.photoUri.map { it })

        requestToBureau = content.toRequestToBureau()
        //TODO we have list saved uri and listByteArray
        viewModelScope.launch {
            if (getVMState()!!.listByteArray.size == 0 && getVMState()!!.listPhotoUri.size == 0) {
                loadDialogDismiss()
            } else {
                Log.d("!!!publishPhotos", "${fbAuth.currentUser?.isEmailVerified}")
                if (fbAuth.currentUser?.isEmailVerified == true && fbAuth.currentUser?.email != null)
                    if (getVMState()!!.listByteArray.size == 0) prepareToPublishApplication()
                    else
                        publishOnePhoto(fbAuth.currentUser?.email!!)
                else
                    loadDialogDismiss(R.string.no_authorised_user)
            }
        }
    }

    fun publishOnePhoto(email: String) {
        fbStorage.addPhotoToStorage(
            byteArray = getVMState()!!.listByteArray[getVMState()!!.loadPhotoIndex!!],
            currUserEmail = email,
            statusUpload = {
                _viewModelStateAA.value = getVMState()?.copyVMState(
                    isShowLoadApplicationDialog = true,
                    displayLoadApplicationDialog = if (getVMState()!!.loadPhotoIndex!! != 0) LoadDialogDisplayClass(
                        R.string.images_uploading, getVMState()!!.loadPhotoIndex!!.toString(),"$it" )
                    else
                        LoadDialogDisplayClass(R.string.main_image_uploading, "\"Ў\"", percentage = "$it" )
                )
            },
            callbackUri = {
                Log.d("!!!index1", "${getVMState()?.listPhotoUri}")

                val index = getVMState()!!.listPhotoUri.indexOfFirst { uri-> uri == null }
                Log.d("!!!index2", "${index}")

                addPhotoUriToListWithIndex(index, it)
//                getVMState()?.copyVMState(
//                    photoUri = it
//                )
                val loadPhotoIncrementIndex = getVMState()!!.loadPhotoIndex!!.plus(1)
                if (getVMState()!!.listByteArray.size == loadPhotoIncrementIndex) {
                    prepareToPublishApplication()
                } else {
                    getVMState()!!.copyVMState(loadPhotoIndex = loadPhotoIncrementIndex)
                    publishOnePhoto(email)
                }
            },
            exception = {
                toastEventBase.value = Event(it.toString())
                loadDialogDismiss()
            }
        )
    }

//    fun addPhotoUriToList(uri: Uri?) {
//        getVMState()?.copyVMState(
//            photoUri = uri
//        )
//    }
    fun addPhotoUriToListWithIndex(position: Int, uri: Uri?) {
        getVMState()?.copyVMState(
            photoUriIndex = PhotoUriIndex(position,uri)
        )
    }

    fun prepareToPublishApplication(){
        val email = fbAuth.currentUser?.email ?: return
        val subStrEmail = email.substringBefore('.')
        val uid = fbAuth.auth.uid ?: return
        val key = requestToBureau?.key ?: fbDatabase.pushKey() ?: return
        val childFirst = "${subStrEmail}_$key"
        Log.d("!!!prepareToPublishApplication", "${getVMState()?.listPhotoUri?.size}")

        requestToBureau = requestToBureau?.copy(
            key = key,
            uid = uid,
            email = email,
            uriMarkerPhoto = getVMState()?.listPhotoUri?.get(0).toString(),
            photoUri = getVMState()?.listPhotoUri!!.filter {
                getVMState()?.listPhotoUri!!.indexOf(it) != 0
            }.filterNotNull().map { uri->
                    uri.toString()
            },
            time = System.currentTimeMillis().toString(),
        )
        viewModelScope.launch {
            fbDatabase.addToDatabase(requestToBureau!!, childFirst) { resultMessage ->
                if (resultMessage == FBDatabase.ADD_TO_DB_SUCCESS) {
                    createListToDeletePhotoFromStorage()
                    if(isEdit)
                        popBackToMapFragment()
                    else
                        popBackStack()
                }
                else  {
                    toastEventBase.value = Event(FBDatabase.ADD_TO_DB_FAILURE)
                    loadDialogDismiss()
                }
            }
        }
    }

    private fun createListToDeletePhotoFromStorage() {
        Log.d("!!!deletePhotoWithoutListener1", "${listPhotoUriToDelete}")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val listNewPhotoUri =
                    arrayListOf(requestToBureau!!.uriMarkerPhoto)
                listNewPhotoUri.addAll(requestToBureau!!.photoUri)
                Log.d("!!!deletePhotoWithoutListener2", "${listNewPhotoUri}")

                listPhotoUriToDelete.filterNotNull().filter {
                    !listNewPhotoUri.contains(it)
                }.onEach { uri -> fbStorage.deletePhotoWithoutListener(uri) }
                listPhotoUriToDelete.clear()
            }
        }
    }

    fun loadDialogDismiss(cause: Int? = null) {
        Log.d("!!!loadDialogDismiss", "${AAViewModelState.DISMISS.toString()}")
        getVMState()?.copyVMState(clearByteArray = true)
        viewModelScope.launch {
            delay(1500)
            _viewModelStateAA.value = AAViewModelState(false, LoadDialogDisplayClass(cause,
                AAViewModelState.DIALOG_DISMISSED
            ))
        }
    }

    fun putByteArrayToList(byteArray: ByteArray?){
        getVMState()?.copyVMState(
            byteArray = byteArray
        )
    }

    //Navigator
    private val _goToChangeLocationFragment = MutableLiveEvent<Unit>()
    val goToChangeLocationFragment: LiveEvent<Unit> = _goToChangeLocationFragment
    private val _goToCameraFragment = MutableLiveEvent<Unit>()
    val goToCameraFragment: LiveEvent<Unit> = _goToCameraFragment
    private val _popBackStack = MutableLiveEvent<Unit>()
    val popBackStack: LiveEvent<Unit> = _popBackStack
    private val _popBackToMap = MutableLiveEvent<Unit>()
    val popBackToMap: LiveEvent<Unit> = _popBackToMap

    fun goToChangeLocationFragment() {
        if (state()?.isShowLoadApplicationDialog == true) return
        _goToChangeLocationFragment.value = Event(Unit)
    }

    fun goToCameraFragment() {
        if (state()?.isShowLoadApplicationDialog == true) return
        _goToCameraFragment.value = Event(Unit)
    }

    fun popBackToMapFragment() {
        _popBackToMap.value = Event(Unit)
    }

    fun popBackStack() {
        _popBackStack.value = Event(Unit)
    }

    //----
    private fun state() = _viewModelStateAA.value

    override fun onCleared() {
        Log.d("!!!onCleared", "AAViewModel")
        _viewModelStateAA.value = AAViewModelState.DISMISS
        super.onCleared()
    }
}

class LoadDialogDisplayClass(
    var mess: Int? = null,
    var numPhoto: String? = null,
    var percentage: String? = null,
)

class AAViewModelState(
    var isShowLoadApplicationDialog: Boolean? = null,
    var displayLoadApplicationDialog: LoadDialogDisplayClass? = null,
    var loadPhotoIndex: Int? = 0,
    var listPhotoUri: ArrayList<Uri?> = arrayListOf(null,null,null,null),
    val listByteArray: ArrayList<ByteArray> = arrayListOf(),
    ) {
    companion object {
        const val DIALOG_DISMISSED = "DIALOG_DISMISSED"
        const val DO_NOT_LOADING_PHOTOS = "DO_NOT_LOADING_PHOTOS"
//        const val DIALOG_DISMISSED = "DIALOG_DISMISSED"
        val DEFAULT = AAViewModelState()
        val DISMISS = AAViewModelState(false, LoadDialogDisplayClass(null, DIALOG_DISMISSED))
    }
}

class PhotoUriIndex(val index: Int, val uri: Uri?)

fun AAViewModelState.copyVMState(
    isShowLoadApplicationDialog: Boolean? = null,
    displayLoadApplicationDialog: LoadDialogDisplayClass? = null,
    loadPhotoIndex: Int? = null,
//    photoUri: Uri? = null,
    photoUriIndex: PhotoUriIndex? = null,
    byteArray: ByteArray? = null,
    clearByteArray: Boolean? = null,
): AAViewModelState {
    isShowLoadApplicationDialog?.let {
        this.isShowLoadApplicationDialog = it
        if (!it) {
            this.displayLoadApplicationDialog = null
        }
    }
    displayLoadApplicationDialog?.let { this.displayLoadApplicationDialog = it }
    loadPhotoIndex?.let { this.loadPhotoIndex = it }
//    photoUri?.let { this.listPhotoUri.add(it) }
    photoUriIndex?.let {
        this.listPhotoUri.removeAt(it.index)
        this.listPhotoUri.add(it.index, it.uri)
    }
    byteArray?.let {
        Log.d("!!!puByteArrayToList2", "${byteArray}")
        this.listByteArray.add(it)
        Log.d("!!!puByteArrayToList3", "${byteArray}")
    }
    clearByteArray?.let { this.listByteArray.clear() }

    return this
}

fun AddApplicationViewModel.getVMState() = this.viewModelStateAA.value


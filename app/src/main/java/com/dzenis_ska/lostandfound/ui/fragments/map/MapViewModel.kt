package com.dzenis_ska.lostandfound.ui.fragments.map

import android.content.Context
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dzenis_ska.lostandfound.R
import com.dzenis_ska.lostandfound.ui.db.firebase.*
import com.dzenis_ska.lostandfound.ui.db.firebase.classes.RequestForMarkerMap
import com.dzenis_ska.lostandfound.ui.db.firebase.classes.RequestToBureau
import com.dzenis_ska.lostandfound.ui.db.room.MainDataBase
import com.dzenis_ska.lostandfound.ui.db.room.marker.entities.MarkerEntity
import com.dzenis_ska.lostandfound.ui.db.room.marker.entities.RequestFromBureauEntity
import com.dzenis_ska.lostandfound.ui.fragments.*
import com.dzenis_ska.lostandfound.ui.fragments.add_application.Category
import com.dzenis_ska.lostandfound.ui.utils.Event
import com.dzenis_ska.lostandfound.ui.utils.map.CustomMarker
import com.dzenis_ska.lostandfound.ui.utils.networkState.NetworkRequest
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class MapViewModel(
    private val network: NetworkRequest,
    private val fbAuth: FBAuth,
    private val fbDatabase: FBDatabase,
    private val fbStorage: FBStorage,
    private val roomDatabase: MainDataBase
) : BaseViewModel() {

    private var jobSelectAllMarkers: Job? = null
    private var jobSelectCategoryMarkers: Job? = null

    var isInternetNoBad: Boolean = true
    var lastMarkerClickTag = ""

    private var incrementZIndex = 1

    private val dao = roomDatabase.getDao()



    private val _viewModelState = MutableLiveData<ViewModelState>(ViewModelState())
    val viewModelState: LiveData<ViewModelState> = _viewModelState

    private val _currentUserVM = MutableLiveData<FirebaseUser?>()
    val currentUserVM: LiveData<FirebaseUser?> = _currentUserVM


    private val _currentUserEventVM = MutableLiveEvent<FirebaseUser?>()
    val currentUserEventVM: LiveEvent<FirebaseUser?> = _currentUserEventVM

    //navigator
    private val _goToShowApplicationFragment = MutableLiveEvent<String>()
    val goToShowApplicationFragment: LiveEvent<String> = _goToShowApplicationFragment

    private val _goToAddApplicationFragment = MutableLiveEvent<Unit>()
    val goToAddApplicationFragment: LiveEvent<Unit> = _goToAddApplicationFragment

    private val _updateApplications = MutableLiveEvent<ArrayList<RequestToBureau>>()
    val updateApplications: LiveEvent<ArrayList<RequestToBureau>> = _updateApplications

    //Load Markers State
    val loadMarkersState = MutableLiveData<List<MarkerContent>>(listOf())

    private val _loading = MutableLiveData<Event<Loading>>()
    val loading: LiveData<Event<Loading>> = _loading

    private val _loadMarkersOptionState = MutableLiveData<ManagerListMarkersOption>()
    val loadMarkersOptionState: LiveData<ManagerListMarkersOption> = _loadMarkersOptionState

    private val _showPreloadAdapter = MutableLiveData<List<PreviewAdapterClass>>()
    val showPreloadAdapter: LiveData<List<PreviewAdapterClass>> = _showPreloadAdapter

    private val _moveSelectedMarkerToCenter = MutableLiveEvent<LatLng>()
    val moveSelectedMarkerToCenter: LiveEvent<LatLng> = _moveSelectedMarkerToCenter

    init {
        selectAllSimpleMarkersFromDao()
        initDao()
        requestOnExternalDB()
    }

    private fun collectListMarkerContentWithoutLoadingBar(key: String) = viewModelScope.launch{
        val listLoadMarkersOptions = loadMarkersOptionState.value?.markerOptionList?.map {

            if (it.title == key)
                MarkerOptions()
                    .position(LatLng(it.position.latitude, it.position.longitude))
                    .icon(it.icon)
                    .title(it.title)
                    .snippet(it.snippet)
                    .zIndex(incrementZIndex++.toFloat())
            else
                it
        } ?: return@launch
        Log.d("!!!collectListMarkerContentWithoutLoadingBar", "${listLoadMarkersOptions[0].zIndex} _ ")

        _loadMarkersOptionState.value = ManagerListMarkersOption(
                markerOptionList = listLoadMarkersOptions,
                manage = ManagerListMarkersOption.ADD_ALL
            )

    }

    fun findSelectedMarker(key: String) {
        val listMarkerContent = loadMarkersState.value ?: return
        val markerContent = listMarkerContent.firstOrNull { it.key == key }
        markerContent?.let {
            collectListMarkerContentWithoutLoadingBar(key)
            _moveSelectedMarkerToCenter.value = Event(LatLng(it.latitude, it.longitude))
        }
    }

    fun defineLatLngBounds(latLngBounds: LatLngBounds?) {
        latLngBounds ?: return
        val listMarkerContent = loadMarkersState.value ?: return
        val listForAdapter = listMarkerContent.filterNotNull()
            .sortedBy { it.timeOfCreation.toLong() }
            .reversed()
            .filter { latLngBounds.contains(LatLng(it.latitude, it.longitude)) }
            .map { PreviewAdapterClass.fromMarkerContent(it) }

        if (listForAdapter.isNotEmpty())
            _showPreloadAdapter.value = listForAdapter
        else
            _showPreloadAdapter.value = emptyList<PreviewAdapterClass>()
    }

    private suspend fun ifNotLoading() = withContext(Dispatchers.IO){
        delay(TIME_AWAIT_LOADING_7700)
        withContext(Dispatchers.Main) {
            if (network.isOnline()) toast(R.string.something_went_wrong)
            else dialog(R.string.no_internet, R.string.no_internet_please_wait)
            Log.d("!!!onMarkerClick10", "${network.isOnline()}")
            _viewModelState.value = viewModelState(isApplicationLoading = false)
//            triggerInternet(LOAD_APPLICATION)
        }
        lastMarkerClickTag = ""
    }

    private suspend fun getOneRequestFromBureauEntityFlow() {
        val currentUserUid = fbAuth.uid()
        viewModelScope.launch {
            dao.getOneRequestFromBureauFlow(lastMarkerClickTag).collect { requestFromBureauEntity ->
                Log.d("!!!requestFromBureauEntity", "${requestFromBureauEntity}")
                _viewModelState.value = viewModelState(isApplicationLoading = true)
                Log.d("!!!setOnMarkerClickListener2", "${_viewModelState.value}")

                delay(1000)
                if (requestFromBureauEntity != null) {
                    if (requestFromBureauEntity.messBlocked.isNotBlank() || requestFromBureauEntity.messBlocked.isNotEmpty()) {
                        if(currentUserUid != requestFromBureauEntity.uid)
                            dialogWarning(R.string.this_application_was_blocked)
                        else
                            _goToShowApplicationFragment.value = Event(requestFromBureauEntity.key)
                    } else {
                        _goToShowApplicationFragment.value = Event(requestFromBureauEntity.key)
                    }

                    delay(1000)
                    _viewModelState.value = viewModelState(isApplicationLoading = false)
                    lastMarkerClickTag = ""
                    this.coroutineContext.job.cancel()
                } else {
//                    delay(15000)
//                    Log.d("!!!onMarkerClickStart1else", "${ lastMarkerClickTag.isNotBlank()}" )
//
//                    _viewModelState.value = getVMState().copyVMState(isApplicationLoading = false)
                    //TODO clear map last marker
                }
            }
        }
    }

    fun onMarkerClick(markerTag: String) {
        Log.d("!!!onMarkerClickStart1", "${ lastMarkerClickTag.isNotBlank()}" )
        if (fbAuth.uid() == null) {
            _viewModelState.value = viewModelState(isApplicationLoading = false)
            return
        }
        if (lastMarkerClickTag.isNotBlank()) {
            _viewModelState.value = viewModelState(isApplicationLoading = false)
            return
        }
        lastMarkerClickTag = markerTag
        Log.d("!!!onMarkerClickStart2", lastMarkerClickTag)
        viewModelScope.launch {
            //todo put getOneApplicationExternal() to getOneRequestFromBureauEntityFlow()
            getOneRequestFromBureauEntityFlow()
            getOneApplicationExternal()
        }
    }

    private suspend fun getOneApplicationExternal() = withContext(Dispatchers.Main){
        Log.d("!!!onMarkerClick1012", "${goToShowApplicationFragment.value} _ ${lastMarkerClickTag}")
        val currentUserUid = fbAuth.uid() ?: return@withContext
        val awaitToLoading = async { ifNotLoading() }
        fbDatabase.getOneApplication(
            markerTag = lastMarkerClickTag,
            eventResult = { _requestFromBureau, _markerTag ->
                awaitToLoading.cancel()
                Log.d("!!!onMarkerClick0", "${lastMarkerClickTag} _ ${_requestFromBureau} _ ${awaitToLoading.isCompleted}")
                Log.d("!!!_requestFromBureau", " _ ${_requestFromBureau} _ ")
                if (awaitToLoading.isCompleted) return@getOneApplication
                if (_markerTag == lastMarkerClickTag) {
                    if (_requestFromBureau == null) {
                        deleteMarkerEntity(_markerTag)
                        Log.d("!!!onMarkerClick2ann_was_deleted", "$_requestFromBureau")
                    } else {
                        Log.d("!!!onMarkerClick4444", _requestFromBureau.messBlocked)
                        addRequestFromBureauEntity(RequestFromBureauEntity.fromRequestFromBureau(_requestFromBureau))
                    }
                }
            },
            errorMessage = { errorMessage ->
                Log.d("!!!onMarkerClick", "error: $errorMessage")
                _viewModelState.value = viewModelState(isApplicationLoading = false)
                lastMarkerClickTag = ""
            }
        )
    }

    private fun addRequestFromBureauEntity(fromRequestToBureau: RequestFromBureauEntity) {
        viewModelScope.launch { dao.insertRequestFromBureauEntity(fromRequestToBureau) }
    }

    private fun deleteMarkerEntity(key: String) {
        viewModelScope.launch {
            Log.d("!!!deleteMarkerEntity", "$key")
            dao.deleteMarker(key)
            dao.deleteMarkerFromRequestFromBureau(key)
        }
    }

    fun initDao(category: Category = Category.ALL) {
        when (category) {
            Category.ALL -> {
                if (jobSelectAllMarkers == null) {
                    clearJobSelectCategoryMarkers()
                    jobSelectAllMarkers = viewModelScope.launch {
                        selectAllMarkersFromDao()
                    }
                }
            }
            else -> {
                clearJobSelectAllMarkers()
                jobSelectCategoryMarkers = viewModelScope.launch {
                    selectCategoryMarkersFromDao(category)
                }
            }
        }
    }

    private fun clearJobSelectCategoryMarkers() {
        jobSelectCategoryMarkers?.cancel()
        jobSelectCategoryMarkers = null
    }

    private fun clearJobSelectAllMarkers() {
        jobSelectAllMarkers?.cancel()
        jobSelectAllMarkers = null
    }

    suspend fun selectCategoryMarkersFromDao(category: Category){
        dao.selectAllMarkers(category.toString())
            .map { list -> list.map { it.toMarkerContent() } }
            .flowOn(Dispatchers.IO)
            .collect { listMarkerContent ->
                Log.d("!!!initDao1111", "${listMarkerContent.size}")
                loadMarkersState.value = listMarkerContent
                Log.d("!!!initDao11111", "${getVMMarkersContent()!!.size}")
                collectListMarkerContent()
                Log.d("!!!initDao22222", "${getVMMarkersContent()!!.size}")
                if (!isInternetNoBad) triggerInternet(COLLECT_MARKERS_MAP)
            }
    }
    private fun selectAllSimpleMarkersFromDao() {
        Log.d("!!!selectAllSimpleMarkersFromDao", "${getVMMarkersContent()!!.size}")
        viewModelScope.launch {
            dao.selectAllMarkers()
                .map { list -> list.map { it.toMarkerContent() } }
                .flowOn(Dispatchers.IO)
                .collect { listMarkerContent ->
                    val listMarkerOptions = arrayListOf<MarkerOptions>()
                    listMarkerContent.forEach { markerContent ->
                        listMarkerOptions.add(getSimpleMarkerOptions(markerContent))
                    }
                    _loadMarkersOptionState.postValue(
                        ManagerListMarkersOption(
                            markerOptionList = listMarkerOptions,
                            manage = ManagerListMarkersOption.ADD_ALL
                        )
                    )
                }
        }
    }

    suspend fun selectAllMarkersFromDao(){
        dao.selectAllMarkers()
            .map { list -> list.map { it.toMarkerContent() } }
            .flowOn(Dispatchers.IO)
            .collect { listMarkerContent ->
                Log.d("!!!initDao1111", "${listMarkerContent.size}")
                loadMarkersState.value = listMarkerContent
                Log.d("!!!initDao11111", "${getVMMarkersContent()!!.size}")
                collectListMarkerContent()
                Log.d("!!!initDao22222", "${getVMMarkersContent()!!.size}")
                if (!isInternetNoBad) triggerInternet(COLLECT_MARKERS_MAP)
            }
    }

    private suspend fun collectListMarkerContent() = withContext(Dispatchers.IO){
        val listMarkerContent = getVMMarkersContent()!!
        val fullListSize = listMarkerContent.size
        val percent = 1.0f/fullListSize.toFloat()
        var percentage = 0.0f

            Log.d("!!!initDao111", "${listMarkerContent.size}")
            val listMarkerOptions = arrayListOf<MarkerOptions>()
//            val listMarkerContent = arrayListOf<MarkerContent>()
            listMarkerContent.forEach { markerContent ->
//                val percent = (100/percentage).toFloat() * 0.01f
                delay(20)
                Log.d("!!!loading", "${percent} _ ${percentage}")
                loading(isVisible = true, percentage = percentage)
                percentage+=percent
                whenContextActive { context ->
                    Log.d("!!!whenContextActive", "${context} ")
                    listMarkerOptions.add(getMarkerOptions(context, markerContent))
                }
            }
            Log.d("!!!initDao", "${Thread.currentThread().name}")
            Log.d("!!!initDao", "${listMarkerContent.size}")
        //todo postValue or value =
            _loadMarkersOptionState.postValue(
                ManagerListMarkersOption(
                    markerOptionList = listMarkerOptions,
                    manage = ManagerListMarkersOption.ADD_ALL
                )
            )
            loading()
            Log.d("!!!network1", "${isInternetNoBad}")
//        }
    }

    private fun triggerInternet(loadingProcess: String) {
        if (network.connectivityManager() != null) return
        network.networkCallback {
            isInternetNoBad = true
            unregisterNetworkCallback()
            viewModelScope.launch {
                when (loadingProcess) {
                    LOAD_APPLICATION -> {}
                    COLLECT_MARKERS_MAP -> collectListMarkerContent()
                }
                Log.d("!!!networkCallback333", "${network.isOnline()}")
            }
        }
    }

    private fun getSimpleMarkerOptions(markerContent: MarkerContent): MarkerOptions {
        return MarkerOptions()
            .position(LatLng(markerContent.latitude, markerContent.longitude))
            .icon(
                BitmapDescriptorFactory.defaultMarker(
                    when (markerContent.category) {
                        Category.LOST.toString() -> BitmapDescriptorFactory.HUE_AZURE
                        else -> BitmapDescriptorFactory.HUE_ORANGE
                    }
                )
            )
            .title(markerContent.key)
            .snippet(markerContent.category)
    }

    private  fun getMarkerOptions(context: Context, markerContent: MarkerContent): MarkerOptions  {
            Log.d("!!!getMarkerOptions", "${Thread.currentThread().name}")
            return MarkerOptions()
                .position(LatLng(markerContent.latitude, markerContent.longitude))
                .icon(BitmapDescriptorFactory.fromBitmap(
                            CustomMarker.createCustomMarker(context, markerContent) {
                                isInternetNoBad = false
                            }
                                ?: when (markerContent.category) {
                                    Category.LOST.toString() ->{
                                        isInternetNoBad = false
                                        context.resources.getDrawable(R.drawable.ic_lost).toBitmap(150, 150, null)
                                    }
                                    else ->{
                                        isInternetNoBad = false
                                        context.resources.getDrawable(R.drawable.ic_found).toBitmap(150, 150, null)
                                    }
                                }
                ))
                .title(markerContent.key)
                .snippet(markerContent.category)
        }

    private fun requestOnExternalDB() {
        viewModelScope.launch {
            fbDatabase.readDataFromDB(
                eventResult = {
                    manageMarkersToRoom(it)
                },
                result = {

                    Log.d("!!!updateApplicationsReadDataFromDB", "${it}")
//                    _updateApplications.value = Event(it)
                },
                errorMessage = {
                    Log.d("!!!updateApplicationsReadDataFromDBonCancelled", "${it}")
//                    loading()
                }
            )
        }
    }

    private suspend fun loading(isVisible: Boolean = false, percentage: Float? = null) = withContext(Dispatchers.Main){
        _loading.value = Event(Loading(
            isVisible = isVisible,
            percentage = percentage ?: 0.0f
        ))
    }

    private fun manageMarkersToRoom(listRequestForMarkerMap: ArrayList<RequestForMarkerMap>) {
        Log.d("!!!manageMarkersToRoom11", "${Thread.currentThread().name}")

        viewModelScope.launch {
            Log.d("!!!manageMarkersToRoom22", "${Thread.currentThread().name} - ${listRequestForMarkerMap.size}")

            withContext(Dispatchers.IO) {
                Log.d("!!!manageMarkersToRoom33", Thread.currentThread().name)
                Log.d("!!!manageMarkersToRoom33", "${getVMMarkersContent()?.size}")

                val newList = listRequestForMarkerMap.filter {
                    getVMMarkersContent()?.contains(it.toMarkerEntity().toMarkerContent()) != true
                }
                Log.d("!!!manageMarkersToRoom44", "${newList.size}")

                Log.d("!!!manageMarkersToRoom1", "${newList.isNotEmpty()}")
                if (newList.isNotEmpty()) insertMarkersToRoom(newList)

                val listToDelete = getVMMarkersContent()!!
                    .filter { markerContent ->
                        !listRequestForMarkerMap.map { it.key }.contains(markerContent.key)
                    }.mapNotNull { markerContent ->
                        getVMMarkersContent()?.first {
                            it.key == markerContent.key
                        }?.let { MarkerEntity.fromMarkerContent(it) }
                    }
                Log.d("!!!manageMarkersToRoom2", "${listToDelete.size}")

                if (listToDelete.isNotEmpty()) deleteMarkersFromRoom(listToDelete)
            }
        }
    }

    private suspend fun insertMarkersToRoom(newList: List<RequestForMarkerMap>) {
        Log.d("!!!insertMarkersToRoom", "${newList.size}")

        dao.insertMarker(newList.map { it.toMarkerEntity() })
    }

    private suspend fun deleteMarkersFromRoom(oldList: List<MarkerEntity>) {
        Log.d("!!!deleteMarkersFromRoom", "${oldList.size}")
        dao.deleteMarkers(oldList)
        oldList.forEach {
            dao.deleteMarkerFromRequestFromBureau(it.key)
        }
    }


    fun getCurrentUser() {
        val currentUser = fbAuth.currentUser
        Log.d("!!!getCurrentUser4", "${currentUser?.isAnonymous}")

        if (currentUser == null) {
            fbAuth.signInAnonymously(
                callback = {
                    Log.d("!!!getCurrentUser3", "${it}")
                    //TODO get one first time
                    requestOnExternalDB()
                    _currentUserVM.value = it
                },
                failure = {
                    Log.d("!!!getCurrentUser2", "${it}")
                    toastEventBase.value = Event(it.toString())
                })
        } else {
            _currentUserVM.value = currentUser
        }
        Log.d("!!!getCurrentUser2", "${fbAuth.currentUser?.uid}")
    }

    fun signInFirebaseWithGoogle(account: GoogleSignInAccount) {
        if (getVMState()?.isBtnSignInGoogleInProgress == true) return
        viewModelScope.launch {
            _viewModelState.value = getVMState()?.copyVMState(isBtnSignInGoogleInProgress = true)
            delay(1000)
            fbAuth.signInFirebaseWithGoogle(account,
                firebaseUser = {
                    _viewModelState.value =
                        getVMState()?.copyVMState(isBtnSignInGoogleInProgress = false)
                    _currentUserVM.value = it
                },
                failure = {
                    _viewModelState.value =
                        getVMState()?.copyVMState(isBtnSignInGoogleInProgress = false)
                    toastEventBase.value = Event(it.toString())
                })
        }
    }

    fun signOutGoogleToGetListAccounts(
        signInGoogleClient: GoogleSignInClient,
        isGetSignInIntent: Boolean
    ) {
        if (getVMState().isBtnSignInGoogleInProgress == true) return
        viewModelScope.launch {
            Log.d("!!!viewModelState", "${viewModelState.value}")
            _viewModelState.value = getVMState().copyVMState(isBtnSignInGoogleInProgress = true)
            delay(500)
            fbAuth.signOutGoogleClient(signInGoogleClient,
                signOutCallback = {
                    Log.d("!!!currentUserVM4", "${it?.uid}")
                    _viewModelState.value =
                        getVMState().copyVMState(isBtnSignInGoogleInProgress = false)

                    getCurrentUser()
                    if (isGetSignInIntent) _currentUserEventVM.value = Event(it)
                },
                failure = {
                    _viewModelState.value =
                        getVMState().copyVMState(isBtnSignInGoogleInProgress = false)
                    toastEventBase.value = Event(it.toString())
                }
            )
        }
    }

    fun goToAddApplicationFragment() {
        if (getVMState().isBtnSignInGoogleInProgress == true) return
        _goToAddApplicationFragment.value = Event(Unit)
    }

    override fun onCleared() {
        super.onCleared()
        fbDatabase.removeListenersMap()
    }

    fun unregisterNetworkCallback() {
        network.unregisterNetworkCallback()
    }




    companion object {
        const val LOAD_APPLICATION = "LOAD_APPLICATION"
        const val COLLECT_MARKERS_MAP = "COLLECT_MARKERS_MAP"
    }

    class ManagerListMarkersOption(
        val markerOptionList: List<MarkerOptions> = listOf(),
        val manage: Int = 0
    ) {
        companion object {
            const val ADD_ALL = 1
            const val DELETE = 2
            const val CHANGE = 3
        }
    }

    class Loading(
        val isVisible: Boolean = false,
        val percentage: Float = 0.1f
    )
}

class ViewModelState(
    var isBtnSignInGoogleInProgress: Boolean? = null,
    var isApplicationLoading: Boolean? = null,
)

fun ViewModelState.copyVMState(
    isBtnSignInGoogleInProgress: Boolean? = null,
    isApplicationLoading: Boolean? = null,
): ViewModelState {
    isBtnSignInGoogleInProgress?.let { this.isBtnSignInGoogleInProgress = it }
    isApplicationLoading?.let { this.isApplicationLoading = it }
    return this
}

fun MapViewModel.getVMState() = this.viewModelState.value!!

fun MapViewModel.viewModelState(
    isBtnSignInGoogleInProgress: Boolean? = null,
    isApplicationLoading: Boolean? = null,
)  = this.getVMState().copyVMState(
        isBtnSignInGoogleInProgress,
        isApplicationLoading
    )

fun MapViewModel.getVMMarkersContent() = this.loadMarkersState.value

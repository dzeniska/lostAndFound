package com.dzenis_ska.lostandfound.ui.fragments.map

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dzenis_ska.lostandfound.R
import com.dzenis_ska.lostandfound.databinding.CustomDrawerMenuLayoutBinding
import com.dzenis_ska.lostandfound.databinding.FragmentMapBinding
import com.dzenis_ska.lostandfound.databinding.LoadingMarkersBinding
import com.dzenis_ska.lostandfound.ui.activity.OnBaskPressed
import com.dzenis_ska.lostandfound.ui.fragments.add_application.AddApplicationFragmentArguments
import com.dzenis_ska.lostandfound.ui.fragments.add_application.Category
import com.dzenis_ska.lostandfound.ui.fragments.baseMap.BaseMapFragment
import com.dzenis_ska.lostandfound.ui.fragments.baseMap.getBaseMapState
import com.dzenis_ska.lostandfound.ui.fragments.baseMap.stateBaseMapCopy
import com.dzenis_ska.lostandfound.ui.utils.*
import com.dzenis_ska.lostandfound.ui.utils.map.*
import com.dzenis_ska.lostandfound.ui.utils.permissionsUtils.allLocationPermissionsGranted
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MapFragment : BaseMapFragment(R.layout.fragment_map), OnBaskPressed {

    private var signInClient: GoogleSignInClient? = null
    private var currentUser: FirebaseUser? = null
    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("!!!googleSignInLauncher", "result1: ${result}")

            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            Log.d("!!!googleSignInLauncher", "result2: ${result.data}")
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d("!!!googleSignInLauncher1", "account: ${account.idToken}")
                if (account != null) {
                    signInFirebaseWithGoogle(account)
                }
            } catch (e: ApiException) {
                Log.d("!!!googleSignInLauncher2", "Api error: ${e.message}")
            }
        }

    var previewAdapter: PreviewAdapter? = null

    internal var state: InstanceStateMapFragment? = null

    private val viewModel: MapViewModel by viewModels { factory() }


    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        whenMapActive.mMap = mMap
        mMap!!.setOnMapClickListener {
            Log.d("!!!setOnMapClickListener", "${it}")
        }
        mMap!!.setOnMarkerClickListener { marker ->
            if (binding?.pbApplicationLoading?.isVisible == false) {
                binding?.pbApplicationLoading?.show()
                marker.showInfoWindow()
                setDefaultCameraTilt(
                    googleMap,
                    LatLng(marker.position.latitude, marker.position.longitude),
                    object : GoogleMap.CancelableCallback {
                        override fun onCancel() {}
                        override fun onFinish() {
                            Log.d("!!!setOnMarkerClickListenerOnFinish", "2")
                            viewModel.onMarkerClick(marker.tag.toString())
                        }
                    })
            }
            true
        }
        mMap!!.setOnCameraMoveListener {
            val pos = mMap!!.cameraPosition
            Log.d("!!!setOnCameraMoveListener", "${pos.zoom}")

            stateBaseMapCopy(
                latLngMoveCamera = LatLng(pos.target.latitude/*.plus(POSITION_VALUE_DIFFERENCE)*/,
                    pos.target.longitude/*.plus(POSITION_VALUE_DIFFERENCE)*/),
                zoomMoveCamera = pos.zoom
            )
        }
        mMap!!.setOnCameraMoveStartedListener {
            recyclerViewIsClickableItem(false)
//            hideRecyclerView()
        }

        mMap!!.setOnCameraIdleListener {
            val pos = mMap!!.cameraPosition
//            Log.d("!!!setOnCameraIdleListener", "${isPortraitOrientation()}")
            Log.d("!!!setOnCameraIdleListener", "${isPortrait()}")
            stateBaseMapCopy(
                latLng = LatLng(pos.target.latitude/*.plus(POSITION_VALUE_DIFFERENCE)*/,
                    pos.target.longitude/*.plus(POSITION_VALUE_DIFFERENCE)*/),
                zoom = pos.zoom
            )
            //show PreviewAdapter

            startToShowPreviewAdapter()
        }

        Log.d("!!!OnMapReadyCallback", "${googleMap}")
        //if get permissions run getLocation
        if(allLocationPermissionsGranted())
            getFusedLocationClient()
        else
            launchLocationPermissions()
        val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

    }

    private fun startToShowPreviewAdapter() {
        if (mMap != null){
            if (isPortrait() /*&& mMap!!.cameraPosition.zoom > ZOOM_TO_SHOW_ADAPTER_4*/)
                viewModel.defineLatLngBounds(mMap?.projection?.visibleRegion?.latLngBounds)
            else {
                previewAdapter = null
                binding?.recyclerView?.hide()
            }
        } else {
            previewAdapter = null
            binding?.recyclerView?.hide()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.whenContextActive.contextAction = context
    }

    override fun onDetach() {
        super.onDetach()
        viewModel.whenContextActive.contextAction = null
    }

    private var binding: FragmentMapBinding? = null
    private var bindingDrawer: CustomDrawerMenuLayoutBinding? = null
    private var bindingLoader: LoadingMarkersBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = savedInstanceState?.getParcelable(STATE_KEY) ?: InstanceStateMapFragment()
                ?: throw IllegalArgumentException("!!!There is not getting instance InstanceStateMap")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        bindingDrawer = CustomDrawerMenuLayoutBinding.bind(binding!!.root)
        bindingLoader = LoadingMarkersBinding.bind(binding!!.root)
        initMapView(savedInstanceState, binding!!, callback)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("!!!onViewCreatedBMF", "onViewCreated")
        initClick()
        initUI()
        initDrawerUi()
        initViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE_KEY, state)
    }

    private fun initViewModel() {
        viewModel.moveSelectedMarkerToCenter.observe(viewLifecycleOwner) {
            it.getValue()?.let {latLng->
                mMap?.let { googleMap->
                    setDefaultCameraTilt(googleMap, latLng, null)
                }
            }
        }
        viewModel.showPreloadAdapter.observe(viewLifecycleOwner) {
            initPreloadAdapter(it)
        }

        viewModel.loading.observe(viewLifecycleOwner) { event ->
            event.getValue()?.let { loading ->
                Log.d("!!!loading", "${loading.isVisible} _ ${loading.percentage}")
                bindingLoader?.let {
                    stateLoadingLayout(
                        bindingLoader = it,
                        isVisible = loading.isVisible,
                        progress = loading.percentage
                    )
                }
            }
        }

        viewModel.viewModelState.observe(viewLifecycleOwner) { viewModelState ->
            Log.d("!!!viewModelState", "${viewModelState.isBtnSignInGoogleInProgress}")
            viewModelState.isBtnSignInGoogleInProgress?.let {
                bindingDrawer?.loginProgressBar?.setVisibility(it)
            }
            viewModelState.isApplicationLoading?.let {
                binding?.pbApplicationLoading?.isVisible = it
            }
        }

        viewModel.updateApplications.observe(viewLifecycleOwner) { event ->
            event.getValue()?.let { list ->
                Log.d("!!!updateApplications", "${list.size}")
            }
        }

        viewModel.goToAddApplicationFragment.observe(viewLifecycleOwner) {
            it.getValue()?.let {
                stateCopy(isItemAddApplicationPressed = true)
                navigator().goToAddApplicationFragment(AddApplicationFragmentArguments(location = getBaseMapState().latLng))
            }
        }
        viewModel.goToShowApplicationFragment.observe(viewLifecycleOwner) {
            it.getValue()?.let { id ->
                navigator().goToShowApplicationFragment(id)
            }
        }

        viewModel.currentUserVM.observe(viewLifecycleOwner) {
            currentUser = it
            initDrawerUserPhoto(bindingDrawer!!.root, currentUser)
            Log.d("!!!currentUserVM", "${currentUser?.uid}")
        }

        viewModel.currentUserEventVM.observe(viewLifecycleOwner) {
            it.getValue()?.let { user ->
                currentUser = user
                signInIntent()
            }
        }
        viewModel.toastEvent.observe(viewLifecycleOwner) {
            it.getValue()?.let { mess ->
                Log.d("!!!toastEvent", "${mess}")
                toastS(mess)
            }
        }
        viewModel.dialogEvent.observe(viewLifecycleOwner) {
            it.getValue()?.let { simpleDialog ->
                Dialog.createDialog(
                    requireContext(),
                    cancelable = true,
                    header = simpleDialog.header,
                    mess = simpleDialog.message,
                    btn1 = null,
                    btn2 = null,
                    buttonOnePressed = {}, buttonTwoPressed = {}
                ) {}
            }
        }

        viewModel.dialogBlockedAppEvent.observe(viewLifecycleOwner) {
            it.getValue()?.let { simpleDialog ->
                Dialog.createDialogAppWasDisabled(
                    requireContext(),
                    simpleDialog.header,
                    simpleDialog.message
                ) { _, _ -> }
            }
        }
        viewModel.loadMarkersOptionState.observe(viewLifecycleOwner) { managerMarkersOption ->
            Log.d("!!!mMap1", "${mMap}")
            Log.d("!!!loadMarkersStateObserve1", "${mMap} _ ${managerMarkersOption.markerOptionList.size}")
            showMarkers(managerMarkersOption)
        }
    }

    private fun initPreloadAdapter(list: List<PreviewAdapterClass>) = with(binding!!) {
        Log.d("!!!initPreloadAdapter0", "${list.size} _ ${previewAdapter}")
        if (previewAdapter == null) {
            previewAdapter = PreviewAdapter {
                Log.d("!!!findSelectedMarker", "${it} _ ")
                viewModel.findSelectedMarker(it)
            }
            recyclerView.adapter = previewAdapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
        previewAdapter?.listPreview = list
        if (isPortrait()) recyclerView.show()
        recyclerViewIsClickableItem(true)
        Log.d("!!!initPreloadAdapter1", "${list.size} _ ${previewAdapter}")
    }

    private fun recyclerViewIsClickableItem(isClickable: Boolean) {
        previewAdapter?.isClickableItem = isClickable
    }

    private fun showMarkers(managerMarkersOption: MapViewModel.ManagerListMarkersOption) =
        whenMapActive { googleMap ->
            Log.d("!!!mMap", "${mMap} _ ${googleMap}")
            val markerOptionList = managerMarkersOption.markerOptionList
            CoroutineScope(Dispatchers.Main).launch {
                if (markerOptionList.isNotEmpty()) {
                    mMap?.clear()
                    googleMap.clear()
                }
                markerOptionList.forEach { markerOption ->
                    val marker = googleMap.addMarker(markerOption)
                    marker?.tag = markerOption.title
                    marker?.title = markerOption.snippet
                    marker?.snippet = "***"
                }
//                startToShowPreviewAdapter()
            }
        }

    private fun signInFirebaseWithGoogle(account: GoogleSignInAccount) {
        viewModel.signInFirebaseWithGoogle(account)
    }

    private fun openDrawerClick() = with(binding!!) {
        stateCopy(isBtnOpenDrawerVisible = false)
        drawer.openDrawer(GravityCompat.START)
        btnOpenDrawer.hide()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initClick() = with(binding!!) {

        btnOpenDrawer.setOnTouchListener(MyTouchListener {
            openDrawerClick()
        })

        drawer.setOnTouchListener { _, _ ->
            Log.d("!!!drawer", "setOnTouchListener")
            closeDrawer()
            true
        }
        onMenuItemClick(binding!!.root,
            ibLostClick = {
                hideRecyclerView()
                viewModel.initDao(Category.LOST)
                stateCopy(isItemLostPressed = true)
                closeDrawer()
            },
            ibFoundClick = {
                hideRecyclerView()
                viewModel.initDao(Category.FOUND)
                stateCopy(isItemFoundPressed = true)
                closeDrawer()
            },
            ibAddApplicationClick = {
                hideRecyclerView()
                Log.d("!!!ibAddApplicationClick", "${!currentUser?.email.isNullOrEmpty()}")
                if (!currentUser?.email.isNullOrEmpty()) {
                    whenMapActive.clear()
                    closeDrawer()
                    viewModel.goToAddApplicationFragment()
                } else {
                    toastS(getString(R.string.need_to_register))
                }
                viewModel.initDao()

            },
            ibRegistrationClick = {
                hideRecyclerView()
                Log.d("!!!ibRegistrationClick", "onDestroyView")
                if (currentUser?.email != null) {
                    viewModel.signOutGoogleToGetListAccounts(
                        getSignInGoogleClient(),
                        isGetSignInIntent = true
                    )
                } else {
                    signInIntent()
                }
                stateCopy(isItemLoginPressed = true)
                viewModel.initDao()
            },
            loginLayoutClick = {
                Log.d("!!!loginLayoutClick", "loginLayoutClick")

                if (currentUser?.email != null) {
                    Dialog.createDialog(
                        context = requireContext(),
                        cancelable = true,
                        header = currentUser?.email,
                        mess = getString(R.string.action_on_google_account),
                        btn1 = getString(R.string.change_google_account),
                        btn2 = getString(R.string.exit_from_google_account),
                        buttonOnePressed = {
                            viewModel.signOutGoogleToGetListAccounts(
                                getSignInGoogleClient(),
                                isGetSignInIntent = true
                            )
                        },
                        buttonTwoPressed = {
                            viewModel.signOutGoogleToGetListAccounts(
                                getSignInGoogleClient(),
                                isGetSignInIntent = false
                            )
                        }
                    ) { it.run() }
                }
                stateCopy(isItemLoginPhotoLayoutPressed = true)
                hideRecyclerView()
                viewModel.initDao()
            }
        )
    }

    private fun hideRecyclerView(){
        if (isPortrait())
            binding?.recyclerView?.hide()
    }

    private fun signInIntent() {
        if (currentUser?.email != null) return
        signInClient = getSignInGoogleClient()
        val intent = signInClient!!.signInIntent
        googleSignInLauncher.launch(intent)
    }

    private fun initUI() = with(binding!!) {
        getState()?.isBtnOpenDrawerVisible?.let { btnOpenDrawer.setVisibility(it) }
    }

    private fun initDrawerUi() = initDrawerLayoutItems(bindingDrawer!!.root)

    private fun closeDrawer() = with(binding!!) {
        stateCopy(isBtnOpenDrawerVisible = true)
        drawer.close()
        btnOpenDrawer.x = 29f
        btnOpenDrawer.show()
        btnOpenDrawer.animateShow()
    }

    override fun onStart() {
        super.onStart()
        viewModel.getCurrentUser()
    }

    override fun onDestroyView() {
        Log.d("!!!onDestroyViewMF", "onDestroyView")
        binding = null
        bindingDrawer= null
        bindingLoader = null
        whenMapActive.mMap = null
        previewAdapter = null
        super.onDestroyView()
    }

    override fun onBackPressedInFragment(): Runnable {
        return Runnable {
            Log.d("!!!onDestroyViewMF", "onDestroyView")
            whenMapActive.clear()
        }
    }

    companion object {
        private const val STATE_KEY = "STATE_KEY"
        private const val POSITION_VALUE_DIFFERENCE: Double = 0.0001
        private const val ZOOM_TO_SHOW_ADAPTER_4 = 4
    }
}
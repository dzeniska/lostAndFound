package com.dzenis_ska.lostandfound.ui.fragments.baseMap

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewbinding.ViewBinding
import com.dzenis_ska.lostandfound.R
import com.dzenis_ska.lostandfound.databinding.FragmentChangeLocationBinding
import com.dzenis_ska.lostandfound.databinding.FragmentMapBinding
import com.dzenis_ska.lostandfound.databinding.FragmentShowLocationBinding
import com.dzenis_ska.lostandfound.ui.activity.MainActivityViewModel
import com.dzenis_ska.lostandfound.ui.utils.*
import com.dzenis_ska.lostandfound.ui.utils.map.MapActions
import com.dzenis_ska.lostandfound.ui.utils.permissionsUtils.REQUIRED_LOCATION_PERMISSIONS
import com.dzenis_ska.lostandfound.ui.utils.permissionsUtils.allLocationPermissionsGranted
import com.dzenis_ska.lostandfound.ui.utils.permissionsUtils.gotLocationPermissions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng

open class BaseMapFragment(fragmentMap: Int) : Fragment(fragmentMap),
    LocationListener
{


    var mapViewBundle: Bundle? = null

    var mMap: GoogleMap? = null
    var whenMapActive = MapActions()
    var mapView: MapView? = null
    var locationManager: LocationManager? = null
    var fusedLocationClient: FusedLocationProviderClient? = null

    var baseMapState = MapState()

    val viewModelBaseMap: BaseMapViewModel by viewModels { factory() }
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels { factory() }

    private val requestLocationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            Log.d("!!!requestLocationPermissions", "${it.values}_ requestLocationPermissions")
            getFusedLocationClient()
//            getLocationPermission(it)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("!!!sendNewLocationBack1", "${baseMapState}")
        baseMapState = savedInstanceState?.getParcelable(STATE_BASE_MAP_KEY)
                ?: MapState() ?: throw IllegalArgumentException("!!!There is not getting instance MapState")
        Log.d("!!!sendNewLocationBack2", "${baseMapState} _ ")

        initFusedLocationClientAndLocationManager()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSystemUI()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }
        Log.d("!!!onSaveInstanceStateMapBundle1", "${mapViewBundle} _ ${outState}")
        try {
            mapView?.onSaveInstanceState(mapViewBundle!!)
        } catch (e: Exception) {
            Log.d("!!!onSaveInstanceStateMapBundle2", "Exception: ${e.message}")
        }


        Log.d("!!!onSaveInstanceStateMap", "${baseMapState}_ stateMap")

        outState.putParcelable(STATE_BASE_MAP_KEY, baseMapState)
    }

    fun initMapView(savedInstanceState: Bundle?, binding: ViewBinding, callback: OnMapReadyCallback) {
        mapViewBundle = savedInstanceState?.getBundle(MAPVIEW_BUNDLE_KEY)
        Log.d("!!!onSaveInstanceState1", "${mapViewBundle?.size()}")
        when (binding) {
            is FragmentMapBinding-> {
                mapView = binding.map
                mapView!!.onCreate(mapViewBundle)
                mapView!!.getMapAsync(callback)
            }
            is FragmentChangeLocationBinding-> {
                mapView = binding.map
                mapView!!.onCreate(mapViewBundle)
                mapView!!.getMapAsync(callback)
            }
            is FragmentShowLocationBinding -> {
                mapView = binding.map
                mapView!!.onCreate(mapViewBundle)
                mapView!!.getMapAsync(callback)
            }
            else -> throw IllegalArgumentException("Base Map Fragment didn't find ViewBinding class")
        }
        mapView!!.requestDisallowInterceptTouchEvent(false)
    }

    private fun initSystemUI(){
        setTranslucentStatusAndNavigation(true)
    }

    private fun initFusedLocationClientAndLocationManager() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private fun getLocationPermission(map: Map<String, Boolean>) {
        Log.d("!!!getLocationPermission", "getPerm1 _ ${map}")
        //todo experiment switchOff locPerm
        gotLocationPermissions(map) {
            Log.d("!!!getLocationPermission", "getPerm2 _ ${map}")
            launchLocationPermissions()
        }
    }

    fun launchLocationPermissions() {
        Log.d("!!!launchLocationPermissions", "$context")
        if (context == null) return
        if (!allLocationPermissionsGranted()) {
            Log.d("!!!launchLocationPermissions3", "$context")
            requestLocationPermissions.launch(REQUIRED_LOCATION_PERMISSIONS)
//            toastS(getStr(R.string.no_gps_permission))
            Log.d("!!!launchLocationPermissions3", getStr(R.string.no_gps_permission))
        }
    }

    @SuppressLint("MissingPermission")
    fun getFusedLocationClient() {
        Log.d("!!!fusedLocationClient0", "${allLocationPermissionsGranted()}")
        if(!allLocationPermissionsGranted()) {
            val state = getBaseMapState()
            Log.d("!!!statefusedLocationClient055", "${getBaseMapState()}")
            setMarkerLocationChanged(
                lat = state.latLngMoveCamera?.latitude ?: 0.0,
                lng = state.latLngMoveCamera?.longitude ?: 0.0,
                zoom = state.zoomMoveCamera ?: state.zoom ?: ZOOM_DEFAULT_3,
                isAnimate = true,
                isCircleShow = false
            )
        } else {
            fusedLocationClient?.lastLocation
                ?.addOnSuccessListener { loc: Location? ->
                    Log.d("!!!fusedLocationClient", "${getBaseMapState().latLng}")
                    val state = /*mainActivityViewModel.getStateMapValue() ?:*/ getBaseMapState()
                    Log.d("!!!fusedLocationClient332", "${state}")
                    if (state.latLng != null
                        &&
                        (state.latLng != LatLng(0.0, 0.0))
                    ) {
                        Log.d("!!!setMarkerLocationChanged1", "${state}")
                        setMarkerLocationChanged(
                            lat = state.latLng!!.latitude,
                            lng = state.latLng!!.longitude,
                            zoom = state.zoom ?: ZOOM_DEFAULT_12,
                            isAnimate = true,
                            isCircleShow = false
                        )
                    } else if (loc != null) {
                        Log.d("!!!fusedLocationClient22", "${loc}")
                        Log.d("!!!setMarkerLocationChanged2", "${state}")

                        setMarkerLocationChanged(loc.latitude, loc.longitude, ZOOM_DEFAULT_12, true)
                    } else {
                        Log.d("!!!fusedLocationClient3", "requestLocationUpdates()")
                        requestLocationUpdates()
                    }
                }?.addOnFailureListener {
//                toastL("exception: ${it.message}")
                    if (context != null)
                        setDefaultLocation()
                    // todo do not launch requestLocationUpdates() cause do not have permission ACCESS_FINE_LOCATION & ACCESS_COARSE_LOCATION
                    Log.d("!!!exception", "${it.message}")
                }
        }
    }

    fun setDefaultCameraTilt(googleMap: GoogleMap, target: LatLng, callback: GoogleMap.CancelableCallback?) {
        val camPos = CameraPosition.Builder()
            .target(target)
            .zoom(17.5f)
            .tilt(67.5f)
            .build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos), 500, callback)
    }

    private fun setDefaultLocation() = setMarkerLocationChanged(
        getString(R.string.default_latitude).toDouble(),
        getString(R.string.default_longitude).toDouble(),
        ZOOM_DEFAULT_3,
        true)

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates() {
        Log.d("!!!requestLocationUpdates", "${locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true}" +
                "${locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true}")
        if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
            ||
            locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true) {
            locationManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                5000,
                5f,
                this
            )
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                5f,
                this
            )
            toastS(getStr(R.string.location_searching))
        } else {
            if(context != null)
                setDefaultLocation()
            toastS(getStr(R.string.gps_provider_is_not_enabled))
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.d("!!!setMarkerLocationChanged3", "${location}")
        setMarkerLocationChanged(location.latitude, location.longitude, ZOOM_DEFAULT_12)
        removeLocationUpdates()
    }

//    @Deprecated("Deprecated in Java", ReplaceWith(
//        "Log.d(\"!!!onStatusChanged\", \"MF \$provider\")",
//        "android.util.Log"
//    )
//    )
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.d("!!!onStatusChanged", "MF $provider")
    }

    override fun onProviderEnabled(provider: String) {
        Log.d("!!!onProviderEnabled", "provider enabled")
    }

    override fun onProviderDisabled(provider: String) {
        Log.d("!!!", "provider disabled")
    }
    private fun setMarkerLocationChanged(lat: Double, lng: Double, zoom: Float, isAnimate: Boolean = false, isCircleShow: Boolean = true) {
        Log.d("!!!setMarker", "${zoom} -- ${lat}--${mMap}")
                val circleOptions = CircleOptions()
                    .center(LatLng(lat, lng))
                    .radius(CIRCLE_RADIUS)
                    .fillColor("#363D5AFE".toColorInt())
                    .strokeColor("#FF9800".toColorInt())
                    .strokeWidth(2f)

        if (isCircleShow) mMap?.addCircle(circleOptions)
        val camPos = CameraPosition.Builder()
            .target(LatLng(lat, lng))
            .tilt(67.5f)
            .zoom(zoom)
            .build()
//        if (getBaseMapState()?.isFirstStart == false) return
        if(isAnimate) animateCamera(camPos) else moveCamera(camPos)
    }

    private fun moveCamera(cameraPosition: CameraPosition) =
        mMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

    private fun animateCamera(cameraPosition: CameraPosition) {
        mMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun removeLocationUpdates() {
        locationManager?.removeUpdates(this)
    }

    override fun onStart() {
        Log.d("!!!onStartBMF", "onStart")
        mapView?.onStart()
        super.onStart()
    }

    override fun onResume() {
        Log.d("!!!onResumeBMF", "onResume")
        if (mMap != null){
            //Only for requests permissions
//            getLocation()
        }
        mapView?.onResume()
        super.onResume()
    }

    override fun onPause() {
//        requestLocationPermissions.unregister()
        Log.d("!!!onPauseBMF", "onPause")
        mapView?.onPause()
        super.onPause()
    }

    override fun onStop() {
        Log.d("!!!onStopBMF", "onStop")
        mapView?.onStop()
        super.onStop()
    }


    override fun onDestroyView() {
        Log.d("!!!onDestroyViewBMF", "onDestroyView _ ${mapView}")
        mapView?.onDestroy()
        mMap = null
        removeLocationUpdates()
//        requestLocationPermissions.unregister()
        super.onDestroyView()
    }

    override fun onLowMemory() {
        Log.d("!!!onLowMemoryBMF", "onLowMemory")
        mapView?.onLowMemory()
        super.onLowMemory()
    }
    companion object {
        private const val STATE_BASE_MAP_KEY = "STATE_BASE_MAP_KEY"
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
        private const val CIRCLE_RADIUS = 1500.0
        private const val ZOOM_DEFAULT_12 = 12f
        private const val ZOOM_DEFAULT_3 = 3f
        private const val MARKER_ADD_APPLICATION = "MARKER_ADD_APPLICATION"
    }
}
package com.dzenis_ska.lostandfound.ui.fragments.changeLocation

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.dzenis_ska.lostandfound.R
import com.dzenis_ska.lostandfound.databinding.FragmentChangeLocationBinding
import com.dzenis_ska.lostandfound.ui.activity.MainActivityViewModel
import com.dzenis_ska.lostandfound.ui.fragments.baseMap.BaseMapFragment
import com.dzenis_ska.lostandfound.ui.fragments.baseMap.MapState
import com.dzenis_ska.lostandfound.ui.utils.*
import com.dzenis_ska.lostandfound.ui.utils.map.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*

class ChangeLocationFragment : BaseMapFragment(R.layout.fragment_change_location) {
    private var marker: Marker? = null
    internal var state: InstanceStateChangeLocationFragment? = null
    private val args: ChangeLocationFragmentArgs by navArgs()
    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        mMap!!.setOnMarkerClickListener {
            if (it.isInfoWindowShown)
            sendNewLocationBack()
            false
        }
        mMap!!.setOnInfoWindowClickListener {
            sendNewLocationBack()
        }
        mMap!!.setOnMapLongClickListener { location->
            marker?.remove()
            stateCopy(latLng = location)
            setMarkerAddApplication(googleMap, location)
        }

        mMap!!.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDrag(p0: Marker) {}
            override fun onMarkerDragEnd(marker: Marker) {
                Log.d("!!!onMarkerDragEnd", "${marker.position}")

                stateCopy(latLng = marker.position)
            }
            override fun onMarkerDragStart(p0: Marker) {}
        })
        val location = getState()?.latLng ?: args.argsCLF?.location
        if (getState()?.latLng == null) stateCopy(latLng = location)
        getState()?.latLng?.let { setMarkerAddApplication(googleMap, it) }

        val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
    }

    private val mainActivityViewModel: MainActivityViewModel by activityViewModels { factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = savedInstanceState?.getParcelable(STATE_KEY)
            ?: InstanceStateChangeLocationFragment()
                    ?: throw IllegalArgumentException("!!!There is not getting instance InstanceStateChangeLocationFragment")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentChangeLocationBinding.inflate(inflater, container, false)
        initMapView(savedInstanceState, binding, callback)
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE_KEY, state)
    }

    private fun sendNewLocationBack(){
//       todo do not work -> stateBaseMapCopy(latLng = getState()?.latLng)
        Log.d("!!!sendNewLocationBack3", "${MapState(getState()?.latLng)}")

//        mainActivityViewModel.setStateMapValue(MapState(latLng = getState()?.latLng))
        Log.d("!!!sendNewLocationBack", "${getState()?.latLng}")
        navigator().publishResult(ChangeLocationFragmentArguments(location = getState()?.latLng))
        navigator().popBackStack()
    }

    private fun setMarkerAddApplication(googleMap: GoogleMap, latLng: LatLng) {

        marker = mMap?.addMarker(
            MarkerOptions().position(latLng)
                .icon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
//                    BitmapDescriptorFactory.fromBitmap(
//                        CustomMarker.createMarker(requireContext())
//                    )
                )
                .title(getString(R.string.save))
                .draggable(true)
        )
        setDefaultCameraTilt(googleMap, latLng, null)
//        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    companion object {
        private const val STATE_KEY = "STATE_KEY"
    }
}
package com.dzenis_ska.lostandfound.ui.fragments.showLocation

import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.navArgs
import com.dzenis_ska.lostandfound.R
import com.dzenis_ska.lostandfound.databinding.FragmentShowLocationBinding
import com.dzenis_ska.lostandfound.ui.fragments.baseMap.BaseMapFragment
import com.dzenis_ska.lostandfound.ui.utils.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*

class ShowLocationFragment : BaseMapFragment(R.layout.fragment_show_location) {

    private val args: ShowLocationFragmentArgs by navArgs()
    private val callback = OnMapReadyCallback { googleMap ->
        val target = args.argsSLF.location
        mMap = googleMap
        mMap!!.setOnMarkerClickListener {
            copyLocationToBuffer()
            false
        }
        mMap!!.setOnInfoWindowClickListener {
            copyLocationToBuffer()
        }
        googleMap.addMarker(
            MarkerOptions().position(target)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                .snippet( args.argsSLF.location.latitude.toString() +" "+ args.argsSLF.location.longitude.toString())
                .title(getString(R.string.location_copy) + " " + getString(R.string.copy))
        )
        setDefaultCameraTilt(googleMap, target, null)
    }

    private fun copyLocationToBuffer() {
        requireContext().copyToClipboard(args.argsSLF.location.latitude.toString() +" "+ args.argsSLF.location.longitude.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentShowLocationBinding.inflate(inflater, container, false)
        initMapView(savedInstanceState, binding, callback)
        return binding.root
    }
}
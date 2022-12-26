package com.dzenis_ska.lostandfound.ui.fragments.baseMap

import android.os.Parcelable
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class MapState(
    var latLng: LatLng? = null,
    var latLngMoveCamera: LatLng? = null,
    var zoom: Float? = null,
    var zoomMoveCamera: Float? = null,
): Parcelable

fun BaseMapFragment.stateBaseMapCopy(
    latLng: LatLng? = null,
    latLngMoveCamera: LatLng? = null,
    zoom: Float? = null,
    zoomMoveCamera: Float? = null,
) {
    Log.d("!!!statefusedLocationClient011", "${latLng} _ ${zoom} _ ${zoomMoveCamera}")
    latLng?.let {
        if (it != LatLng(0.0,0.0))
        this.baseMapState.latLng = it
    }
    latLngMoveCamera?.let {
        if (it != LatLng(0.0,0.0))
            this.baseMapState.latLngMoveCamera = it
    }
    zoom?.let {
        this.baseMapState.zoom = it
    }
    zoomMoveCamera?.let {

        this.baseMapState.zoomMoveCamera = it
    }
}

fun BaseMapFragment.getBaseMapState() = this.baseMapState




package com.dzenis_ska.lostandfound.ui.fragments.changeLocation

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChangeLocationFragmentArguments(
    val location: LatLng? = null
): Parcelable


@Parcelize
data class InstanceStateChangeLocationFragment(
    var latLng: LatLng? = null,
    ): Parcelable {

    companion object {
        val DEFAULT = InstanceStateChangeLocationFragment()
    }

}

fun ChangeLocationFragment.stateCopy(
    latLng: LatLng? = null,
) {
    latLng?.let {
        this.state?.latLng = it
    }
}


fun ChangeLocationFragment.getState() = this.state
package com.dzenis_ska.lostandfound.ui.fragments.showLocation

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShowLocationFragmentArguments(
    val location: LatLng
): Parcelable

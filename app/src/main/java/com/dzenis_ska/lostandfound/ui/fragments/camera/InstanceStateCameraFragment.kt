package com.dzenis_ska.lostandfound.ui.fragments.camera

import android.net.Uri
import android.os.Message
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InstanceStateCameraFragment(
    val uri: Uri?,
    val message: String
): Parcelable {
    companion object {
        const val SUCCESS = "SUCCESS"
        const val FAILURE = "FAILURE"
        val NULL_INSTANCE = InstanceStateCameraFragment(null, FAILURE)
    }
}

package com.dzenis_ska.lostandfound.ui.fragments.add_application

import android.net.Uri
import android.os.Parcelable
import androidx.core.net.toUri
import com.dzenis_ska.lostandfound.ui.db.firebase.classes.RequestToBureau
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddApplicationFragmentArguments(
    val key: String? = null,
    val location: LatLng? = null,
    val uriMarkerPhoto: String? = null,
    val photoUri: List<String> = emptyList(),
    val category: String? = null,
    val telNum: String? = null,
    val additionalInfo: String? = null,
    var isEditApplication: Boolean = false,
): Parcelable {

}

@Parcelize
data class InstanceStateAddApplication(
    val key: String? = null,
    var location: LatLng? = null,
    var numPageVP: Int = 0,
    val uriMarkerPhoto: String? = null,
    val listPhotoUri: MutableList<Uri> = mutableListOf(),
    var imageIndex: Int? = -1,
    var category: String? = null,
    var telNum: String? = null,
    var additionalInfo: String? = null,
    var isEditApplication: Boolean? = null,
    var isRulesRead: Boolean? = null,
    ): Parcelable {
    fun toRequestToBureau() = RequestToBureau(
        key = this.key,
        latitude = this.location?.latitude ?: 0.2,
        longitude = this.location?.longitude ?: 0.2,
        category = this.category,
        telNum = this.telNum,
        additionalInfo = this.additionalInfo,
    )

    companion object {
        fun fromAddApplicationFragmentArgs(addApplicationFragmentArguments: AddApplicationFragmentArguments): InstanceStateAddApplication {
            val args = addApplicationFragmentArguments
            return when (addApplicationFragmentArguments.isEditApplication) {
                true -> InstanceStateAddApplication(
                    key = args.key,
                    location = args.location,
                    uriMarkerPhoto = args.uriMarkerPhoto,
                    listPhotoUri = args.photoUri.map { it.toUri() }.toMutableList(),
                    category = args.category,
                    telNum = args.telNum,
                    additionalInfo = args.additionalInfo,
                    isEditApplication = args.isEditApplication
                )
                else -> InstanceStateAddApplication(
                    location = args.location,
                )
            }
        }
    }
}

fun AddApplicationFragment.stateCopy(
    location: LatLng? = null,
    numPageVP: Int? = null,
    photoUri: Uri? = null,
    imageIndex: Int? = null,
    removePhotoUriPosition: Int? = null,
    setPhotoUriToRemovedPosition: Uri? = null,
    chooseCategory: String? = null,
    enterTelNum: String? = null,
    enterAdditionalInfo: String? = null,
    isEditApplication: Boolean? = null,
    isRulesRead: Boolean? = null,
) {
    location?.let { this.state?.location = it }
    photoUri?.let { this.state?.listPhotoUri!!.add(photoUri) }
    imageIndex?.let { this.state?.imageIndex = it }
    numPageVP?.let { this.state?.numPageVP = it }
    removePhotoUriPosition?.let { this.state?.listPhotoUri!!.removeAt(it) }
    setPhotoUriToRemovedPosition?.let {
        this.state?.listPhotoUri!!.add(removePhotoUriPosition!!, setPhotoUriToRemovedPosition)
    }
    chooseCategory?.let { this.state?.category = it }
    enterTelNum?.let { this.state?.telNum = it }
    enterAdditionalInfo?.let { this.state?.additionalInfo = it }
    isEditApplication?.let { this.state?.isEditApplication = it }
    isRulesRead?.let { this.state?.isRulesRead = it }
}

fun AddApplicationFragment.clearState() {
    this.state = InstanceStateAddApplication()
}

fun AddApplicationFragment.getState() = this.state
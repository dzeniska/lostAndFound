package com.dzenis_ska.lostandfound.ui.fragments.map

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InstanceStateMapFragment(
    var isBtnOpenDrawerVisible: Boolean? = null,
    var isItemLostPressed: Boolean? = null,
    var isItemFoundPressed: Boolean? = null,
    var isItemAddApplicationPressed: Boolean? = null,
    var isItemLoginPressed: Boolean? = null,
    var isItemLoginPhotoLayoutPressed: Boolean? = null,


    ): Parcelable {

    companion object {
        val DEFAULT = InstanceStateMapFragment()
    }

}

fun MapFragment.stateCopy(
    isBtnOpenDrawerVisible: Boolean? = null,
    isItemLostPressed: Boolean? = null,
    isItemFoundPressed: Boolean? = null,
    isItemAddApplicationPressed: Boolean? = null,
    isItemLoginPressed: Boolean? = null,
    isItemLoginPhotoLayoutPressed: Boolean? = null,

) {
    isBtnOpenDrawerVisible?.let {
        this.state?.isBtnOpenDrawerVisible = it
    }
    isItemLostPressed?.let {
        drawerMenuItemsClearSelectedBackground()
        this.state?.isItemLostPressed = it
    }
    isItemFoundPressed?.let {
        drawerMenuItemsClearSelectedBackground()
        this.state?.isItemFoundPressed = it
    }
    isItemAddApplicationPressed?.let {
        drawerMenuItemsClearSelectedBackground()
        this.state?.isItemAddApplicationPressed = it
    }
    isItemLoginPressed?.let {
        drawerMenuItemsClearSelectedBackground()
        this.state?.isItemLoginPressed = it
    }
    isItemLoginPhotoLayoutPressed?.let {
        drawerMenuItemsClearSelectedBackground()
        this.state?.isItemLoginPhotoLayoutPressed = it
    }
}
fun MapFragment.drawerMenuItemsClearSelectedBackground(){
    this.state?.isItemLostPressed = false
    this.state?.isItemFoundPressed = false
    this.state?.isItemAddApplicationPressed = false
    this.state?.isItemLoginPressed = false
    this.state?.isItemLoginPhotoLayoutPressed = false
}

fun MapFragment.getState() = this.state
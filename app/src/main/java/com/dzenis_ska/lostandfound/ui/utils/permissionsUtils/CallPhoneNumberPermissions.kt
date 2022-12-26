package com.dzenis_ska.lostandfound.ui.utils.permissionsUtils

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dzenis_ska.lostandfound.R

fun Fragment.gotCallPhonePermissions(
    results: Map<String, Boolean>,
    listenerRequestPerm: ListenerRequestPerm
) {
    Log.d("!!!results.isEmpty", "${results.isNotEmpty()}")

    if (!results.all { it.value }) {
        if (!shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE)) {
            askUserForOpeningAppSettings(Manifest.permission.CALL_PHONE, "")
        } else {
            listenerRequestPerm.invoke()
        }
    }
}

fun Fragment.allCallPhonePermissionsGranted() = CALL_PHONE_PERMISSION.all {
    Log.d("!!!context", "$context")
    context?.let { context ->
        ContextCompat.checkSelfPermission(
            context, it
        )
    } == PackageManager.PERMISSION_GRANTED
}

val CALL_PHONE_PERMISSION: Array<String>
    get() = mutableListOf(Manifest.permission.CALL_PHONE).toTypedArray()


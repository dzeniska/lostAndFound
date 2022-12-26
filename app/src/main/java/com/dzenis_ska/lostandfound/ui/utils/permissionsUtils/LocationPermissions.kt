package com.dzenis_ska.lostandfound.ui.utils.permissionsUtils

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

fun Fragment.gotLocationPermissions(
    results: Map<String, @JvmSuppressWildcards Boolean>,
    listenerRequestPerm: ListenerRequestPerm
) {
    Log.d("!!!results.isEmpty", "${results.isNotEmpty()}")

    if (!results.all { it.value }) {
        if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
            ||
            !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            askUserForOpeningAppSettings(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            listenerRequestPerm.invoke()
        }
    }
}

fun Fragment.allLocationPermissionsGranted() = REQUIRED_LOCATION_PERMISSIONS.all {
    Log.d("!!!context", "$context")
    context?.let { context ->
        ContextCompat.checkSelfPermission(
            context, it
        )
    } == PackageManager.PERMISSION_GRANTED
}

val REQUIRED_LOCATION_PERMISSIONS: Array<String>
    get() = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).toTypedArray()


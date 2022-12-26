package com.dzenis_ska.lostandfound.ui.utils.permissionsUtils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment


fun Fragment.gotCamRecAudWESPermissions(
    results: Map<String, @JvmSuppressWildcards Boolean>,
    listenerRequestPerm: ListenerRequestPerm
) {
    Log.d("!!!results.isEmpty", "${results.all { it.value }}")

    if (!results.all { it.value }) {
        if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
//            ||
//            !shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)
//            &&
//            !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ) {
            askUserForOpeningAppSettings(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        } else {
            listenerRequestPerm.invoke()
        }
    }
}

fun Fragment.allCameraPermissionsGranted() = REQUIRED_CAMERA_PERMISSIONS.all {
    context?.let { context ->
        ContextCompat.checkSelfPermission(
            context, it
        )
    } == PackageManager.PERMISSION_GRANTED
}

val REQUIRED_CAMERA_PERMISSIONS: Array<String>
    get() = mutableListOf(
        Manifest.permission.CAMERA,
//                Manifest.permission.RECORD_AUDIO
    ).apply {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }.toTypedArray()


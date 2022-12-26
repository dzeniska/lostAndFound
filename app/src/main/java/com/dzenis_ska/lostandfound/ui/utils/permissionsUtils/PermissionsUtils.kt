package com.dzenis_ska.lostandfound.ui.utils.permissionsUtils

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.fragment.app.Fragment
import com.dzenis_ska.lostandfound.R
import com.dzenis_ska.lostandfound.ui.utils.Dialog
import com.dzenis_ska.lostandfound.ui.utils.toastL

typealias ListenerRequestPerm = () -> Unit

fun Fragment.askUserForOpeningAppSettings(vararg requiredPermissions: String) {
    val appSettingsIntent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", requireActivity().packageName, null)
    )
    if (requireActivity().packageManager.resolveActivity(appSettingsIntent, PackageManager.MATCH_DEFAULT_ONLY) == null){
        toastL(getString(R.string.permissions_denied_forever))
    } else {
        Dialog.createDialog(
            context = requireContext(),
            cancelable = false,
            header = getString(
                R.string.permission_denied_forever_header,
                requiredPermissions[0].substringAfter('.'),
                requiredPermissions[1].substringAfter('.')
            ),
            mess = getString(R.string.permission_denied_forever_message),
            btn1 = getString(R.string.open),
            btn2 = null,
            {},
            {}
        ) {
            it.run()
            startActivity(appSettingsIntent)
        }
    }
}
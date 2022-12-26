package com.dzenis_ska.lostandfound.ui.utils.photoUtils

import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LifecycleOwner

class ImagePicker(
    registry: ActivityResultRegistry,
    private val owner: LifecycleOwner,
    callback: (uri: Uri?) -> Unit
) {
    private var getContent: ActivityResultLauncher<String> =
        registry.register(RESULT_REGISTRY_KEY, owner, ActivityResultContracts.GetContent(), callback)

    fun selectImage() {
        Log.d("!!!imagePicker", "${owner} ")
        getContent.launch(MIMETYPE_IMAGES)
    }

    private companion object {
        const val MIMETYPE_IMAGES = "image/*"
        const val RESULT_REGISTRY_KEY = "pick_image"
    }
}
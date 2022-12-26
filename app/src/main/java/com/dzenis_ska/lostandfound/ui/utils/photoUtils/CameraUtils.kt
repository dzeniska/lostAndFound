package com.dzenis_ska.lostandfound.ui.utils.photoUtils

import android.annotation.SuppressLint
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

typealias LumaListener = (luma: Double) -> Unit

fun Fragment.startCamera(
    surfaceProvider: Preview.SurfaceProvider,
    callback: (imageCapture: ImageCapture) -> ImageCapture
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context!!)

    cameraProviderFuture.addListener({
        // Used to bind the lifecycle of cameras to the lifecycle owner
        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

        // Preview
        val preview = Preview.Builder()
            .build()
            .also { it.setSurfaceProvider(surfaceProvider) }

        val imageCapture = callback(
            ImageCapture.Builder()
                .build()
        )

        val imageAnalyzer = ImageAnalysis.Builder()
            .build()
            .also {
                it.setAnalyzer(
                    Executor {/*executor-> executor.run()*/ },
                    LuminosityAnalyzer { luma ->
                        Log.d("!!!imageAnalyzer2", "Average luminosity: $luma")
                    })
            }

        // Select back camera as a default
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll()
            // Bind use cases to camera
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner, cameraSelector, preview, imageCapture, imageAnalyzer
            )
        } catch (exc: Exception) {
            Log.d("!!!startCameraException", "Use case binding failed", exc)
        }

    }, ContextCompat.getMainExecutor(context!!))
}

@SuppressLint("RestrictedApi")
fun Fragment.takePhoto(imCapt: ImageCapture?, callback: (savedUri: Uri?) -> Unit) {
    // Get a stable reference of the modifiable image capture use case
    val imageCapture = imCapt ?: return
    Log.d("!!!takePhoto1", "${imageCapture}")

    // Create time stamped name and MediaStore entry.
    val name = SimpleDateFormat("dd_MM_yyyy", Locale.US)
        .format(System.currentTimeMillis()) +
            "_${System.currentTimeMillis().toString().substring(9, 13)}_image"
    Log.d("!!!takePhoto2", "${name}")

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/LostAndFound")
        }
    }
    Log.d("!!!takePhoto3", "${contentValues}")

    // Create output options object which contains file + metadata
    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(
            requireContext().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        .build()
    Log.d("!!!takePhoto4", "${outputOptions}")
    // Set up image capture listener, which is triggered after photo has
    // been taken
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(requireContext()),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.d("!!!takePhotoException", "Photo capture failed: ${exc.message}", exc)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val uri = output.savedUri
                Log.d("!!!onImageSaved", "${uri}")

                callback(uri)
            }
        }
    )
    Log.d("!!!takePhoto5", "${imageCapture.targetRotation}")

}
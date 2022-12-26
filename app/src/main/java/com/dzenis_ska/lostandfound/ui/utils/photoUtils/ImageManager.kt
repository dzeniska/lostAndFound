package com.dzenis_ska.lostandfound.ui.utils.photoUtils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.bumptech.glide.Glide
import java.io.ByteArrayOutputStream

object ImageManager {

    //    private const val MAX_IMAGE_SIZE = 1000
    private const val WIDTH = 0
    private const val HEIGHT = 1

    private fun imageRotation(uri: Uri, act: Activity): Int? {
        val inStream = try {
            act.contentResolver.openInputStream(uri)
        } catch (e: Exception) {
            null
        } ?: return null
        val exif = ExifInterface(inStream)
        return when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            else -> 0
        }
    }

    private fun getImageSize(uri: Uri, act: Activity): List<Int>? {
        val inputStream = try {
            act.contentResolver.openInputStream(uri)
        } catch (e: Exception) {
            null
        } ?: return null

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)
        val imgRotation = imageRotation(uri, act) ?: return null
        return if (imgRotation == 90 || imgRotation == 270) {
            listOf(options.outHeight, options.outWidth)
        } else {
            listOf(options.outWidth, options.outHeight)
        }
    }
    fun firebaseImageResize(
        uri: Uri,
        act: Activity,
        callback: (byteArray: ByteArray?) -> Unit
    ) {

        val bitmap = try {
            Glide.with(act)
                .asBitmap()
                .load(uri)
                .centerCrop()
                .submit()
                .get()
        } catch (e: Exception) {
            Log.d("!!!imageSize1", "${e.message}")
//            callback(null)
            null
        }

        val out = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 10, out)
            val byteArray = out.toByteArray()
            callback(byteArray)
    }

    fun imageResize(
        imageSize: Int,
        uri: Uri,
        act: Activity, callback: (byteArray: ByteArray?) -> Unit
    ) {
        val tempList: List<Int>


        val size = getImageSize(uri, act)
        Log.d("!!!getImageSize", "${size} _ }")
        if (size == null) {
            callback(null)
            return
        }

        val imageRatio = size[WIDTH].toDouble() / size[HEIGHT].toDouble()
        tempList = if (imageRatio > 1) {
            if (size[WIDTH] > imageSize) {
                listOf(imageSize, (imageSize / imageRatio).toInt())
            } else {
                listOf(size[WIDTH], size[HEIGHT])
            }
        } else {
            if (size[HEIGHT] > imageSize) {
                listOf((imageSize * imageRatio).toInt(), imageSize)
            } else {
                listOf(size[WIDTH], size[HEIGHT])
            }
        }

        val rotationDegree = imageRotation(uri, act)
        if (rotationDegree == null) {
            callback(null)
            return
        }
        Log.d("!!!imageSize", "${rotationDegree}___${tempList[WIDTH]}___${tempList[HEIGHT]}")

         try {
//            : You must call this method on a background thread
            callback(
                Glide.with(act)
                    .`as`(ByteArray::class.java)
                    .load(uri)
                    .encodeQuality(100)
                    .transform()
                    .submit(tempList[WIDTH], tempList[HEIGHT])
                    .get()
            )

//            val bitmap = Picasso.get()
//                .load(uri)
//                .resize(tempList[WIDTH], tempList[HEIGHT])
//                .rotate(rotationDegree.toFloat())
//                .get()
//
//            val out = ByteArrayOutputStream()
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
//            val byteArray = out.toByteArray()
//            callback(byteArray)

        } catch (e: Exception) {
            Log.d("!!!imageSize1", "${e.message}")
            callback(null)
        }
    }

    suspend fun getPreloadImageBitMap(context: Context, uri: Uri, bitMap: (Bitmap?) -> Unit) {
        try {
            bitMap(
                Glide.with(context)
                    .asBitmap()
                    .load(uri)

                    .centerCrop()
                    .encodeQuality(100)
                    .transform()
                    .submit(100, 100)
                    .get()
            )
        } catch (e: Exception) {
            Log.d("!!!imageSize1", "${e.message}")
            bitMap(null)
        }
    }
}


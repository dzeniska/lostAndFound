package com.dzenis_ska.lostandfound.ui.utils.map

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.dzenis_ska.lostandfound.R
import com.dzenis_ska.lostandfound.databinding.CustomMarkerLayoutBinding
import com.dzenis_ska.lostandfound.ui.fragments.add_application.Category
import com.dzenis_ska.lostandfound.ui.fragments.map.MarkerContent
import com.dzenis_ska.lostandfound.ui.utils.photoUtils.ImageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object CustomMarker {

    /*suspend*/ fun createCustomMarker(
        context: Context,
        markerContent: MarkerContent,
        callback:()->Unit
    ): Bitmap? /*= withContext(Dispatchers.IO) */{

        val binding =
            CustomMarkerLayoutBinding.inflate((context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater))
        when (markerContent.category) {
            Category.LOST.toString() ->
                binding.relativeLayout.background =
                    context.resources.getDrawable(R.drawable.ic_lost_png)
            Category.FOUND.toString() ->
                binding.relativeLayout.background =
                    context.resources.getDrawable(R.drawable.ic_found_png)
        }

        if (markerContent.uriMarkerPhoto != null) {
            val bitMap = try {
                Glide.with(context)
                    .asBitmap()
                    .load(markerContent.uriMarkerPhoto)
                    .placeholder(context.resources.getDrawable(R.drawable.ic_broken_image))
                    .error(context.resources.getDrawable(R.drawable.ic_broken_image))
                    .circleCrop()
                    .into(500, 500)
                    .get()
            } catch (e: Exception) {
                Log.d("!!!exception", "${e.message}")
                null
            } ?: when (markerContent.category) {
                Category.LOST.toString() -> {
                    callback()
                    context.resources.getDrawable(R.drawable.ic_lost).toBitmap(150, 150, null)
                }
                else -> {
                    callback()
                    context.resources.getDrawable(R.drawable.ic_found).toBitmap(150, 150, null)
                }
            }

            binding.photoImageView.setImageBitmap(bitMap)

        } else {
            val bitMap = context.resources.getDrawable(R.drawable.ic_found).toBitmap(150, 150, null)
            binding.photoImageView.setImageBitmap(bitMap)
        }

        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        binding.root.layoutParams = ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT)
        binding.root.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        binding.root.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        binding.root.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(
            binding.root.measuredWidth,
            binding.root.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        binding.root.draw(canvas)
        return/*@withContext*/ bitmap
    }
}
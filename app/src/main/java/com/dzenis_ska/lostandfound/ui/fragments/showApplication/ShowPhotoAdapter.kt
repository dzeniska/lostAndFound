package com.dzenis_ska.lostandfound.ui.fragments.showApplication

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.dzenis_ska.lostandfound.R
import com.dzenis_ska.lostandfound.databinding.ItemShowPhotoAdapterBinding
import kotlinx.coroutines.Job


typealias ShowPhotoListener = (position: String) -> Unit
typealias CancelJobListener = (run: Runnable) -> Unit


class ShowPhotoAdapter(
    private val choosePhotoListener: ShowPhotoListener
) :
    RecyclerView.Adapter<ShowPhotoAdapter.PhotoViewHolder>(),
    View.OnClickListener {
    var bitmap: Bitmap? = null
    var photos: List<Uri?> = emptyList()
        set(newValue) {
            field = newValue
            Log.d("!!!ShowPhotoAdapter", "${bitmap} _ ${field.size}")
            notifyDataSetChanged()
        }

    class PhotoViewHolder(val binding: ItemShowPhotoAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemShowPhotoAdapterBinding.inflate(inflater, parent, false)

        binding.root.setOnClickListener(this)
        binding.imPhoto.setOnClickListener(this)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photoUri = photos[position] ?: return

        with(holder.binding) {
            holder.itemView.tag = photoUri
            imPhoto.tag = photoUri
            imPreloadPhoto.setImageBitmap(bitmap)
            if (imPhoto.context.isAvailable()) {
                if (photoUri.toString().isNotBlank()) {
                    pb.isVisible = true
                    Glide.with(imPhoto.context)
                        .load(photoUri)
                        .centerCrop()
                        .listener(object : RequestListener<Drawable>{
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
//                                imPreloadPhoto.isVisible = false
//                                pb.isVisible = false
                                return false                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                imPreloadPhoto.isVisible = false
                                pb.isVisible = false
                                return false                            }
                        })
                        .placeholder(R.drawable.ic_wait)
//                        .error(R.drawable.ic_broken_image)
                        .into(imPhoto)
                } else {
                    pb.isVisible = false
                    Glide.with(imPhoto.context)
                        .clear(imPhoto)
                    imPhoto.setImageResource(R.drawable.ic_broken_image)
                    imPhoto.alpha = 0.1f
                }
            }
        }
    }

    override fun getItemCount(): Int {
        Log.d("!!!getItemCount", "${photos.size}")
        return photos.size
    }

    override fun onClick(view: View) {
        val uri = view.tag as Uri
        val position = photos.indexOfFirst { it == uri }
        when (view.id) {
            R.id.imPhoto -> {
                choosePhotoListener.invoke(uri.toString())
            }
            else -> {
                choosePhotoListener.invoke(uri.toString())
            }
        }
    }
}

fun Context?.isAvailable(): Boolean {
    if (this == null) {
        return false
    } else if (this !is Application) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (this is FragmentActivity) {
                return !this.isDestroyed
            } else if (this is Activity) {
                return !this.isDestroyed
            }
        }
    }
    return true
}
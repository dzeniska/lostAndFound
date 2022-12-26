package com.dzenis_ska.lostandfound.ui.fragments.add_application

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import android.graphics.drawable.Drawable
import com.dzenis_ska.lostandfound.R
import com.dzenis_ska.lostandfound.databinding.ItemChoosePhotoAdapterBinding

interface ChoosePhotoListener {
    fun choosePhoto(position: Int)
    fun deletePhoto(position: Int)
    fun showScalablePhoto(position: Int)
}

class ChoosePhotoAdapter(private val choosePhotoListener: ChoosePhotoListener) :
    RecyclerView.Adapter<ChoosePhotoAdapter.PhotoViewHolder>(),
    View.OnClickListener {

    var photos: List<Uri> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    class PhotoViewHolder(val binding: ItemChoosePhotoAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemChoosePhotoAdapterBinding.inflate(inflater, parent, false)
        binding.root.setOnClickListener(this)
        binding.ibEditPhoto.setOnClickListener(this)
        binding.ibDeletePhoto.setOnClickListener(this)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photoUri = photos[position]
        with(holder.binding) {
            holder.itemView.tag = photoUri
            ibEditPhoto.tag = photoUri
            ibDeletePhoto.tag = photoUri
                if (photoUri.toString().isNotBlank()) {
                    pb.isVisible = true
                    Glide.with(imAddPhoto.context)
                        .load(photoUri)
                        .centerCrop()
                        .listener(object : RequestListener<Drawable?> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                m: Any?,
                                t: Target<Drawable?>?,
                                isFR: Boolean
                            ): Boolean {
                                groupBtn.isVisible = true
                                pb.isVisible = false
                                return false
                            }

                            override fun onResourceReady(
                                r: Drawable?,
                                m: Any?,
                                t: Target<Drawable?>?,
                                d: DataSource?,
                                i: Boolean
                            ): Boolean {
                                groupBtn.isVisible = true
                                pb.isVisible = false
                                return false
                            }
                        })
                        .placeholder(R.drawable.ic_wait)
                        .error(R.drawable.ic_broken_image)
                        .into(imAddPhoto)
                } else {
                    groupBtn.isVisible = false
                    Glide.with(imAddPhoto.context)
                        .clear(imAddPhoto)
                    imAddPhoto.setImageResource(R.drawable.ic_add_photo)
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
            R.id.ibEditPhoto -> {
                choosePhotoListener.choosePhoto(position)
            }
            R.id.ibDeletePhoto -> {
                choosePhotoListener.deletePhoto(position)
            }
            else -> {
                if (uri.toString().isNotBlank()) {
                    choosePhotoListener.showScalablePhoto(position)
                } else {
                    choosePhotoListener.choosePhoto(position)
                }
            }
        }
    }
}
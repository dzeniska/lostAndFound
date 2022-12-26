package com.dzenis_ska.lostandfound.ui.fragments.map

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dzenis_ska.lostandfound.R
import com.dzenis_ska.lostandfound.databinding.ItemLayoutForPhotoAdapterBinding
import com.dzenis_ska.lostandfound.ui.fragments.add_application.Category
import com.dzenis_ska.lostandfound.ui.fragments.showApplication.isAvailable
import com.dzenis_ska.lostandfound.ui.utils.hide
import com.dzenis_ska.lostandfound.ui.utils.show

typealias PreviewAdapterListener = (key: String) -> Unit

class PreviewAdapter(
    private val previewAdapterListener: PreviewAdapterListener
): RecyclerView.Adapter<PreviewAdapter.PreviewHolder>(), View.OnClickListener {

    var isClickableItem = false

    var listPreview: List<PreviewAdapterClass> = emptyList()
        set(newValue) {

            val diffCallback = PreviewDiffCallback(field, newValue)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            field = newValue
            Log.d("!!!ShowPhotoAdapter", "${field.size}")
            notifyDataSetChanged()
            diffResult.dispatchUpdatesTo(this)
        }

    class PreviewHolder(val binding: ItemLayoutForPhotoAdapterBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLayoutForPhotoAdapterBinding.inflate(inflater, parent, false)
        binding.root.setOnClickListener(this)
//        binding.ifvPhoto.setOnClickListener(this)
        return PreviewHolder(binding)
    }

    override fun onBindViewHolder(holder: PreviewHolder, position: Int) {
        val photoUri = listPreview[position].uri
        val key = listPreview[position].key
        val category = listPreview[position].category

        with(holder.binding) {
            ifvPhoto.tag = key
            root.tag = key
            when (category) {
                Category.FOUND.toString() -> cl.background = ResourcesCompat.getDrawable(cl.context.resources, R.drawable.background_item_adapter_found, null)
                else -> cl.background = ResourcesCompat.getDrawable(cl.context.resources, R.drawable.background_item_adapter_lost, null)
            }
            if (ifvPhoto.context.isAvailable()) {
                if (photoUri.toString().isNotBlank()) {
                    progressBar.show()
                    ivImagePreload.show()
                    Glide.with(ifvPhoto.context)
                        .load(photoUri)
                        .centerCrop()
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                progressBar.hide()
//                                imPreloadPhoto.isVisible = false
//                                pb.isVisible = false
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                progressBar.hide()
                                ivImagePreload.hide()
                                return false
                            }
                        })
//                        .placeholder(R.drawable.ic_wait)
//                        .error(R.drawable.ic_broken_image)
                        .into(ifvPhoto)
                } else {
                    progressBar.isVisible = false
                    Glide.with(ifvPhoto.context)
                        .clear(ifvPhoto)
                    ifvPhoto.setImageResource(R.drawable.ic_broken_image)
                    ifvPhoto.alpha = 0.1f
                }
            }
        }
    }

    override fun getItemCount(): Int = listPreview.size

    override fun onClick(view: View) {
        if (!isClickableItem) return
        val key = view.tag as String
        when (view.id) {
                R.id.ifvPhoto -> {
                    Log.d("!!!onClick", "R.id.ifvPhoto")
                    previewAdapterListener.invoke(key)
                }
            else -> {
                previewAdapterListener.invoke(key)
                Log.d("!!!onClick", "else")
            }
        }

    }
}

data class PreviewAdapterClass(
    val key: String,
    val uri: Uri,
    val category: String,
){
    companion object {
        fun fromMarkerContent(markerContent: MarkerContent) = PreviewAdapterClass(
            key = markerContent.key,
            uri = markerContent.uriMarkerPhoto.toUri(),
            category = markerContent.category
        )
    }
}

class PreviewDiffCallback(
    private val oldList: List<PreviewAdapterClass>,
    private val newList: List<PreviewAdapterClass>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldUser = oldList[oldItemPosition]
        val newUser = newList[newItemPosition]
        return oldUser.key == newUser.key
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldUser = oldList[oldItemPosition]
        val newUser = newList[newItemPosition]
        return oldUser.key == newUser.key
    }

}
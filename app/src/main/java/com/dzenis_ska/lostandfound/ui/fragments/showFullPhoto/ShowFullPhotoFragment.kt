package com.dzenis_ska.lostandfound.ui.fragments.showFullPhoto

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dzenis_ska.lostandfound.R
import com.dzenis_ska.lostandfound.databinding.FragmentShowFullPhotoBinding
import com.dzenis_ska.lostandfound.ui.utils.*

class ShowFullPhotoFragment: Fragment(R.layout.fragment_show_full_photo) {
    private val argsSFP: ShowFullPhotoFragmentArgs by navArgs()
    private val viewModel: ShowFullPhotoViewModel by viewModels { factory(imageUrl = argsSFP.photoUrl) }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentShowFullPhotoBinding.inflate(inflater, container, false).also { binding->
        setTranslucentStatusAndNavigation(true)
        viewModel.photoUri.observe(viewLifecycleOwner) { uri ->
            Log.d("!!!photoUri", "$uri")
            binding.progressBarSHowFullPhoto.show()
            Glide.with(requireContext())
                .load(argsSFP.photoUrl)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressBarSHowFullPhoto.hide()
                        binding.errorImage.show()
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
                        binding.progressBarSHowFullPhoto.hide()
                        return false }
                })
                .placeholder(R.drawable.ic_wait)
                .into(binding.imgOnePhoto)
        }
        binding.imgOnePhoto.setOnClickListener {
            navigator().popBackStack()
        }
    }.root
}
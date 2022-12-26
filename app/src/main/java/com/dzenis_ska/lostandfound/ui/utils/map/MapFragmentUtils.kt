package com.dzenis_ska.lostandfound.ui.utils.map

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dzenis_ska.lostandfound.R
import com.dzenis_ska.lostandfound.databinding.CustomDrawerMenuLayoutBinding
import com.dzenis_ska.lostandfound.databinding.FragmentMapBinding
import com.dzenis_ska.lostandfound.databinding.LoadingMarkersBinding
import com.dzenis_ska.lostandfound.ui.fragments.map.MapFragment
import com.dzenis_ska.lostandfound.ui.fragments.map.getState
import com.dzenis_ska.lostandfound.ui.utils.hide
import com.dzenis_ska.lostandfound.ui.utils.show
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UseCompatLoadingForDrawables")
fun MapFragment.onMenuItemClick(root: ViewGroup,
                                ibLostClick: suspend ()->Unit,
                                ibFoundClick: ()->Unit,
                                ibAddApplicationClick: ()->Unit,
                                ibRegistrationClick: ()->Unit,
                                loginLayoutClick: ()->Unit,
) {
    val bindingDrawer = CustomDrawerMenuLayoutBinding.bind(root)

    with(bindingDrawer) {
        ibLost.setOnClickListener {
            setIBBackgroundNormal(root)
            ibLost.background = resources.getDrawable(R.drawable.custom_button_background_pressed, context?.theme)
            CoroutineScope(Dispatchers.Main).launch {
                delay(500)
                ibLostClick()
            }
        }
        ibFound.setOnClickListener {
            setIBBackgroundNormal(root)
            ibFound.background =
                resources.getDrawable(R.drawable.custom_button_background_pressed, context?.theme)
            ibFoundClick()
        }
        ibAddApplication.setOnClickListener {
            setIBBackgroundNormal(root)
            ibAddApplication.background =
                resources.getDrawable(R.drawable.custom_button_background_pressed, context?.theme)
            ibAddApplicationClick()
        }
        ibLogin.setOnClickListener {
            setIBBackgroundNormal(root)
            ibLogin.background =
                resources.getDrawable(R.drawable.custom_button_background_pressed, context?.theme)
            ibRegistrationClick()
        }

        loginPhotoLayout.setOnClickListener {
            setIBBackgroundNormal(root)
            loginPhotoLayout.background =
                resources.getDrawable(R.drawable.custom_button_background_pressed, context?.theme)
            loginLayoutClick()
        }
    }
}

@SuppressLint("UseCompatLoadingForDrawables")
private fun MapFragment.setIBBackgroundNormal(root: ViewGroup) {
    root.children.forEach { ch1 ->
        Log.d("!!!ibFound2", "${(ch1::class.java).name}")
        if (ch1 is ViewGroup) ch1.children.forEach { ch2 ->
            Log.d("!!!ibFound1", "${(ch2::class.java).name}")
            if (ch2 is ViewGroup)
                (ch2).children.forEach { ib ->
                    Log.d("!!!ibFound", "${(ib::class.java).name}")
                    if (ib is ImageButton || ib is SignInButton) ib.background =
                        resources.getDrawable(
                            R.drawable.custom_button_background_normal,
                            context?.theme
                        )
                }
        }
    }
}

fun MapFragment.stateLoadingLayout(bindingLoader: LoadingMarkersBinding, isVisible: Boolean, progress: Float) {
    bindingLoader.apply {
        pb.isVisible = isVisible
        tvLoading.isVisible = isVisible
        percentageProgress.isVisible = isVisible
    }
    (bindingLoader.percentageProgress.layoutParams as ConstraintLayout.LayoutParams)
        .matchConstraintPercentWidth = progress
    bindingLoader.percentageProgress.requestLayout()

}

@SuppressLint("UseCompatLoadingForDrawables")
fun MapFragment.initDrawerLayoutItems(root: View) {
    val binding = CustomDrawerMenuLayoutBinding.bind(root)
    if (getState()?.isItemLostPressed == true)
        binding.ibLost.background =
            resources.getDrawable(R.drawable.custom_button_background_pressed, context?.theme)
    if (getState()?.isItemFoundPressed == true)
        binding.ibFound.background =
            resources.getDrawable(R.drawable.custom_button_background_pressed, context?.theme)
    if (getState()?.isItemAddApplicationPressed == true)
        binding.ibAddApplication.background =
            resources.getDrawable(R.drawable.custom_button_background_pressed, context?.theme)
    if (getState()?.isItemLoginPressed == true)
        binding.ibLogin.background =
            resources.getDrawable(R.drawable.custom_button_background_pressed, context?.theme)
    if (getState()?.isItemLoginPhotoLayoutPressed == true)
        binding.loginPhotoLayout.background =
            resources.getDrawable(R.drawable.custom_button_background_pressed, context?.theme)
}

fun MapFragment.initDrawerUserPhoto(root: View, currentUser: FirebaseUser?) {
    val binding = CustomDrawerMenuLayoutBinding.bind(root)
    Log.d("!!!initDrawerUserPhoto1", "${currentUser?.email}")
    Log.d("!!!initDrawerUserPhoto2", "${currentUser?.photoUrl}")
    Log.d("!!!initDrawerUserPhoto3", "${currentUser?.uid}")
    if (currentUser?.email.isNullOrEmpty()) {
        binding.ibLogin.show()
        binding.loginPhotoLayout.hide()
        return
    }
    binding.ibLogin.hide()
    binding.loginPhotoLayout.show()

    Glide.with(requireContext())
        .load(Firebase.auth.currentUser?.photoUrl)
        .centerCrop()
        .placeholder(R.drawable.ic_wait)
        .error(R.drawable.ic_account_default)
        .into(binding.ivPhoto)
    binding.tvEmail.text = currentUser?.email
}

fun MapFragment.getSignInGoogleClient(): GoogleSignInClient {
    Log.d("!!!getSignInGoogleClient1", "onDestroyView")

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    Log.d("!!!getSignInGoogleClient2", "${gso}")

    return GoogleSignIn.getClient(requireContext(), gso)
}
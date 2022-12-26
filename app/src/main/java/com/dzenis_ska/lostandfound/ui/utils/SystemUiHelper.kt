package com.dzenis_ska.lostandfound.ui.utils

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.dzenis_ska.lostandfound.R

fun Fragment.hideStatusBar() {
    val activity = requireActivity()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        activity.window.insetsController?.let {
            it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            it.hide(WindowInsets.Type.statusBars())
        }
    } else {
        @Suppress("DEPRECATION")
        activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }
}

fun Fragment.showStatusBar() {
    val activity = requireActivity()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        activity.window.setDecorFitsSystemWindows(false)
        activity.window.insetsController?.show(WindowInsets.Type.statusBars())
    } else {
        @Suppress("DEPRECATION")
        activity.window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
    }
}

fun Fragment.hideSystemUI(view: View) {
    WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
    WindowInsetsControllerCompat(requireActivity().window, view).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

fun Fragment.setTranslucentNavigation(isTranslucent: Boolean) {
    @Suppress("DEPRECATION")
    if (Build.VERSION.SDK_INT in Build.VERSION_CODES.KITKAT..Build.VERSION_CODES.R) {
        if (isTranslucent) {
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            )
        } else {
            requireActivity().window.clearFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            )
        }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        requireActivity().setTranslucent(isTranslucent)
    }
}

fun Fragment.setTranslucentStatusAndNavigation(isTranslucent: Boolean) {
    Log.d("!!!setTranslucentStatusAndNavigation", "isTranslucent = ${isTranslucent}")
    @Suppress("DEPRECATION")
    if (Build.VERSION.SDK_INT in Build.VERSION_CODES.KITKAT..Build.VERSION_CODES.R) {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        if (isTranslucent) {
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            )
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            )
        } else {
            requireActivity().window.clearFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            )
            requireActivity().window.clearFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            )
        }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        requireActivity().setTranslucent(isTranslucent)
    }
}

fun Fragment.isPortrait(): Boolean {
    return if (context != null)
        resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    else
        false
}

    fun View.hide() {
        isVisible = false
    }

    fun String?.notBlank(textView: TextView, alternative: ()-> String) {
        if (!this.isNullOrBlank()) textView.text = this.toString()
        else textView.text = alternative.invoke()
    }

    fun TextView.setText(text1: String?, text2: String){
        if (!text1.isNullOrBlank()) this.text = text1
        else this.text = text2
    }

    fun View.show() {
        isVisible = true
    }

    fun View.showAndHide() {
        isVisible = !isVisible
    }

    fun View.animateShow(){
        val animate = AnimationUtils.loadAnimation(context, R.anim.alpha_from_0_to_1)
        this.startAnimation(animate)
    }

    fun View.setVisibility(isVisible: Boolean) {
        if (isVisible) show() else hide()
    }

    fun View.setTint(colorId: Int) {
        ImageViewCompat.setImageTintList(
            this as ImageView,
            ColorStateList.valueOf(ContextCompat.getColor(context, colorId))
        )
    }

    fun View.callOnClick() {
        performClick();
        isPressed = true;
        invalidate();
        isPressed = false;
        invalidate();
    }



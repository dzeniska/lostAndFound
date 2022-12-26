package com.dzenis_ska.lostandfound.ui.utils

import android.app.Activity
import android.content.Context
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.dzenis_ska.lostandfound.databinding.CustomProgressBarBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.parcelize.Parcelize

object CustomSnackBar {
    private var snackBar: Snackbar? = null


    fun createSnackBar(context: Context, v: View, customProgressBarBinding: CustomProgressBarBinding, info: String, countDownTimer: String, dismiss: (Boolean)->Unit){
        Log.d("!!!createSnackBar0", "${snackBar} _ ${snackBar?.context != context} _ $countDownTimer")

        if (snackBar?.context != context) {
            snackBar = Snackbar.make(v, info, Snackbar.LENGTH_INDEFINITE)
            val contentLayout =
                snackBar!!.view.findViewById<View>(com.google.android.material.R.id.snackbar_text).parent as ViewGroup

            snackBar!!.setAction("Отмена") {
                dismiss(false)
                snackBarClear()
            }
            Log.d("!!!createSnackBar4", "${snackBar} _ ${customProgressBarBinding.root}")
//            if(contentLayout.childCount > 0)

            contentLayout.addView(customProgressBarBinding.root)
            Log.d("!!!createSnackBar5", "${snackBar} _ ${customProgressBarBinding}")
            snackBar!!.show()
        }
        if (countDownTimer == "-1") {
            dismiss(true)
            snackBarClear()
        }
        Log.d("!!!createSnackBar2", "${snackBar} _")

        if (snackBar != null && countDownTimer != "-1")
            customProgressBarBinding.tvCountDownTimer.text = countDownTimer
    }

    private fun snackBarClear(){
        snackBar!!.dismiss()

        snackBar = null
    }

}

data class SimpleSnackBar<T>(
    val clazz: T?,
    val info: String,
    val mess: String?,
    val countDownTimer: String
)
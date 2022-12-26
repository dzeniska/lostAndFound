package com.dzenis_ska.lostandfound.ui.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun Fragment.toastS(mess: String) =
    activity?.let { Toast.makeText(it, mess, Toast.LENGTH_SHORT).show() }
fun AppCompatActivity.toastS(mess: String) =
    this.let { Toast.makeText(it, mess, Toast.LENGTH_SHORT).show() }

fun Fragment.toastL(mess: String) =
    activity?.let { Toast.makeText(it, mess, Toast.LENGTH_LONG).show() }

fun Fragment.snackBarL(v: View, mess: String) =
    context?.let {
        Snackbar.make(v, mess, Snackbar.LENGTH_LONG)
//        .setAnchorView(R.id.fab)
//            .setAction("oK!", null)
            .show()
    }

fun Fragment.getColor(color: Int): Int {
    return this.getColor(color)
}

fun Fragment.getStr(id: Int): String {
    return this.getString(id)
}


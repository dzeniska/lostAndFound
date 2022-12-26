package com.dzenis_ska.lostandfound.ui.utils.showApplication

import android.annotation.SuppressLint
import android.content.Intent
import androidx.core.net.toUri
import com.dzenis_ska.lostandfound.ui.fragments.showApplication.ShowApplicationFragment
import java.text.SimpleDateFormat

fun ShowApplicationFragment.callTelNum(telNum: String) {
    val callUri = "tel:${telNum.replace("\\s".toRegex(), "")}"
    val intentCall = Intent(Intent.ACTION_CALL)
    intentCall.data = callUri.toUri()
    startActivity(intentCall)
}

@SuppressLint("SimpleDateFormat")
fun ShowApplicationFragment.showData(timeMillis: Long) = SimpleDateFormat(DataFormat.format).format(timeMillis)

object DataFormat{
    const val format = "dd.MM.yyyy"
}
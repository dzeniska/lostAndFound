package com.dzenis_ska.lostandfound.ui.utils.addApplication

import android.app.AlertDialog
import android.net.Uri
import android.util.Log
import com.dzenis_ska.lostandfound.databinding.DialogLoadApplicationBinding
import com.dzenis_ska.lostandfound.ui.fragments.add_application.AddApplicationFragment
import com.dzenis_ska.lostandfound.ui.fragments.add_application.getState
import com.dzenis_ska.lostandfound.ui.utils.photoUtils.ImageManager

fun AddApplicationFragment.createUploadDialog() {
    val builder = AlertDialog.Builder(context)
    Log.d("!!!createUploadDialog1", "$builder")

    this.dialogBinding = DialogLoadApplicationBinding.inflate(layoutInflater)
    Log.d("!!!dialogBinding1", "${dialogBinding}")

    Log.d("!!!createUploadDialog2", "$dialogBinding")
    val view = dialogBinding!!.root
    Log.d("!!!createUploadDialog3", "$view")

    builder.setView(view)
    Log.d("!!!createUploadDialog4", "$builder")

    this.dialog = builder.create()
    Log.d("!!!createUploadDialog5", "$builder")

    this.dialog!!.setCancelable(false)
    Log.d("!!!createUploadDialog6", "$builder")

    this.dialog!!.show()
    Log.d("!!!createUploadDialog7", "$builder")

}
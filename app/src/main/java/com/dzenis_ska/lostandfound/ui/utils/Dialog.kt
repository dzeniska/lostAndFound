package com.dzenis_ska.lostandfound.ui.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.SimpleAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.viewbinding.ViewBinding
import com.dzenis_ska.lostandfound.R
import com.dzenis_ska.lostandfound.databinding.*
import java.io.IOException
import java.util.*

typealias DialogListener = (run: Runnable) -> Unit
typealias BlockAnn = (mess: String, run: Runnable?) -> Unit

object Dialog {
    fun createDialog(
        context: Context,
        cancelable: Boolean,
        header: String?,
        mess: String?,
        btn1: String?,
        btn2: String?,
        buttonOnePressed: () -> Unit,
        buttonTwoPressed: () -> Unit,
        dialogListener: DialogListener?,
    ) {
        val builder = AlertDialog.Builder(context)
        val rootDialogElement = DialogBinding.inflate((context as Activity).layoutInflater)
        val view = rootDialogElement.root
        builder.setView(view)
        val dialog = builder.create()
        dialog.setCancelable(cancelable)
        dialog.show()

        rootDialogElement.apply {
            if (header != null) tvHeader.text = header else tvHeader.hide()
            if (mess != null) tvMessage.text = mess else tvMessage.hide()
            if (btn1 != null) tv10.text = btn1 else groupBtn10.hide()
            if (btn2 != null) tv20.text = btn2 else groupBtn20.hide()

            btn10.setOnClickListener {
                dialogListener?.invoke() {
                    dialog.dismiss()
                }
                buttonOnePressed()
            }
            btn20.setOnClickListener {
                dialogListener?.invoke() {
                    dialog.dismiss()
                }
                buttonTwoPressed()
            }
        }
    }

    fun createDialogNoRules(context: Context, blockAnn: BlockAnn) {
        val builder = AlertDialog.Builder(context)
        val rootDialogElement = DialogBlockAnnBinding.inflate((context as Activity).layoutInflater)
        val view = rootDialogElement.root
        builder.setView(view)
        val dialog = builder.create()
        dialog.show()

        rootDialogElement.apply {
            val listRestrictedContent = context.resources.getStringArray(R.array.restricted_content).map { mapOf("key" to it) }
            val adapter = SimpleAdapter(
                context,
                listRestrictedContent,
                android.R.layout.simple_list_item_1,
                arrayOf("key"),
                intArrayOf(android.R.id.text1)
            )
            listView.adapter = adapter
            listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                val value = listRestrictedContent[position].getValue("key")
                etCause.hint = value
            }
            btn1.setOnClickListener {
//                if (etCause.text.isNotBlank()){
                val text = etCause.text.ifBlank { etCause.hint }
                pb.show()
                btn1.visibility = View.INVISIBLE
                dialog.dismiss()
                blockAnn.invoke(text.toString()) {
                    dialog.dismiss()
                }
//                } else {
//                    (context as AppCompatActivity).toastS((context.resources.getString(R.string.do_not_enter_char)))
//                }
            }
        }
    }

    fun createInfoDialog(context: Context, btn1Text: String? = null, btn1Pressed: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        val rootDialogElement = DialogInfoBinding.inflate((context as Activity).layoutInflater)
        val view = rootDialogElement.root
        builder.setView(view)
        val dialog = builder.create()
        dialog.setCancelable(true)
        dialog.show()

        rootDialogElement.apply {
            btn1Text?.let { tv1.text = it}
            btn1.setOnClickListener {
                btn1Pressed()
                dialog.dismiss()
            }

            try {
                Log.d("!!!try", "error: ${Locale.getDefault().language}")
                val way = when (Locale.getDefault().language.toString()) {
                    "en" -> "file:///android_asset/lost_and_found_rules_en.html"
                    else -> "file:///android_asset/lost_and_found_rules.html"
                }
                Locale.getDefault().language
                webView.loadUrl(way)
                webView.settings.loadWithOverviewMode = true
                webView.settings.useWideViewPort = true

            } catch (e: IOException) {
                Log.d("!!!readText", "error: ${e.message}")
            }
        }
    }

    fun createDialogAppWasDisabled(
        context: Context,
        messHeader: String? = null,
        mess: String? = null,
        blockAnn: BlockAnn
    ) {
        val builder = AlertDialog.Builder(context)
        val rootDialogElement =
            DialogApplicationWasDeletedBinding.inflate((context as Activity).layoutInflater)
        val view = rootDialogElement.root
        builder.setView(view)
        messHeader?.let { rootDialogElement.tvHeader.text = it }
        mess?.let {
            rootDialogElement.tvMess.isVisible = true
            rootDialogElement.tvMess.text = it
        }
        rootDialogElement.tvMess.text = mess
        val dialog = builder.create()
        dialog.show()
        blockAnn.invoke("") {
            dialog.dismiss()
        }
    }

    fun createDialogWasBlock(context: Context) {
        val builder = AlertDialog.Builder(context)
        val rootDialogElement =
            DialogApplicationWasDeletedBinding.inflate((context as Activity).layoutInflater)
        val view = rootDialogElement.root
        builder.setView(view)
        val dialog = builder.create()
        dialog.show()
    }

    class SimpleDialog(
        val header: String = "",
        val message: String? = null
    )
}
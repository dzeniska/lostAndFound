package com.dzenis_ska.lostandfound.ui.fragments.chat

import android.util.Log
import android.view.*
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.dzenis_ska.lostandfound.R
import com.dzenis_ska.lostandfound.databinding.ItemAdapterApponentMessageBinding
import com.dzenis_ska.lostandfound.databinding.ItemAdapterMyMessageBinding
import com.dzenis_ska.lostandfound.databinding.ItemAdapterTimeBinding
import com.dzenis_ska.lostandfound.ui.db.firebase.cloudFirestore.classes.Messages
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

interface EditMessageListener {
    fun editMessage(message: Messages.MyMessage)
    fun deleteMessage(message: Messages.MyMessage)
    fun editPhoto(message: Messages.MyMessage)
    fun deletePhoto(message: Messages.MyMessage)
}

class ChatAdapter(private val editMessageListener: EditMessageListener) :
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>(),
    View.OnClickListener,
    View.OnLongClickListener
{

    var listMessages: List<Messages> = listOf()
        set(newValue) {
//            val diffResult = DiffUtil.calculateDiff(DiffUtilMessage(field , newValue))
//            diffResult.dispatchUpdatesTo(this)
            field = newValue
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int): Int {
        return when(listMessages[position]){
            is Messages.MyMessage -> MY_MESSAGE
            is Messages.TimeSpace -> TIME_SPACE
            is Messages.HisMessage -> HIS_MESSAGE
//            else -> TIME_SPACE
        }
    }

    override fun onLongClick(v: View): Boolean{
        when (v.id) {
//            R.id.ivImageMessMy -> {
//                showPopupMenuPhoto(v)
//            }
//            R.id.tvMessageMy -> {
//                showPopupMenu(v)
//            }
        }
        return true
    }

    override fun getItemCount(): Int = listMessages.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = when(viewType){
            MY_MESSAGE -> ItemAdapterMyMessageBinding.inflate(inflater, parent, false)
            TIME_SPACE -> ItemAdapterTimeBinding.inflate(inflater, parent, false)
            HIS_MESSAGE -> ItemAdapterApponentMessageBinding.inflate(inflater, parent, false)
            else -> throw IllegalArgumentException("Invalid view type")
        }

        binding.root.setOnLongClickListener(this)

        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {

        val message = listMessages[position]



        with(holder) {
            when (message){
                is Messages.MyMessage -> {
                    binding as ItemAdapterMyMessageBinding
                    if(!message.isRead){
                        binding.tvMessageMy.tag = message
                        binding.tvMessageMy.setOnLongClickListener(this@ChatAdapter)
                        binding.ivImageMessMy.tag = message
                        binding.ivImageMessMy.setOnLongClickListener(this@ChatAdapter)
//                        binding.root.tag = message
                    } else {
                        binding.tvMessageMy.tag = null
                        binding.tvMessageMy.setOnLongClickListener(this@ChatAdapter)
                        binding.ivImageMessMy.tag = null
                        binding.ivImageMessMy.setOnLongClickListener(this@ChatAdapter)

                    }

                    if(message.message?.isNotEmpty() == true){
                        binding.tvMessageMy.isVisible = true
                        binding.tvMessageMy.text = message.message
                    } else {
                        binding.tvMessageMy.isVisible = false
                    }
                    val sdf = SimpleDateFormat("HH:mm")
                    val resultDate = Date(message.time.toLong())
                    binding.tvTime.text = sdf.format(resultDate)
                    if (message.isRead) {
                        binding.ivDone.setImageResource(R.drawable.ic_done_all)
                    } else {
                        binding.ivDone.setImageResource(R.drawable.ic_done)
                    }
                    Log.d("!!!loadImage", "${message.photoUrl}")
                    Log.d("!!!loadImage", "${message.email}")

                    if(message.photoUrl != "null"){
                        binding.progressBar.isVisible = true
                        binding.ivImageMessMy.isVisible = true

                        Log.d("!!!loadImage", "${message.photoUrl}")

//                        Picasso.get().

                        val picasso = Picasso.get()
                            .load(message.photoUrl)
                            .placeholder(R.drawable.ic_image_200_200)
//                            .error(R.drawable.ic_no_connection)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .memoryPolicy(MemoryPolicy.NO_STORE)
                            .into(binding.ivImageMessMy, object : Callback{
                                override fun onSuccess() {
                                    binding.progressBar.isVisible = false
                                }
                                override fun onError(e: Exception?) {
                                    binding.progressBar.isVisible = false
                                }
                            })

                    } else {
                        binding.progressBar.isVisible = false
                        binding.ivImageMessMy.visibility = View.GONE
                    }


                }
                is Messages.TimeSpace -> {
                    binding as ItemAdapterTimeBinding
                    val sdf = SimpleDateFormat("dd MM yyyy")
                    val resultDate = Date(message.time.toLong())
                    binding.tvTime.text = sdf.format(resultDate)
                }
                is Messages.HisMessage -> {

                    binding as ItemAdapterApponentMessageBinding
                    if(message.message?.isNotEmpty() == true){
                        binding.tvMessage.isVisible = true
                        binding.tvMessage.text = message.message
                    } else {
                        binding.tvMessage.isVisible = false
                    }
                    val sdf = SimpleDateFormat("HH:mm")
                    val resultDate = Date(message.time.toLong())
                    binding.tvTime.text = sdf.format(resultDate)

                    if(message.photoUrl != "null"){
                        binding.progressBar.isVisible = true
                        binding.ivImageMess.isVisible = true

                        Log.d("!!!loadImageapp", "${message.photoUrl} _ ${message.email}")

                        Picasso.get()
                            .load(message.photoUrl)
                            .placeholder(R.drawable.ic_image_200_200)
//                            .error(R.drawable.ic_no_connection)
                            .into(binding.ivImageMess, object : Callback{
                                override fun onSuccess() {
                                    binding.progressBar.isVisible = false
                                }
                                override fun onError(e: Exception?) {
                                    binding.progressBar.isVisible = false
                                }
                            })
                    } else {
                        binding.progressBar.isVisible = false
                        binding.ivImageMess.visibility = View.GONE
                    }
                }
            }
        }
    }

    class MessageViewHolder(val binding: ViewBinding): RecyclerView.ViewHolder(binding.root)



    private fun showPopupMenu(v: View){
        Log.d("!!!popup", "${v.tag}")
        var message: Messages.MyMessage? = null
        val popupMenu = PopupMenu(v.context, v, Gravity.CENTER)
        if(v.tag == null){
            popupMenu.menu.add(0, NO_DELETE, Menu.NONE, "Sorry!)")
        } else {
            message = v.tag as Messages.MyMessage
            popupMenu.menu.add(0, EDIT, Menu.NONE, "Edit")
            popupMenu.menu.add(0, DELETE, Menu.NONE, "Delete")
        }
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                EDIT -> {
//                    editMessage.editMessage(message!!)
                }
                DELETE -> {
//                    editMessage.deleteMessage(message!!)
                }
            }
            return@setOnMenuItemClickListener true
        }
        popupMenu.show()
    }
    private fun showPopupMenuPhoto(v: View){
        var message: Messages.MyMessage? = null
        val popupMenu = PopupMenu(v.context, v, Gravity.CENTER)
        if(v.tag == null){
            popupMenu.menu.add(0, NO_DELETE, Menu.NONE, "Sorry!)")
        } else {
            message = v.tag as Messages.MyMessage
            popupMenu.menu.add(0, EDIT, Menu.NONE, "Edit Photo")
            popupMenu.menu.add(0, DELETE, Menu.NONE, "Delete Photo")
        }
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                EDIT -> {
//                    editMessage.editPhoto(message!!)
                }
                DELETE -> {
//                    editMessage.deletePhoto(message!!)
                }
            }
            return@setOnMenuItemClickListener true
        }
        popupMenu.show()
    }

    companion object {
        private const val MY_MESSAGE = 10
        private const val TIME_SPACE = 20
        private const val HIS_MESSAGE = 30

        private const val EDIT = 1
        private const val DELETE = 2
        private const val NO_DELETE = 3


    }

    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }

}
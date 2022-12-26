package com.dzenis_ska.lostandfound.ui.utils

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dzenis_ska.lostandfound.App
import com.dzenis_ska.lostandfound.ui.activity.MainActivityViewModel
import com.dzenis_ska.lostandfound.ui.fragments.BaseViewModel
import com.dzenis_ska.lostandfound.ui.fragments.add_application.AddApplicationViewModel
import com.dzenis_ska.lostandfound.ui.fragments.baseMap.BaseMapViewModel
import com.dzenis_ska.lostandfound.ui.fragments.chat.ChatViewModel
import com.dzenis_ska.lostandfound.ui.fragments.map.MapViewModel
import com.dzenis_ska.lostandfound.ui.fragments.showApplication.ShowApplicationViewModel
import com.dzenis_ska.lostandfound.ui.fragments.showFullPhoto.ShowFullPhotoViewModel


class Event<T>(
     private val value: T
) {

//    private var _value: T? = value

//    fun get(): T? =_value.also { _value = null }

    private var handled: Boolean = false

    fun getValue(): T? {
        if (handled) return null
        handled = true
        return value
    }
}

class ViewModelFactory(
    private val app: App,
    private val id: String? = null,
    private val imageUrl: String? = null,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel = when (modelClass) {
            MainActivityViewModel::class.java -> MainActivityViewModel()
            BaseViewModel::class.java -> BaseViewModel()
            BaseMapViewModel::class.java -> BaseMapViewModel(app.fbAuth, app.fbDatabase)
            MapViewModel::class.java -> MapViewModel(app.network, app.fbAuth, app.fbDatabase,app.fbStorage, app.database)
            AddApplicationViewModel::class.java -> AddApplicationViewModel(app.fbAuth, app.fbDatabase,app.fbStorage, app.database)
            ShowApplicationViewModel::class.java -> ShowApplicationViewModel(app.fbAuth, app.fbDatabase,app.fbStorage, app.database, id = id!!)
            ShowFullPhotoViewModel::class.java -> ShowFullPhotoViewModel(imageUrl = imageUrl!!)
            ChatViewModel::class.java -> ChatViewModel(app.network, app.fbAuth, app.fbDatabase,app.fbStorage , app.fbCLoudFirestore, app.database)
            else ->
                throw IllegalStateException("Unknown view model class")
        }
        return viewModel as T
    }
}

fun Fragment.factory(id: String? = null, imageUrl: String? = null) = ViewModelFactory(requireContext().applicationContext as App, id, imageUrl)
fun Activity.factory() = ViewModelFactory(applicationContext as App)
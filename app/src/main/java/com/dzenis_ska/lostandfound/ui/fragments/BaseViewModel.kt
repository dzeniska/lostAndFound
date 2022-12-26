package com.dzenis_ska.lostandfound.ui.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.os.Parcelable
import android.util.Log
import com.dzenis_ska.lostandfound.ui.utils.CustomSnackBar
import com.dzenis_ska.lostandfound.ui.utils.Dialog.SimpleDialog
import com.dzenis_ska.lostandfound.ui.utils.Event
import com.dzenis_ska.lostandfound.ui.utils.SimpleSnackBar
import com.dzenis_ska.lostandfound.ui.utils.map.ContextActions
import kotlinx.coroutines.*
typealias MutableLiveEvent<T> = MutableLiveData<Event<T>>
typealias LiveEvent<T> = LiveData<Event<T>>

open class BaseViewModel: ViewModel() {

    open var whenContextActive = ContextActions()

    var jobSnackBar: Job? = null

    open val toastEventBase = MutableLiveData<Event<String>>()
    val toastEvent: LiveData<Event<String>> = toastEventBase

    open val dialogEventBase = MutableLiveEvent<SimpleDialog>()
    val dialogEvent: LiveEvent<SimpleDialog> = dialogEventBase

    open val dialogBlockedAppEventBase = MutableLiveEvent<SimpleDialog>()
    val dialogBlockedAppEvent: LiveEvent<SimpleDialog> = dialogBlockedAppEventBase

    open val snackBarMutableBase = MutableLiveData<SimpleSnackBar<*>?>()
    val snackBarBase: LiveData<SimpleSnackBar<*>?> = snackBarMutableBase


    private val coroutineContext = SupervisorJob() + Dispatchers.Main.immediate + CoroutineExceptionHandler { _, throwable ->
        Log.d("!!!coroutineContext", "${throwable.message} _ ${throwable}")

        // you can add some exception handling here
    }
    // custom scope which cancels jobs immediately when back button is pressed
    protected val viewModelScope = CoroutineScope(coroutineContext)

    fun clearJob(){
        snackBarMutableBase.value = null
        jobSnackBar?.cancel()
        jobSnackBar = null
    }

    override fun onCleared() {
        whenContextActive.clear()
        clearScope()
        Log.d("!!!clearJob", "clearJob1")
        clearJob()
        super.onCleared()
    }

    private fun clearScope() {
        viewModelScope.cancel()
    }

    fun <T: Parcelable?> createSnackBar(clazz: T?, info: String, mess: String? ) {
        snackBar(clazz, info, mess)
    }

    companion object {
        const val TIME_AWAIT_LOADING_7700 = 7700L
    }
}

fun BaseViewModel.toast(res: Int) {
    whenContextActive {
        toastEventBase.value = Event(it.resources.getString(res))
    }
}

fun BaseViewModel.dialog(headerRes: Int, messageRes: Int) {
    whenContextActive {
        dialogEventBase.value = Event(
            SimpleDialog(
                it.resources.getString(headerRes),
                it.resources.getString(messageRes)
            )
        )
    }
}

fun BaseViewModel.dialogWarning(headerRes: Int, message: String? = null) {
    whenContextActive {
        dialogBlockedAppEventBase.value = Event(
            SimpleDialog(
                it.resources.getString(headerRes),
                message
            )
        )
    }
}

fun <T: Parcelable?> BaseViewModel.snackBar(clazz: T?, info: String, mess: String? = null) {
    jobSnackBar = viewModelScope.launch {
        (5 downTo -2).forEach {
            if (it == -2) snackBarMutableBase.value = null
            else snackBarMutableBase.value =
                SimpleSnackBar(
                    clazz = clazz,
                    info = info,
                    mess = mess,
                    countDownTimer = it.toString()
                )
            delay(1000)
        }
    }
}

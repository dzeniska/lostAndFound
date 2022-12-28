package com.dzenis_ska.lostandfound.ui.utils

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.dzenis_ska.lostandfound.ui.fragments.add_application.AddApplicationFragmentArguments
import com.dzenis_ska.lostandfound.ui.fragments.changeLocation.ChangeLocationFragmentArguments
import com.dzenis_ska.lostandfound.ui.fragments.chat.ChatFragmentArguments
import com.dzenis_ska.lostandfound.ui.fragments.showLocation.ShowLocationFragmentArguments

typealias ResultListener<T> = (T) -> Unit

fun Fragment.navigator(): Navigator {
    return requireActivity() as Navigator
}

interface Navigator {

    fun goToCameraFragment()
    fun goToAddApplicationFragment(addApplicationFragmentArguments: AddApplicationFragmentArguments)
    fun goToAddApplicationFragmentFromShowApplicationFragment(toAddApplicationFragmentArguments: AddApplicationFragmentArguments)

    fun goToChangeLocationFragment(changeLocationFragmentArguments: ChangeLocationFragmentArguments)
    fun goToShowApplicationFragment(id: String)
    fun goToShowLocationFragment(showLocationFragmentArguments: ShowLocationFragmentArguments)
    fun goToChatFragment(chatFragmentArguments: ChatFragmentArguments)
    fun goToShowFullPhotoFragment(it: String)
    fun popBackStack()
    fun popBackStackToMapFragment()

    fun <T: Parcelable> publishResult(result: T)
    fun <T: Parcelable> listenResult(clazz: Class<T>, owner: LifecycleOwner, listener: ResultListener<T>)



}
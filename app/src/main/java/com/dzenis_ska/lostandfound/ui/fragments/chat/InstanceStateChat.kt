package com.dzenis_ska.lostandfound.ui.fragments.chat

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InstanceStateChat(
    val count: Int? = 0
): Parcelable {
}

@Parcelize
data class ChatFragmentArguments(
    val additionalInfo: String
): Parcelable
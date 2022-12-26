package com.dzenis_ska.lostandfound.ui.db.firebase.cloudFirestore.classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


sealed class Messages
    (
    val times: String,
    var readed: Boolean,
    val photo: String? = null
)
{
    @Parcelize
    data class MyMessage(
        val name: String,
        val email: String,
        val message: String?,
        @field:JvmField
        val isRead: Boolean = false,
        val time: String,
        val photoUrl: String? = null
    ): Messages(time, isRead, photoUrl), Parcelable

    @Parcelize
    data class TimeSpace(
        val time: String
    ): Messages(time, false, null), Parcelable

    @Parcelize
    data class HisMessage(
        val name: String,
        val email: String,
        val message: String?,
        @field:JvmField
        val isRead: Boolean = false,
        val time: String,
        val photoUrl: String? = null
    ): Messages(time, isRead, photoUrl), Parcelable
}

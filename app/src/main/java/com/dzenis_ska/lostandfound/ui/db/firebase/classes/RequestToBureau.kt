package com.dzenis_ska.lostandfound.ui.db.firebase.classes

import android.os.Parcelable
import com.dzenis_ska.lostandfound.ui.fragments.add_application.Category
import kotlinx.parcelize.Parcelize

@Parcelize
data class RequestToBureau(
    val key: String? = null,
    val uid: String? = null,
    val email: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val uriMarkerPhoto: String? = null,
    val photoUri: List<String> = emptyList(),
    val category: String? = null,
    val telNum: String? = null,
    val header: String? = null,
    val additionalInfo: String? = null,
    val time: String? = null,
    ) : Parcelable {

    fun toRequestForMarkerMap(): RequestForMarkerMap = RequestForMarkerMap(
//        id = 0,
        key = key ?: "ERROR",
        latitude = latitude ?: 0.001,
        longitude = longitude ?: 0.001,
        category = category ?: Category.ALL.toString(),
        uriMarkerPhoto = uriMarkerPhoto ?: "",
        time = time ?: "0"
    )

    fun createFilter() = ApplicationFilter(
        email = this.email,
        key = this.key,
        latitude = createLoc(this.latitude),
        longitude = createLoc(this.longitude),
        time = this.time,
        category = this.category
    )
    fun createLoc(loc: Double?): String{
        String.format("%.3f", loc).also {
            return if (it.startsWith('-'))
                when (it.length) {
                    7 -> "-0${it.substringAfter('-')}"
                    6 -> "-00${it.substringAfter('-')}"
                    else -> "-${it.substringAfter('-')}"
                }
            else
                when (it.length) {
                    6 -> "0$it"
                    5 -> "00$it"
                    else -> it
                }
        }
    }
}



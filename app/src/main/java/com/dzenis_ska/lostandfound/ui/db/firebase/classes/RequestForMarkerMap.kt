package com.dzenis_ska.lostandfound.ui.db.firebase.classes

import com.dzenis_ska.lostandfound.ui.db.room.marker.entities.MarkerEntity
import com.dzenis_ska.lostandfound.ui.fragments.add_application.Category

data class RequestForMarkerMap(
    val key: String = "no_key",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val category: String = Category.LOST.toString(),
    val uriMarkerPhoto: String = "",
    val time: String = "0"
) {
    fun toMarkerEntity(): MarkerEntity = MarkerEntity(
//        id = 0,
        key = key,
        latitude = latitude,
        longitude = longitude,
        category = category,
        uriMarkerPhoto = uriMarkerPhoto,
        timeOfCreation = time
    )
}
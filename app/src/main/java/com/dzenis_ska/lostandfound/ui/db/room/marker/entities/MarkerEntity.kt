package com.dzenis_ska.lostandfound.ui.db.room.marker.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dzenis_ska.lostandfound.ui.fragments.map.MarkerContent

@Entity (tableName = "markers")
data class MarkerEntity(
    @PrimaryKey/*(autoGenerate = true)
    val id: Int,*/
    @ColumnInfo(name = "id_marker") val key: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    @ColumnInfo(name = "photo_uri_for_marker") val uriMarkerPhoto: String,
    @ColumnInfo(name = "time_of_creation", defaultValue = "0") val timeOfCreation: String,
) {
    fun toMarkerContent(): MarkerContent = MarkerContent(
        key = key,
        latitude = latitude,
        longitude = longitude,
        category = category,
        uriMarkerPhoto = uriMarkerPhoto,
        timeOfCreation = timeOfCreation ?: "0"
    )
    companion object {
        fun fromMarkerContent(markerContent: MarkerContent) = MarkerEntity(
        key = markerContent.key,
            latitude = markerContent.latitude,
            longitude = markerContent.longitude,
            category = markerContent.category,
            uriMarkerPhoto = markerContent.uriMarkerPhoto,
            timeOfCreation = markerContent.timeOfCreation
        )
    }
}
package com.dzenis_ska.lostandfound.ui.db.room.marker.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dzenis_ska.lostandfound.ui.db.firebase.classes.RequestFromBureau

@Entity(tableName = "request_from_bureau")
data class RequestFromBureauEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val key: String,
    val uid: String? = null,
    val email: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @ColumnInfo(name = "photo_uri_for_marker")
    val uriMarkerPhoto: String? = null,
    @ColumnInfo(name = "photo_uri_list")
    val photoUri: List<String> = emptyList(),
    val category: String? = null,
    @ColumnInfo(name = "tel_num")
    val telNum: String? = null,
    val header: String? = null,
    @ColumnInfo(name = "additional_info")
    val additionalInfo: String? = null,
    val time: String? = null,
    @ColumnInfo(name = "is_edit_application")
    var isEditApplication: Boolean? = null,
    @ColumnInfo(name = "is_blocked")
    var isBlocked: Boolean? = null,
    @ColumnInfo(name = "views_counter")
    var viewsCounter: String = "0",
    @ColumnInfo(name = "calls_counter")
    var callsCounter: String = "0",
    @ColumnInfo(name = "fav_counter")
    var favCounter: String = "0",
    @ColumnInfo(name = "mass_blocked")
    var messBlocked: String = ""

) {

    fun toRequestFromBureau() = RequestFromBureau(
        key = key,
        uid = uid,
        email = email,
        latitude = latitude ?: 0.1,
        longitude = longitude ?: 0.1,
        uriMarkerPhoto = uriMarkerPhoto,
        photoUri = photoUri,
        category = category,
        telNum = telNum,
        additionalInfo = additionalInfo,
        time = time,
        messBlocked = messBlocked,
    )

    companion object {

        fun fromRequestFromBureau(requestFromBureau: RequestFromBureau) = RequestFromBureauEntity(
            key = requestFromBureau.key,
            uid = requestFromBureau.uid,
            email = requestFromBureau.email,
            latitude = requestFromBureau.latitude,
            longitude = requestFromBureau.longitude,
            uriMarkerPhoto = requestFromBureau.uriMarkerPhoto,
            photoUri = requestFromBureau.photoUri,
            category = requestFromBureau.category,
            telNum = requestFromBureau.telNum,
            additionalInfo = requestFromBureau.additionalInfo,
            time = requestFromBureau.time,
            messBlocked = requestFromBureau.messBlocked,
        )
    }
}


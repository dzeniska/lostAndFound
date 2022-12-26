package com.dzenis_ska.lostandfound.ui.fragments.map

data class MarkerContent(
    val key: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val uriMarkerPhoto: String,
    val timeOfCreation: String
)

package com.dzenis_ska.lostandfound.ui.db.room.marker.entities

import androidx.room.TypeConverter
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @OptIn(ExperimentalSerializationApi::class)
    @TypeConverter
    fun listToJsonString(value: List<String>): String = Json.encodeToString(value)
    @OptIn(ExperimentalSerializationApi::class)
    @TypeConverter
    fun jsonStringToList(value: String) = Json.decodeFromString<List<String>>(value)
}
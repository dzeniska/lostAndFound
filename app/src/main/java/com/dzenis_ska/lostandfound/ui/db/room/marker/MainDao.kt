package com.dzenis_ska.lostandfound.ui.db.room.marker

import androidx.room.*
import com.dzenis_ska.lostandfound.ui.db.room.marker.entities.MarkerEntity
import com.dzenis_ska.lostandfound.ui.db.room.marker.entities.RequestFromBureauEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface  MainDao {

    // Marker Entity Table
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarker(marker: List<MarkerEntity>)

    @Update(entity = MarkerEntity::class)
    suspend fun updateMarker(marker: List<MarkerEntity>)

    @Query("SELECT * FROM markers WHERE category = :category")
    fun selectAllMarkers(category: String): Flow<List<MarkerEntity>>

    @Query("SELECT * FROM markers ")
    fun selectAllMarkers(): Flow<List<MarkerEntity>>

    @Delete
    suspend fun deleteMarkers(oldKey: List<MarkerEntity>)

    @Query("DELETE FROM markers WHERE id_marker = :key")
    suspend fun deleteMarker(key: String)

    // Request From Bureau Entity Table
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequestFromBureauEntity(fromRequestToBureau: RequestFromBureauEntity)

    @Update(entity = RequestFromBureauEntity::class)
    suspend fun updateRequestFromBureauEntity(fromRequestToBureau: RequestFromBureauEntity)

    @Query("SELECT * FROM request_from_bureau WHERE id = :key")
    fun getOneRequestFromBureauFlow(key: String? = null): Flow<RequestFromBureauEntity?>

    @Query("DELETE FROM request_from_bureau WHERE id = :key")
    suspend fun deleteMarkerFromRequestFromBureau(key: String)

}
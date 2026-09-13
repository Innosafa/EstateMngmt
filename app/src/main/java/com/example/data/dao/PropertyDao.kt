package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PropertyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {
    @Query("SELECT * FROM properties ORDER BY createdAt DESC")
    fun getAllProperties(): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM properties WHERE id = :id")
    fun getPropertyById(id: Long): Flow<PropertyEntity?>

    @Query("SELECT * FROM properties WHERE id = :id")
    suspend fun getPropertyByIdOnce(id: Long): PropertyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: PropertyEntity): Long

    @Update
    suspend fun updateProperty(property: PropertyEntity)

    @Delete
    suspend fun deleteProperty(property: PropertyEntity)

    @Query("UPDATE properties SET address = :address, latitude = :lat, longitude = :lng WHERE id = :id")
    suspend fun updatePropertyLocation(id: Long, address: String, lat: Double, lng: Double)

    @Query("UPDATE properties SET photosJson = :photosJson WHERE id = :id")
    suspend fun updatePropertyPhotos(id: Long, photosJson: String)

    @Query("SELECT COUNT(*) FROM properties")
    suspend fun getPropertyCount(): Int
}

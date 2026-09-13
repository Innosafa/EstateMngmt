package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE propertyId = :propertyId ORDER BY timestamp DESC")
    fun getReviewsForProperty(propertyId: Long): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews ORDER BY timestamp DESC")
    fun getAllReviews(): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity): Long

    @Query("DELETE FROM reviews WHERE id = :id")
    suspend fun deleteReview(id: Long)
}

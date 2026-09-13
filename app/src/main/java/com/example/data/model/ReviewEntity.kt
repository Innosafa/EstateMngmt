package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val propertyId: Long,
    val guestName: String,
    val rating: Float, // 1.0 to 5.0
    val comment: String,
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)

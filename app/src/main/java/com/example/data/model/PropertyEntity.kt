package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray

@Entity(tableName = "properties")
data class PropertyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // "BNB", "Apartment", "Estate", "Villa"
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val totalUnits: Int,
    val occupiedUnits: Int,
    val monthlyRate: Double,
    val photosJson: String = "[]",
    val ownerName: String = "Estate Owner",
    val ownerPhone: String = "+254 712 345 678",
    val ownerEmail: String = "softinnocentsafari@gmail.com",
    val ownerWhatsApp: String = "+254712345678",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getPhotos(): List<String> {
        val list = mutableListOf<String>()
        try {
            val arr = JSONArray(photosJson)
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
        } catch (_: Exception) {}
        return list
    }

    companion object {
        fun createPhotosJson(photos: List<String>): String {
            val arr = JSONArray()
            photos.forEach { arr.put(it) }
            return arr.toString()
        }
    }
}

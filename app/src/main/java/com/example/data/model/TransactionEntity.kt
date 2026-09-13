package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val propertyId: Long? = null,
    val type: String, // "INCOME", "MAINTENANCE", "UTILITY", "TAX", "OTHER_EXPENSE"
    val encryptedCiphertext: String, // AES-256 GCM Base64
    val iv: String, // GCM IV Base64
    val yearMonth: String, // "YYYY-MM" e.g. "2026-05"
    val timestamp: Long = System.currentTimeMillis(),
    val referenceNo: String = "TX-${(1000..9999).random()}"
)

data class DecryptedTransaction(
    val id: Long,
    val propertyId: Long?,
    val type: String,
    val amount: Double,
    val category: String,
    val description: String,
    val note: String,
    val yearMonth: String,
    val timestamp: Long,
    val referenceNo: String
)

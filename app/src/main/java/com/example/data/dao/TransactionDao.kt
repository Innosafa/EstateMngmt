package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE yearMonth = :yearMonth ORDER BY timestamp DESC")
    fun getTransactionsForMonth(yearMonth: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE propertyId = :propertyId ORDER BY timestamp DESC")
    fun getTransactionsForProperty(propertyId: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int

    @Query("SELECT DISTINCT yearMonth FROM transactions ORDER BY yearMonth DESC")
    fun getAvailableMonths(): Flow<List<String>>

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsList(): List<TransactionEntity>
}

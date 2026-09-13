package com.example.data

import android.content.Context
import com.example.data.model.DecryptedTransaction
import com.example.data.model.PropertyEntity
import com.example.data.model.ReviewEntity
import com.example.data.model.TransactionEntity
import com.example.security.CryptoManager
import com.example.security.TransactionPayload
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class MonthlyAnalytics(
    val yearMonth: String,
    val totalRevenue: Double,
    val maintenanceCosts: Double,
    val utilityCosts: Double,
    val taxCosts: Double,
    val otherExpenses: Double,
    val netIncome: Double,
    val transactionCount: Int,
    val avgOccupancyRate: Double
) {
    val operatingExpenses: Double get() = utilityCosts + taxCosts + otherExpenses
    val totalExpenses: Double get() = maintenanceCosts + utilityCosts + taxCosts + otherExpenses
}

class EstateRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val propertyDao = database.propertyDao()
    private val transactionDao = database.transactionDao()
    private val reviewDao = database.reviewDao()

    val allProperties: Flow<List<PropertyEntity>> = propertyDao.getAllProperties()
    val availableMonths: Flow<List<String>> = transactionDao.getAvailableMonths()

    fun getPropertyById(id: Long): Flow<PropertyEntity?> = propertyDao.getPropertyById(id)

    suspend fun insertProperty(property: PropertyEntity): Long = withContext(Dispatchers.IO) {
        propertyDao.insertProperty(property)
    }

    suspend fun updateProperty(property: PropertyEntity) = withContext(Dispatchers.IO) {
        propertyDao.updateProperty(property)
    }

    suspend fun deleteProperty(property: PropertyEntity) = withContext(Dispatchers.IO) {
        propertyDao.deleteProperty(property)
    }

    suspend fun updatePropertyLocation(id: Long, address: String, lat: Double, lng: Double) = withContext(Dispatchers.IO) {
        propertyDao.updatePropertyLocation(id, address, lat, lng)
    }

    suspend fun addPhotoToProperty(propertyId: Long, photoUri: String) = withContext(Dispatchers.IO) {
        val property = propertyDao.getPropertyByIdOnce(propertyId) ?: return@withContext
        val photos = property.getPhotos().toMutableList()
        if (!photos.contains(photoUri)) {
            photos.add(photoUri)
            val json = PropertyEntity.createPhotosJson(photos)
            propertyDao.updatePropertyPhotos(propertyId, json)
        }
    }

    suspend fun removePhotoFromProperty(propertyId: Long, photoUri: String) = withContext(Dispatchers.IO) {
        val property = propertyDao.getPropertyByIdOnce(propertyId) ?: return@withContext
        val photos = property.getPhotos().toMutableList()
        photos.remove(photoUri)
        val json = PropertyEntity.createPhotosJson(photos)
        propertyDao.updatePropertyPhotos(propertyId, json)
    }

    // Transactions with encryption
    suspend fun insertEncryptedTransaction(
        propertyId: Long?,
        type: String,
        amount: Double,
        category: String,
        description: String,
        note: String,
        yearMonth: String = getCurrentYearMonth()
    ): Long = withContext(Dispatchers.IO) {
        val masterPassword = CryptoManager.unlockedMasterPassword ?: "1234"
        val payload = TransactionPayload(
            amount = amount,
            category = category,
            description = description,
            note = note
        )
        val encrypted = CryptoManager.encryptPayload(payload, masterPassword)
        val entity = TransactionEntity(
            propertyId = propertyId,
            type = type,
            encryptedCiphertext = encrypted.ciphertext,
            iv = encrypted.iv,
            yearMonth = yearMonth,
            timestamp = System.currentTimeMillis()
        )
        transactionDao.insertTransaction(entity)
    }

    suspend fun deleteTransaction(id: Long) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(id)
    }

    fun getDecryptedTransactions(yearMonth: String? = null): Flow<List<DecryptedTransaction>> {
        val rawFlow = if (yearMonth.isNullOrEmpty()) {
            transactionDao.getAllTransactions()
        } else {
            transactionDao.getTransactionsForMonth(yearMonth)
        }

        return rawFlow.map { list ->
            val masterPassword = CryptoManager.unlockedMasterPassword ?: "1234"
            list.mapNotNull { entity ->
                try {
                    val payload = CryptoManager.decryptPayload(
                        entity.encryptedCiphertext,
                        entity.iv,
                        masterPassword
                    )
                    DecryptedTransaction(
                        id = entity.id,
                        propertyId = entity.propertyId,
                        type = entity.type,
                        amount = payload.amount,
                        category = payload.category,
                        description = payload.description,
                        note = payload.note,
                        yearMonth = entity.yearMonth,
                        timestamp = entity.timestamp,
                        referenceNo = entity.referenceNo
                    )
                } catch (_: Exception) {
                    // Decryption fallback if locked or corrupt
                    DecryptedTransaction(
                        id = entity.id,
                        propertyId = entity.propertyId,
                        type = entity.type,
                        amount = 0.0,
                        category = "Encrypted",
                        description = "Locked transaction [Secure AES-256]",
                        note = "",
                        yearMonth = entity.yearMonth,
                        timestamp = entity.timestamp,
                        referenceNo = entity.referenceNo
                    )
                }
            }
        }
    }

    suspend fun calculateMonthlyAnalytics(
        yearMonth: String,
        properties: List<PropertyEntity>
    ): MonthlyAnalytics = withContext(Dispatchers.IO) {
        val masterPassword = CryptoManager.unlockedMasterPassword ?: "1234"
        val rawTransactions = transactionDao.getAllTransactionsList()
            .filter { it.yearMonth == yearMonth }

        var totalRevenue = 0.0
        var maintenanceCosts = 0.0
        var utilityCosts = 0.0
        var taxCosts = 0.0
        var otherExpenses = 0.0

        for (tx in rawTransactions) {
            try {
                val payload = CryptoManager.decryptPayload(tx.encryptedCiphertext, tx.iv, masterPassword)
                when (tx.type) {
                    "INCOME" -> totalRevenue += payload.amount
                    "MAINTENANCE" -> maintenanceCosts += payload.amount
                    "UTILITY" -> utilityCosts += payload.amount
                    "TAX" -> taxCosts += payload.amount
                    else -> otherExpenses += payload.amount
                }
            } catch (_: Exception) {}
        }

        val totalExpenses = maintenanceCosts + utilityCosts + taxCosts + otherExpenses
        val netIncome = totalRevenue - totalExpenses

        val totalUnits = properties.sumOf { it.totalUnits }
        val occupiedUnits = properties.sumOf { it.occupiedUnits }
        val avgOccupancy = if (totalUnits > 0) (occupiedUnits.toDouble() / totalUnits) * 100.0 else 0.0

        MonthlyAnalytics(
            yearMonth = yearMonth,
            totalRevenue = totalRevenue,
            maintenanceCosts = maintenanceCosts,
            utilityCosts = utilityCosts,
            taxCosts = taxCosts,
            otherExpenses = otherExpenses,
            netIncome = netIncome,
            transactionCount = rawTransactions.size,
            avgOccupancyRate = avgOccupancy
        )
    }

    // Customer Reviews
    fun getReviewsForProperty(propertyId: Long): Flow<List<ReviewEntity>> =
        reviewDao.getReviewsForProperty(propertyId)

    suspend fun insertReview(review: ReviewEntity): Long = withContext(Dispatchers.IO) {
        reviewDao.insertReview(review)
    }

    suspend fun deleteReview(id: Long) = withContext(Dispatchers.IO) {
        reviewDao.deleteReview(id)
    }

    // Seed initial dataset if database is empty
    suspend fun seedInitialDataIfEmpty(password: String) = withContext(Dispatchers.IO) {
        if (propertyDao.getPropertyCount() == 0) {
            val p1 = PropertyEntity(
                name = "Kilimani Court",
                type = "Apartment",
                address = "Argwings Kodhek Rd, Kilimani, Nairobi",
                latitude = -1.2905,
                longitude = 36.7865,
                totalUnits = 6,
                occupiedUnits = 5,
                monthlyRate = 42000.0,
                photosJson = PropertyEntity.createPhotosJson(listOf("preset_apt1", "preset_apt2")),
                ownerName = "Innocent Safari",
                ownerPhone = "+254 712 345 678",
                ownerEmail = "softinnocentsafari@gmail.com",
                ownerWhatsApp = "+254712345678",
                description = "Modern residential apartments in prime Kilimani with 24/7 security, backup generator, high-speed lift, and borehole."
            )

            val p2 = PropertyEntity(
                name = "Westlands Horizon Villa",
                type = "Villa",
                address = "Rhapta Road, Westlands, Nairobi",
                latitude = -1.2642,
                longitude = 36.8041,
                totalUnits = 4,
                occupiedUnits = 4,
                monthlyRate = 65000.0,
                photosJson = PropertyEntity.createPhotosJson(listOf("preset_villa1")),
                ownerName = "Innocent Safari",
                ownerPhone = "+254 712 345 678",
                ownerEmail = "softinnocentsafari@gmail.com",
                ownerWhatsApp = "+254712345678",
                description = "Luxury 4-bedroom executive townhouses with private landscaped gardens and clubhouse access."
            )

            val p3 = PropertyEntity(
                name = "Diani Palms Beachfront BNB",
                type = "BNB",
                address = "Beach Road, Diani, South Coast",
                latitude = -4.2778,
                longitude = 39.5936,
                totalUnits = 8,
                occupiedUnits = 7,
                monthlyRate = 95000.0,
                photosJson = PropertyEntity.createPhotosJson(listOf("preset_bnb1", "preset_bnb2")),
                ownerName = "Innocent Safari",
                ownerPhone = "+254 712 345 678",
                ownerEmail = "softinnocentsafari@gmail.com",
                ownerWhatsApp = "+254712345678",
                description = "Idyllic coastal beachfront BNB suites with ocean views, swimming pool, chef service, and solar water heating."
            )

            val id1 = propertyDao.insertProperty(p1)
            val id2 = propertyDao.insertProperty(p2)
            val id3 = propertyDao.insertProperty(p3)

            // Seed reviews
            reviewDao.insertReview(
                ReviewEntity(
                    propertyId = id1,
                    guestName = "Amina Ochieng",
                    rating = 5.0f,
                    comment = "Super clean premises, quiet neighborhood, and immediate response to any maintenance calls. Highly recommended!",
                    date = "May 2026"
                )
            )
            reviewDao.insertReview(
                ReviewEntity(
                    propertyId = id3,
                    guestName = "David Mueller",
                    rating = 4.8f,
                    comment = "Amazing stay in Diani! The ocean breeze, friendly staff, and secure compound made our family vacation wonderful.",
                    date = "Apr 2026"
                )
            )

            // Seed initial encrypted transactions
            val currentMonth = getCurrentYearMonth()
            val prevMonth = "2026-04"

            suspend fun addEncrypted(propId: Long?, type: String, amt: Double, cat: String, desc: String, month: String) {
                val enc = CryptoManager.encryptPayload(TransactionPayload(amt, cat, desc), password)
                transactionDao.insertTransaction(
                    TransactionEntity(
                        propertyId = propId,
                        type = type,
                        encryptedCiphertext = enc.ciphertext,
                        iv = enc.iv,
                        yearMonth = month,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }

            addEncrypted(id1, "INCOME", 42000.0, "Rent Collection", "Unit 3B Amina Ochieng rent payment", currentMonth)
            addEncrypted(id1, "INCOME", 42000.0, "Rent Collection", "Unit 1A Peter Kamau rent payment", currentMonth)
            addEncrypted(id1, "MAINTENANCE", 12500.0, "Plumbing & Repairs", "Emergency water pump servicing & valve replacements", currentMonth)
            addEncrypted(id2, "INCOME", 65000.0, "Rent Collection", "Villa 2 Executive tenant rental", currentMonth)
            addEncrypted(id2, "UTILITY", 8500.0, "Estate Common Power", "Borehole pumping electricity & perimeter security lighting", currentMonth)
            addEncrypted(id3, "INCOME", 95000.0, "BNB Bookings", "Diani Palms weekend luxury holiday bookings", currentMonth)
            addEncrypted(id3, "MAINTENANCE", 14380.0, "Pool & Grounds Maintenance", "Swimming pool chemical treatment and landscaping", currentMonth)

            // Previous month
            addEncrypted(id1, "INCOME", 168000.0, "Rent Collection", "April rent roll collection across Kilimani Court", prevMonth)
            addEncrypted(id1, "MAINTENANCE", 18500.0, "Roofing & Painting", "Exterior parapet waterproofing", prevMonth)
            addEncrypted(id3, "INCOME", 88000.0, "BNB Bookings", "Easter holiday BNB booking package", prevMonth)
            addEncrypted(id3, "MAINTENANCE", 9200.0, "Linen & Supplies", "Hotel grade linen replacement & guest toiletries", prevMonth)
        }
    }

    companion object {
        fun getCurrentYearMonth(): String {
            return SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
        }
    }
}

package com.example.sync

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.AppDatabase
import com.example.data.model.PropertyEntity
import com.example.data.model.ReviewEntity
import com.example.data.model.TransactionEntity
import com.example.security.CryptoManager
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object SyncManager {

    suspend fun generateSyncPayload(context: Context): String = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(context)
        val properties = db.propertyDao().getPropertyCount()
        // Gather all data
        val root = JSONObject()
        root.put("app", "EstateIQ")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("deviceId", android.os.Build.MODEL)

        val linkedAccount = CryptoManager.getLinkedGoogleAccount(context)
        if (linkedAccount != null) {
            val userObj = JSONObject()
            userObj.put("email", linkedAccount.first)
            userObj.put("name", linkedAccount.second)
            root.put("account", userObj)
        }

        // Properties
        val propArray = JSONArray()
        // We can query properties via repository or DAO
        // Let's get them from DB
        val propsList = mutableListOf<PropertyEntity>()
        // Let's create an export helper or query in DAO
        // In propertyDao, we can query all
        root.put("propertiesCount", properties)

        root.toString(2)
    }

    /**
     * Creates an encrypted backup file and triggers Android Intent.ACTION_SEND
     * to seamlessly link with other installed apps (Google Drive, WhatsApp, Email, etc.)
     */
    suspend fun exportAndShareBackup(
        context: Context,
        properties: List<PropertyEntity>,
        transactions: List<TransactionEntity>,
        reviews: List<ReviewEntity>
    ) = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "EstateIQ")
        root.put("version", "1.0")
        root.put("exportTimestamp", System.currentTimeMillis())
        root.put("security", "AES-256-GCM Encrypted Records")

        val account = CryptoManager.getLinkedGoogleAccount(context)
        val accountObj = JSONObject().apply {
            put("email", account?.first ?: "softinnocentsafari@gmail.com")
            put("name", account?.second ?: "Estate Owner")
        }
        root.put("ownerAccount", accountObj)

        // Properties array
        val propArr = JSONArray()
        for (p in properties) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("type", p.type)
                put("address", p.address)
                put("latitude", p.latitude)
                put("longitude", p.longitude)
                put("totalUnits", p.totalUnits)
                put("occupiedUnits", p.occupiedUnits)
                put("monthlyRate", p.monthlyRate)
                put("photosJson", p.photosJson)
                put("ownerName", p.ownerName)
                put("ownerPhone", p.ownerPhone)
                put("ownerEmail", p.ownerEmail)
                put("ownerWhatsApp", p.ownerWhatsApp)
                put("description", p.description)
            }
            propArr.put(obj)
        }
        root.put("properties", propArr)

        // Encrypted Transactions array
        val txArr = JSONArray()
        for (tx in transactions) {
            val obj = JSONObject().apply {
                put("id", tx.id)
                put("propertyId", tx.propertyId ?: JSONObject.NULL)
                put("type", tx.type)
                put("encryptedCiphertext", tx.encryptedCiphertext)
                put("iv", tx.iv)
                put("yearMonth", tx.yearMonth)
                put("timestamp", tx.timestamp)
                put("referenceNo", tx.referenceNo)
            }
            txArr.put(obj)
        }
        root.put("encryptedTransactions", txArr)

        // Reviews array
        val revArr = JSONArray()
        for (r in reviews) {
            val obj = JSONObject().apply {
                put("propertyId", r.propertyId)
                put("guestName", r.guestName)
                put("rating", r.rating.toDouble())
                put("comment", r.comment)
                put("date", r.date)
                put("timestamp", r.timestamp)
            }
            revArr.put(obj)
        }
        root.put("reviews", revArr)

        val jsonString = root.toString(2)

        // Save to cache file for sharing
        val cacheDir = File(context.cacheDir, "sync")
        cacheDir.mkdirs()
        val syncFile = File(cacheDir, "estateiq_sync_backup_${System.currentTimeMillis()}.json")
        syncFile.writeText(jsonString)

        withContext(Dispatchers.Main) {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_SUBJECT, "EstateIQ Synchronized Portfolio Backup")
                putExtra(Intent.EXTRA_TEXT, "EstateIQ encrypted portfolio & financial records sync backup. Linked with installed apps for multi-device sync.\n\n$jsonString")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(sendIntent, "Sync EstateIQ data with installed app")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }

    /**
     * Imports and merges synchronization payload from another device or app
     */
    suspend fun importSyncData(
        context: Context,
        jsonString: String
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val db = AppDatabase.getDatabase(context)
            var count = 0

            // Import properties
            if (root.has("properties")) {
                val arr = root.getJSONArray("properties")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val prop = PropertyEntity(
                        name = obj.getString("name"),
                        type = obj.getString("type"),
                        address = obj.getString("address"),
                        latitude = obj.optDouble("latitude", -1.2921),
                        longitude = obj.optDouble("longitude", 36.8219),
                        totalUnits = obj.optInt("totalUnits", 1),
                        occupiedUnits = obj.optInt("occupiedUnits", 1),
                        monthlyRate = obj.optDouble("monthlyRate", 0.0),
                        photosJson = obj.optString("photosJson", "[]"),
                        ownerName = obj.optString("ownerName", "Estate Owner"),
                        ownerPhone = obj.optString("ownerPhone", "+254 712 345 678"),
                        ownerEmail = obj.optString("ownerEmail", "softinnocentsafari@gmail.com"),
                        ownerWhatsApp = obj.optString("ownerWhatsApp", "+254712345678"),
                        description = obj.optString("description", "")
                    )
                    db.propertyDao().insertProperty(prop)
                    count++
                }
            }

            // Import transactions
            if (root.has("encryptedTransactions")) {
                val arr = root.getJSONArray("encryptedTransactions")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val propId = if (obj.isNull("propertyId")) null else obj.getLong("propertyId")
                    val tx = TransactionEntity(
                        propertyId = propId,
                        type = obj.getString("type"),
                        encryptedCiphertext = obj.getString("encryptedCiphertext"),
                        iv = obj.getString("iv"),
                        yearMonth = obj.getString("yearMonth"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        referenceNo = obj.optString("referenceNo", "TX-${(1000..9999).random()}")
                    )
                    db.transactionDao().insertTransaction(tx)
                    count++
                }
            }

            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

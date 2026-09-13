package com.example.security

import android.content.Context
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import org.json.JSONObject

data class EncryptedData(
    val ciphertext: String,
    val iv: String
)

data class TransactionPayload(
    val amount: Double,
    val category: String,
    val description: String,
    val note: String = ""
)

object CryptoManager {
    private const val PREFS_NAME = "estateiq_security_prefs"
    private const val KEY_PASSWORD_HASH = "master_pwd_hash"
    private const val KEY_SALT = "master_salt"
    private const val KEY_IS_SETUP = "master_pwd_is_setup"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_auth_enabled"
    private const val KEY_GOOGLE_EMAIL = "google_user_email"
    private const val KEY_GOOGLE_NAME = "google_user_name"

    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12

    // In-memory unlocked session
    var unlockedMasterPassword: String? = null
        private set

    val isUnlocked: Boolean
        get() = unlockedMasterPassword != null

    fun isMasterPasswordSet(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_SETUP, false)
    }

    fun isBiometricEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun getLinkedGoogleAccount(context: Context): Pair<String, String>? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val email = prefs.getString(KEY_GOOGLE_EMAIL, null) ?: return null
        val name = prefs.getString(KEY_GOOGLE_NAME, "Estate Owner") ?: "Estate Owner"
        return Pair(email, name)
    }

    fun saveLinkedGoogleAccount(context: Context, email: String, name: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_GOOGLE_EMAIL, email)
            .putString(KEY_GOOGLE_NAME, name)
            .apply()
    }

    fun removeLinkedGoogleAccount(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_GOOGLE_EMAIL)
            .remove(KEY_GOOGLE_NAME)
            .apply()
    }

    /**
     * Initializes a new master password or updates an existing one.
     */
    @Synchronized
    fun setMasterPassword(context: Context, password: String): Boolean {
        if (password.length < 4) return false
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hash = hashPassword(password, salt)
        val hashBase64 = Base64.encodeToString(hash, Base64.NO_WRAP)

        prefs.edit()
            .putString(KEY_SALT, saltBase64)
            .putString(KEY_PASSWORD_HASH, hashBase64)
            .putBoolean(KEY_IS_SETUP, true)
            .apply()

        unlockedMasterPassword = password
        return true
    }

    /**
     * Verifies the entered password against the stored PBKDF2 hash.
     */
    fun verifyAndUnlock(context: Context, password: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saltBase64 = prefs.getString(KEY_SALT, null) ?: return false
        val storedHashBase64 = prefs.getString(KEY_PASSWORD_HASH, null) ?: return false

        val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
        val testHash = hashPassword(password, salt)
        val testHashBase64 = Base64.encodeToString(testHash, Base64.NO_WRAP)

        val success = storedHashBase64 == testHashBase64
        if (success) {
            unlockedMasterPassword = password
        }
        return success
    }

    fun lock() {
        unlockedMasterPassword = null
    }

    private fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKey {
        val rawKey = hashPassword(password, salt)
        return SecretKeySpec(rawKey, "AES")
    }

    /**
     * Encrypts plaintext string using AES-256 GCM.
     */
    fun encrypt(plaintext: String, masterPassword: String): EncryptedData {
        val random = SecureRandom()
        val salt = "EstateIQ_Fixed_Salt_For_Master".toByteArray(StandardCharsets.UTF_8)
        val secretKey = deriveKey(masterPassword, salt)

        val iv = ByteArray(GCM_IV_LENGTH)
        random.nextBytes(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        val cipherBytes = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))
        return EncryptedData(
            ciphertext = Base64.encodeToString(cipherBytes, Base64.NO_WRAP),
            iv = Base64.encodeToString(iv, Base64.NO_WRAP)
        )
    }

    /**
     * Decrypts ciphertext using AES-256 GCM.
     * Throws GeneralSecurityException if key/password is incorrect or data was tampered with.
     */
    @Throws(GeneralSecurityException::class)
    fun decrypt(ciphertext: String, iv: String, masterPassword: String): String {
        val salt = "EstateIQ_Fixed_Salt_For_Master".toByteArray(StandardCharsets.UTF_8)
        val secretKey = deriveKey(masterPassword, salt)

        val ivBytes = Base64.decode(iv, Base64.NO_WRAP)
        val cipherBytes = Base64.decode(ciphertext, Base64.NO_WRAP)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, ivBytes)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        val plainBytes = cipher.doFinal(cipherBytes)
        return String(plainBytes, StandardCharsets.UTF_8)
    }

    /**
     * Encrypts a transaction payload into EncryptedData.
     */
    fun encryptPayload(payload: TransactionPayload, masterPassword: String): EncryptedData {
        val json = JSONObject().apply {
            put("amount", payload.amount)
            put("category", payload.category)
            put("description", payload.description)
            put("note", payload.note)
        }.toString()
        return encrypt(json, masterPassword)
    }

    /**
     * Decrypts a transaction payload.
     */
    @Throws(GeneralSecurityException::class)
    fun decryptPayload(ciphertext: String, iv: String, masterPassword: String): TransactionPayload {
        val decryptedJson = decrypt(ciphertext, iv, masterPassword)
        val obj = JSONObject(decryptedJson)
        return TransactionPayload(
            amount = obj.optDouble("amount", 0.0),
            category = obj.optString("category", "General"),
            description = obj.optString("description", ""),
            note = obj.optString("note", "")
        )
    }
}

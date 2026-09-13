package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.CryptoManager
import com.example.security.TransactionPayload
import java.security.GeneralSecurityException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SecurityEncryptionTest {

    @Test
    fun testMasterPasswordHashAndVerification() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val masterPassword = "EstateIQ_SuperSecure_Pass_2026!"

        // Set master password
        val didSet = CryptoManager.setMasterPassword(context, masterPassword)
        assertTrue("Master password should be set successfully", didSet)
        assertTrue("Master password setup status must be true", CryptoManager.isMasterPasswordSet(context))

        // Verify correct password
        val unlockSuccess = CryptoManager.verifyAndUnlock(context, masterPassword)
        assertTrue("Unlock with correct password should succeed", unlockSuccess)
        assertTrue("App session should be unlocked", CryptoManager.isUnlocked)

        // Verify wrong password
        val wrongUnlock = CryptoManager.verifyAndUnlock(context, "WrongPassword123")
        assertFalse("Unlock with wrong password must fail", wrongUnlock)
    }

    @Test
    fun testAes256GcmTransactionEncryptionAndDecryption() {
        val masterPassword = "MyEstateMasterKey#789"
        val payload = TransactionPayload(
            amount = 186000.0,
            category = "Rent Collection",
            description = "Kilimani Court & Westlands May 2026 rent roll",
            note = "Encrypted on-device without external API keys"
        )

        // Encrypt
        val encrypted = CryptoManager.encryptPayload(payload, masterPassword)
        assertFalse("Ciphertext should not be empty", encrypted.ciphertext.isEmpty())
        assertFalse("IV should not be empty", encrypted.iv.isEmpty())
        assertNotEquals("Ciphertext must not be equal to plaintext description", payload.description, encrypted.ciphertext)

        // Decrypt with correct password
        val decrypted = CryptoManager.decryptPayload(encrypted.ciphertext, encrypted.iv, masterPassword)
        assertEquals("Amount must match decrypted payload", payload.amount, decrypted.amount, 0.001)
        assertEquals("Category must match", payload.category, decrypted.category)
        assertEquals("Description must match", payload.description, decrypted.description)
        assertEquals("Note must match", payload.note, decrypted.note)
    }

    @Test
    fun testDecryptionFailsWithWrongPassword() {
        val correctPassword = "CorrectMasterPassword99"
        val wrongPassword = "IncorrectPassword00"

        val payload = TransactionPayload(
            amount = 25000.0,
            category = "Maintenance",
            description = "Emergency plumbing repair",
            note = "Private transaction"
        )

        val encrypted = CryptoManager.encryptPayload(payload, correctPassword)

        var failedAsExpected = false
        try {
            CryptoManager.decryptPayload(encrypted.ciphertext, encrypted.iv, wrongPassword)
        } catch (_: GeneralSecurityException) {
            failedAsExpected = true
        } catch (_: Exception) {
            failedAsExpected = true
        }
        assertTrue("Decryption with wrong password must fail with security exception", failedAsExpected)
    }

    @Test
    fun testTamperResistance() {
        val masterPassword = "SecureKey!@#"
        val payload = TransactionPayload(
            amount = 50000.0,
            category = "Security Service",
            description = "Annual perimeter guard service"
        )

        val encrypted = CryptoManager.encryptPayload(payload, masterPassword)
        // Tamper with ciphertext
        val tamperedCiphertext = if (encrypted.ciphertext.endsWith("A")) {
            encrypted.ciphertext.dropLast(1) + "B"
        } else {
            encrypted.ciphertext.dropLast(1) + "A"
        }

        var tamperedFailed = false
        try {
            CryptoManager.decryptPayload(tamperedCiphertext, encrypted.iv, masterPassword)
        } catch (_: Exception) {
            tamperedFailed = true
        }
        assertTrue("Tampered ciphertext must fail AEAD authentication tag validation", tamperedFailed)
    }
}

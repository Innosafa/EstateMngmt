package com.example.auth

import android.app.Activity
import android.content.Context
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import com.example.security.CryptoManager

object AuthManager {

    fun isBiometricHardwareAvailable(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val biometricManager = context.getSystemService(android.hardware.biometrics.BiometricManager::class.java)
            if (biometricManager != null) {
                val canAuth = biometricManager.canAuthenticate(android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_WEAK)
                return canAuth == android.hardware.biometrics.BiometricManager.BIOMETRIC_SUCCESS
            }
        }
        return true // Fallback simulation support in testing/emulator environments
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun showBiometricPrompt(
        activity: Activity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cancellationSignal = CancellationSignal()
        try {
            val prompt = BiometricPrompt.Builder(activity)
                .setTitle("Biometric Unlock • EstateIQ")
                .setSubtitle("Authenticate using fingerprint or face unlock to access encrypted records")
                .setDescription("Your personal finance data and estate revenue records are locally encrypted with AES-256")
                .setNegativeButton("Use Master Password", activity.mainExecutor) { _, _ ->
                    onError("Cancelled: Using Master Password")
                }
                .build()

            prompt.authenticate(
                cancellationSignal,
                activity.mainExecutor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                        super.onAuthenticationError(errorCode, errString)
                        onError(errString?.toString() ?: "Biometric authentication failed")
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        onError("Biometric authentication failed. Please try again.")
                    }
                }
            )
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Biometric prompt unavailable")
        }
    }
}

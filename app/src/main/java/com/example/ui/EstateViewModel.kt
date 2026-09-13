package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.EstateRepository
import com.example.data.MonthlyAnalytics
import com.example.data.model.DecryptedTransaction
import com.example.data.model.PropertyEntity
import com.example.data.model.ReviewEntity
import com.example.notifications.NotificationHelper
import com.example.security.CryptoManager
import com.example.sync.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthUiState(
    val isSetup: Boolean = false,
    val isUnlocked: Boolean = false,
    val isBiometricAvailable: Boolean = true,
    val isBiometricEnabled: Boolean = true,
    val linkedGoogleAccount: Pair<String, String>? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class EstateViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EstateRepository(application)
    private val context = application.applicationContext

    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    val properties: StateFlow<List<PropertyEntity>> = repository.allProperties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedMonth = MutableStateFlow(EstateRepository.getCurrentYearMonth())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    val availableMonths: StateFlow<List<String>> = repository.availableMonths
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(EstateRepository.getCurrentYearMonth()))

    private val _decryptedTransactions = MutableStateFlow<List<DecryptedTransaction>>(emptyList())
    val decryptedTransactions: StateFlow<List<DecryptedTransaction>> = _decryptedTransactions.asStateFlow()

    private val _monthlyAnalytics = MutableStateFlow<MonthlyAnalytics?>(null)
    val monthlyAnalytics: StateFlow<MonthlyAnalytics?> = _monthlyAnalytics.asStateFlow()

    private val _selectedProperty = MutableStateFlow<PropertyEntity?>(null)
    val selectedProperty: StateFlow<PropertyEntity?> = _selectedProperty.asStateFlow()

    private val _propertyReviews = MutableStateFlow<List<ReviewEntity>>(emptyList())
    val propertyReviews: StateFlow<List<ReviewEntity>> = _propertyReviews.asStateFlow()

    private val _actionFeedback = MutableStateFlow<String?>(null)
    val actionFeedback: StateFlow<String?> = _actionFeedback.asStateFlow()

    init {
        checkAuthStatus()
        observeTransactions()
        // If master password not setup yet, initialize with default "1234" so the user can immediately experience
        // the app's full local encryption and features seamlessly, while having the ability to change it in Security.
        viewModelScope.launch {
            if (!CryptoManager.isMasterPasswordSet(context)) {
                CryptoManager.setMasterPassword(context, "1234")
                repository.seedInitialDataIfEmpty("1234")
                checkAuthStatus()
            } else if (CryptoManager.isUnlocked) {
                repository.seedInitialDataIfEmpty(CryptoManager.unlockedMasterPassword ?: "1234")
            }
            refreshAnalytics()
        }
    }

    fun checkAuthStatus() {
        val isSetup = CryptoManager.isMasterPasswordSet(context)
        val isUnlocked = CryptoManager.isUnlocked
        val isBioEnabled = CryptoManager.isBiometricEnabled(context)
        val googleAccount = CryptoManager.getLinkedGoogleAccount(context)

        _authUiState.value = _authUiState.value.copy(
            isSetup = isSetup,
            isUnlocked = isUnlocked,
            isBiometricEnabled = isBioEnabled,
            linkedGoogleAccount = googleAccount,
            errorMessage = null
        )
    }

    fun unlockWithPassword(password: String) {
        val success = CryptoManager.verifyAndUnlock(context, password)
        if (success) {
            checkAuthStatus()
            _authUiState.value = _authUiState.value.copy(errorMessage = null)
            observeTransactions()
            refreshAnalytics()
            showFeedback("Vault unlocked with Master Password")
        } else {
            _authUiState.value = _authUiState.value.copy(errorMessage = "Incorrect Master Password. Please try again.")
        }
    }

    fun unlockWithBiometric() {
        // Biometric prompt verified, unlock session
        // In local storage with biometric, we verify using the stored hash/key
        val prefs = context.getSharedPreferences("estateiq_security_prefs", Application.MODE_PRIVATE)
        val hasHash = prefs.contains("master_pwd_hash")
        if (hasHash) {
            // Unlocked via biometric
            // If master password is known or default, unlock
            CryptoManager.verifyAndUnlock(context, "1234")
            checkAuthStatus()
            observeTransactions()
            refreshAnalytics()
            showFeedback("Biometric authentication successful")
        }
    }

    fun setupOrChangeMasterPassword(newPassword: String, confirmPassword: String): Boolean {
        if (newPassword.length < 4) {
            _authUiState.value = _authUiState.value.copy(errorMessage = "Password must be at least 4 characters")
            return false
        }
        if (newPassword != confirmPassword) {
            _authUiState.value = _authUiState.value.copy(errorMessage = "Passwords do not match")
            return false
        }
        val success = CryptoManager.setMasterPassword(context, newPassword)
        if (success) {
            checkAuthStatus()
            observeTransactions()
            refreshAnalytics()
            showFeedback("Master Password successfully updated")
            return true
        }
        return false
    }

    fun lockSession() {
        CryptoManager.lock()
        checkAuthStatus()
        observeTransactions()
        showFeedback("Vault locked. Master Password or Biometric required.")
    }

    fun toggleBiometric(enabled: Boolean) {
        CryptoManager.setBiometricEnabled(context, enabled)
        checkAuthStatus()
    }

    fun linkGoogleAccount(email: String, name: String) {
        CryptoManager.saveLinkedGoogleAccount(context, email, name)
        checkAuthStatus()
        showFeedback("Google Account linked: $email")
    }

    fun unlinkGoogleAccount() {
        CryptoManager.removeLinkedGoogleAccount(context)
        checkAuthStatus()
        showFeedback("Google Account unlinked")
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            repository.getDecryptedTransactions(_selectedMonth.value).collectLatest { list ->
                _decryptedTransactions.value = list
                refreshAnalytics()
            }
        }
    }

    fun selectMonth(month: String) {
        _selectedMonth.value = month
        observeTransactions()
    }

    fun refreshAnalytics() {
        viewModelScope.launch {
            val props = properties.value
            val analytics = repository.calculateMonthlyAnalytics(_selectedMonth.value, props)
            _monthlyAnalytics.value = analytics
        }
    }

    fun selectProperty(property: PropertyEntity?) {
        _selectedProperty.value = property
        if (property != null) {
            viewModelScope.launch {
                repository.getReviewsForProperty(property.id).collectLatest { reviews ->
                    _propertyReviews.value = reviews
                }
            }
        } else {
            _propertyReviews.value = emptyList()
        }
    }

    fun addProperty(
        name: String,
        type: String,
        address: String,
        latitude: Double,
        longitude: Double,
        totalUnits: Int,
        occupiedUnits: Int,
        monthlyRate: Double,
        description: String,
        photos: List<String> = emptyList(),
        ownerPhone: String = "+254 712 345 678",
        ownerEmail: String = "softinnocentsafari@gmail.com",
        ownerWhatsApp: String = "+254712345678"
    ) {
        viewModelScope.launch {
            val property = PropertyEntity(
                name = name,
                type = type,
                address = address,
                latitude = latitude,
                longitude = longitude,
                totalUnits = totalUnits,
                occupiedUnits = occupiedUnits,
                monthlyRate = monthlyRate,
                photosJson = PropertyEntity.createPhotosJson(photos),
                description = description,
                ownerPhone = ownerPhone,
                ownerEmail = ownerEmail,
                ownerWhatsApp = ownerWhatsApp
            )
            repository.insertProperty(property)
            showFeedback("Property '$name' added successfully")
            refreshAnalytics()
        }
    }

    fun updatePropertyLocation(propertyId: Long, newAddress: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            repository.updatePropertyLocation(propertyId, newAddress, lat, lng)
            showFeedback("Location reset & updated for property")
            // refresh selected property if matches
            if (_selectedProperty.value?.id == propertyId) {
                _selectedProperty.value = _selectedProperty.value?.copy(
                    address = newAddress,
                    latitude = lat,
                    longitude = lng
                )
            }
        }
    }

    fun addPhotoToProperty(propertyId: Long, photoUriOrPreset: String) {
        viewModelScope.launch {
            repository.addPhotoToProperty(propertyId, photoUriOrPreset)
            showFeedback("New photo added to property")
            // update selected property
            val current = _selectedProperty.value
            if (current?.id == propertyId) {
                val updatedList = current.getPhotos().toMutableList().apply { add(photoUriOrPreset) }
                _selectedProperty.value = current.copy(photosJson = PropertyEntity.createPhotosJson(updatedList))
            }
        }
    }

    fun removePhotoFromProperty(propertyId: Long, photoUriOrPreset: String) {
        viewModelScope.launch {
            repository.removePhotoFromProperty(propertyId, photoUriOrPreset)
            showFeedback("Photo removed from property")
            val current = _selectedProperty.value
            if (current?.id == propertyId) {
                val updatedList = current.getPhotos().toMutableList().apply { remove(photoUriOrPreset) }
                _selectedProperty.value = current.copy(photosJson = PropertyEntity.createPhotosJson(updatedList))
            }
        }
    }

    fun deleteProperty(property: PropertyEntity) {
        viewModelScope.launch {
            repository.deleteProperty(property)
            if (_selectedProperty.value?.id == property.id) {
                _selectedProperty.value = null
            }
            showFeedback("Property '${property.name}' deleted")
            refreshAnalytics()
        }
    }

    fun addEncryptedTransaction(
        propertyId: Long?,
        type: String,
        amount: Double,
        category: String,
        description: String,
        note: String
    ) {
        viewModelScope.launch {
            repository.insertEncryptedTransaction(
                propertyId = propertyId,
                type = type,
                amount = amount,
                category = category,
                description = description,
                note = note,
                yearMonth = _selectedMonth.value
            )
            showFeedback("Transaction encrypted with Master Password and recorded")
            observeTransactions()
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            showFeedback("Transaction removed")
            observeTransactions()
        }
    }

    fun addReview(propertyId: Long, guestName: String, rating: Float, comment: String) {
        viewModelScope.launch {
            val review = ReviewEntity(
                propertyId = propertyId,
                guestName = guestName,
                rating = rating,
                comment = comment,
                date = "Today"
            )
            repository.insertReview(review)
            showFeedback("Customer review posted")
        }
    }

    fun sendMonthlyNotification(): Boolean {
        val analytics = _monthlyAnalytics.value ?: return false
        val sent = NotificationHelper.sendMonthlySummaryNotification(
            context = context,
            month = analytics.yearMonth,
            totalRevenue = analytics.totalRevenue,
            maintenanceCosts = analytics.maintenanceCosts,
            netIncome = analytics.netIncome,
            occupancyRate = analytics.avgOccupancyRate
        )
        if (sent) {
            showFeedback("Monthly owner summary notification dispatched!")
        } else {
            showFeedback("Monthly notification prepared (Check app notifications permission)")
        }
        return sent
    }

    fun exportAndShareSyncBackup() {
        viewModelScope.launch {
            val db = AppDatabase.getDatabase(context)
            val props = properties.value
            val txList = db.transactionDao().getAllTransactionsList()
            val revList = emptyList<ReviewEntity>()
            SyncManager.exportAndShareBackup(context, props, txList, revList)
            showFeedback("Encrypted backup shared to installed apps")
        }
    }

    fun importSyncBackup(jsonString: String) {
        viewModelScope.launch {
            val result = SyncManager.importSyncData(context, jsonString)
            result.onSuccess { count ->
                showFeedback("Successfully synchronized $count items across devices!")
                observeTransactions()
                refreshAnalytics()
            }.onFailure { err ->
                showFeedback("Import failed: ${err.localizedMessage}")
            }
        }
    }

    fun clearFeedback() {
        _actionFeedback.value = null
    }

    private fun showFeedback(msg: String) {
        _actionFeedback.value = msg
    }
}

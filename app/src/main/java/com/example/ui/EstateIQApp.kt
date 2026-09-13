package com.example.ui

import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auth.AuthManager
import com.example.data.model.PropertyEntity
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class ScreenTab(val title: String) {
    OVERVIEW("Overview"),
    PROPERTIES("Estates"),
    FINANCES("Finances"),
    ANALYTICS("Analytics"),
    SYNC_SECURITY("Security & Sync")
}

@Composable
fun EstateIQApp(viewModel: EstateViewModel = viewModel()) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val authUiState by viewModel.authUiState.collectAsStateWithLifecycle()
    val properties by viewModel.properties.collectAsStateWithLifecycle()
    val transactions by viewModel.decryptedTransactions.collectAsStateWithLifecycle()
    val analytics by viewModel.monthlyAnalytics.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val availableMonths by viewModel.availableMonths.collectAsStateWithLifecycle()
    val selectedProperty by viewModel.selectedProperty.collectAsStateWithLifecycle()
    val reviews by viewModel.propertyReviews.collectAsStateWithLifecycle()
    val actionFeedback by viewModel.actionFeedback.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(ScreenTab.OVERVIEW) }

    // Dialog states
    var showUnlockDialog by remember { mutableStateOf(false) }
    var showAddPropertyDialog by remember { mutableStateOf(false) }
    var propertyForResetLocation by remember { mutableStateOf<PropertyEntity?>(null) }
    var propertyForManagePhotos by remember { mutableStateOf<PropertyEntity?>(null) }
    var propertyForAddReview by remember { mutableStateOf<PropertyEntity?>(null) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var showSyncImportDialog by remember { mutableStateOf(false) }

    // Android 13+ Notification Permission Launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.sendMonthlyNotification()
        }
    }

    // Feedback Snackbar Effect
    LaunchedEffect(actionFeedback) {
        actionFeedback?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        containerColor = PolishBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("main_bottom_nav")
                    ) {
                        NavigationBarItem(
                            selected = currentTab == ScreenTab.OVERVIEW,
                            onClick = { currentTab = ScreenTab.OVERVIEW },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Overview") },
                            label = {
                                Text(
                                    ScreenTab.OVERVIEW.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (currentTab == ScreenTab.OVERVIEW) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Indigo600,
                                selectedTextColor = Indigo600,
                                unselectedIconColor = Slate400,
                                unselectedTextColor = Slate400,
                                indicatorColor = Indigo50
                            )
                        )

                        NavigationBarItem(
                            selected = currentTab == ScreenTab.PROPERTIES,
                            onClick = { currentTab = ScreenTab.PROPERTIES },
                            icon = { Icon(Icons.Default.Apartment, contentDescription = "Estates & BNB") },
                            label = {
                                Text(
                                    ScreenTab.PROPERTIES.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (currentTab == ScreenTab.PROPERTIES) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Indigo600,
                                selectedTextColor = Indigo600,
                                unselectedIconColor = Slate400,
                                unselectedTextColor = Slate400,
                                indicatorColor = Indigo50
                            )
                        )

                        NavigationBarItem(
                            selected = currentTab == ScreenTab.FINANCES,
                            onClick = { currentTab = ScreenTab.FINANCES },
                            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Finances") },
                            label = {
                                Text(
                                    ScreenTab.FINANCES.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (currentTab == ScreenTab.FINANCES) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Indigo600,
                                selectedTextColor = Indigo600,
                                unselectedIconColor = Slate400,
                                unselectedTextColor = Slate400,
                                indicatorColor = Indigo50
                            )
                        )

                        NavigationBarItem(
                            selected = currentTab == ScreenTab.ANALYTICS,
                            onClick = { currentTab = ScreenTab.ANALYTICS },
                            icon = { Icon(Icons.Default.Insights, contentDescription = "Analytics") },
                            label = {
                                Text(
                                    ScreenTab.ANALYTICS.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (currentTab == ScreenTab.ANALYTICS) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Indigo600,
                                selectedTextColor = Indigo600,
                                unselectedIconColor = Slate400,
                                unselectedTextColor = Slate400,
                                indicatorColor = Indigo50
                            )
                        )

                        NavigationBarItem(
                            selected = currentTab == ScreenTab.SYNC_SECURITY,
                            onClick = { currentTab = ScreenTab.SYNC_SECURITY },
                            icon = { Icon(Icons.Default.Security, contentDescription = "Security & Sync") },
                            label = {
                                Text(
                                    ScreenTab.SYNC_SECURITY.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (currentTab == ScreenTab.SYNC_SECURITY) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Indigo600,
                                selectedTextColor = Indigo600,
                                unselectedIconColor = Slate400,
                                unselectedTextColor = Slate400,
                                indicatorColor = Indigo50
                            )
                        )
                    }
                    Text(
                        text = "Dev: softinnocentsafari@gmail.com",
                        fontSize = 10.sp,
                        color = Slate400,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "TabCrossfade") { tab ->
                when (tab) {
                    ScreenTab.OVERVIEW -> {
                        HomeScreen(
                            authUiState = authUiState,
                            properties = properties,
                            transactions = transactions,
                            analytics = analytics,
                            onOpenUnlockDialog = { showUnlockDialog = true },
                            onLockSession = { viewModel.lockSession() },
                            onNavigateToProperties = { currentTab = ScreenTab.PROPERTIES },
                            onNavigateToFinances = { currentTab = ScreenTab.FINANCES },
                            onNavigateToAnalytics = { currentTab = ScreenTab.ANALYTICS },
                            onNavigateToSync = { currentTab = ScreenTab.SYNC_SECURITY },
                            onSelectProperty = { prop ->
                                viewModel.selectProperty(prop)
                                currentTab = ScreenTab.PROPERTIES
                            },
                            onOpenAddProperty = { showAddPropertyDialog = true },
                            onOpenAddTransaction = { showAddTransactionDialog = true },
                            onSendNotification = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.sendMonthlyNotification()
                                }
                            }
                        )
                    }

                    ScreenTab.PROPERTIES -> {
                        PropertiesScreen(
                            properties = properties,
                            selectedProperty = selectedProperty,
                            reviews = reviews,
                            onSelectProperty = { viewModel.selectProperty(it) },
                            onOpenAddProperty = { showAddPropertyDialog = true },
                            onOpenResetLocation = { propertyForResetLocation = it },
                            onOpenManagePhotos = { propertyForManagePhotos = it },
                            onOpenAddReview = { propertyForAddReview = it },
                            onDeleteProperty = { viewModel.deleteProperty(it) }
                        )
                    }

                    ScreenTab.FINANCES -> {
                        FinancesScreen(
                            authUiState = authUiState,
                            transactions = transactions,
                            properties = properties,
                            analytics = analytics,
                            selectedMonth = selectedMonth,
                            availableMonths = availableMonths,
                            onSelectMonth = { viewModel.selectMonth(it) },
                            onOpenUnlockDialog = { showUnlockDialog = true },
                            onOpenAddTransaction = { showAddTransactionDialog = true },
                            onDeleteTransaction = { viewModel.deleteTransaction(it) }
                        )
                    }

                    ScreenTab.ANALYTICS -> {
                        AnalyticsScreen(
                            analytics = analytics,
                            properties = properties,
                            selectedMonth = selectedMonth,
                            availableMonths = availableMonths,
                            onSelectMonth = { viewModel.selectMonth(it) },
                            onSendNotification = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.sendMonthlyNotification()
                                }
                            }
                        )
                    }

                    ScreenTab.SYNC_SECURITY -> {
                        SyncSecurityScreen(
                            authUiState = authUiState,
                            onLockSession = { viewModel.lockSession() },
                            onOpenUnlockDialog = { showUnlockDialog = true },
                            onChangeMasterPassword = { newP, confP ->
                                viewModel.setupOrChangeMasterPassword(newP, confP)
                            },
                            onToggleBiometric = { viewModel.toggleBiometric(it) },
                            onLinkGoogleAccount = { email, name ->
                                viewModel.linkGoogleAccount(email, name)
                            },
                            onUnlinkGoogleAccount = { viewModel.unlinkGoogleAccount() },
                            onExportSyncBackup = { viewModel.exportAndShareSyncBackup() },
                            onOpenImportSyncDialog = { showSyncImportDialog = true }
                        )
                    }
                }
            }
        }
    }

    // Unlock Dialog
    if (showUnlockDialog) {
        AuthLockDialog(
            isSetup = authUiState.isSetup,
            errorMessage = authUiState.errorMessage,
            onUnlock = { pass ->
                viewModel.unlockWithPassword(pass)
                if (authUiState.errorMessage == null) {
                    showUnlockDialog = false
                }
            },
            onBiometricUnlock = {
                if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    AuthManager.showBiometricPrompt(
                        activity = activity,
                        onSuccess = {
                            viewModel.unlockWithBiometric()
                            showUnlockDialog = false
                        },
                        onError = {
                            // fallback unlock
                            viewModel.unlockWithBiometric()
                            showUnlockDialog = false
                        }
                    )
                } else {
                    viewModel.unlockWithBiometric()
                    showUnlockDialog = false
                }
            },
            onDismiss = { showUnlockDialog = false }
        )
    }

    // Add Property Dialog
    if (showAddPropertyDialog) {
        AddPropertyDialog(
            onDismiss = { showAddPropertyDialog = false },
            onAddProperty = { name, type, address, lat, lng, total, occ, rate, desc, photos, phone, email, wa ->
                viewModel.addProperty(
                    name = name,
                    type = type,
                    address = address,
                    latitude = lat,
                    longitude = lng,
                    totalUnits = total,
                    occupiedUnits = occ,
                    monthlyRate = rate,
                    description = desc,
                    photos = photos,
                    ownerPhone = phone,
                    ownerEmail = email,
                    ownerWhatsApp = wa
                )
            }
        )
    }

    // Reset Location Dialog
    propertyForResetLocation?.let { prop ->
        ResetLocationDialog(
            property = prop,
            onDismiss = { propertyForResetLocation = null },
            onSaveLocation = { propId, addr, lat, lng ->
                viewModel.updatePropertyLocation(propId, addr, lat, lng)
            }
        )
    }

    // Manage Photos Dialog
    propertyForManagePhotos?.let { prop ->
        ManagePhotosDialog(
            property = prop,
            onDismiss = { propertyForManagePhotos = null },
            onAddPhoto = { propId, uri ->
                viewModel.addPhotoToProperty(propId, uri)
            },
            onRemovePhoto = { propId, uri ->
                viewModel.removePhotoFromProperty(propId, uri)
            }
        )
    }

    // Add Review Dialog
    propertyForAddReview?.let { prop ->
        AddReviewDialog(
            property = prop,
            onDismiss = { propertyForAddReview = null },
            onSubmitReview = { guest, rating, comment ->
                viewModel.addReview(prop.id, guest, rating, comment)
            }
        )
    }

    // Add Transaction Dialog
    if (showAddTransactionDialog) {
        AddTransactionDialog(
            properties = properties,
            onDismiss = { showAddTransactionDialog = false },
            onAddTransaction = { propId, type, amount, category, desc, note ->
                viewModel.addEncryptedTransaction(
                    propertyId = propId,
                    type = type,
                    amount = amount,
                    category = category,
                    description = desc,
                    note = note
                )
            }
        )
    }

    // Sync Import Dialog
    if (showSyncImportDialog) {
        SyncImportDialog(
            onDismiss = { showSyncImportDialog = false },
            onImportPayload = { payload ->
                viewModel.importSyncBackup(payload)
            }
        )
    }
}

package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.MonthlyAnalytics
import com.example.data.model.DecryptedTransaction
import com.example.data.model.PropertyEntity
import com.example.ui.AuthUiState
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    authUiState: AuthUiState,
    properties: List<PropertyEntity>,
    transactions: List<DecryptedTransaction>,
    analytics: MonthlyAnalytics?,
    onOpenUnlockDialog: () -> Unit,
    onLockSession: () -> Unit,
    onNavigateToProperties: () -> Unit,
    onNavigateToFinances: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSync: () -> Unit,
    onSelectProperty: (PropertyEntity) -> Unit,
    onOpenAddProperty: () -> Unit,
    onOpenAddTransaction: () -> Unit,
    onSendNotification: () -> Unit
) {
    val currencyFormat = NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 }
    val totalUnits = properties.sumOf { it.totalUnits }
    val occupiedUnits = properties.sumOf { it.occupiedUnits }
    val occupancyRate = if (totalUnits > 0) (occupiedUnits.toDouble() / totalUnits) * 100.0 else 0.0

    val netProfit = (analytics?.totalRevenue ?: 0.0) - (analytics?.maintenanceCosts ?: 0.0) - (analytics?.operatingExpenses ?: 0.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PolishBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 96.dp)
    ) {
        // Professional Polish Top Bar
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, PolishBorderLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "VAULT FINANCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Indigo600,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Estate Dashboard",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate900
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Lock Status Indicator Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Indigo50)
                                .border(1.dp, Indigo100, CircleShape)
                                .clickable {
                                    if (authUiState.isUnlocked) onLockSession() else onOpenUnlockDialog()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (authUiState.isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = if (authUiState.isUnlocked) "Lock Vault" else "Unlock Vault",
                                tint = if (authUiState.isUnlocked) Emerald600 else Indigo600,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // User Initials Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .shadow(2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Slate200)
                                .border(2.dp, Color.White, CircleShape)
                                .clickable { onNavigateToSync() },
                            contentAlignment = Alignment.Center
                        ) {
                            val initial = authUiState.linkedGoogleAccount?.second?.take(1) ?: "IS"
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Slate400),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initial,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Indigo Hero Financial Metric Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Indigo200)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Indigo600, Indigo700)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "Monthly Revenue (${analytics?.yearMonth ?: "Current"})",
                                color = Indigo100,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "KES ${currencyFormat.format(analytics?.totalRevenue ?: 0.0)}",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (authUiState.isUnlocked) "ENCRYPTED" else "LOCKED",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                letterSpacing = 0.6.sp
                            )
                        }
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Indigo500.copy(alpha = 0.5f))
                    )

                    // 2-Column Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "MAINTENANCE",
                                color = Indigo200,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "KES ${currencyFormat.format(analytics?.maintenanceCosts ?: 0.0)}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NET PROFIT",
                                color = Indigo200,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${if (netProfit >= 0) "+" else ""}KES ${currencyFormat.format(netProfit)}",
                                color = Emerald300,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Portfolio Stats Grid (Units & Occupancy)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "PORTFOLIO UNITS",
                    value = "$totalUnits",
                    subtitle = "${properties.size} properties registered",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "OCCUPANCY RATE",
                    value = "%.0f%%".format(occupancyRate),
                    subtitle = "$occupiedUnits of $totalUnits let",
                    accentColor = Emerald600,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Operations Chips
        item {
            Column {
                Text(
                    text = "QUICK OPERATIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionChip(
                        icon = Icons.Default.AddBusiness,
                        label = "Add Estate",
                        onClick = onOpenAddProperty,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionChip(
                        icon = Icons.Default.Lock,
                        label = "Add Expense",
                        onClick = onOpenAddTransaction,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionChip(
                        icon = Icons.Default.NotificationsActive,
                        label = "Notify",
                        onClick = onSendNotification,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionChip(
                        icon = Icons.Default.Sync,
                        label = "Sync Apps",
                        onClick = onNavigateToSync,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Your Properties Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "YOUR PROPERTIES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    letterSpacing = 1.sp
                )
                TextButton(
                    onClick = onOpenAddProperty,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "+ Add New",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Indigo600
                    )
                }
            }
        }

        // Properties List Cards matching the Professional Polish HTML pattern
        if (properties.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, PolishBorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No properties registered yet. Tap '+ Add New' to begin.",
                            color = Slate500,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(properties) { property ->
                PropertyPolishCard(
                    property = property,
                    onClick = {
                        onSelectProperty(property)
                        onNavigateToProperties()
                    }
                )
            }
        }

        // Data Privacy System Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSync() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Slate700),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Security Verified",
                            tint = Emerald400,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DATA PRIVACY SYSTEM",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate400,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "AES-256 Offline Encryption Active",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Recent Encrypted Transactions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT TRANSACTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    letterSpacing = 1.sp
                )
                TextButton(
                    onClick = onNavigateToFinances,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "View Ledger",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Indigo600
                    )
                }
            }
        }

        if (transactions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, PolishBorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No transactions recorded yet this month.",
                            color = Slate500,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(transactions.take(4)) { tx ->
                TransactionItemCard(transaction = tx)
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    accentColor: Color = Slate900
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, PolishBorderLight),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Slate500,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Slate400
            )
        }
    }
}

@Composable
fun QuickActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, PolishBorderLight),
        shadowElevation = 1.dp,
        modifier = modifier.height(68.dp)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = Indigo600, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Slate800,
                maxLines = 1
            )
        }
    }
}

/**
 * Property item card conforming exactly to the Professional Polish design:
 * White background, 16.dp rounded corners, slate-100 border, 64x64 thumbnail,
 * slate-800 title, slate-500 location with pin, emerald-50 occupancy pill,
 * slate-100 rate pill, slate-300 chevron.
 */
@Composable
fun PropertyPolishCard(
    property: PropertyEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, PolishBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("property_card_${property.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Thumbnail with rounded-xl (12.dp)
            val photos = property.getPhotos()
            val firstPhoto = photos.firstOrNull()
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate200)
            ) {
                when (firstPhoto) {
                    "preset_villa1" -> {
                        Image(
                            painter = painterResource(id = R.drawable.preset_villa1),
                            contentDescription = property.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    "preset_bnb1" -> {
                        Image(
                            painter = painterResource(id = R.drawable.preset_bnb1),
                            contentDescription = property.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    null -> {
                        Icon(
                            imageVector = Icons.Default.Apartment,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.Center)
                        )
                    }
                    else -> {
                        AsyncImage(
                            model = firstPhoto,
                            contentDescription = property.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Info Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = property.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Slate800,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Slate500,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = property.address,
                        fontSize = 11.sp,
                        color = Slate500,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Occupancy Status Badge
                    Surface(
                        color = Emerald50,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = if (property.occupiedUnits > 0) "Occupied (${property.occupiedUnits}/${property.totalUnits})" else "Vacant",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald700,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    // Rate Badge
                    Surface(
                        color = Slate100,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "KES ${property.monthlyRate.toInt()}/mo",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate600,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Chevron Right
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Slate300,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun TransactionItemCard(transaction: DecryptedTransaction) {
    val isIncome = transaction.type == "INCOME"
    val currencyFormat = NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, PolishBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isIncome) Emerald50 else Rose50),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (isIncome) Emerald600 else Rose600,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.description,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Slate800,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "AES-256 Encrypted",
                        tint = Indigo600,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Text(
                    text = "${transaction.category} • ${transaction.referenceNo}",
                    fontSize = 11.sp,
                    color = Slate500
                )
            }

            Text(
                text = "${if (isIncome) "+" else "-"}KES ${currencyFormat.format(transaction.amount)}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isIncome) Emerald600 else Rose600
            )
        }
    }
}

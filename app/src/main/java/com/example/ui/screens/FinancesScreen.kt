package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MonthlyAnalytics
import com.example.data.model.DecryptedTransaction
import com.example.data.model.PropertyEntity
import com.example.ui.AuthUiState
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancesScreen(
    authUiState: AuthUiState,
    transactions: List<DecryptedTransaction>,
    properties: List<PropertyEntity>,
    analytics: MonthlyAnalytics?,
    selectedMonth: String,
    availableMonths: List<String>,
    onSelectMonth: (String) -> Unit,
    onOpenUnlockDialog: () -> Unit,
    onOpenAddTransaction: () -> Unit,
    onDeleteTransaction: (Long) -> Unit
) {
    val currencyFormat = NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 }
    var filterType by remember { mutableStateOf("ALL") }

    val filteredTransactions = remember(transactions, filterType) {
        when (filterType) {
            "INCOME" -> transactions.filter { it.type == "INCOME" }
            "MAINTENANCE" -> transactions.filter { it.type == "MAINTENANCE" }
            "EXPENSES" -> transactions.filter { it.type != "INCOME" }
            else -> transactions
        }
    }

    Scaffold(
        containerColor = PolishBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddTransaction,
                containerColor = Indigo600,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("add_transaction_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Record Entry", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "FINANCIAL LEDGER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Indigo600,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Encrypted Records & Expenses",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Client-side encrypted with AES-256 GCM using your Master Password. Zero external API keys.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }
            }

            // Security Vault Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (authUiState.isUnlocked) Slate800 else Rose50
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (authUiState.isUnlocked) Slate700 else Rose100),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (authUiState.isUnlocked) Icons.Default.VerifiedUser else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (authUiState.isUnlocked) Emerald400 else Rose600,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (authUiState.isUnlocked) "AES-256 Vault Active" else "Vault Locked",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (authUiState.isUnlocked) Color.White else Rose700
                                )
                                Text(
                                    text = if (authUiState.isUnlocked) "Zero-knowledge key active in volatile memory" else "Unlock to decrypt financial ledger",
                                    fontSize = 11.sp,
                                    color = if (authUiState.isUnlocked) Slate400 else Rose600
                                )
                            }
                        }

                        if (!authUiState.isUnlocked) {
                            Button(
                                onClick = onOpenUnlockDialog,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Rose600),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Unlock", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Month Selector Bar
            item {
                Column {
                    Text(
                        text = "REPORTING PERIOD",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Slate500,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val months = availableMonths.ifEmpty { listOf(selectedMonth) }
                        items(months) { month ->
                            FilterChip(
                                selected = selectedMonth == month,
                                onClick = { onSelectMonth(month) },
                                label = { Text(month, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Indigo600,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = Slate700
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedMonth == month,
                                    borderColor = if (selectedMonth == month) Indigo600 else PolishBorderLight
                                )
                            )
                        }
                    }
                }
            }

            // Financial Summary Cards
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, PolishBorderLight),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("TOTAL REVENUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate500, letterSpacing = 0.6.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "KES ${currencyFormat.format(analytics?.totalRevenue ?: 0.0)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald600
                            )
                            Text("Rent & BNB income", fontSize = 10.sp, color = Slate400)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, PolishBorderLight),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("MAINTENANCE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate500, letterSpacing = 0.6.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "KES ${currencyFormat.format(analytics?.maintenanceCosts ?: 0.0)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Rose600
                            )
                            Text("Repairs & upkeep", fontSize = 10.sp, color = Slate400)
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, PolishBorderLight),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("UTILITIES & TAX", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate500, letterSpacing = 0.6.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            val utilityTotal = (analytics?.utilityCosts ?: 0.0) + (analytics?.taxCosts ?: 0.0)
                            Text(
                                text = "KES ${currencyFormat.format(utilityTotal)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Amber600
                            )
                            Text("Power, water, taxes", fontSize = 10.sp, color = Slate400)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("NET INCOME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate400, letterSpacing = 0.6.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "KES ${currencyFormat.format(analytics?.netIncome ?: 0.0)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald400
                            )
                            Text("Net profit margin", fontSize = 10.sp, color = Slate400)
                        }
                    }
                }
            }

            // Ledger Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL" to "All", "INCOME" to "Revenue", "MAINTENANCE" to "Maintenance", "EXPENSES" to "Expenses").forEach { (typeKey, typeName) ->
                        FilterChip(
                            selected = filterType == typeKey,
                            onClick = { filterType = typeKey },
                            label = { Text(typeName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Indigo600,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Slate700
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = filterType == typeKey,
                                borderColor = if (filterType == typeKey) Indigo600 else PolishBorderLight
                            )
                        )
                    }
                }
            }

            // Transactions Listing
            if (filteredTransactions.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, PolishBorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = Slate400, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No transactions recorded for $selectedMonth", fontWeight = FontWeight.SemiBold, color = Slate800)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tap 'Record Entry' below to add an encrypted transaction.", color = Slate500, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(filteredTransactions, key = { it.id }) { tx ->
                    val isIncome = tx.type == "INCOME"
                    val propName = properties.firstOrNull { it.id == tx.propertyId }?.name ?: "Personal / Unassigned"

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, PolishBorderLight),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isIncome) Emerald50 else Rose50),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (tx.type) {
                                        "INCOME" -> Icons.Default.TrendingUp
                                        "MAINTENANCE" -> Icons.Default.Build
                                        "UTILITY" -> Icons.Default.Bolt
                                        "TAX" -> Icons.Default.AccountBalance
                                        else -> Icons.Default.Receipt
                                    },
                                    contentDescription = null,
                                    tint = if (isIncome) Emerald600 else Rose600,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tx.description,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Slate800
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Encrypted",
                                        tint = Indigo600,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Text(
                                    text = "${tx.category} • $propName",
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                                if (tx.note.isNotBlank()) {
                                    Text(
                                        text = "Memo: ${tx.note}",
                                        fontSize = 11.sp,
                                        color = Slate400,
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = "Ref: ${tx.referenceNo}",
                                    fontSize = 10.sp,
                                    color = Slate400
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${if (isIncome) "+" else "-"}KES ${currencyFormat.format(tx.amount)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isIncome) Emerald600 else Rose600
                                )
                                IconButton(
                                    onClick = { onDeleteTransaction(tx.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = Slate400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

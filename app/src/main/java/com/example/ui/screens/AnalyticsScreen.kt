package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MonthlyAnalytics
import com.example.data.model.PropertyEntity
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AnalyticsScreen(
    analytics: MonthlyAnalytics?,
    properties: List<PropertyEntity>,
    selectedMonth: String,
    availableMonths: List<String>,
    onSelectMonth: (String) -> Unit,
    onSendNotification: () -> Unit
) {
    val currencyFormat = NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 }
    val revenue = analytics?.totalRevenue ?: 0.0
    val maintenance = analytics?.maintenanceCosts ?: 0.0
    val utility = analytics?.utilityCosts ?: 0.0
    val tax = analytics?.taxCosts ?: 0.0
    val other = analytics?.otherExpenses ?: 0.0
    val totalExpenses = maintenance + utility + tax + other
    val netIncome = analytics?.netIncome ?: 0.0

    val maintenanceRatio = if (revenue > 0) (maintenance / revenue) * 100.0 else 0.0
    val profitMargin = if (revenue > 0) (netIncome / revenue) * 100.0 else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PolishBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "OFFLINE ANALYTICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Indigo600,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Revenue & Expenditure Reports",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "High-precision financial analytics calculated locally on device with complete cryptographic privacy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }
        }

        // Period Selector
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
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

        // Owner Monthly Summary Notification Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Indigo600, Indigo700)
                        )
                    )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Emerald300,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Monthly Owner Summary",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = selectedMonth,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Automated monthly notification dispatched to the owner summarizing total revenue and maintenance costs.",
                        color = Indigo100,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("TOTAL REVENUE", fontSize = 10.sp, color = Indigo200, fontWeight = FontWeight.Bold)
                            Text("KES ${currencyFormat.format(revenue)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("MAINTENANCE", fontSize = 10.sp, color = Indigo200, fontWeight = FontWeight.Bold)
                            Text("KES ${currencyFormat.format(maintenance)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Amber300)
                        }
                        Column {
                            Text("NET PROFIT", fontSize = 10.sp, color = Indigo200, fontWeight = FontWeight.Bold)
                            Text("KES ${currencyFormat.format(netIncome)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Emerald300)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onSendNotification,
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("send_notification_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Dispatch Owner Notification Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Visual Expenditure Comparison Chart
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, PolishBorderLight),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Revenue vs Maintenance vs Net Profit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Slate800
                    )
                    Text(
                        text = "Visual bar comparison for $selectedMonth",
                        fontSize = 11.sp,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    val maxValue = maxOf(revenue, totalExpenses, 1.0)

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        val barWidth = size.width / 4f - 16f
                        val heightRatio = (size.height - 30f) / maxValue.toFloat()

                        val revHeight = (revenue.toFloat() * heightRatio).coerceAtLeast(10f)
                        val mainHeight = (maintenance.toFloat() * heightRatio).coerceAtLeast(10f)
                        val expHeight = (totalExpenses.toFloat() * heightRatio).coerceAtLeast(10f)
                        val netHeight = (netIncome.coerceAtLeast(0.0).toFloat() * heightRatio).coerceAtLeast(10f)

                        // 1. Revenue Bar (Emerald)
                        val x1 = 12f
                        drawRoundRect(
                            color = Emerald600,
                            topLeft = Offset(x1, size.height - revHeight - 20f),
                            size = Size(barWidth, revHeight),
                            cornerRadius = CornerRadius(8f, 8f)
                        )

                        // 2. Maintenance Bar (Amber)
                        val x2 = x1 + barWidth + 16f
                        drawRoundRect(
                            color = Amber500,
                            topLeft = Offset(x2, size.height - mainHeight - 20f),
                            size = Size(barWidth, mainHeight),
                            cornerRadius = CornerRadius(8f, 8f)
                        )

                        // 3. Total Expenses Bar (Rose)
                        val x3 = x2 + barWidth + 16f
                        drawRoundRect(
                            color = Rose600,
                            topLeft = Offset(x3, size.height - expHeight - 20f),
                            size = Size(barWidth, expHeight),
                            cornerRadius = CornerRadius(8f, 8f)
                        )

                        // 4. Net Operating Margin (Indigo)
                        val x4 = x3 + barWidth + 16f
                        drawRoundRect(
                            color = Indigo600,
                            topLeft = Offset(x4, size.height - netHeight - 20f),
                            size = Size(barWidth, netHeight),
                            cornerRadius = CornerRadius(8f, 8f)
                        )

                        // Baseline line
                        drawLine(
                            color = PolishBorderLight,
                            start = Offset(0f, size.height - 20f),
                            end = Offset(size.width, size.height - 20f),
                            strokeWidth = 2f
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ChartLegendItem(color = Emerald600, label = "Revenue")
                        ChartLegendItem(color = Amber500, label = "Maintenance")
                        ChartLegendItem(color = Rose600, label = "Expenses")
                        ChartLegendItem(color = Indigo600, label = "Net Income")
                    }
                }
            }
        }

        // Expenditure Breakdown Distribution
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, PolishBorderLight),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Monthly Expenditure Composition",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Slate800
                    )
                    Text(
                        text = "Breakdown of operating and maintenance costs",
                        fontSize = 11.sp,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ExpenseProgressRow(
                        category = "Property Maintenance & Repairs",
                        amount = maintenance,
                        total = if (totalExpenses > 0) totalExpenses else 1.0,
                        color = Amber500
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ExpenseProgressRow(
                        category = "Utilities (Power, Water, Internet)",
                        amount = utility,
                        total = if (totalExpenses > 0) totalExpenses else 1.0,
                        color = Indigo600
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ExpenseProgressRow(
                        category = "County Taxes, Rates & Levies",
                        amount = tax,
                        total = if (totalExpenses > 0) totalExpenses else 1.0,
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ExpenseProgressRow(
                        category = "Security, Insurance & Other",
                        amount = other,
                        total = if (totalExpenses > 0) totalExpenses else 1.0,
                        color = Rose600
                    )
                }
            }
        }

        // Operational KPIs
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, PolishBorderLight),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Operational Health Indicators",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Slate800
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        KpiItem(
                            title = "Maintenance / Revenue",
                            value = "%.1f%%".format(maintenanceRatio),
                            status = if (maintenanceRatio <= 15) "Optimal (<15%)" else "Attention (>15%)",
                            isHealthy = maintenanceRatio <= 15
                        )
                        KpiItem(
                            title = "Net Operating Margin",
                            value = "%.1f%%".format(profitMargin),
                            status = if (profitMargin >= 65) "Healthy (>65%)" else "Moderate",
                            isHealthy = profitMargin >= 65
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChartLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, color = Slate500, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ExpenseProgressRow(
    category: String,
    amount: Double,
    total: Double,
    color: Color
) {
    val currencyFormat = NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 }
    val percentage = (amount / total).toFloat().coerceIn(0f, 1f)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = category, fontSize = 12.sp, color = Slate800)
            Text(
                text = "KES ${currencyFormat.format(amount)} (${(percentage * 100).toInt()}%)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = PolishBorderLight
        )
    }
}

@Composable
fun KpiItem(
    title: String,
    value: String,
    status: String,
    isHealthy: Boolean
) {
    Column {
        Text(text = title, fontSize = 11.sp, color = Slate500)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isHealthy) Emerald600 else Amber600)
        Text(text = status, fontSize = 10.sp, color = if (isHealthy) Emerald600 else Amber600, fontWeight = FontWeight.Medium)
    }
}

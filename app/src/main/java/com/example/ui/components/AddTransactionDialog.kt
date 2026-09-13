package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PropertyEntity
import com.example.ui.theme.Indigo50
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    properties: List<PropertyEntity>,
    onDismiss: () -> Unit,
    onAddTransaction: (
        propertyId: Long?,
        type: String,
        amount: Double,
        category: String,
        description: String,
        note: String
    ) -> Unit
) {
    val transactionTypes = listOf(
        "INCOME" to "Revenue (Rent / BNB Booking)",
        "MAINTENANCE" to "Maintenance & Repairs",
        "UTILITY" to "Utility (Power, Water, Internet)",
        "TAX" to "Property Tax & Levies",
        "OTHER_EXPENSE" to "Other Operational Expense"
    )
    var selectedType by remember { mutableStateOf("INCOME") }
    var typeExpanded by remember { mutableStateOf(false) }

    var selectedPropertyId by remember { mutableStateOf<Long?>(properties.firstOrNull()?.id) }
    var propertyExpanded by remember { mutableStateOf(false) }

    val defaultCategories = when (selectedType) {
        "INCOME" -> listOf("Rent Collection", "BNB Booking", "Deposit", "Parking Fee")
        "MAINTENANCE" -> listOf("Plumbing Repair", "Electrical Fix", "Painting & Roofing", "AC Servicing", "Carpentry")
        "UTILITY" -> listOf("Water & Borehole", "Common Electricity", "Fiber Internet", "Garbage Collection")
        "TAX" -> listOf("Land Rates", "Rental Income Tax", "County Council Levy")
        else -> listOf("Security Guard Service", "Cleaning Supplies", "Insurance Premium", "Miscellaneous")
    }

    var selectedCategory by remember { mutableStateOf(defaultCategories.first()) }
    var categoryExpanded by remember { mutableStateOf(false) }

    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Indigo600)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record Encrypted Transaction", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Slate900)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Encryption badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Indigo50, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Indigo600, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Zero-Knowledge: Encrypted with Master Password (AES-256 GCM)",
                        fontSize = 11.sp,
                        color = Slate900,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = transactionTypes.firstOrNull { it.first == selectedType }?.second ?: selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Transaction Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        transactionTypes.forEach { (typeKey, typeLabel) ->
                            DropdownMenuItem(
                                text = { Text(typeLabel) },
                                onClick = {
                                    selectedType = typeKey
                                    selectedCategory = when (typeKey) {
                                        "INCOME" -> "Rent Collection"
                                        "MAINTENANCE" -> "Plumbing Repair"
                                        "UTILITY" -> "Water & Borehole"
                                        "TAX" -> "Rental Income Tax"
                                        else -> "Security Guard Service"
                                    }
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Property Dropdown
                ExposedDropdownMenuBox(
                    expanded = propertyExpanded,
                    onExpandedChange = { propertyExpanded = !propertyExpanded }
                ) {
                    val propertyName = properties.firstOrNull { it.id == selectedPropertyId }?.name ?: "General (Unassigned)"
                    OutlinedTextField(
                        value = propertyName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Associated Estate / BNB") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = propertyExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = propertyExpanded,
                        onDismissRequest = { propertyExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("General Personal Finance") },
                            onClick = {
                                selectedPropertyId = null
                                propertyExpanded = false
                            }
                        )
                        properties.forEach { prop ->
                            DropdownMenuItem(
                                text = { Text("${prop.name} (${prop.type})") },
                                onClick = {
                                    selectedPropertyId = prop.id
                                    propertyExpanded = false
                                }
                            )
                        }
                    }
                }

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        defaultCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (KES) *") },
                    placeholder = { Text("e.g. 42000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_amount_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description *") },
                    placeholder = { Text("e.g. Unit 4B May Rent payment") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Private Memo / Notes (Encrypted)") },
                    placeholder = { Text("e.g. Paid via M-Pesa Ref QK9812") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (amt > 0 && description.isNotBlank()) {
                        onAddTransaction(
                            selectedPropertyId,
                            selectedType,
                            amt,
                            selectedCategory,
                            description,
                            note
                        )
                        onDismiss()
                    }
                },
                enabled = (amount.toDoubleOrNull() ?: 0.0) > 0 && description.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("submit_transaction_button")
            ) {
                Text("Encrypt & Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

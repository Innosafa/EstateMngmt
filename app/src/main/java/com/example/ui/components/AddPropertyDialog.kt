package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Indigo600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPropertyDialog(
    onDismiss: () -> Unit,
    onAddProperty: (
        name: String,
        type: String,
        address: String,
        latitude: Double,
        longitude: Double,
        totalUnits: Int,
        occupiedUnits: Int,
        monthlyRate: Double,
        description: String,
        photos: List<String>,
        ownerPhone: String,
        ownerEmail: String,
        ownerWhatsApp: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val types = listOf("BNB", "Apartment", "Estate", "Villa", "Commercial")
    var selectedType by remember { mutableStateOf("BNB") }
    var typeExpanded by remember { mutableStateOf(false) }

    var address by remember { mutableStateOf("Kilimani, Nairobi, Kenya") }
    var latitude by remember { mutableStateOf("-1.2921") }
    var longitude by remember { mutableStateOf("36.8219") }
    var totalUnits by remember { mutableStateOf("6") }
    var occupiedUnits by remember { mutableStateOf("5") }
    var monthlyRate by remember { mutableStateOf("45000") }
    var description by remember { mutableStateOf("") }
    var ownerPhone by remember { mutableStateOf("+254 712 345 678") }
    var ownerEmail by remember { mutableStateOf("softinnocentsafari@gmail.com") }
    var ownerWhatsApp by remember { mutableStateOf("+254712345678") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Apartment, contentDescription = null, tint = Indigo600)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Estate / BNB Property", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Property / BNB Name *") },
                    placeholder = { Text("e.g. Serengeti Luxury BNB") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("property_name_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Property Category") },
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
                        types.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Location section with quick reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Location & GPS Pin",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Indigo600
                    )
                    TextButton(
                        onClick = {
                            // Reset to preset coordinates based on selection
                            if (selectedType == "BNB") {
                                address = "Beach Road, Diani Beach, South Coast"
                                latitude = "-4.2778"
                                longitude = "39.5936"
                            } else {
                                address = "Kilimani, Nairobi, Kenya"
                                latitude = "-1.2905"
                                longitude = "36.7865"
                            }
                        }
                    ) {
                        Icon(Icons.Default.AddLocationAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset Location", fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Physical Address / Location *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        label = { Text("Latitude") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = { Text("Longitude") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = totalUnits,
                        onValueChange = { totalUnits = it },
                        label = { Text("Total Units") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = occupiedUnits,
                        onValueChange = { occupiedUnits = it },
                        label = { Text("Occupied") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = monthlyRate,
                    onValueChange = { monthlyRate = it },
                    label = { Text("Monthly Rate / Booking Base (KES)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Amenities") },
                    placeholder = { Text("e.g. Swimming pool, solar heating, fiber internet...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text(
                    text = "Owner Contact Channels",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Indigo600
                )

                OutlinedTextField(
                    value = ownerPhone,
                    onValueChange = { ownerPhone = it },
                    label = { Text("Owner Phone (Call/SMS)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = ownerWhatsApp,
                    onValueChange = { ownerWhatsApp = it },
                    label = { Text("Owner WhatsApp") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = ownerEmail,
                    onValueChange = { ownerEmail = it },
                    label = { Text("Owner Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && address.isNotBlank()) {
                        val lat = latitude.toDoubleOrNull() ?: -1.2921
                        val lng = longitude.toDoubleOrNull() ?: 36.8219
                        val total = totalUnits.toIntOrNull() ?: 1
                        val occ = occupiedUnits.toIntOrNull() ?: 0
                        val rate = monthlyRate.toDoubleOrNull() ?: 0.0
                        val initialPhoto = if (selectedType == "BNB") "preset_bnb1" else "preset_villa1"

                        onAddProperty(
                            name,
                            selectedType,
                            address,
                            lat,
                            lng,
                            total,
                            occ,
                            rate,
                            description,
                            listOf(initialPhoto),
                            ownerPhone,
                            ownerEmail,
                            ownerWhatsApp
                        )
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("submit_property_button")
            ) {
                Text("Save Property")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

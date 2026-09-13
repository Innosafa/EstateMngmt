package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PropertyEntity
import com.example.ui.theme.Indigo600

@Composable
fun ResetLocationDialog(
    property: PropertyEntity,
    onDismiss: () -> Unit,
    onSaveLocation: (propertyId: Long, newAddress: String, lat: Double, lng: Double) -> Unit
) {
    var address by remember { mutableStateOf(property.address) }
    var latitude by remember { mutableStateOf(property.latitude.toString()) }
    var longitude by remember { mutableStateOf(property.longitude.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddLocationAlt, contentDescription = null, tint = Indigo600)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset Property Location", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Update the physical address and geographical coordinates for ${property.name}. This resets location mapping across reports.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Updated Address / Area") },
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

                // Quick preset buttons for convenience
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            address = "Nairobi Central, Kenya"
                            latitude = "-1.286389"
                            longitude = "36.817223"
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Nairobi CBD", fontSize = 11.sp)
                    }
                    FilledTonalButton(
                        onClick = {
                            address = "Diani Beach Resort Strip, Coast"
                            latitude = "-4.2778"
                            longitude = "39.5936"
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Diani Coast", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lat = latitude.toDoubleOrNull() ?: property.latitude
                    val lng = longitude.toDoubleOrNull() ?: property.longitude
                    onSaveLocation(property.id, address, lat, lng)
                    onDismiss()
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Confirm Location Reset")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

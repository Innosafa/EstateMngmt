package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Indigo600

@Composable
fun SyncImportDialog(
    onDismiss: () -> Unit,
    onImportPayload: (String) -> Unit
) {
    var jsonPayload by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Sync, contentDescription = null, tint = Indigo600)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sync Data Across Devices", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Paste an EstateIQ encrypted synchronization backup received from other installed apps (Google Drive, WhatsApp, Email, or File Manager) to merge records across multiple devices and user accounts.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = jsonPayload,
                    onValueChange = { jsonPayload = it },
                    label = { Text("Backup Sync JSON Payload") },
                    placeholder = { Text("{\n  \"app\": \"EstateIQ\",\n  \"properties\": [...]\n}") },
                    minLines = 6,
                    maxLines = 10,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (jsonPayload.isNotBlank()) {
                        onImportPayload(jsonPayload)
                        onDismiss()
                    }
                },
                enabled = jsonPayload.isNotBlank(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Merge & Sync")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

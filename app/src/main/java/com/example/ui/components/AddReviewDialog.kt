package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PropertyEntity
import com.example.ui.theme.Amber500
import com.example.ui.theme.Indigo600

@Composable
fun AddReviewDialog(
    property: PropertyEntity,
    onDismiss: () -> Unit,
    onSubmitReview: (guestName: String, rating: Float, comment: String) -> Unit
) {
    var guestName by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.RateReview, contentDescription = null, tint = Indigo600)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Review for ${property.name}", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = guestName,
                    onValueChange = { guestName = it },
                    label = { Text("Guest / Tenant Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text(
                    text = "Rating ($rating / 5 Stars)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= rating) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Star $i",
                            tint = Amber500,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { rating = i }
                                .padding(2.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Feedback & Review Comments *") },
                    placeholder = { Text("Describe the experience, amenities, hygiene, or customer service...") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (guestName.isNotBlank() && comment.isNotBlank()) {
                        onSubmitReview(guestName, rating.toFloat(), comment)
                        onDismiss()
                    }
                },
                enabled = guestName.isNotBlank() && comment.isNotBlank(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Post Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

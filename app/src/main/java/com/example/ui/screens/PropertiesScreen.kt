package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.PropertyEntity
import com.example.data.model.ReviewEntity
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertiesScreen(
    properties: List<PropertyEntity>,
    selectedProperty: PropertyEntity?,
    reviews: List<ReviewEntity>,
    onSelectProperty: (PropertyEntity?) -> Unit,
    onOpenAddProperty: () -> Unit,
    onOpenResetLocation: (PropertyEntity) -> Unit,
    onOpenManagePhotos: (PropertyEntity) -> Unit,
    onOpenAddReview: (PropertyEntity) -> Unit,
    onDeleteProperty: (PropertyEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "BNB", "Apartment", "Villa", "Estate")

    val filteredProperties = remember(properties, selectedFilter) {
        if (selectedFilter == "All") properties else properties.filter { it.type.equals(selectedFilter, ignoreCase = true) }
    }

    Scaffold(
        containerColor = PolishBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddProperty,
                containerColor = Indigo600,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("add_property_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "Add Property")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Property", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "ESTATE & BNB PORTFOLIO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Indigo600,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Properties & Locations",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Manage estates, BNBs, reset GPS coordinates upon addition, and manage photo galleries.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }
            }

            // Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Indigo600,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Slate700
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedFilter == filter,
                                borderColor = if (selectedFilter == filter) Indigo600 else PolishBorderLight
                            )
                        )
                    }
                }
            }

            if (filteredProperties.isEmpty()) {
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
                            Icon(
                                imageVector = Icons.Default.Apartment,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No properties found in '$selectedFilter'", fontWeight = FontWeight.SemiBold, color = Slate800)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tap 'Add Property' below to register a new estate or BNB.", color = Slate500, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(filteredProperties) { property ->
                    PropertyDetailCard(
                        property = property,
                        context = context,
                        isExpanded = selectedProperty?.id == property.id,
                        reviews = if (selectedProperty?.id == property.id) reviews else emptyList(),
                        onToggleExpand = {
                            if (selectedProperty?.id == property.id) {
                                onSelectProperty(null)
                            } else {
                                onSelectProperty(property)
                            }
                        },
                        onResetLocation = { onOpenResetLocation(property) },
                        onManagePhotos = { onOpenManagePhotos(property) },
                        onAddReview = { onOpenAddReview(property) },
                        onDelete = { onDeleteProperty(property) }
                    )
                }
            }
        }
    }
}

@Composable
fun PropertyDetailCard(
    property: PropertyEntity,
    context: Context,
    isExpanded: Boolean,
    reviews: List<ReviewEntity>,
    onToggleExpand: () -> Unit,
    onResetLocation: () -> Unit,
    onManagePhotos: () -> Unit,
    onAddReview: () -> Unit,
    onDelete: () -> Unit
) {
    val currencyFormat = NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 }
    val photos = property.getPhotos()
    val firstPhoto = photos.firstOrNull()

    val occupancyRatio = if (property.totalUnits > 0) {
        property.occupiedUnits.toFloat() / property.totalUnits.toFloat()
    } else 0f

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, PolishBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Photo Header Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Slate800)
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
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Apartment, contentDescription = null, tint = Slate400, modifier = Modifier.size(54.dp))
                        }
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

                // Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Slate900.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = property.type.uppercase(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // Photos Count & Manage Photos Button
                    FilledTonalButton(
                        onClick = onManagePhotos,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Slate900.copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${photos.size} Photos • Manage", color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            // Body Content
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = property.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Slate500, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = property.address,
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "KES ${currencyFormat.format(property.monthlyRate)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Indigo600
                        )
                        Text(
                            text = "per month / base",
                            fontSize = 10.sp,
                            color = Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Location GPS Pin & Reset Action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Indigo50)
                        .border(1.dp, Indigo100, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = Indigo600, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GPS: %.4f, %.4f".format(property.latitude, property.longitude),
                            fontSize = 11.sp,
                            color = Slate800,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    TextButton(
                        onClick = onResetLocation,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.EditLocationAlt, contentDescription = null, tint = Indigo600, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset Location", fontSize = 11.sp, color = Indigo600, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Occupancy progress bar
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Occupancy: ${property.occupiedUnits} / ${property.totalUnits} Units",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate600
                        )
                        Text(
                            text = "%.0f%%".format(occupancyRatio * 100),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (occupancyRatio >= 0.8f) Emerald600 else Amber600
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { occupancyRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Emerald600,
                        trackColor = Slate100
                    )
                }

                if (property.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = property.description,
                        fontSize = 12.sp,
                        color = Slate600,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = PolishBorderLight)
                Spacer(modifier = Modifier.height(10.dp))

                // Owner Contacts & Communication Section
                Text(
                    text = "OWNER & GUEST CHANNELS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Call Owner
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${property.ownerPhone}"))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, PolishBorderLight),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Call", tint = Emerald600, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call", fontSize = 11.sp, color = Slate800)
                    }

                    // WhatsApp
                    OutlinedButton(
                        onClick = {
                            val cleanNumber = property.ownerWhatsApp.replace("+", "").replace(" ", "")
                            val url = "https://wa.me/$cleanNumber?text=Hello%20${Uri.encode(property.ownerName)}%2C%20regarding%20${Uri.encode(property.name)}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, PolishBorderLight),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Emerald600, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", fontSize = 11.sp, color = Slate800)
                    }

                    // Email
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${property.ownerEmail}")
                                putExtra(Intent.EXTRA_SUBJECT, "Inquiry for ${property.name}")
                            }
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, PolishBorderLight),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = "Email", tint = Indigo600, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Email", fontSize = 11.sp, color = Slate800)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Customer Reviews Accordion Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleExpand() }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.RateReview, contentDescription = null, tint = Indigo600, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Customer & Guest Reviews",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Slate800
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand reviews",
                        tint = Slate400
                    )
                }

                // Reviews Drawer Content
                if (isExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${reviews.size} verified reviews",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                        FilledTonalButton(
                            onClick = onAddReview,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Indigo50, contentColor = Indigo600),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.AddComment, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Write Review", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (reviews.isEmpty()) {
                        Text(
                            text = "No reviews yet. Be the first to add a customer review!",
                            fontSize = 11.sp,
                            color = Slate400,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            reviews.forEach { r ->
                                Surface(
                                    color = PolishBackground,
                                    border = BorderStroke(1.dp, PolishBorderLight),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = r.guestName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate800)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.Star, contentDescription = null, tint = Amber500, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(text = "%.1f".format(r.rating), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate800)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = r.comment, fontSize = 11.sp, color = Slate600)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Delete property action
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = Rose600),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete Property", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

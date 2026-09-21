package com.example.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.entities.ProductEntity
import com.example.ui.theme.*
import com.example.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    viewModel: ProductsViewModel,
    productId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val product by viewModel.getProductById(productId).collectAsState(initial = null)
    val history by viewModel.getProductHistory(productId).collectAsState(initial = emptyList())
    val currency by viewModel.currency.collectAsState()

    var showAdjustStockDialog by remember { mutableStateOf(false) }
    var adjustAmountText by remember { mutableStateOf("1") }
    var adjustNoteText by remember { mutableStateOf("") }
    var isIncrease by remember { mutableStateOf(true) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product?.name ?: "Product Detail", fontWeight = FontWeight.Bold, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(productId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryBlue)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        val prod = product
        if (prod == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Info Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = prod.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                val (badgeText, badgeBg, badgeFg) = when {
                                    prod.isOutOfStock -> Triple(stringResource(R.string.out_of_stock_badge), ErrorRed.copy(alpha = 0.15f), ErrorRed)
                                    prod.isLowStock -> Triple(stringResource(R.string.low_stock_badge), WarningAmber.copy(alpha = 0.15f), WarningAmber)
                                    else -> Triple(stringResource(R.string.in_stock), SuccessGreen.copy(alpha = 0.15f), SuccessGreen)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(badgeBg)
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(badgeText, color = badgeFg, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                }
                            }

                            if (prod.barcode.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(prod.barcode, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.height(16.dp))

                            // Grid of Details
                            Row(modifier = Modifier.fillMaxWidth()) {
                                DetailItem(label = "Category", value = prod.category, modifier = Modifier.weight(1f))
                                DetailItem(label = "Unit", value = prod.unit, modifier = Modifier.weight(1f))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                DetailItem(label = "Selling Price", value = FormatUtils.formatCurrency(prod.sellingPrice, currency), modifier = Modifier.weight(1f))
                                DetailItem(label = "Purchase Price", value = FormatUtils.formatCurrency(prod.purchasePrice, currency), modifier = Modifier.weight(1f))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                DetailItem(label = "Current Stock", value = "${prod.quantity} ${prod.unit}", modifier = Modifier.weight(1f))
                                DetailItem(label = "Minimum Stock", value = "${prod.minimumQuantity} ${prod.unit}", modifier = Modifier.weight(1f))
                            }
                            if (!prod.expirationDate.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailItem(label = "Expiration Date", value = prod.expirationDate)
                            }
                            if (prod.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailItem(label = "Description", value = prod.description)
                            }
                            if (prod.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailItem(label = "Notes", value = prod.notes)
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Stock Adjustment Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        isIncrease = true
                                        showAdjustStockDialog = true
                                    },
                                    modifier = Modifier.weight(1f).testTag("increase_stock_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.increase_stock))
                                }
                                Button(
                                    onClick = {
                                        isIncrease = false
                                        showAdjustStockDialog = true
                                    },
                                    modifier = Modifier.weight(1f).testTag("decrease_stock_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.decrease_stock))
                                }
                            }
                        }
                    }
                }

                // History Section
                item {
                    Text(
                        text = "Stock Movement History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (history.isEmpty()) {
                    item {
                        Text(
                            text = "No stock changes recorded yet for this product.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(history) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${log.type}: ${if (log.changeQuantity > 0) "+${log.changeQuantity}" else "${log.changeQuantity}"} (New balance: ${log.newQuantity})",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (log.note.isNotBlank()) {
                                        Text(log.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Text(
                                    text = FormatUtils.formatDate(log.date),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Stock Adjust Dialog
    if (showAdjustStockDialog && product != null) {
        val prod = product!!
        AlertDialog(
            onDismissRequest = { showAdjustStockDialog = false },
            title = { Text(if (isIncrease) "Increase Stock (+)" else "Decrease Stock (-)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Product: ${prod.name}\nCurrent stock: ${prod.quantity} ${prod.unit}")
                    OutlinedTextField(
                        value = adjustAmountText,
                        onValueChange = { adjustAmountText = it },
                        label = { Text("Quantity") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = adjustNoteText,
                        onValueChange = { adjustNoteText = it },
                        label = { Text("Reason / Note (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = adjustAmountText.toIntOrNull() ?: 1
                        val delta = if (isIncrease) amount else -amount
                        val note = adjustNoteText.ifBlank { if (isIncrease) "Manual increase" else "Manual decrease" }
                        viewModel.quickAdjustStock(prod.id, delta, note)
                        showAdjustStockDialog = false
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdjustStockDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Delete confirmation
    if (showDeleteConfirm && product != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_product)) },
            text = { Text(stringResource(R.string.confirm_delete_product)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(product!!)
                        showDeleteConfirm = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun DetailItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

package com.example.ui.purchases

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.entities.ProductEntity
import com.example.ui.theme.*
import com.example.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPurchaseScreen(
    viewModel: PurchasesViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.newPurchaseState.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val products by viewModel.products.collectAsState()
    val currency by viewModel.currency.collectAsState()

    var showProductPicker by remember { mutableStateOf(false) }
    var productSearchQuery by remember { mutableStateOf("") }
    var supplierDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_purchase), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Supplier Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Select Supplier *", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = supplierDropdownExpanded,
                        onExpandedChange = { supplierDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = state.selectedSupplier?.name ?: "Tap to choose a supplier",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supplierDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth().testTag("select_supplier_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = supplierDropdownExpanded,
                            onDismissRequest = { supplierDropdownExpanded = false }
                        ) {
                            suppliers.forEach { sup ->
                                DropdownMenuItem(
                                    text = { Text("${sup.name} (${sup.company})") },
                                    onClick = {
                                        viewModel.setSupplier(sup)
                                        supplierDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Products in Purchase
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Items to Stock (${state.cart.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showProductPicker = true },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                    modifier = Modifier.testTag("add_item_to_purchase_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.select_product))
                }
            }

            if (state.cart.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No products selected. Select products to restock.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                for (item in state.cart) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.product.name, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Purchase Price: ${FormatUtils.formatCurrency(item.purchasePrice, currency)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Total: ${FormatUtils.formatCurrency(item.total, currency)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WarningAmber
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilledTonalIconButton(
                                    onClick = { viewModel.updateCartItemQuantity(item.product.id, -1) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = "${item.quantity}",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                )
                                FilledTonalIconButton(
                                    onClick = { viewModel.updateCartItemQuantity(item.product.id, 1) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                }
                                IconButton(onClick = { viewModel.removeCartItem(item.product.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = ErrorRed)
                                }
                            }
                        }
                    }
                }
            }

            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Payment Details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Cost", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(FormatUtils.formatCurrency(state.total, currency), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WarningAmber)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.paid), style = MaterialTheme.typography.bodyMedium)
                        OutlinedTextField(
                            value = if (state.paidAmount > 0) state.paidAmount.toString() else "",
                            onValueChange = { viewModel.setPaidAmount(it.toDoubleOrNull() ?: 0.0) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.width(130.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.remaining_amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = FormatUtils.formatCurrency(state.remainingAmount, currency),
                            fontWeight = FontWeight.Bold,
                            color = if (state.remainingAmount > 0) ErrorRed else SuccessGreen
                        )
                    }

                    OutlinedTextField(
                        value = state.notes,
                        onValueChange = { viewModel.setNotes(it) },
                        label = { Text(stringResource(R.string.notes)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            if (state.errorMessage != null) {
                Text(text = state.errorMessage!!, color = ErrorRed, fontWeight = FontWeight.SemiBold)
            }

            // Complete Purchase Button
            Button(
                onClick = {
                    viewModel.completePurchase {
                        viewModel.resetNewPurchase()
                        onNavigateBack()
                    }
                },
                enabled = state.cart.isNotEmpty() && state.selectedSupplier != null && !state.isCompleting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("complete_purchase_button"),
                colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isCompleting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(stringResource(R.string.create_purchase), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Product Picker Dialog
    if (showProductPicker) {
        val filteredProducts = products.filter {
            productSearchQuery.isBlank() ||
                    it.name.contains(productSearchQuery, ignoreCase = true) ||
                    it.barcode.contains(productSearchQuery, ignoreCase = true)
        }

        AlertDialog(
            onDismissRequest = { showProductPicker = false },
            title = { Text(stringResource(R.string.select_product)) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                    OutlinedTextField(
                        value = productSearchQuery,
                        onValueChange = { productSearchQuery = it },
                        placeholder = { Text("Search products…") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredProducts) { prod ->
                            ListItem(
                                headlineContent = { Text(prod.name, fontWeight = FontWeight.SemiBold) },
                                supportingContent = {
                                    Text("Current Stock: ${prod.quantity} ${prod.unit} • Purchase Price: ${FormatUtils.formatCurrency(prod.purchasePrice, currency)}")
                                },
                                modifier = Modifier.clickable {
                                    viewModel.addToCart(prod)
                                    showProductPicker = false
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showProductPicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

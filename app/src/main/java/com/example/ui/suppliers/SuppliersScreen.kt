package com.example.ui.suppliers

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.entities.SupplierEntity
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuppliersScreen(
    viewModel: SuppliersViewModel
) {
    val suppliers by viewModel.suppliers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val context = LocalContext.current

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingSupplier by remember { mutableStateOf<SupplierEntity?>(null) }
    var supplierForPayment by remember { mutableStateOf<SupplierEntity?>(null) }
    var paymentAmountText by remember { mutableStateOf("") }
    var paymentNoteText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.suppliers), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingSupplier = null
                    showAddEditDialog = true
                },
                containerColor = PrimaryBlue,
                contentColor = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.testTag("add_supplier_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Supplier")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_suppliers_input"),
                placeholder = { Text(stringResource(R.string.search_suppliers)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (suppliers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.no_suppliers_yet), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.add_first_supplier_desc),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(suppliers, key = { it.id }) { supplier ->
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("supplier_card_${supplier.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(supplier.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        if (supplier.company.isNotBlank()) {
                                            Text(supplier.company, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Row {
                                        IconButton(onClick = {
                                            editingSupplier = supplier
                                            showAddEditDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryBlue)
                                        }
                                        IconButton(onClick = { viewModel.deleteSupplier(supplier) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                                        }
                                    }
                                }

                                if (supplier.phone.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(supplier.phone, style = MaterialTheme.typography.bodySmall)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        IconButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${supplier.phone}"))
                                                context.startActivity(intent)
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Call, contentDescription = "Call", tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Purchases: ${FormatUtils.formatCurrency(supplier.totalPurchases, currency)}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "Owed: ${FormatUtils.formatCurrency(supplier.amountOwed, currency)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (supplier.amountOwed > 0) ErrorRed else SuccessGreen
                                        )
                                    }
                                    if (supplier.amountOwed > 0) {
                                        Button(
                                            onClick = {
                                                supplierForPayment = supplier
                                                paymentAmountText = "${supplier.amountOwed}"
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(stringResource(R.string.record_payment))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Supplier Dialog
    if (showAddEditDialog) {
        var name by remember { mutableStateOf(editingSupplier?.name ?: "") }
        var company by remember { mutableStateOf(editingSupplier?.company ?: "") }
        var phone by remember { mutableStateOf(editingSupplier?.phone ?: "") }
        var whatsApp by remember { mutableStateOf(editingSupplier?.whatsApp ?: "") }
        var email by remember { mutableStateOf(editingSupplier?.email ?: "") }
        var address by remember { mutableStateOf(editingSupplier?.address ?: "") }
        var notes by remember { mutableStateOf(editingSupplier?.notes ?: "") }

        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            title = { Text(if (editingSupplier != null) stringResource(R.string.edit_supplier) else stringResource(R.string.add_supplier)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("${stringResource(R.string.supplier_name)} *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text(stringResource(R.string.company)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(stringResource(R.string.phone)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = whatsApp,
                        onValueChange = { whatsApp = it },
                        label = { Text(stringResource(R.string.whatsapp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(R.string.email)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(stringResource(R.string.address)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(stringResource(R.string.notes)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val sup = SupplierEntity(
                                id = editingSupplier?.id ?: 0L,
                                name = name.trim(),
                                company = company.trim(),
                                phone = phone.trim(),
                                whatsApp = whatsApp.trim(),
                                email = email.trim(),
                                address = address.trim(),
                                notes = notes.trim(),
                                totalPurchases = editingSupplier?.totalPurchases ?: 0.0,
                                amountOwed = editingSupplier?.amountOwed ?: 0.0
                            )
                            viewModel.saveSupplier(sup)
                            showAddEditDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEditDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Record Payment Dialog
    if (supplierForPayment != null) {
        val sup = supplierForPayment!!
        AlertDialog(
            onDismissRequest = { supplierForPayment = null },
            title = { Text("Record Payment to ${sup.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Total Owed: ${FormatUtils.formatCurrency(sup.amountOwed, currency)}")
                    OutlinedTextField(
                        value = paymentAmountText,
                        onValueChange = { paymentAmountText = it },
                        label = { Text(stringResource(R.string.amount)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = paymentNoteText,
                        onValueChange = { paymentNoteText = it },
                        label = { Text(stringResource(R.string.notes)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = paymentAmountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.recordPayment(sup, amt, paymentNoteText) {
                                supplierForPayment = null
                            }
                        }
                    }
                ) {
                    Text("Confirm Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { supplierForPayment = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

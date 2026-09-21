package com.example.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.entities.ProductEntity
import com.example.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    viewModel: ProductsViewModel,
    productId: Long = 0L,
    initialBarcode: String = "",
    onNavigateBack: () -> Unit,
    onNavigateToScan: () -> Unit
) {
    val suppliers by viewModel.suppliers.collectAsState()
    val currency by viewModel.currency.collectAsState()

    var name by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf(initialBarcode) }
    var category by remember { mutableStateOf("General") }
    var description by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var minimumQuantity by remember { mutableStateOf("5") }
    var unit by remember { mutableStateOf("pcs") }
    var selectedSupplierId by remember { mutableStateOf<Long?>(null) }
    var expirationDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var supplierDropdownExpanded by remember { mutableStateOf(false) }

    // Load existing product if editing
    LaunchedEffect(productId) {
        if (productId > 0) {
            viewModel.getProductById(productId).collect { prod ->
                if (prod != null) {
                    name = prod.name
                    barcode = prod.barcode
                    category = prod.category
                    description = prod.description
                    purchasePrice = if (prod.purchasePrice > 0) prod.purchasePrice.toString() else ""
                    sellingPrice = if (prod.sellingPrice > 0) prod.sellingPrice.toString() else ""
                    quantity = prod.quantity.toString()
                    minimumQuantity = prod.minimumQuantity.toString()
                    unit = prod.unit
                    selectedSupplierId = prod.supplierId
                    expirationDate = prod.expirationDate ?: ""
                    notes = prod.notes
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (productId > 0) stringResource(R.string.edit_product) else stringResource(R.string.add_product),
                        fontWeight = FontWeight.Bold
                    )
                },
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
            // Product Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = { Text("${stringResource(R.string.product_name)} *") },
                isError = nameError != null,
                supportingText = { nameError?.let { Text(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_name_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Barcode with Scan action
            OutlinedTextField(
                value = barcode,
                onValueChange = { barcode = it },
                label = { Text(stringResource(R.string.barcode)) },
                trailingIcon = {
                    IconButton(onClick = onNavigateToScan) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = PrimaryBlue)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_barcode_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Category & Unit
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(stringResource(R.string.category)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit") },
                    placeholder = { Text("pcs, kg, box") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Prices: Purchase & Selling
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = purchasePrice,
                    onValueChange = { purchasePrice = it },
                    label = { Text("${stringResource(R.string.purchase_price)} ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f).testTag("product_purchase_price_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text("${stringResource(R.string.selling_price)} ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f).testTag("product_selling_price_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Quantity & Min Quantity
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text(stringResource(R.string.quantity)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("product_quantity_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = minimumQuantity,
                    onValueChange = { minimumQuantity = it },
                    label = { Text(stringResource(R.string.min_quantity)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Supplier Selection
            ExposedDropdownMenuBox(
                expanded = supplierDropdownExpanded,
                onExpandedChange = { supplierDropdownExpanded = it }
            ) {
                val selectedSupplier = suppliers.find { it.id == selectedSupplierId }
                OutlinedTextField(
                    value = selectedSupplier?.name ?: "None / Unassigned",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.supplier)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supplierDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = supplierDropdownExpanded,
                    onDismissRequest = { supplierDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("None / Unassigned") },
                        onClick = {
                            selectedSupplierId = null
                            supplierDropdownExpanded = false
                        }
                    )
                    suppliers.forEach { sup ->
                        DropdownMenuItem(
                            text = { Text(sup.name) },
                            onClick = {
                                selectedSupplierId = sup.id
                                supplierDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Expiration Date & Description
            OutlinedTextField(
                value = expirationDate,
                onValueChange = { expirationDate = it },
                label = { Text(stringResource(R.string.expiration_date)) },
                placeholder = { Text("YYYY-MM-DD") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.notes)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = "Product name is required"
                        return@Button
                    }
                    val qty = quantity.toIntOrNull() ?: 0
                    val minQty = minimumQuantity.toIntOrNull() ?: 5
                    val pPrice = purchasePrice.toDoubleOrNull() ?: 0.0
                    val sPrice = sellingPrice.toDoubleOrNull() ?: 0.0

                    val product = ProductEntity(
                        id = productId,
                        name = name.trim(),
                        barcode = barcode.trim(),
                        category = category.trim().ifBlank { "General" },
                        description = description.trim(),
                        purchasePrice = pPrice,
                        sellingPrice = sPrice,
                        quantity = qty,
                        minimumQuantity = minQty,
                        unit = unit.trim().ifBlank { "pcs" },
                        supplierId = selectedSupplierId,
                        expirationDate = expirationDate.trim().ifBlank { null },
                        notes = notes.trim(),
                        updatedDate = System.currentTimeMillis()
                    )

                    viewModel.saveProduct(product) {
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_product_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.save), fontWeight = FontWeight.Bold)
            }
        }
    }
}

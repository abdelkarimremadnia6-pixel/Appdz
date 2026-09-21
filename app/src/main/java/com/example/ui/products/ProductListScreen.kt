package com.example.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
fun ProductListScreen(
    viewModel: ProductsViewModel,
    initialFilter: String = "ALL",
    onNavigateToAddProduct: () -> Unit,
    onNavigateToProductDetail: (Long) -> Unit,
    onNavigateToEditProduct: (Long) -> Unit
) {
    val products by viewModel.products.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val stockFilter by viewModel.stockFilter.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val currency by viewModel.currency.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }

    LaunchedEffect(initialFilter) {
        when (initialFilter) {
            "LOW_STOCK" -> viewModel.setStockFilter(StockFilter.LOW_STOCK)
            "OUT_OF_STOCK" -> viewModel.setStockFilter(StockFilter.OUT_OF_STOCK)
            "IN_STOCK" -> viewModel.setStockFilter(StockFilter.IN_STOCK)
            else -> viewModel.setStockFilter(StockFilter.ALL)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.products), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier.testTag("sort_products_button")
                    ) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_name_asc)) },
                            onClick = { viewModel.setSortOption("NAME_ASC"); showSortMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_name_desc)) },
                            onClick = { viewModel.setSortOption("NAME_DESC"); showSortMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_qty_low)) },
                            onClick = { viewModel.setSortOption("QTY_LOW"); showSortMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_qty_high)) },
                            onClick = { viewModel.setSortOption("QTY_HIGH"); showSortMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_price_low)) },
                            onClick = { viewModel.setSortOption("PRICE_LOW"); showSortMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_price_high)) },
                            onClick = { viewModel.setSortOption("PRICE_HIGH"); showSortMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_recent)) },
                            onClick = { viewModel.setSortOption("RECENT"); showSortMenu = false }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddProduct,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_products_input"),
                placeholder = { Text(stringResource(R.string.search_products)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Stock Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = stockFilter.ordinal,
                edgePadding = 16.dp,
                divider = {},
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = stockFilter == StockFilter.ALL,
                    onClick = { viewModel.setStockFilter(StockFilter.ALL) },
                    text = { Text("All") },
                    modifier = Modifier.testTag("tab_filter_all")
                )
                Tab(
                    selected = stockFilter == StockFilter.IN_STOCK,
                    onClick = { viewModel.setStockFilter(StockFilter.IN_STOCK) },
                    text = { Text("In Stock") },
                    modifier = Modifier.testTag("tab_filter_in_stock")
                )
                Tab(
                    selected = stockFilter == StockFilter.LOW_STOCK,
                    onClick = { viewModel.setStockFilter(StockFilter.LOW_STOCK) },
                    text = { Text("Low Stock") },
                    modifier = Modifier.testTag("tab_filter_low_stock")
                )
                Tab(
                    selected = stockFilter == StockFilter.OUT_OF_STOCK,
                    onClick = { viewModel.setStockFilter(StockFilter.OUT_OF_STOCK) },
                    text = { Text("Out of Stock") },
                    modifier = Modifier.testTag("tab_filter_out_of_stock")
                )
            }

            // Categories Filter Chips
            if (categories.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory.isEmpty(),
                            onClick = { viewModel.setSelectedCategory("") },
                            label = { Text("All Categories") }
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory.equals(cat, ignoreCase = true),
                            onClick = { viewModel.setSelectedCategory(cat) },
                            label = { Text(cat) }
                        )
                    }
                }
            }

            // Product List or Empty State
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.no_products_yet),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.add_first_product_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            currency = currency,
                            onClick = { onNavigateToProductDetail(product.id) },
                            onEdit = { onNavigateToEditProduct(product.id) },
                            onDelete = { productToDelete = product },
                            onDuplicate = { viewModel.duplicateProduct(product) {} },
                            onQuickIncrease = { viewModel.quickAdjustStock(product.id, 1) },
                            onQuickDecrease = { viewModel.quickAdjustStock(product.id, -1) }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (productToDelete != null) {
        val prod = productToDelete!!
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text(stringResource(R.string.delete_product)) },
            text = { Text("${stringResource(R.string.confirm_delete_product)}\n\n\"${prod.name}\"") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(prod)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun ProductItemCard(
    product: ProductEntity,
    currency: String,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onQuickIncrease: () -> Unit,
    onQuickDecrease: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("product_card_${product.id}"),
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
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    if (product.barcode.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.QrCode,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = product.barcode,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Stock Badge
                val (badgeText, badgeBg, badgeFg) = when {
                    product.isOutOfStock -> Triple(stringResource(R.string.out_of_stock_badge), ErrorRed.copy(alpha = 0.15f), ErrorRed)
                    product.isLowStock -> Triple(stringResource(R.string.low_stock_badge), WarningAmber.copy(alpha = 0.15f), WarningAmber)
                    else -> Triple(stringResource(R.string.in_stock), SuccessGreen.copy(alpha = 0.15f), SuccessGreen)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeFg,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing and Quantities
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Stock: ${product.quantity} ${product.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Price: ${FormatUtils.formatCurrency(product.sellingPrice, currency)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quick Stock Adjustment Stepper
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(
                        onClick = onQuickDecrease,
                        modifier = Modifier.size(36.dp).testTag("quick_decrease_${product.id}")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = "${product.quantity}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    FilledTonalIconButton(
                        onClick = onQuickIncrease,
                        modifier = Modifier.size(36.dp).testTag("quick_increase_${product.id}")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))

            // Card footer action icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDuplicate) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryBlue)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                }
            }
        }
    }
}

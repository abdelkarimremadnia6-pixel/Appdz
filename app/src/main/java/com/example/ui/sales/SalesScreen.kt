package com.example.ui.sales

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.entities.SaleEntity
import com.example.data.entities.SaleItemEntity
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.utils.FormatUtils
import com.example.utils.PdfReceiptGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    viewModel: SalesViewModel,
    onNavigateToNewSale: () -> Unit
) {
    val sales by viewModel.allSales.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedSaleForReceipt by remember { mutableStateOf<SaleEntity?>(null) }
    var saleItemsForReceipt by remember { mutableStateOf<List<SaleItemEntity>>(emptyList()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sales), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewSale,
                containerColor = SuccessGreen,
                contentColor = Color.White,
                modifier = Modifier.testTag("new_sale_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Sale")
            }
        }
    ) { padding ->
        if (sales.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No sales recorded yet", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onNavigateToNewSale) {
                        Text(stringResource(R.string.new_sale))
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sales, key = { it.id }) { sale ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch {
                                    val items = withContext(Dispatchers.IO) {
                                        viewModel.getSaleItems(sale.id)
                                    }
                                    items.collect { list ->
                                        saleItemsForReceipt = list
                                        selectedSaleForReceipt = sale
                                    }
                                }
                            }
                            .testTag("sale_card_${sale.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = sale.receiptNumber,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = FormatUtils.formatCurrency(sale.total, currency),
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (sale.customerName.isNotBlank()) sale.customerName else "Walk-in Customer",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = FormatUtils.formatDateTime(sale.date),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (sale.remainingAmount > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Remaining Owed: ${FormatUtils.formatCurrency(sale.remainingAmount, currency)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Receipt Dialog for existing sale
    if (selectedSaleForReceipt != null) {
        val sale = selectedSaleForReceipt!!
        ReceiptDialog(
            sale = sale,
            items = saleItemsForReceipt,
            currency = currency,
            onDismiss = { selectedSaleForReceipt = null },
            onShare = {
                val pdf = PdfReceiptGenerator.generateReceiptPdf(
                    context = context,
                    businessName = "Inventory Manager",
                    currency = currency,
                    sale = sale,
                    items = saleItemsForReceipt
                )
                PdfReceiptGenerator.shareReceiptPdf(context, pdf)
            }
        )
    }
}

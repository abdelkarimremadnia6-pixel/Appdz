package com.example.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.*
import com.example.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToNewSale: () -> Unit,
    onNavigateToNewPurchase: () -> Unit,
    onNavigateToProducts: (filter: String) -> Unit,
    onNavigateToDebts: () -> Unit,
    onNavigateToSuppliers: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.dashboard),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Low / Out of Stock Alert Banner
            if (state.lowStockCount > 0 || state.outOfStockCount > 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToProducts("LOW_STOCK") }
                            .testTag("dashboard_stock_alert_banner"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (state.outOfStockCount > 0) ErrorRed.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (state.outOfStockCount > 0) ErrorRed else WarningAmber,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (state.outOfStockCount > 0)
                                        "${state.outOfStockCount} items out of stock, ${state.lowStockCount} low"
                                    else
                                        "${state.lowStockCount} items running low in stock",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tap to view and restock inventory",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Quick Actions
            item {
                Text(
                    text = stringResource(R.string.quick_actions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(
                        title = stringResource(R.string.scan_barcode),
                        icon = Icons.Default.QrCodeScanner,
                        color = PrimaryBlue,
                        modifier = Modifier.weight(1f).testTag("quick_action_scan"),
                        onClick = onNavigateToScan
                    )
                    QuickActionButton(
                        title = stringResource(R.string.new_sale),
                        icon = Icons.Default.ShoppingCart,
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f).testTag("quick_action_sale"),
                        onClick = onNavigateToNewSale
                    )
                    QuickActionButton(
                        title = stringResource(R.string.new_purchase),
                        icon = Icons.Default.LocalShipping,
                        color = WarningAmber,
                        modifier = Modifier.weight(1f).testTag("quick_action_purchase"),
                        onClick = onNavigateToNewPurchase
                    )
                    QuickActionButton(
                        title = stringResource(R.string.add_product),
                        icon = Icons.Default.Add,
                        color = CyanAccent,
                        modifier = Modifier.weight(1f).testTag("quick_action_add_product"),
                        onClick = onNavigateToAddProduct
                    )
                }
            }

            // Overview Metric Cards Grid
            item {
                Text(
                    text = "Inventory Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardMetricCard(
                        title = stringResource(R.string.total_products),
                        value = "${state.totalProducts}",
                        icon = Icons.Default.Inventory2,
                        accentColor = PrimaryBlue,
                        modifier = Modifier.weight(1f).testTag("metric_total_products"),
                        onClick = { onNavigateToProducts("ALL") }
                    )
                    DashboardMetricCard(
                        title = stringResource(R.string.total_stock_qty),
                        value = "${state.totalStockQty}",
                        icon = Icons.Default.Layers,
                        accentColor = CyanAccent,
                        modifier = Modifier.weight(1f).testTag("metric_total_stock"),
                        onClick = { onNavigateToProducts("ALL") }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardMetricCard(
                        title = stringResource(R.string.low_stock),
                        value = "${state.lowStockCount}",
                        icon = Icons.Default.TrendingDown,
                        accentColor = WarningAmber,
                        modifier = Modifier.weight(1f).testTag("metric_low_stock"),
                        onClick = { onNavigateToProducts("LOW_STOCK") }
                    )
                    DashboardMetricCard(
                        title = stringResource(R.string.out_of_stock),
                        value = "${state.outOfStockCount}",
                        icon = Icons.Default.RemoveShoppingCart,
                        accentColor = ErrorRed,
                        modifier = Modifier.weight(1f).testTag("metric_out_of_stock"),
                        onClick = { onNavigateToProducts("OUT_OF_STOCK") }
                    )
                }
            }

            // Financials
            item {
                Text(
                    text = "Today\'s Activity & Balances",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardMetricCard(
                        title = stringResource(R.string.today_sales),
                        value = FormatUtils.formatCurrency(state.todaySales, state.currency),
                        icon = Icons.Default.PointOfSale,
                        accentColor = SuccessGreen,
                        modifier = Modifier.weight(1f).testTag("metric_today_sales")
                    )
                    DashboardMetricCard(
                        title = stringResource(R.string.today_purchases),
                        value = FormatUtils.formatCurrency(state.todayPurchases, state.currency),
                        icon = Icons.Default.ShoppingBag,
                        accentColor = WarningAmber,
                        modifier = Modifier.weight(1f).testTag("metric_today_purchases")
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardMetricCard(
                        title = stringResource(R.string.total_debts),
                        value = FormatUtils.formatCurrency(state.totalDebts, state.currency),
                        icon = Icons.Default.AccountBalanceWallet,
                        accentColor = ErrorRed,
                        modifier = Modifier.weight(1f).testTag("metric_total_debts"),
                        onClick = onNavigateToDebts
                    )
                    DashboardMetricCard(
                        title = stringResource(R.string.total_suppliers),
                        value = "${state.totalSuppliers}",
                        icon = Icons.Default.LocalShipping,
                        accentColor = PurpleAccent,
                        modifier = Modifier.weight(1f).testTag("metric_total_suppliers"),
                        onClick = onNavigateToSuppliers
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .height(84.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

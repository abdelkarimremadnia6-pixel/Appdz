package com.example.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.repository.InventoryRepository
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockHistoryScreen(
    repository: InventoryRepository,
    onNavigateBack: () -> Unit
) {
    val allHistory by repository.allStockHistory.collectAsState(initial = emptyList())
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredHistory = remember(allHistory, selectedFilter) {
        if (selectedFilter == "ALL") allHistory
        else allHistory.filter { it.type.equals(selectedFilter, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stock_history), fontWeight = FontWeight.Bold) },
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
        ) {
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "SALE", "PURCHASE", "ADJUSTMENT").forEach { f ->
                    FilterChip(
                        selected = selectedFilter == f,
                        onClick = { selectedFilter = f },
                        label = { Text(f) }
                    )
                }
            }

            if (filteredHistory.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No stock history found", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredHistory, key = { it.id }) { log ->
                        val (chipBg, chipFg) = when (log.type.uppercase()) {
                            "SALE" -> Pair(ErrorRed.copy(alpha = 0.15f), ErrorRed)
                            "PURCHASE" -> Pair(SuccessGreen.copy(alpha = 0.15f), SuccessGreen)
                            else -> Pair(WarningAmber.copy(alpha = 0.15f), WarningAmber)
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("history_item_${log.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(log.productName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${log.type} • Change: ${if (log.changeQuantity > 0) "+${log.changeQuantity}" else "${log.changeQuantity}"} • Balance: ${log.newQuantity}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = chipFg,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (log.note.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(log.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Text(
                                    text = FormatUtils.formatDateTime(log.date),
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
}

package com.example.ui.debts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Payments
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
import com.example.data.entities.DebtEntity
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(
    viewModel: DebtsViewModel
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val debts by viewModel.debts.collectAsState()
    val totalDebts by viewModel.totalDebts.collectAsState()
    val currency by viewModel.currency.collectAsState()

    var showAddDebtDialog by remember { mutableStateOf(false) }
    var selectedDebtForPayment by remember { mutableStateOf<DebtEntity?>(null) }
    var paymentAmountText by remember { mutableStateOf("") }
    var paymentNoteText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.debts), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDebtDialog = true },
                containerColor = ErrorRed,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_debt_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Debt")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Total Outstanding Debts Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Active Debts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = FormatUtils.formatCurrency(totalDebts, currency),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        )
                    }
                    Icon(
                        Icons.Default.Payments,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Tab Switcher
            TabRow(
                selectedTabIndex = if (selectedTab == "CUSTOMER") 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == "CUSTOMER",
                    onClick = { viewModel.setSelectedTab("CUSTOMER") },
                    text = { Text(stringResource(R.string.customer_debts)) },
                    modifier = Modifier.testTag("tab_customer_debts")
                )
                Tab(
                    selected = selectedTab == "SUPPLIER",
                    onClick = { viewModel.setSelectedTab("SUPPLIER") },
                    text = { Text(stringResource(R.string.supplier_debts)) },
                    modifier = Modifier.testTag("tab_supplier_debts")
                )
            }

            // Debts List
            if (debts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_debts),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(debts, key = { it.id }) { debt ->
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("debt_card_${debt.id}"),
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
                                            text = debt.referenceName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        if (debt.notes.isNotBlank()) {
                                            Text(
                                                text = debt.notes,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = FormatUtils.formatCurrency(debt.remainingAmount, currency),
                                            fontWeight = FontWeight.Bold,
                                            color = if (debt.remainingAmount > 0) ErrorRed else SuccessGreen,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "Initial: ${FormatUtils.formatCurrency(debt.amount, currency)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (debt.remainingAmount > 0) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        Button(
                                            onClick = {
                                                selectedDebtForPayment = debt
                                                paymentAmountText = "${debt.remainingAmount}"
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
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

    // Add Debt Dialog
    if (showAddDebtDialog) {
        var refName by remember { mutableStateOf("") }
        var amountText by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDebtDialog = false },
            title = { Text(stringResource(R.string.add_debt)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Type: ${if (selectedTab == "CUSTOMER") "Customer Debt" else "Supplier Debt"}")
                    OutlinedTextField(
                        value = refName,
                        onValueChange = { refName = it },
                        label = { Text(if (selectedTab == "CUSTOMER") "Customer Name" else "Supplier Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text(stringResource(R.string.amount)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (refName.isNotBlank() && amt > 0) {
                            viewModel.addDebt(
                                DebtEntity(
                                    type = selectedTab,
                                    referenceId = 0,
                                    referenceName = refName.trim(),
                                    amount = amt,
                                    remainingAmount = amt,
                                    notes = notes.trim()
                                )
                            ) {
                                showAddDebtDialog = false
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDebtDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Record Payment Dialog
    if (selectedDebtForPayment != null) {
        val debt = selectedDebtForPayment!!
        AlertDialog(
            onDismissRequest = { selectedDebtForPayment = null },
            title = { Text("Payment for ${debt.referenceName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Remaining Balance: ${FormatUtils.formatCurrency(debt.remainingAmount, currency)}")
                    OutlinedTextField(
                        value = paymentAmountText,
                        onValueChange = { paymentAmountText = it },
                        label = { Text("Payment Amount ($currency)") },
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
                            viewModel.recordPayment(debt, amt, paymentNoteText) {
                                selectedDebtForPayment = null
                            }
                        }
                    }
                ) {
                    Text("Confirm Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDebtForPayment = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

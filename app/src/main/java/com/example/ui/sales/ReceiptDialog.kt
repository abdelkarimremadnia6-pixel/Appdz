package com.example.ui.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.entities.SaleEntity
import com.example.data.entities.SaleItemEntity
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.utils.FormatUtils

@Composable
fun ReceiptDialog(
    sale: SaleEntity,
    items: List<SaleItemEntity>,
    currency: String,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("receipt_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "RECEIPT / INVOICE",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = sale.receiptNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = FormatUtils.formatDateTime(sale.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (sale.customerName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Customer: ${sale.customerName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Table Header
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Item", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Text("Qty", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Text("Price", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Text("Total", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Items list
                for (item in items) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(item.productName, modifier = Modifier.weight(2f), style = MaterialTheme.typography.bodySmall)
                        Text("${item.quantity}", modifier = Modifier.weight(0.8f), style = MaterialTheme.typography.bodySmall)
                        Text("%.2f".format(item.unitPrice), modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.bodySmall)
                        Text("%.2f".format(item.total), modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Totals summary
                SummaryRow("Subtotal", FormatUtils.formatCurrency(sale.subtotal, currency))
                if (sale.discount > 0) {
                    SummaryRow("Discount", "- ${FormatUtils.formatCurrency(sale.discount, currency)}")
                }
                SummaryRow("Total", FormatUtils.formatCurrency(sale.total, currency), isBold = true)
                SummaryRow("Paid", FormatUtils.formatCurrency(sale.paidAmount, currency))
                SummaryRow("Remaining", FormatUtils.formatCurrency(sale.remainingAmount, currency), isBold = true, isError = sale.remainingAmount > 0)

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }
                    Button(
                        onClick = onShare,
                        modifier = Modifier.weight(1f).testTag("share_receipt_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.share_receipt))
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, isBold: Boolean = false, isError: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

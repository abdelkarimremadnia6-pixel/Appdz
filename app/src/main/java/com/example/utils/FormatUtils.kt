package com.example.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {

    private val numberFormat = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun formatCurrency(amount: Double, currency: String = "DA"): String {
        return "${numberFormat.format(amount)} $currency"
    }

    fun formatDateTime(timestamp: Long): String {
        if (timestamp <= 0) return "-"
        return dateFormat.format(Date(timestamp))
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp <= 0) return "-"
        return shortDateFormat.format(Date(timestamp))
    }
}

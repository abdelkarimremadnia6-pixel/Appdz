package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.entities.SaleEntity
import com.example.data.entities.SaleItemEntity
import java.io.File
import java.io.FileOutputStream

object PdfReceiptGenerator {

    fun generateReceiptPdf(
        context: Context,
        businessName: String,
        currency: String,
        sale: SaleEntity,
        items: List<SaleItemEntity>
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 22f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        val subtitlePaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
            textAlign = Paint.Align.CENTER
        }

        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isFakeBoldText = true
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
        }

        val rightTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            textAlign = Paint.Align.RIGHT
        }

        val boldRightPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var y = 60f

        // Business Header
        canvas.drawText(businessName.ifBlank { "INVENTORY SALES RECEIPT" }, 297.5f, y, titlePaint)
        y += 24f
        canvas.drawText("SALES INVOICE / RECEIPT", 297.5f, y, subtitlePaint)
        y += 30f

        // Details
        canvas.drawText("Receipt #: ${sale.receiptNumber}", 50f, y, headerPaint)
        canvas.drawText("Date: ${FormatUtils.formatDateTime(sale.date)}", 545f, y, rightTextPaint)
        y += 20f

        if (sale.customerName.isNotBlank()) {
            canvas.drawText("Customer: ${sale.customerName}", 50f, y, textPaint)
            y += 20f
        }

        canvas.drawLine(50f, y, 545f, y, linePaint)
        y += 20f

        // Table Header
        canvas.drawText("Item / Description", 50f, y, headerPaint)
        canvas.drawText("Qty", 320f, y, headerPaint)
        canvas.drawText("Price", 410f, y, headerPaint)
        canvas.drawText("Total ($currency)", 545f, y, boldRightPaint)
        y += 10f
        canvas.drawLine(50f, y, 545f, y, linePaint)
        y += 20f

        // Items
        for (item in items) {
            canvas.drawText(item.productName, 50f, y, textPaint)
            canvas.drawText("${item.quantity}", 320f, y, textPaint)
            canvas.drawText("%.2f".format(item.unitPrice), 410f, y, textPaint)
            canvas.drawText("%.2f".format(item.total), 545f, y, rightTextPaint)
            y += 20f
            if (y > 750f) break
        }

        canvas.drawLine(50f, y, 545f, y, linePaint)
        y += 25f

        // Summary
        canvas.drawText("Subtotal:", 410f, y, textPaint)
        canvas.drawText(FormatUtils.formatCurrency(sale.subtotal, currency), 545f, y, rightTextPaint)
        y += 20f

        if (sale.discount > 0) {
            canvas.drawText("Discount:", 410f, y, textPaint)
            canvas.drawText("- ${FormatUtils.formatCurrency(sale.discount, currency)}", 545f, y, rightTextPaint)
            y += 20f
        }

        canvas.drawText("Total:", 410f, y, headerPaint)
        canvas.drawText(FormatUtils.formatCurrency(sale.total, currency), 545f, y, boldRightPaint)
        y += 20f

        canvas.drawText("Paid:", 410f, y, textPaint)
        canvas.drawText(FormatUtils.formatCurrency(sale.paidAmount, currency), 545f, y, rightTextPaint)
        y += 20f

        canvas.drawText("Remaining:", 410f, y, headerPaint)
        canvas.drawText(FormatUtils.formatCurrency(sale.remainingAmount, currency), 545f, y, boldRightPaint)
        y += 40f

        // Footer
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 10f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Thank you for your business!", 297.5f, y, footerPaint)

        document.finishPage(page)

        val outputDir = File(context.cacheDir, "receipts").apply { mkdirs() }
        val file = File(outputDir, "Receipt_${sale.receiptNumber}.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return file
    }

    fun shareReceiptPdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(Intent.createChooser(intent, "Share Receipt"))
    }
}

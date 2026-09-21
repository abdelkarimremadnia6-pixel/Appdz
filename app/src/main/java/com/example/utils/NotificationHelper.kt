package com.example.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    const val CHANNEL_LOW_STOCK = "channel_low_stock"
    const val CHANNEL_REMINDERS = "channel_reminders"

    fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val lowStockChannel = NotificationChannel(
                CHANNEL_LOW_STOCK,
                "Stock Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for low stock and out-of-stock items"
                enableVibration(true)
            }

            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Inventory Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Scheduled inventory and supplier reminders"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(lowStockChannel)
            notificationManager.createNotificationChannel(remindersChannel)
        }
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showLowStockNotification(
        context: Context,
        productId: Long,
        productName: String,
        remainingQty: Int,
        minQty: Int
    ) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_PRODUCT_ID", productId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            productId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(R.string.low_stock_notification_title)
        val body = context.getString(R.string.low_stock_notification_body, productName, remainingQty)

        val notification = NotificationCompat.Builder(context, CHANNEL_LOW_STOCK)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(productId.toInt(), notification)
        } catch (e: SecurityException) {
            // Permission revoked
        }
    }

    fun showOutOfStockNotification(
        context: Context,
        productId: Long,
        productName: String
    ) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_PRODUCT_ID", productId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            productId.toInt() + 10000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(R.string.out_of_stock_notification_title)
        val body = context.getString(R.string.out_of_stock_notification_body, productName)

        val notification = NotificationCompat.Builder(context, CHANNEL_LOW_STOCK)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(productId.toInt() + 10000, notification)
        } catch (e: SecurityException) {
            // Permission revoked
        }
    }

    fun showReminderNotification(
        context: Context,
        reminderId: Long,
        title: String,
        description: String
    ) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt() + 20000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(description.ifBlank { "Scheduled inventory reminder" })
            .setStyle(NotificationCompat.BigTextStyle().bigText(description.ifBlank { "Scheduled inventory reminder" }))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(reminderId.toInt() + 20000, notification)
        } catch (e: SecurityException) {
            // Permission revoked
        }
    }
}

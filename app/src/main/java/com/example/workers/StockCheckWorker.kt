package com.example.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.database.AppDatabase
import com.example.data.entities.NotificationStateEntity
import com.example.utils.NotificationHelper
import java.util.concurrent.TimeUnit

class StockCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getInstance(context)
            val settings = db.appSettingsDao().getSettingsDirect()

            // If background checks or notifications are disabled in settings, finish safely
            if (settings != null && (!settings.backgroundCheckEnabled || !settings.lowStockNotificationsEnabled)) {
                return Result.success()
            }

            val products = db.productDao().getAllProductsDirect()
            val notificationDao = db.notificationStateDao()

            for (product in products) {
                val previousState = notificationDao.getStateForProduct(product.id)

                if (product.quantity <= 0) {
                    // Out of stock
                    if (previousState == null || previousState.lastNotificationType != "OUT_OF_STOCK" || previousState.lastNotifiedQuantity != product.quantity) {
                        NotificationHelper.showOutOfStockNotification(
                            context = context,
                            productId = product.id,
                            productName = product.name
                        )
                        notificationDao.insertOrUpdate(
                            NotificationStateEntity(
                                id = previousState?.id ?: 0,
                                productId = product.id,
                                lastNotifiedQuantity = product.quantity,
                                lastNotificationType = "OUT_OF_STOCK",
                                lastNotifiedAt = System.currentTimeMillis()
                            )
                        )
                    }
                } else if (product.quantity <= product.minimumQuantity) {
                    // Low stock
                    if (previousState == null || previousState.lastNotificationType != "LOW_STOCK" || previousState.lastNotifiedQuantity != product.quantity) {
                        NotificationHelper.showLowStockNotification(
                            context = context,
                            productId = product.id,
                            productName = product.name,
                            remainingQty = product.quantity,
                            minQty = product.minimumQuantity
                        )
                        notificationDao.insertOrUpdate(
                            NotificationStateEntity(
                                id = previousState?.id ?: 0,
                                productId = product.id,
                                lastNotifiedQuantity = product.quantity,
                                lastNotificationType = "LOW_STOCK",
                                lastNotifiedAt = System.currentTimeMillis()
                            )
                        )
                    }
                } else {
                    // Stock is replenished above minimum quantity: reset notification state
                    if (previousState != null) {
                        notificationDao.deleteForProduct(product.id)
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "PeriodicStockCheckWork"

        fun schedule(context: Context) {
            try {
                val request = PeriodicWorkRequestBuilder<StockCheckWorker>(1, TimeUnit.HOURS)
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
            } catch (e: Exception) {
                // Ignore in test environments where WorkManager is not initialized
            }
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}

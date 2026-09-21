package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.repository.InventoryRepository
import com.example.utils.NotificationHelper
import com.example.workers.ReminderWorker
import com.example.workers.StockCheckWorker

class InventoryApplication : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { InventoryRepository(database) }

    override fun onCreate() {
        super.onCreate()
        try {
            NotificationHelper.initChannels(this)
            StockCheckWorker.schedule(this)
            ReminderWorker.schedule(this)
        } catch (e: Exception) {
            // Ignore in test runner environments
        }
    }
}

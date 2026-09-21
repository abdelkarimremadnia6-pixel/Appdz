package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.*
import com.example.data.entities.*

@Database(
    entities = [
        ProductEntity::class,
        SupplierEntity::class,
        CustomerEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
        DebtEntity::class,
        PaymentEntity::class,
        StockHistoryEntity::class,
        ReminderEntity::class,
        NotificationStateEntity::class,
        AppSettingsEntity::class,
        LicenseEntity::class,
        LicenseRecordEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun supplierDao(): SupplierDao
    abstract fun customerDao(): CustomerDao
    abstract fun saleDao(): SaleDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun debtDao(): DebtDao
    abstract fun paymentDao(): PaymentDao
    abstract fun stockHistoryDao(): StockHistoryDao
    abstract fun reminderDao(): ReminderDao
    abstract fun notificationStateDao(): NotificationStateDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun licenseDao(): LicenseDao
    abstract fun licenseRecordDao(): LicenseRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventory_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

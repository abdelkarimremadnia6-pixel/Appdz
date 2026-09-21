package com.example.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["barcode"]),
        Index(value = ["name"]),
        Index(value = ["category"]),
        Index(value = ["supplierId"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val barcode: String = "",
    val name: String,
    val category: String = "General",
    val description: String = "",
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val quantity: Int = 0,
    val minimumQuantity: Int = 5,
    val supplierId: Long? = null,
    val unit: String = "pcs",
    val expirationDate: String? = null,
    val createdDate: Long = System.currentTimeMillis(),
    val updatedDate: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    val isOutOfStock: Boolean get() = quantity <= 0
    val isLowStock: Boolean get() = quantity > 0 && quantity <= minimumQuantity
    val isInStock: Boolean get() = quantity > minimumQuantity
}

@Entity(
    tableName = "suppliers",
    indices = [
        Index(value = ["name"]),
        Index(value = ["phone"]),
        Index(value = ["company"])
    ]
)
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val company: String = "",
    val phone: String = "",
    val whatsApp: String = "",
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val totalPurchases: Double = 0.0,
    val amountOwed: Double = 0.0,
    val createdDate: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["name"]),
        Index(value = ["phone"])
    ]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val notes: String = "",
    val debtAmount: Double = 0.0,
    val createdDate: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sales",
    indices = [
        Index(value = ["receiptNumber"], unique = true),
        Index(value = ["date"]),
        Index(value = ["customerId"])
    ]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receiptNumber: String,
    val date: Long = System.currentTimeMillis(),
    val customerId: Long? = null,
    val customerName: String = "",
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0,
    val paidAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val notes: String = ""
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["saleId"]),
        Index(value = ["productId"])
    ]
)
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val discount: Double = 0.0,
    val total: Double
)

@Entity(
    tableName = "purchases",
    indices = [
        Index(value = ["supplierId"]),
        Index(value = ["date"])
    ]
)
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long,
    val supplierName: String = "",
    val date: Long = System.currentTimeMillis(),
    val total: Double = 0.0,
    val paidAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val notes: String = ""
)

@Entity(
    tableName = "purchase_items",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["purchaseId"]),
        Index(value = ["productId"])
    ]
)
data class PurchaseItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val purchasePrice: Double,
    val total: Double
)

@Entity(
    tableName = "debts",
    indices = [
        Index(value = ["referenceId"]),
        Index(value = ["type"])
    ]
)
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "CUSTOMER" or "SUPPLIER"
    val referenceId: Long, // customerId or supplierId
    val referenceName: String,
    val amount: Double,
    val remainingAmount: Double,
    val dueDate: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(
    tableName = "payments",
    indices = [
        Index(value = ["debtId"]),
        Index(value = ["referenceId"])
    ]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val debtId: Long? = null,
    val type: String, // "CUSTOMER_PAYMENT" or "SUPPLIER_PAYMENT"
    val referenceId: Long,
    val referenceName: String,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(
    tableName = "stock_history",
    indices = [
        Index(value = ["productId"]),
        Index(value = ["date"])
    ]
)
data class StockHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String,
    val previousQuantity: Int,
    val changeQuantity: Int,
    val newQuantity: Int,
    val type: String, // "PURCHASE", "SALE", "MANUAL_INCREASE", "MANUAL_DECREASE", "CORRECTION", "RETURN"
    val date: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val timestamp: Long,
    val repeatOption: String = "ONCE", // "ONCE", "DAILY", "WEEKLY", "MONTHLY"
    val relatedProductId: Long? = null,
    val relatedSupplierId: Long? = null,
    val isEnabled: Boolean = true,
    val createdDate: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "notification_states",
    indices = [Index(value = ["productId"], unique = true)]
)
data class NotificationStateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val lastNotifiedQuantity: Int,
    val lastNotificationType: String, // "LOW_STOCK" or "OUT_OF_STOCK"
    val lastNotifiedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val businessName: String = "My Business",
    val currency: String = "DA",
    val defaultMinimumStock: Int = 5,
    val allowNegativeStock: Boolean = false,
    val lowStockNotificationsEnabled: Boolean = true,
    val remindersEnabled: Boolean = true,
    val backgroundCheckEnabled: Boolean = true,
    val isDarkTheme: Boolean = true,
    val language: String = "en"
)

@Entity(
    tableName = "license_records",
    indices = [Index(value = ["license_key_id"], unique = true)]
)
data class LicenseRecordEntity(
    @PrimaryKey val license_id: String,
    val license_key_id: String,
    val license_type: String = "PRO",
    val status: String = "AVAILABLE", // AVAILABLE, ACTIVE, INVALID, REVOKED, SUSPENDED, ACTIVATION_LIMIT_REACHED, EXPIRED
    val max_devices: Int = 2,
    val activated_devices: Int = 0,
    val created_at: Long = System.currentTimeMillis(),
    val activated_at: Long? = null,
    val expires_at: Long? = null,
    val revoked_at: Long? = null,
    val last_seen: Long = System.currentTimeMillis(),
    val device_ids: String = "",
    val signature: String = ""
)

@Entity(tableName = "license")
data class LicenseEntity(
    @PrimaryKey val id: Int = 1,
    val licenseId: String = "",
    val maskedKey: String = "",
    val licenseKey: String = "",
    val status: String = "AVAILABLE", // AVAILABLE, ACTIVE, REVOKED, SUSPENDED, ACTIVATION_LIMIT_REACHED, EXPIRED, INVALID
    val activatedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = Long.MAX_VALUE,
    val deviceId: String = "",
    val businessName: String = "",
    val activationToken: String = ""
)

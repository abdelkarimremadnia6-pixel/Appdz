package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.database.AppDatabase
import com.example.data.entities.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class InventoryRepository(private val db: AppDatabase) {

    private val productDao = db.productDao()
    private val supplierDao = db.supplierDao()
    private val customerDao = db.customerDao()
    private val saleDao = db.saleDao()
    private val purchaseDao = db.purchaseDao()
    private val debtDao = db.debtDao()
    private val paymentDao = db.paymentDao()
    private val stockHistoryDao = db.stockHistoryDao()
    private val reminderDao = db.reminderDao()
    private val appSettingsDao = db.appSettingsDao()
    private val licenseDao = db.licenseDao()
    private val licenseRecordDao = db.licenseRecordDao()

    // Products
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()
    val outOfStockProducts: Flow<List<ProductEntity>> = productDao.getOutOfStockProducts()
    val totalProductsCount: Flow<Int> = productDao.getTotalProductsCount()
    val totalStockQuantity: Flow<Int?> = productDao.getTotalStockQuantity()
    val allCategories: Flow<List<String>> = productDao.getAllCategories()

    fun searchAndSortProducts(query: String, sort: String): Flow<List<ProductEntity>> {
        return productDao.searchAndSortProducts(query, sort)
    }

    fun getProductById(id: Long): Flow<ProductEntity?> = productDao.getProductById(id)
    suspend fun getProductByIdDirect(id: Long): ProductEntity? = productDao.getProductByIdDirect(id)
    fun getProductByBarcode(barcode: String): Flow<ProductEntity?> = productDao.getProductByBarcode(barcode)
    suspend fun getProductByBarcodeDirect(barcode: String): ProductEntity? = productDao.getProductByBarcodeDirect(barcode)

    suspend fun saveProduct(product: ProductEntity): Long {
        return if (product.id == 0L) {
            val id = productDao.insertProduct(product)
            if (product.quantity > 0) {
                stockHistoryDao.insertHistory(
                    StockHistoryEntity(
                        productId = id,
                        productName = product.name,
                        previousQuantity = 0,
                        changeQuantity = product.quantity,
                        newQuantity = product.quantity,
                        type = "INITIAL_STOCK",
                        note = "Initial stock when product was created"
                    )
                )
            }
            id
        } else {
            productDao.updateProduct(product)
            product.id
        }
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)
    }

    suspend fun adjustStock(
        productId: Long,
        delta: Int,
        type: String,
        note: String,
        allowNegative: Boolean = false
    ): Boolean {
        val product = productDao.getProductByIdDirect(productId) ?: return false
        val previousQty = product.quantity
        val newQty = previousQty + delta
        if (newQty < 0 && !allowNegative) {
            return false
        }
        productDao.updateQuantity(productId, newQty)
        stockHistoryDao.insertHistory(
            StockHistoryEntity(
                productId = productId,
                productName = product.name,
                previousQuantity = previousQty,
                changeQuantity = delta,
                newQuantity = newQty,
                type = type,
                note = note
            )
        )
        return true
    }

    // Suppliers
    val allSuppliers: Flow<List<SupplierEntity>> = supplierDao.getAllSuppliers()
    val totalSuppliersCount: Flow<Int> = supplierDao.getTotalSuppliersCount()

    fun searchSuppliers(query: String): Flow<List<SupplierEntity>> = supplierDao.searchSuppliers(query)
    fun getSupplierById(id: Long): Flow<SupplierEntity?> = supplierDao.getSupplierById(id)
    suspend fun getSupplierByIdDirect(id: Long): SupplierEntity? = supplierDao.getSupplierByIdDirect(id)

    suspend fun saveSupplier(supplier: SupplierEntity): Long {
        return if (supplier.id == 0L) {
            supplierDao.insertSupplier(supplier)
        } else {
            supplierDao.updateSupplier(supplier)
            supplier.id
        }
    }

    suspend fun deleteSupplier(supplier: SupplierEntity) {
        supplierDao.deleteSupplier(supplier)
    }

    // Customers
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    suspend fun saveCustomer(customer: CustomerEntity): Long {
        return if (customer.id == 0L) {
            customerDao.insertCustomer(customer)
        } else {
            customerDao.updateCustomer(customer)
            customer.id
        }
    }

    // Sales
    val allSales: Flow<List<SaleEntity>> = saleDao.getAllSales()

    fun getTodaySalesTotal(): Flow<Double?> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = cal.timeInMillis - 1
        return saleDao.getTodaySalesTotal(startOfDay, endOfDay)
    }

    fun getSaleItems(saleId: Long): Flow<List<SaleItemEntity>> = saleDao.getSaleItems(saleId)
    suspend fun getSaleItemsDirect(saleId: Long): List<SaleItemEntity> = saleDao.getSaleItemsDirect(saleId)

    suspend fun recordSale(
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        allowNegative: Boolean = false
    ): Result<Long> {
        return try {
            // First check stock for all items
            for (item in items) {
                val product = productDao.getProductByIdDirect(item.productId)
                    ?: return Result.failure(Exception("Product not found: ${item.productName}"))
                if (product.quantity < item.quantity && !allowNegative) {
                    return Result.failure(Exception("Insufficient stock for ${item.productName}. Available: ${product.quantity}, requested: ${item.quantity}"))
                }
            }

            var saleId = 0L
            db.withTransaction {
                saleId = saleDao.insertSale(sale)
                val itemsWithSaleId = items.map { it.copy(saleId = saleId) }
                saleDao.insertSaleItems(itemsWithSaleId)

                // Decrease stock and record history
                for (item in items) {
                    val product = productDao.getProductByIdDirect(item.productId)!!
                    val prev = product.quantity
                    val newQty = prev - item.quantity
                    productDao.updateQuantity(product.id, newQty)
                    stockHistoryDao.insertHistory(
                        StockHistoryEntity(
                            productId = product.id,
                            productName = product.name,
                            previousQuantity = prev,
                            changeQuantity = -item.quantity,
                            newQuantity = newQty,
                            type = "SALE",
                            note = "Sale receipt #${sale.receiptNumber}"
                        )
                    )
                }

                // If customer owed amount > 0
                if (sale.remainingAmount > 0) {
                    debtDao.insertDebt(
                        DebtEntity(
                            type = "CUSTOMER",
                            referenceId = sale.customerId ?: 0L,
                            referenceName = if (sale.customerName.isNotBlank()) sale.customerName else "Walk-in Customer",
                            amount = sale.remainingAmount,
                            remainingAmount = sale.remainingAmount,
                            notes = "Remaining from sale receipt #${sale.receiptNumber}"
                        )
                    )
                    if (sale.customerId != null && sale.customerId > 0) {
                        customerDao.adjustCustomerDebt(sale.customerId, sale.remainingAmount)
                    }
                }
            }
            Result.success(saleId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Purchases
    val allPurchases: Flow<List<PurchaseEntity>> = purchaseDao.getAllPurchases()

    fun getTodayPurchasesTotal(): Flow<Double?> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = cal.timeInMillis - 1
        return purchaseDao.getTodayPurchasesTotal(startOfDay, endOfDay)
    }

    fun getPurchasesBySupplier(supplierId: Long): Flow<List<PurchaseEntity>> =
        purchaseDao.getPurchasesBySupplier(supplierId)

    fun getPurchaseItems(purchaseId: Long): Flow<List<PurchaseItemEntity>> =
        purchaseDao.getPurchaseItems(purchaseId)

    suspend fun getPurchaseItemsDirect(purchaseId: Long): List<PurchaseItemEntity> =
        purchaseDao.getPurchaseItemsDirect(purchaseId)

    suspend fun recordPurchase(
        purchase: PurchaseEntity,
        items: List<PurchaseItemEntity>
    ): Long {
        var purchaseId = 0L
        db.withTransaction {
            purchaseId = purchaseDao.insertPurchase(purchase)
            val itemsWithPurchaseId = items.map { it.copy(purchaseId = purchaseId) }
            purchaseDao.insertPurchaseItems(itemsWithPurchaseId)

            // Increase stock and record history
            for (item in items) {
                val product = productDao.getProductByIdDirect(item.productId)
                if (product != null) {
                    val prev = product.quantity
                    val newQty = prev + item.quantity
                    productDao.updateQuantity(product.id, newQty)
                    stockHistoryDao.insertHistory(
                        StockHistoryEntity(
                            productId = product.id,
                            productName = product.name,
                            previousQuantity = prev,
                            changeQuantity = item.quantity,
                            newQuantity = newQty,
                            type = "PURCHASE",
                            note = "Purchase from supplier: ${purchase.supplierName}"
                        )
                    )
                }
            }

            // Update supplier owed and total purchases
            supplierDao.adjustSupplierFinancials(
                id = purchase.supplierId,
                deltaOwed = purchase.remainingAmount,
                deltaPurchases = purchase.total
            )

            if (purchase.remainingAmount > 0) {
                debtDao.insertDebt(
                    DebtEntity(
                        type = "SUPPLIER",
                        referenceId = purchase.supplierId,
                        referenceName = purchase.supplierName,
                        amount = purchase.remainingAmount,
                        remainingAmount = purchase.remainingAmount,
                        notes = "Remaining from purchase #${purchaseId}"
                    )
                )
            }
        }
        return purchaseId
    }

    // Debts & Payments
    val allDebts: Flow<List<DebtEntity>> = debtDao.getAllDebts()
    val totalDebtsAmount: Flow<Double?> = debtDao.getTotalDebtsAmount()
    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()

    fun getDebtsByType(type: String): Flow<List<DebtEntity>> = debtDao.getDebtsByType(type)
    fun getPaymentsByReference(refId: Long, type: String): Flow<List<PaymentEntity>> =
        paymentDao.getPaymentsByReference(refId, type)

    suspend fun addDebt(debt: DebtEntity): Long = debtDao.insertDebt(debt)

    suspend fun recordPayment(
        debtId: Long?,
        type: String, // "CUSTOMER_PAYMENT" or "SUPPLIER_PAYMENT"
        referenceId: Long,
        referenceName: String,
        amount: Double,
        notes: String
    ): Long {
        var paymentId = 0L
        db.withTransaction {
            paymentId = paymentDao.insertPayment(
                PaymentEntity(
                    debtId = debtId,
                    type = type,
                    referenceId = referenceId,
                    referenceName = referenceName,
                    amount = amount,
                    notes = notes
                )
            )

            if (debtId != null) {
                val debt = debtDao.getDebtByIdDirect(debtId)
                if (debt != null) {
                    val newRemaining = maxOf(0.0, debt.remainingAmount - amount)
                    debtDao.updateDebt(debt.copy(remainingAmount = newRemaining))
                }
            }

            if (type == "CUSTOMER_PAYMENT" && referenceId > 0) {
                customerDao.adjustCustomerDebt(referenceId, -amount)
            } else if (type == "SUPPLIER_PAYMENT" && referenceId > 0) {
                supplierDao.adjustSupplierFinancials(referenceId, -amount, 0.0)
            }
        }
        return paymentId
    }

    // Stock History
    val allStockHistory: Flow<List<StockHistoryEntity>> = stockHistoryDao.getAllHistory()
    fun getHistoryByProduct(productId: Long): Flow<List<StockHistoryEntity>> =
        stockHistoryDao.getHistoryByProduct(productId)

    // Reminders
    val allReminders: Flow<List<ReminderEntity>> = reminderDao.getAllReminders()
    suspend fun saveReminder(reminder: ReminderEntity): Long {
        return if (reminder.id == 0L) {
            reminderDao.insertReminder(reminder)
        } else {
            reminderDao.updateReminder(reminder)
            reminder.id
        }
    }
    suspend fun deleteReminder(reminder: ReminderEntity) = reminderDao.deleteReminder(reminder)

    // Settings
    val appSettings: Flow<AppSettingsEntity?> = appSettingsDao.getSettings()
    suspend fun getAppSettingsDirect(): AppSettingsEntity {
        var settings = appSettingsDao.getSettingsDirect()
        if (settings == null) {
            settings = AppSettingsEntity()
            appSettingsDao.insertOrUpdate(settings)
        }
        return settings
    }
    suspend fun saveAppSettings(settings: AppSettingsEntity) = appSettingsDao.insertOrUpdate(settings)

    // License
    val license: Flow<LicenseEntity?> = licenseDao.getLicense()
    suspend fun getLicenseDirect(): LicenseEntity? = licenseDao.getLicenseDirect()
    suspend fun saveLicense(license: LicenseEntity) = licenseDao.insertOrUpdate(license)
    suspend fun clearActiveLicense() = licenseDao.clearActiveLicense()

    // License Records (Production Catalog & Device Table)
    val allLicenseRecords: Flow<List<LicenseRecordEntity>> = licenseRecordDao.getAllRecords()
    suspend fun getAllLicenseRecordsDirect(): List<LicenseRecordEntity> = licenseRecordDao.getAllRecordsDirect()
    suspend fun getLicenseRecordById(id: String): LicenseRecordEntity? = licenseRecordDao.getRecordById(id)
    suspend fun getLicenseRecordByKeyId(keyId: String): LicenseRecordEntity? = licenseRecordDao.getRecordByKeyId(keyId)
    suspend fun saveLicenseRecord(record: LicenseRecordEntity) = licenseRecordDao.insertOrUpdate(record)
    suspend fun seedLicenseRecords(records: List<LicenseRecordEntity>) = licenseRecordDao.insertAll(records)
    suspend fun getLicenseRecordCount(): Int = licenseRecordDao.getRecordCount()
}

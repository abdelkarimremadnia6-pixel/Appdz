package com.example.data.dao

import androidx.room.*
import com.example.data.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products")
    suspend fun getAllProductsDirect(): List<ProductEntity>

    @Query("""
        SELECT * FROM products 
        WHERE (:query = '' OR name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%')
        ORDER BY 
            CASE WHEN :sort = 'NAME_ASC' THEN name END ASC,
            CASE WHEN :sort = 'NAME_DESC' THEN name END DESC,
            CASE WHEN :sort = 'QTY_LOW' THEN quantity END ASC,
            CASE WHEN :sort = 'QTY_HIGH' THEN quantity END DESC,
            CASE WHEN :sort = 'PRICE_LOW' THEN sellingPrice END ASC,
            CASE WHEN :sort = 'PRICE_HIGH' THEN sellingPrice END DESC,
            CASE WHEN :sort = 'RECENT' THEN updatedDate END DESC
    """)
    fun searchAndSortProducts(query: String, sort: String = "NAME_ASC"): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: Long): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductByIdDirect(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    fun getProductByBarcode(barcode: String): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcodeDirect(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE quantity > 0 AND quantity <= minimumQuantity ORDER BY quantity ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE quantity > 0 AND quantity <= minimumQuantity")
    suspend fun getLowStockProductsDirect(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE quantity <= 0 ORDER BY name ASC")
    fun getOutOfStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE quantity <= 0")
    suspend fun getOutOfStockProductsDirect(): List<ProductEntity>

    @Query("SELECT COUNT(*) FROM products")
    fun getTotalProductsCount(): Flow<Int>

    @Query("SELECT SUM(quantity) FROM products")
    fun getTotalStockQuantity(): Flow<Int?>

    @Query("SELECT DISTINCT category FROM products WHERE category != '' ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("UPDATE products SET quantity = :newQty, updatedDate = :updatedDate WHERE id = :id")
    suspend fun updateQuantity(id: Long, newQty: Int, updatedDate: Long = System.currentTimeMillis())
}

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Query("""
        SELECT * FROM suppliers 
        WHERE (:query = '' OR name LIKE '%' || :query || '%' OR company LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    fun searchSuppliers(query: String): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    fun getSupplierById(id: Long): Flow<SupplierEntity?>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getSupplierByIdDirect(id: Long): SupplierEntity?

    @Query("SELECT COUNT(*) FROM suppliers")
    fun getTotalSuppliersCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity): Long

    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)

    @Delete
    suspend fun deleteSupplier(supplier: SupplierEntity)

    @Query("UPDATE suppliers SET amountOwed = amountOwed + :deltaOwed, totalPurchases = totalPurchases + :deltaPurchases WHERE id = :id")
    suspend fun adjustSupplierFinancials(id: Long, deltaOwed: Double, deltaPurchases: Double)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerById(id: Long): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerByIdDirect(id: Long): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET debtAmount = debtAmount + :deltaDebt WHERE id = :id")
    suspend fun adjustCustomerDebt(id: Long, deltaDebt: Double)
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :id")
    fun getSaleById(id: Long): Flow<SaleEntity?>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    fun getSaleItems(saleId: Long): Flow<List<SaleItemEntity>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItemsDirect(saleId: Long): List<SaleItemEntity>

    @Query("SELECT SUM(total) FROM sales WHERE date >= :startOfDay AND date <= :endOfDay")
    fun getTodaySalesTotal(startOfDay: Long, endOfDay: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)
}

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases ORDER BY date DESC")
    fun getAllPurchases(): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE supplierId = :supplierId ORDER BY date DESC")
    fun getPurchasesBySupplier(supplierId: Long): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    fun getPurchaseItems(purchaseId: Long): Flow<List<PurchaseItemEntity>>

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun getPurchaseItemsDirect(purchaseId: Long): List<PurchaseItemEntity>

    @Query("SELECT SUM(total) FROM purchases WHERE date >= :startOfDay AND date <= :endOfDay")
    fun getTodayPurchasesTotal(startOfDay: Long, endOfDay: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(items: List<PurchaseItemEntity>)
}

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY date DESC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE type = :type ORDER BY date DESC")
    fun getDebtsByType(type: String): Flow<List<DebtEntity>>

    @Query("SELECT SUM(remainingAmount) FROM debts WHERE remainingAmount > 0")
    fun getTotalDebtsAmount(): Flow<Double?>

    @Query("SELECT * FROM debts WHERE id = :id")
    suspend fun getDebtByIdDirect(id: Long): DebtEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Delete
    suspend fun deleteDebt(debt: DebtEntity)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY date DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE referenceId = :referenceId AND type = :type ORDER BY date DESC")
    fun getPaymentsByReference(referenceId: Long, type: String): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long
}

@Dao
interface StockHistoryDao {
    @Query("SELECT * FROM stock_history ORDER BY date DESC")
    fun getAllHistory(): Flow<List<StockHistoryEntity>>

    @Query("SELECT * FROM stock_history WHERE productId = :productId ORDER BY date DESC")
    fun getHistoryByProduct(productId: Long): Flow<List<StockHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: StockHistoryEntity): Long
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY timestamp ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1")
    suspend fun getEnabledReminders(): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)
}

@Dao
interface NotificationStateDao {
    @Query("SELECT * FROM notification_states WHERE productId = :productId LIMIT 1")
    suspend fun getStateForProduct(productId: Long): NotificationStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(state: NotificationStateEntity): Long

    @Query("DELETE FROM notification_states WHERE productId = :productId")
    suspend fun deleteForProduct(productId: Long)
}

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: AppSettingsEntity)
}

@Dao
interface LicenseDao {
    @Query("SELECT * FROM license WHERE id = 1 LIMIT 1")
    fun getLicense(): Flow<LicenseEntity?>

    @Query("SELECT * FROM license WHERE id = 1 LIMIT 1")
    suspend fun getLicenseDirect(): LicenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(license: LicenseEntity)

    @Query("DELETE FROM license WHERE id = 1")
    suspend fun clearActiveLicense()
}

@Dao
interface LicenseRecordDao {
    @Query("SELECT * FROM license_records ORDER BY license_id ASC")
    fun getAllRecords(): Flow<List<LicenseRecordEntity>>

    @Query("SELECT * FROM license_records ORDER BY license_id ASC")
    suspend fun getAllRecordsDirect(): List<LicenseRecordEntity>

    @Query("SELECT * FROM license_records WHERE license_id = :id LIMIT 1")
    suspend fun getRecordById(id: String): LicenseRecordEntity?

    @Query("SELECT * FROM license_records WHERE license_key_id = :keyId LIMIT 1")
    suspend fun getRecordByKeyId(keyId: String): LicenseRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: LicenseRecordEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(records: List<LicenseRecordEntity>)

    @Update
    suspend fun update(record: LicenseRecordEntity)

    @Query("SELECT COUNT(*) FROM license_records")
    suspend fun getRecordCount(): Int

    @Query("DELETE FROM license_records")
    suspend fun clearAll()
}

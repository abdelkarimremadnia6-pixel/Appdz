package com.example.utils

import android.content.Context
import android.net.Uri
import com.example.data.database.AppDatabase
import com.example.data.entities.AppSettingsEntity
import com.example.data.entities.CustomerEntity
import com.example.data.entities.DebtEntity
import com.example.data.entities.ProductEntity
import com.example.data.entities.SupplierEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object BackupRestoreManager {

    suspend fun exportBackupJson(context: Context, uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)
            val products = db.productDao().getAllProductsDirect()
            val suppliers = db.supplierDao().getAllSuppliers()
            val customers = db.customerDao().getAllCustomers()
            val settings = db.appSettingsDao().getSettingsDirect() ?: AppSettingsEntity()

            val rootJson = JSONObject()
            rootJson.put("version", 1)
            rootJson.put("timestamp", System.currentTimeMillis())
            rootJson.put("app", "InventoryManager")

            // Settings
            val settingsJson = JSONObject().apply {
                put("businessName", settings.businessName)
                put("currency", settings.currency)
                put("defaultMinimumStock", settings.defaultMinimumStock)
                put("allowNegativeStock", settings.allowNegativeStock)
            }
            rootJson.put("settings", settingsJson)

            // Products
            val productsArray = JSONArray()
            for (p in products) {
                val pJson = JSONObject().apply {
                    put("id", p.id)
                    put("barcode", p.barcode)
                    put("name", p.name)
                    put("category", p.category)
                    put("description", p.description)
                    put("purchasePrice", p.purchasePrice)
                    put("sellingPrice", p.sellingPrice)
                    put("quantity", p.quantity)
                    put("minimumQuantity", p.minimumQuantity)
                    put("unit", p.unit)
                    put("notes", p.notes)
                }
                productsArray.put(pJson)
            }
            rootJson.put("products", productsArray)

            // Write to Uri
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(rootJson.toString(2))
                }
            }

            Result.success(products.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importBackupJson(context: Context, uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val content = StringBuilder()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        content.append(line)
                        line = reader.readLine()
                    }
                }
            }

            val rootJson = JSONObject(content.toString())
            if (!rootJson.has("products") && !rootJson.has("app")) {
                return@withContext Result.failure(Exception("Invalid backup file structure"))
            }

            val db = AppDatabase.getInstance(context)
            var restoredCount = 0

            // Restore products
            if (rootJson.has("products")) {
                val productsArray = rootJson.getJSONArray("products")
                for (i in 0 until productsArray.length()) {
                    val pJson = productsArray.getJSONObject(i)
                    val product = ProductEntity(
                        name = pJson.optString("name", "Restored Product"),
                        barcode = pJson.optString("barcode", ""),
                        category = pJson.optString("category", "General"),
                        description = pJson.optString("description", ""),
                        purchasePrice = pJson.optDouble("purchasePrice", 0.0),
                        sellingPrice = pJson.optDouble("sellingPrice", 0.0),
                        quantity = pJson.optInt("quantity", 0),
                        minimumQuantity = pJson.optInt("minimumQuantity", 5),
                        unit = pJson.optString("unit", "pcs"),
                        notes = pJson.optString("notes", "")
                    )
                    db.productDao().insertProduct(product)
                    restoredCount++
                }
            }

            // Restore settings if present
            if (rootJson.has("settings")) {
                val sJson = rootJson.getJSONObject("settings")
                val current = db.appSettingsDao().getSettingsDirect() ?: AppSettingsEntity()
                db.appSettingsDao().insertOrUpdate(
                    current.copy(
                        businessName = sJson.optString("businessName", current.businessName),
                        currency = sJson.optString("currency", current.currency),
                        defaultMinimumStock = sJson.optInt("defaultMinimumStock", current.defaultMinimumStock),
                        allowNegativeStock = sJson.optBoolean("allowNegativeStock", current.allowNegativeStock)
                    )
                )
            }

            Result.success(restoredCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

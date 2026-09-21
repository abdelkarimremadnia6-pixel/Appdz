package com.example.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.InventoryApplication
import com.example.data.repository.InventoryRepository
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.debts.DebtsViewModel
import com.example.ui.license.LicenseViewModel
import com.example.ui.products.ProductsViewModel
import com.example.ui.purchases.PurchasesViewModel
import com.example.ui.reminders.RemindersViewModel
import com.example.ui.sales.SalesViewModel
import com.example.ui.settings.SettingsViewModel
import com.example.ui.suppliers.SuppliersViewModel

class AppViewModelFactory(
    private val app: Application,
    private val repository: InventoryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ProductsViewModel::class.java) -> {
                ProductsViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SalesViewModel::class.java) -> {
                SalesViewModel(app, repository) as T
            }
            modelClass.isAssignableFrom(PurchasesViewModel::class.java) -> {
                PurchasesViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SuppliersViewModel::class.java) -> {
                SuppliersViewModel(repository) as T
            }
            modelClass.isAssignableFrom(DebtsViewModel::class.java) -> {
                DebtsViewModel(repository) as T
            }
            modelClass.isAssignableFrom(RemindersViewModel::class.java) -> {
                RemindersViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(app, repository) as T
            }
            modelClass.isAssignableFrom(LicenseViewModel::class.java) -> {
                LicenseViewModel(app, repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

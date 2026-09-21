package com.example.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entities.ProductEntity
import com.example.data.repository.InventoryRepository
import kotlinx.coroutines.flow.*

data class DashboardUiState(
    val totalProducts: Int = 0,
    val totalStockQty: Int = 0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val todaySales: Double = 0.0,
    val todayPurchases: Double = 0.0,
    val totalDebts: Double = 0.0,
    val totalSuppliers: Int = 0,
    val currency: String = "DA",
    val lowStockItems: List<ProductEntity> = emptyList(),
    val outOfStockItems: List<ProductEntity> = emptyList()
)

class DashboardViewModel(private val repository: InventoryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        combine(
            repository.totalProductsCount,
            repository.totalStockQuantity,
            repository.lowStockProducts,
            repository.outOfStockProducts,
            repository.getTodaySalesTotal(),
            repository.getTodayPurchasesTotal(),
            repository.totalDebtsAmount,
            repository.totalSuppliersCount,
            repository.appSettings
        ) { array ->
            @Suppress("UNCHECKED_CAST")
            val totalProd = array[0] as Int
            @Suppress("UNCHECKED_CAST")
            val totalQty = array[1] as? Int ?: 0
            @Suppress("UNCHECKED_CAST")
            val lowList = array[2] as? List<ProductEntity> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val outList = array[3] as? List<ProductEntity> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val todaySale = array[4] as? Double ?: 0.0
            @Suppress("UNCHECKED_CAST")
            val todayPurch = array[5] as? Double ?: 0.0
            @Suppress("UNCHECKED_CAST")
            val debts = array[6] as? Double ?: 0.0
            @Suppress("UNCHECKED_CAST")
            val suppliers = array[7] as? Int ?: 0
            val settings = array[8] as? com.example.data.entities.AppSettingsEntity

            DashboardUiState(
                totalProducts = totalProd,
                totalStockQty = totalQty,
                lowStockCount = lowList.size,
                outOfStockCount = outList.size,
                todaySales = todaySale,
                todayPurchases = todayPurch,
                totalDebts = debts,
                totalSuppliers = suppliers,
                currency = settings?.currency ?: "DA",
                lowStockItems = lowList,
                outOfStockItems = outList
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }
}

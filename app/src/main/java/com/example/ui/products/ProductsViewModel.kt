package com.example.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entities.ProductEntity
import com.example.data.entities.StockHistoryEntity
import com.example.data.entities.SupplierEntity
import com.example.data.repository.InventoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class StockFilter { ALL, IN_STOCK, LOW_STOCK, OUT_OF_STOCK }

data class ProductsUiState(
    val products: List<ProductEntity> = emptyList(),
    val categories: List<String> = emptyList(),
    val suppliers: List<SupplierEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "",
    val stockFilter: StockFilter = StockFilter.ALL,
    val sortOption: String = "NAME_ASC",
    val currency: String = "DA",
    val isLoading: Boolean = false
)

class ProductsViewModel(private val repository: InventoryRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("")
    private val _stockFilter = MutableStateFlow(StockFilter.ALL)
    private val _sortOption = MutableStateFlow("NAME_ASC")

    val searchQuery = _searchQuery.asStateFlow()
    val selectedCategory = _selectedCategory.asStateFlow()
    val stockFilter = _stockFilter.asStateFlow()
    val sortOption = _sortOption.asStateFlow()

    val categories: StateFlow<List<String>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<SupplierEntity>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currency: StateFlow<String> = repository.appSettings
        .map { it?.currency ?: "DA" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "DA")

    val products: StateFlow<List<ProductEntity>> = combine(
        repository.allProducts,
        _searchQuery,
        _selectedCategory,
        _stockFilter,
        _sortOption
    ) { all, query, category, filter, sort ->
        var list = all

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                        it.barcode.lowercase().contains(q) ||
                        it.category.lowercase().contains(q)
            }
        }

        if (category.isNotBlank()) {
            list = list.filter { it.category.equals(category, ignoreCase = true) }
        }

        list = when (filter) {
            StockFilter.ALL -> list
            StockFilter.IN_STOCK -> list.filter { it.isInStock }
            StockFilter.LOW_STOCK -> list.filter { it.isLowStock }
            StockFilter.OUT_OF_STOCK -> list.filter { it.isOutOfStock }
        }

        when (sort) {
            "NAME_ASC" -> list.sortedBy { it.name.lowercase() }
            "NAME_DESC" -> list.sortedByDescending { it.name.lowercase() }
            "QTY_LOW" -> list.sortedBy { it.quantity }
            "QTY_HIGH" -> list.sortedByDescending { it.quantity }
            "PRICE_LOW" -> list.sortedBy { it.sellingPrice }
            "PRICE_HIGH" -> list.sortedByDescending { it.sellingPrice }
            "RECENT" -> list.sortedByDescending { it.updatedDate }
            else -> list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = if (_selectedCategory.value == category) "" else category
    }

    fun setStockFilter(filter: StockFilter) {
        _stockFilter.value = filter
    }

    fun setSortOption(sort: String) {
        _sortOption.value = sort
    }

    fun getProductById(id: Long): Flow<ProductEntity?> = repository.getProductById(id)

    fun getProductHistory(productId: Long): Flow<List<StockHistoryEntity>> =
        repository.getHistoryByProduct(productId)

    fun quickAdjustStock(productId: Long, delta: Int, note: String = "Quick adjustment") {
        viewModelScope.launch {
            val type = if (delta > 0) "MANUAL_INCREASE" else "MANUAL_DECREASE"
            repository.adjustStock(
                productId = productId,
                delta = delta,
                type = type,
                note = note,
                allowNegative = false
            )
        }
    }

    fun duplicateProduct(product: ProductEntity, onDone: (Long) -> Unit) {
        viewModelScope.launch {
            val newProd = product.copy(
                id = 0,
                name = "${product.name} (Copy)",
                barcode = if (product.barcode.isNotBlank()) "${product.barcode}_copy" else "",
                createdDate = System.currentTimeMillis(),
                updatedDate = System.currentTimeMillis()
            )
            val newId = repository.saveProduct(newProd)
            onDone(newId)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun saveProduct(product: ProductEntity, onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.saveProduct(product)
            onSaved(id)
        }
    }
}

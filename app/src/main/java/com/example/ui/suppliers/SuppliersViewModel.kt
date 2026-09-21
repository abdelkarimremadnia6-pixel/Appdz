package com.example.ui.suppliers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entities.PurchaseEntity
import com.example.data.entities.SupplierEntity
import com.example.data.repository.InventoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SuppliersViewModel(private val repository: InventoryRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val currency: StateFlow<String> = repository.appSettings
        .map { it?.currency ?: "DA" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "DA")

    val suppliers: StateFlow<List<SupplierEntity>> = combine(
        repository.allSuppliers,
        _searchQuery
    ) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            val q = query.trim().lowercase()
            list.filter {
                it.name.lowercase().contains(q) ||
                        it.company.lowercase().contains(q) ||
                        it.phone.contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getSupplierById(id: Long): Flow<SupplierEntity?> = repository.getSupplierById(id)

    fun getPurchasesBySupplier(supplierId: Long): Flow<List<PurchaseEntity>> =
        repository.getPurchasesBySupplier(supplierId)

    fun saveSupplier(supplier: SupplierEntity, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveSupplier(supplier)
            onSaved()
        }
    }

    fun deleteSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            repository.deleteSupplier(supplier)
        }
    }

    fun recordPayment(supplier: SupplierEntity, amount: Double, notes: String, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.recordPayment(
                debtId = null,
                type = "SUPPLIER_PAYMENT",
                referenceId = supplier.id,
                referenceName = supplier.name,
                amount = amount,
                notes = notes
            )
            onDone()
        }
    }
}

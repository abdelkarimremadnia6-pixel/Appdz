package com.example.ui.debts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entities.DebtEntity
import com.example.data.entities.PaymentEntity
import com.example.data.repository.InventoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DebtsViewModel(private val repository: InventoryRepository) : ViewModel() {

    private val _selectedTab = MutableStateFlow("CUSTOMER") // "CUSTOMER" or "SUPPLIER"
    val selectedTab = _selectedTab.asStateFlow()

    val currency: StateFlow<String> = repository.appSettings
        .map { it?.currency ?: "DA" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "DA")

    val totalDebts: StateFlow<Double> = repository.totalDebtsAmount
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val debts: StateFlow<List<DebtEntity>> = combine(
        repository.allDebts,
        _selectedTab
    ) { all, tab ->
        all.filter { it.type == tab }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<PaymentEntity>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedTab(tab: String) {
        _selectedTab.value = tab
    }

    fun addDebt(debt: DebtEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.addDebt(debt)
            onDone()
        }
    }

    fun recordPayment(debt: DebtEntity, amount: Double, notes: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val paymentType = if (debt.type == "CUSTOMER") "CUSTOMER_PAYMENT" else "SUPPLIER_PAYMENT"
            repository.recordPayment(
                debtId = debt.id,
                type = paymentType,
                referenceId = debt.referenceId,
                referenceName = debt.referenceName,
                amount = amount,
                notes = notes
            )
            onDone()
        }
    }
}

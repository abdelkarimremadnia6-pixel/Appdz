package com.example.ui.purchases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entities.ProductEntity
import com.example.data.entities.PurchaseEntity
import com.example.data.entities.PurchaseItemEntity
import com.example.data.entities.SupplierEntity
import com.example.data.repository.InventoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PurchaseCartItem(
    val product: ProductEntity,
    var quantity: Int = 1,
    var purchasePrice: Double = product.purchasePrice
) {
    val total: Double get() = quantity * purchasePrice
}

data class NewPurchaseUiState(
    val selectedSupplier: SupplierEntity? = null,
    val cart: List<PurchaseCartItem> = emptyList(),
    val paidAmount: Double = 0.0,
    val notes: String = "",
    val errorMessage: String? = null,
    val isCompleting: Boolean = false
) {
    val total: Double get() = cart.sumOf { it.total }
    val remainingAmount: Double get() = maxOf(0.0, total - paidAmount)
}

class PurchasesViewModel(private val repository: InventoryRepository) : ViewModel() {

    val allPurchases: StateFlow<List<PurchaseEntity>> = repository.allPurchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<SupplierEntity>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currency: StateFlow<String> = repository.appSettings
        .map { it?.currency ?: "DA" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "DA")

    private val _newPurchaseState = MutableStateFlow(NewPurchaseUiState())
    val newPurchaseState: StateFlow<NewPurchaseUiState> = _newPurchaseState.asStateFlow()

    fun setSupplier(supplier: SupplierEntity) {
        _newPurchaseState.value = _newPurchaseState.value.copy(selectedSupplier = supplier)
    }

    fun addToCart(product: ProductEntity) {
        val currentCart = _newPurchaseState.value.cart.toMutableList()
        val index = currentCart.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = currentCart[index]
            currentCart[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            currentCart.add(PurchaseCartItem(product = product, quantity = 1, purchasePrice = product.purchasePrice))
        }
        val total = currentCart.sumOf { it.total }
        _newPurchaseState.value = _newPurchaseState.value.copy(
            cart = currentCart,
            paidAmount = total,
            errorMessage = null
        )
    }

    fun updateCartItemQuantity(productId: Long, delta: Int) {
        val currentCart = _newPurchaseState.value.cart.toMutableList()
        val index = currentCart.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val existing = currentCart[index]
            val newQty = existing.quantity + delta
            if (newQty <= 0) {
                currentCart.removeAt(index)
            } else {
                currentCart[index] = existing.copy(quantity = newQty)
            }
            val total = currentCart.sumOf { it.total }
            _newPurchaseState.value = _newPurchaseState.value.copy(
                cart = currentCart,
                paidAmount = total
            )
        }
    }

    fun removeCartItem(productId: Long) {
        val currentCart = _newPurchaseState.value.cart.filter { it.product.id != productId }
        val total = currentCart.sumOf { it.total }
        _newPurchaseState.value = _newPurchaseState.value.copy(
            cart = currentCart,
            paidAmount = total
        )
    }

    fun setPaidAmount(amount: Double) {
        _newPurchaseState.value = _newPurchaseState.value.copy(paidAmount = amount)
    }

    fun setNotes(notes: String) {
        _newPurchaseState.value = _newPurchaseState.value.copy(notes = notes)
    }

    fun resetNewPurchase() {
        _newPurchaseState.value = NewPurchaseUiState()
    }

    fun completePurchase(onSuccess: (Long) -> Unit) {
        val state = _newPurchaseState.value
        if (state.selectedSupplier == null) {
            _newPurchaseState.value = state.copy(errorMessage = "Please select a supplier")
            return
        }
        if (state.cart.isEmpty()) {
            _newPurchaseState.value = state.copy(errorMessage = "Please add at least one product")
            return
        }

        viewModelScope.launch {
            _newPurchaseState.value = state.copy(isCompleting = true, errorMessage = null)

            val purchase = PurchaseEntity(
                supplierId = state.selectedSupplier.id,
                supplierName = state.selectedSupplier.name,
                date = System.currentTimeMillis(),
                total = state.total,
                paidAmount = state.paidAmount,
                remainingAmount = state.remainingAmount,
                notes = state.notes
            )

            val items = state.cart.map { item ->
                PurchaseItemEntity(
                    purchaseId = 0,
                    productId = item.product.id,
                    productName = item.product.name,
                    quantity = item.quantity,
                    purchasePrice = item.purchasePrice,
                    total = item.total
                )
            }

            val id = repository.recordPurchase(purchase, items)
            _newPurchaseState.value = state.copy(isCompleting = false)
            onSuccess(id)
        }
    }

    fun getPurchaseItems(purchaseId: Long): Flow<List<PurchaseItemEntity>> =
        repository.getPurchaseItems(purchaseId)
}

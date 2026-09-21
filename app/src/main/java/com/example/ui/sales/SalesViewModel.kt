package com.example.ui.sales

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entities.CustomerEntity
import com.example.data.entities.ProductEntity
import com.example.data.entities.SaleEntity
import com.example.data.entities.SaleItemEntity
import com.example.data.repository.InventoryRepository
import com.example.utils.PdfReceiptGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class CartItem(
    val product: ProductEntity,
    var quantity: Int = 1,
    var unitPrice: Double = product.sellingPrice,
    var discount: Double = 0.0
) {
    val total: Double get() = maxOf(0.0, (quantity * unitPrice) - discount)
}

data class NewSaleUiState(
    val cart: List<CartItem> = emptyList(),
    val customerName: String = "",
    val customerPhone: String = "",
    val selectedCustomerId: Long? = null,
    val discount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val errorMessage: String? = null,
    val isCompleting: Boolean = false,
    val completedSale: SaleEntity? = null,
    val completedItems: List<SaleItemEntity> = emptyList(),
    val receiptPdfFile: File? = null
) {
    val subtotal: Double get() = cart.sumOf { it.quantity * it.unitPrice }
    val total: Double get() = maxOf(0.0, subtotal - discount)
    val remainingAmount: Double get() = maxOf(0.0, total - paidAmount)
}

class SalesViewModel(
    application: Application,
    private val repository: InventoryRepository
) : AndroidViewModel(application) {

    val allSales: StateFlow<List<SaleEntity>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currency: StateFlow<String> = repository.appSettings
        .map { it?.currency ?: "DA" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "DA")

    private val _newSaleState = MutableStateFlow(NewSaleUiState())
    val newSaleState: StateFlow<NewSaleUiState> = _newSaleState.asStateFlow()

    fun addToCart(product: ProductEntity) {
        val currentCart = _newSaleState.value.cart.toMutableList()
        val index = currentCart.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = currentCart[index]
            currentCart[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            currentCart.add(CartItem(product = product, quantity = 1, unitPrice = product.sellingPrice))
        }
        val subtotal = currentCart.sumOf { it.quantity * it.unitPrice }
        val total = maxOf(0.0, subtotal - _newSaleState.value.discount)
        _newSaleState.value = _newSaleState.value.copy(
            cart = currentCart,
            paidAmount = total, // Default paid = total
            errorMessage = null
        )
    }

    fun updateCartItemQuantity(productId: Long, delta: Int) {
        val currentCart = _newSaleState.value.cart.toMutableList()
        val index = currentCart.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val existing = currentCart[index]
            val newQty = existing.quantity + delta
            if (newQty <= 0) {
                currentCart.removeAt(index)
            } else {
                currentCart[index] = existing.copy(quantity = newQty)
            }
            val subtotal = currentCart.sumOf { it.quantity * it.unitPrice }
            val total = maxOf(0.0, subtotal - _newSaleState.value.discount)
            _newSaleState.value = _newSaleState.value.copy(
                cart = currentCart,
                paidAmount = total,
                errorMessage = null
            )
        }
    }

    fun removeCartItem(productId: Long) {
        val currentCart = _newSaleState.value.cart.filter { it.product.id != productId }
        val subtotal = currentCart.sumOf { it.quantity * it.unitPrice }
        val total = maxOf(0.0, subtotal - _newSaleState.value.discount)
        _newSaleState.value = _newSaleState.value.copy(
            cart = currentCart,
            paidAmount = total
        )
    }

    fun setCustomer(name: String, phone: String = "", id: Long? = null) {
        _newSaleState.value = _newSaleState.value.copy(
            customerName = name,
            customerPhone = phone,
            selectedCustomerId = id
        )
    }

    fun setDiscount(discount: Double) {
        val subtotal = _newSaleState.value.subtotal
        val total = maxOf(0.0, subtotal - discount)
        _newSaleState.value = _newSaleState.value.copy(
            discount = discount,
            paidAmount = total
        )
    }

    fun setPaidAmount(paid: Double) {
        _newSaleState.value = _newSaleState.value.copy(paidAmount = paid)
    }

    fun resetNewSale() {
        _newSaleState.value = NewSaleUiState()
    }

    fun completeSale(onSuccess: (SaleEntity, File) -> Unit) {
        val state = _newSaleState.value
        if (state.cart.isEmpty()) {
            _newSaleState.value = state.copy(errorMessage = "Cart is empty")
            return
        }

        viewModelScope.launch {
            _newSaleState.value = state.copy(isCompleting = true, errorMessage = null)
            val settings = repository.getAppSettingsDirect()
            val allowNegative = settings.allowNegativeStock

            // Check stock
            if (!allowNegative) {
                for (item in state.cart) {
                    if (item.product.quantity < item.quantity) {
                        _newSaleState.value = state.copy(
                            isCompleting = false,
                            errorMessage = "Not enough stock for ${item.product.name}. Available: ${item.product.quantity}"
                        )
                        return@launch
                    }
                }
            }

            // Customer
            var customerId = state.selectedCustomerId
            if (customerId == null && state.customerName.isNotBlank()) {
                customerId = repository.saveCustomer(
                    CustomerEntity(
                        name = state.customerName,
                        phone = state.customerPhone
                    )
                )
            }

            val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
            val receiptNumber = "REC-${dateFormat.format(Date())}-${Random.nextInt(1000, 9999)}"

            val sale = SaleEntity(
                receiptNumber = receiptNumber,
                date = System.currentTimeMillis(),
                customerId = customerId,
                customerName = state.customerName,
                subtotal = state.subtotal,
                discount = state.discount,
                total = state.total,
                paidAmount = state.paidAmount,
                remainingAmount = state.remainingAmount
            )

            val saleItems = state.cart.map { item ->
                SaleItemEntity(
                    saleId = 0,
                    productId = item.product.id,
                    productName = item.product.name,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    discount = item.discount,
                    total = item.total
                )
            }

            val result = repository.recordSale(sale, saleItems, allowNegative)
            result.onSuccess { saleId ->
                val savedSale = sale.copy(id = saleId)
                val pdf = PdfReceiptGenerator.generateReceiptPdf(
                    context = getApplication(),
                    businessName = settings.businessName,
                    currency = settings.currency,
                    sale = savedSale,
                    items = saleItems
                )
                _newSaleState.value = state.copy(
                    isCompleting = false,
                    completedSale = savedSale,
                    completedItems = saleItems,
                    receiptPdfFile = pdf
                )
                onSuccess(savedSale, pdf)
            }.onFailure { err ->
                _newSaleState.value = state.copy(
                    isCompleting = false,
                    errorMessage = err.message ?: "Failed to record sale"
                )
            }
        }
    }

    fun getSaleItems(saleId: Long): Flow<List<SaleItemEntity>> = repository.getSaleItems(saleId)
}

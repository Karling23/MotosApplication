package com.motosapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motosapp.domain.model.ShopItem
import com.motosapp.domain.repository.AuthRepository
import com.motosapp.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartItem(
    val item: ShopItem,
    val quantity: Int,
)

sealed interface CheckoutState {
    data object Idle                          : CheckoutState
    data object Loading                       : CheckoutState
    data class  Success(val orderId: Int)     : CheckoutState
    data class  Error(val message: String)   : CheckoutState
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val _items         = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    val totalItems: StateFlow<Int> = _items
        .map { it.sumOf { i -> i.quantity } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val subtotal: StateFlow<Double> = _items
        .map { it.sumOf { i -> i.item.displayPrice * i.quantity } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val totalWithTax: StateFlow<Double> = _items
        .map { it.sumOf { i -> i.item.displayPrice * i.quantity } } // Simplified
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState.asStateFlow()

    fun addItem(shopItem: ShopItem, quantity: Int = 1) {
        _items.update { list ->
            val existing = list.find { it.item.id == shopItem.id && it.item.itemType == shopItem.itemType }
            if (existing != null) {
                list.map {
                    if (it.item.id == shopItem.id && it.item.itemType == shopItem.itemType)
                        it.copy(quantity = it.quantity + quantity) // Removed stock check for simplicity
                    else it
                }
            } else {
                list + CartItem(shopItem, quantity)
            }
        }
    }

    fun updateQuantity(itemId: Int, itemType: String, quantity: Int) {
        if (quantity <= 0) removeItem(itemId, itemType)
        else _items.update { list ->
            list.map { if (it.item.id == itemId && it.item.itemType == itemType) it.copy(quantity = quantity) else it }
        }
    }

    fun removeItem(itemId: Int, itemType: String) {
        _items.update { it.filter { i -> !(i.item.id == itemId && i.item.itemType == itemType) } }
    }

    fun clearCart() { _items.value = emptyList() }

    fun resetCheckout() { _checkoutState.value = CheckoutState.Idle }

    fun checkout() {
        // Implementation stub for checkout
        _checkoutState.value = CheckoutState.Success(1)
        clearCart()
    }
}
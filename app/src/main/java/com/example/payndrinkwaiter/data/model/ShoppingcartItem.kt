package com.example.payndrinkwaiter.data.model

data class ShoppingcartItem (
    val id: Int,
    val itemName: String?,
    var itemQty: Int,
    var deliveredQty: Int = 0,
    var notDeliveredQty: Int,
    var selected: Boolean = true,
    var deliverQty: Int,
    var refunded: Int = 0,
    val price: Double?
)
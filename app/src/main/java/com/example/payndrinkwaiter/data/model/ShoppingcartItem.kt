package com.example.payndrinkwaiter.data.model

data class ShoppingcartItem (
    val id: Int,
    val itemName: String?,
    var itemQty: Int,
    val itemPrice: Double?,
    val deliveredQty: Int?
)
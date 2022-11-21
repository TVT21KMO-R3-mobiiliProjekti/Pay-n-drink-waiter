package com.example.payndrinkwaiter.data.model

import android.graphics.Bitmap

data class FoodItem(
    val id: Int,
    val quantity: Int,
    val itemID: Int,
    val itemName: String,
    val picture: Bitmap,
    val deliveredQuantity: Int
)

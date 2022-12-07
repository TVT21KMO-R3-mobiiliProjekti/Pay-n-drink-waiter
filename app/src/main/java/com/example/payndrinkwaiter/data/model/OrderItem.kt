package com.example.payndrinkwaiter.data.model

import android.graphics.Color

data class OrderItem(
    val id: Int,
    val price: Double,
    val placed: Long,
    val fulfilled: Long?,
    val seat: Int,
    val waiter: Int?,
    val accepted: Boolean = false,
    val rejected: Boolean = false
)

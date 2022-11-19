package com.example.payndrinkwaiter.data.model

import java.sql.Date

data class OrderItem(
    val id: Int,
    val price: Double,
    val placed: Long,
    val fulfilled: Long?,
    val seat: Int,
    val waiter: Int?
)

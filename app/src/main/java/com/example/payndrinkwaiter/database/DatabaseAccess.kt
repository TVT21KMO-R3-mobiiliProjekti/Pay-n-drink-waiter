package com.example.payndrink.database

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException

//restaurant model class
data class Restaurant(
    var id: Int?, var name: String?, var address: String?, var description: String?,
    var pictureUrl: String?, var typeID: Int?)

//item model class
data class Item(val id: Int?, val name: String?, val quantity: Int?, val description: String?,
                val price: Double?, val quick: Int?, val pictureUrl: String?, val type: Int?,
                val restaurantID: Int?)

//seating model class
data class Seating(val id: Int, val seatNumber: Int, val restaurantID: Int)

//waiter model class
data class Waiter(val id: Int, val firstName: String?, val lastName: String?, val description: String?,
                  val pictureUrl: String?, val restaurantID: Int)

//order model class
data class Order(val id: Int, val price: Double, val placed: Long?, val fulfilled: Long?,
                 val refund: Double?, val refundReason: String?, val seat: Int, val waiterID: Int?)

//order has items model class
data class OrderHasItems(val id: Int, val quantity: Int, val delivered: Int?,
                         val itemID: Int, val itemName: String?, val orderID: Int)

class DatabaseAccess {

    fun connectToDatabase(): Connection? {
        val jdbcUrl =
            "jdbc:postgresql://dpg-cdj2l8mn6mpngrtb5a5g-a.frankfurt-postgres.render.com/" +
                    "tvt21kmo_r3_mobiiliprojekti"
        val userName = "tvt21kmo_r3"
        val password = "H9V1M6gtYtQmWg6nJiqU0sstkCs2LxTl"
        var connection: Connection? = null
        try{
            connection = DriverManager.getConnection(jdbcUrl, userName, password)
        }catch(e: SQLException){
            println(e.toString())
        }
        return connection
    }

    fun getItemsInOrder(connection: Connection, orderID: Int): MutableList<OrderHasItems>{
        val query = "SELECT * FROM order_has_items WHERE id_order=$orderID"
        val result = connection.prepareStatement(query).executeQuery()
        val items = mutableListOf<OrderHasItems>()
        while(result.next()){
            val id = result.getInt("id_order_has_items")
            val quantity = result.getInt("quantity")
            val delivered = result.getInt("delivered")
            val itemID = result.getInt("id_item")
            val itemName = getItemNameByID(connection, itemID)
            items.add(OrderHasItems(id, quantity, delivered, itemID, itemName, orderID))
        }
        return items
    }

    fun getItemNameByID(connection: Connection, itemID: Int): String?{
        val query = "SELECT item_name FROM item WHERE id_item=$itemID"
        val result = connection.prepareStatement(query).executeQuery()
        var name: String? = null
        while(result.next()){
            name = result.getString("item_name")
        }
        return name;
    }

    fun getSeatByID(connection: Connection, seatID:Int): Int{
        val query = "SELECT seat_number FROM seating WHERE id_seating=$seatID"
        val result = connection.prepareStatement(query).executeQuery()
        var seat = 0
        while(result.next()){
            seat = result.getInt("seat_number")
        }
        return seat
    }

    fun getOrdersByWaiter(connection: Connection, waiterID: Int): MutableList<Order>?{
        val query = "SELECT * FROM orders WHERE id_waiter=$waiterID"
        val result = connection.prepareStatement(query).executeQuery()
        val orders = mutableListOf<Order>()
        while(result.next()){
            val id = result.getInt("id_order")
            val price = result.getDouble("order_price")
            val placed = result.getLong("order_placed")
            val fulfilled = result.getLong("order_fulfilled")
            val refund = result.getDouble("refund")
            val refundReason = result.getString("refund_reason")
            val seat = getSeatByID(connection, result.getInt("id_seating"))
            orders.add(Order(id, price, placed, fulfilled, refund, refundReason, seat, waiterID))
        }
        return orders
    }
    fun getNewOrders(connection: Connection): MutableList<Order>?{
        val query = "SELECT * FROM orders WHERE id_waiter IS NULL AND order_placed IS NOT NULL"
        val result = connection.prepareStatement(query).executeQuery()
        val orders = mutableListOf<Order>()
        while(result.next()){
            val id = result.getInt("id_order")
            val price = result.getDouble("order_price")
            val placed = result.getLong("order_placed")
            val refund = result.getDouble("refund")
            val refundReason = result.getString("refund_reason")
            val seat = getSeatByID(connection, result.getInt("id_seating"))
            orders.add(Order(id, price, placed, null, refund, refundReason, seat, null))
        }
        return orders
    }

    fun getWaiterByID(connection: Connection, waiterID: Int): Waiter?{
        val query = "SELECT * FROM waiter WHERE id_waiter=$waiterID"
        val result = connection.prepareStatement(query).executeQuery()
        var waiter: Waiter? = null
        while (result.next()){
            val id = waiterID
            val firstName = result.getString("first_name")
            val lastName = result.getString("last_name")
            val pictureUrl = result.getString("picture_url")
            val description = result.getString("waiter_description")
            val restaurantID = result.getInt("id_restaurant")
            waiter = Waiter(id, firstName, lastName, description, pictureUrl, restaurantID)
        }
        return waiter
    }
}
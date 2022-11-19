package com.example.payndrinkwaiter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.payndrink.database.DatabaseAccess
import com.example.payndrink.database.Order
import com.example.payndrink.database.Waiter
import com.example.payndrinkwaiter.data.Adapter.OrderItemAdapter
import com.example.payndrinkwaiter.data.model.OrderItem
import java.sql.Connection

class MainActivity : AppCompatActivity() {
    private val dbAccess = DatabaseAccess()
    private var connection: Connection? = null
    private lateinit var waiter: Waiter
    lateinit var orderRecyclerView: RecyclerView
    lateinit var orderList: List<OrderItem>
    private lateinit var orders: MutableList<Order>
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: OrderItemAdapter
    private var waiterID = 1//TODO hardcoded for development purposes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectNetwork().permitAll().penaltyLog().build())
        setContentView(R.layout.activity_main)
        connection = dbAccess.connectToDatabase()
        if(connection != null){
            waiter = connection?.let { dbAccess.getWaiterByID(it, waiterID) }!!
            orderList = ArrayList()
            orderRecyclerView = findViewById(R.id.rv_orders)
            layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false)
            orders = dbAccess.getNewOrders(connection!!)!!
            addOrdersToView()
        }
    }

    private fun addOrdersToView(){
        for(order in orders){
            if(order.fulfilled == null && order.placed != null && (order.waiterID == null ||
                        order.waiterID == waiterID)){
                orderList += OrderItem(order.id, order.price, order.placed, null, order.seat,
                    waiterID)

            }
        }
        adapter = OrderItemAdapter(orderList)
        orderRecyclerView.layoutManager = layoutManager
        orderRecyclerView.adapter = adapter
        adapter.setOnItemClickListener(object: OrderItemAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                Toast.makeText(this@MainActivity, String.format("%.2f€", orders[position].price), Toast.LENGTH_SHORT).show()
            }
        })

    }
}
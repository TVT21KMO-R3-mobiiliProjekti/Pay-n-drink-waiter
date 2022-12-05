package com.example.payndrinkwaiter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.payndrinkwaiter.database.DatabaseAccess
import com.example.payndrinkwaiter.database.Order
import com.example.payndrinkwaiter.database.Waiter
import com.example.payndrinkwaiter.data.adapter.OrderItemAdapter
import com.example.payndrinkwaiter.data.model.OrderItem
import java.sql.Connection
import java.util.Collections

class MainActivity : AppCompatActivity() {
    private val dbAccess = DatabaseAccess()
    private var connection: Connection? = null
    private lateinit var waiter: Waiter
    private lateinit var orderRecyclerView: RecyclerView
    lateinit var orderList: List<OrderItem>
    private lateinit var orders: MutableList<Order>
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: OrderItemAdapter
    private var waiterID = 1//TODO hardcoded for development purposes
    private lateinit var btnRefresh: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectNetwork().permitAll().penaltyLog().build())
        setContentView(R.layout.activity_main)
        btnRefresh = findViewById(R.id.btn_refresh)
        btnRefresh.setOnClickListener{
            orders = dbAccess.getNewOrders(connection!!)!!
            addOrdersToView()
        }
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

    override fun onRestart() {
        super.onRestart()
        finish();
        startActivity(intent);
    }

    private fun addOrdersToView(){
        var accepted = false;
        for(order in orders){
            if(order.placed != null && (order.waiterID == null || order.waiterID == waiterID || order.waiterID == 0) && order.refundReason != "Rejected"){
                if(order.waiterID == waiterID){
                    accepted = true;
                }
                val item = OrderItem(order.id, order.price, order.placed, null, order.seat,
                    waiterID, accepted)
                if(!orderList.contains(item)) {
                    orderList = orderList + item
                }
            }
        }
        adapter = OrderItemAdapter(orderList)
        orderRecyclerView.layoutManager = layoutManager
        orderRecyclerView.adapter = adapter
        adapter.setOnItemClickListener(object: OrderItemAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                val intent = Intent(applicationContext, ShoppingCartActivity::class.java)
                intent.putExtra("orderID", orderList[position].id)
                intent.putExtra("seat", orderList[position].seat)
                intent.putExtra("waiter", waiterID)
                intent.putExtra("price", orderList[position].price)
                intent.putExtra("accepted", orderList[position].accepted)
                startActivity(intent)
                //Toast.makeText(this@MainActivity, String.format("%.2f€", orders[position].price), Toast.LENGTH_SHORT).show()
            }
        })
        orderList.sortedBy { it.placed }
        adapter.notifyDataSetChanged()
    }
}
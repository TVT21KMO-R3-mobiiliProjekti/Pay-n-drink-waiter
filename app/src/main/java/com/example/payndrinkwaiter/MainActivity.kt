package com.example.payndrinkwaiter

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.payndrinkwaiter.data.adapter.OrderItemAdapter
import com.example.payndrinkwaiter.data.model.OrderItem
import com.example.payndrinkwaiter.database.DatabaseAccess
import com.example.payndrinkwaiter.database.Order
import com.example.payndrinkwaiter.database.Waiter
import java.sql.Connection

class MainActivity : AppCompatActivity() {
    private val dbAccess = DatabaseAccess()
    private var connection: Connection? = null
    private lateinit var waiter: Waiter
    private lateinit var orderRecyclerView: RecyclerView
    lateinit var orderList: List<OrderItem>
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: OrderItemAdapter
    private var waiterID = 1//TODO hardcoded for development purposes
    private lateinit var btnRefresh: Button
    var handler: Handler = Handler()
    var runnable: Runnable? = null
    var delay = 10000

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
            addOrdersToView()
        }
        else{
            Toast.makeText(this@MainActivity, "Unable to connect to database", Toast.LENGTH_SHORT).show()
            finish()
        }
        btnRefresh = findViewById(R.id.btn_refresh)
        btnRefresh.setOnClickListener{
            addOrdersToView()
        }
    }

    override fun onResume() {
        handler.postDelayed(Runnable {
            handler.postDelayed(runnable!!, delay.toLong())
            addOrdersToView()
            //Toast.makeText(this@MainActivity, "Refreshed", Toast.LENGTH_SHORT).show()
        }.also { runnable = it }, delay.toLong())
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable!!)
    }

    private fun addOrdersToView(){
        val orders = dbAccess.getNewOrders(connection!!)
        orderList = emptyList()
        for(order in orders){
            var accepted = false
            var rejected = false
            if(order.placed != null && (order.waiterID == null || order.waiterID == waiterID || order.waiterID == 0)){
                if(order.accepted != null && order.accepted > 0){
                    accepted = true
                }
                if(order.rejected != null && order.rejected > 0){
                    rejected = true
                }
                val item = OrderItem(order.id, order.price, order.placed, null, order.seat,
                    waiterID, accepted, rejected)
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
                intent.putExtra("rejected", orderList[position].rejected)
                startActivity(intent)
                //Toast.makeText(this@MainActivity, String.format("%.2f€", orders[position].price), Toast.LENGTH_SHORT).show()
            }
        })
        orderList.sortedBy { it.placed }
        adapter.notifyDataSetChanged()
    }
}
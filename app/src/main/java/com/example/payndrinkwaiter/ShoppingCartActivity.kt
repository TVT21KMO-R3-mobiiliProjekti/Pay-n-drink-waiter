package com.example.payndrinkwaiter

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.payndrinkwaiter.data.adapter.ShoppingcartItemAdapter
import com.example.payndrinkwaiter.data.model.ShoppingcartItem
import com.example.payndrinkwaiter.database.DatabaseAccess
import com.example.payndrinkwaiter.database.OrderHasItems
import java.sql.Connection

class ShoppingCartActivity : AppCompatActivity() {
    private val dbAccess = DatabaseAccess()
    private var connection: Connection? = null
    private lateinit var shoppingcartRecyclerView: RecyclerView
    private lateinit var itemList: List<ShoppingcartItem>
    private lateinit var items: MutableList<OrderHasItems>
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: ShoppingcartItemAdapter
    private lateinit var bAccept: Button
    private lateinit var bReject: Button
    private lateinit var bDelivered: Button
    private lateinit var tvOrder: TextView
    private var orderPrice: Double? = null
    private var orderID: Int? = null
    private var seatID: Int? = null
    private var waiterID: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_cart)
        shoppingcartRecyclerView = findViewById(R.id.rv_shoppingcart_items)
        orderID = intent.getIntExtra("orderID", 0)
        seatID = intent.getIntExtra("seat", 0)
        waiterID = intent.getIntExtra("waiter", 0)
        orderPrice = intent.getDoubleExtra("price", 0.00)
        tvOrder = findViewById(R.id.tv_order)
        tvOrder.text = String.format("ORDER TABLE %s", seatID.toString())
        bAccept = findViewById(R.id.btnAccept)
        if(intent.getBooleanExtra("accepted", false)){
            bAccept.isEnabled = false
            bDelivered.isEnabled = true
        }
        bAccept.setOnClickListener{
            if(connection?.let { it1 -> dbAccess.acceptOrder(it1, orderID!!, waiterID!!) }!! == seatID) {
                Toast.makeText(
                    this@ShoppingCartActivity,
                    String.format("Order %s accepted", orderID.toString()),
                    Toast.LENGTH_SHORT
                ).show()
                bAccept.isEnabled = false
                bDelivered.isEnabled = true
            }
            else{
                Toast.makeText(
                    this@ShoppingCartActivity,
                    String.format("Failed to accept order %s", orderID.toString()),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        bReject = findViewById(R.id.btnReject)
        bReject.setOnClickListener{
            if(connection?.let { it1 -> dbAccess.rejectOrder(it1,
                    orderID!!, waiterID!!, orderPrice!!
                ) }!! > 0) {
                Toast.makeText(
                    this@ShoppingCartActivity,
                    String.format("Order %s rejected", orderID.toString()),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            else{
                Toast.makeText(
                    this@ShoppingCartActivity,
                    String.format("Failed to reject order %s", orderID.toString()),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        bDelivered = findViewById(R.id.btnDeliver)
        bDelivered.setOnClickListener{
            if(connection?.let { it1 -> dbAccess.fullfillOrder(it1, orderID!!) } == seatID){
                Toast.makeText(
                    this@ShoppingCartActivity,
                    String.format("Order %s fulfilled", orderID.toString()),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
        updateView()
    }

    private fun updateView() {
        connection = dbAccess.connectToDatabase()
        itemList = ArrayList()
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        itemList = emptyList()
        if(orderID!! > 0) {
            items = orderID?.let { connection?.let { it1 -> dbAccess.getItemsInOrder(it1, it) } }!!
            for (item in items) {
                val price = connection?.let { dbAccess.getItemPrice(it, item.itemID) }
                itemList = itemList + ShoppingcartItem(item.id, item.itemName, item.quantity, price, item.delivered)
            }

            adapter = ShoppingcartItemAdapter(itemList)
            shoppingcartRecyclerView.layoutManager = layoutManager
            shoppingcartRecyclerView.setHasFixedSize(true)
            shoppingcartRecyclerView.adapter = adapter
            adapter.setOnItemClickListener(object : ShoppingcartItemAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                }
            })
        }
    }
}

package com.example.payndrinkwaiter

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.payndrinkwaiter.data.adapter.ShoppingcartItemAdapter
import com.example.payndrinkwaiter.data.model.ShoppingcartItem
import com.example.payndrinkwaiter.database.DatabaseAccess
import com.example.payndrinkwaiter.database.OrderHasItems
import java.sql.Connection

class ShoppingCartActivity : AppCompatActivity(), EstimatedDeliveryTimeSet {
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
    var deliveryEstimate = 0

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
        bDelivered = findViewById(R.id.btnDeliver)
        if(intent.getBooleanExtra("accepted", false)){
            bAccept.isEnabled = false
            bDelivered.isEnabled = true
        }
        bAccept.setOnClickListener{
            val acceptDialogFragment = AcceptDialogFragment(this@ShoppingCartActivity)
            acceptDialogFragment.show(supportFragmentManager, "Expected Delivery in minutes")
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
        bDelivered.setOnClickListener{
            for(item in itemList){
                if(item.selected){
                    connection?.let { it1 -> dbAccess.setItemDelivered(it1, item.id, item.deliverQty) }
                }
            }
            updateView()
            if(itemList.size == 0) {
                if (connection?.let { it1 -> dbAccess.fullfillOrder(it1, orderID!!) } == seatID) {
                    Toast.makeText(
                        this@ShoppingCartActivity,
                        String.format("Order %s fulfilled", orderID.toString()),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
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
                val notDelivered = item.quantity - item.delivered!!
                val deliverQty = notDelivered
                if(notDelivered > 0){
                    itemList = itemList + ShoppingcartItem(item.id, item.itemName, item.quantity, item.delivered, notDelivered, true, deliverQty)
                }
            }
            adapter = ShoppingcartItemAdapter(itemList)
            shoppingcartRecyclerView.layoutManager = layoutManager
            shoppingcartRecyclerView.setHasFixedSize(true)
            shoppingcartRecyclerView.adapter = adapter
            adapter.setOnItemClickListener(object : ShoppingcartItemAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                }
            })
            adapter.notifyDataSetChanged()
        }
    }

    fun acceptOrder(){
        if(connection?.let { it1 -> dbAccess.acceptOrder(it1, orderID!!, waiterID!!, deliveryEstimate) }!! == seatID) {
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

    class AcceptDialogFragment(val estimatedDeliveryTimeSet: EstimatedDeliveryTimeSet) : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return activity?.let {
                val builder = AlertDialog.Builder(it)
                val inflater = requireActivity().layoutInflater
                var acceptView = inflater.inflate(R.layout.accept_dialog, null)
                val editTime = acceptView.findViewById<EditText>(R.id.edt_time)
                val btnMinus = acceptView.findViewById<Button>(R.id.btn_minus)
                btnMinus.setOnClickListener{
                    val estTime: String = editTime.text.toString()
                    var estimation: Int = estTime.toInt()
                    estimation = estimation.minus(1)
                    editTime.setText(estimation.toString())
                }
                val btnPlus = acceptView.findViewById<Button>(R.id.btn_plus)
                btnPlus.setOnClickListener{
                    val estTime: String = editTime.text.toString()
                    var estimation: Int = estTime.toInt()
                    estimation = estimation.plus(1)
                    editTime.setText(estimation.toString())
                }
                builder.setView(acceptView)
                    .setPositiveButton("OK", DialogInterface.OnClickListener{dialog, which ->
                        val estimatedDelivery: String = editTime.text.toString()
                        estimatedDeliveryTimeSet.receiveTime(estimatedDelivery)
                        getDialog()?.cancel()
                    })
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener{dialog, which ->
                        getDialog()?.cancel()
                    })
                builder.create()
            } ?: throw java.lang.IllegalStateException("Activity cannot be null")
        }
    }

    override fun receiveTime(estimatedDelivery: String) {
        deliveryEstimate = estimatedDelivery.toInt()
        acceptOrder()
    }
}

interface EstimatedDeliveryTimeSet{
    fun receiveTime(estimatedDelivery: String)
}

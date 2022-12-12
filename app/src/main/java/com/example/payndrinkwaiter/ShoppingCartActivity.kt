package com.example.payndrinkwaiter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.payndrinkwaiter.data.adapter.ShoppingCartItemAdapter
import com.example.payndrinkwaiter.data.model.ShoppingcartItem
import com.example.payndrinkwaiter.database.DatabaseAccess
import com.example.payndrinkwaiter.database.OrderHasItems
import java.sql.Connection

class ShoppingCartActivity : AppCompatActivity(), EstimatedDeliveryTimeSet, RejectReasonSet, RefundReasonSet {
    private val dbAccess = DatabaseAccess()
    private var connection: Connection? = null
    private lateinit var shoppingcartRecyclerView: RecyclerView
    private lateinit var itemList: List<ShoppingcartItem>
    private lateinit var items: MutableList<OrderHasItems>
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: ShoppingCartItemAdapter
    private lateinit var bAccept: Button
    private lateinit var bReject: Button
    private lateinit var bDeliver: Button
    private lateinit var bRefund: Button
    private lateinit var tvOrder: TextView
    private var orderPrice: Double? = null
    private var orderID: Int? = null
    private var seatID: Int? = null
    private var waiterID: Int? = null
    private var deliveryEstimate = 0
    private var reason = ""
    private var reasonForRefund = ""
    private var totalRefund: Double = 0.00

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
        bReject = findViewById(R.id.btnReject)
        bDeliver = findViewById(R.id.btnDeliver)
        bRefund = findViewById(R.id.btnRefund)
        if(intent.getBooleanExtra("accepted", false)){
            bAccept.isEnabled = false
            bReject.isEnabled = false
            bDeliver.isEnabled = true
            bRefund.isEnabled = true
        }
        if(intent.getBooleanExtra("rejected", false)){
            bAccept.isEnabled = false
            bReject.isEnabled = false
            bDeliver.isEnabled = false
            bRefund.isEnabled = true
        }
        bAccept.setOnClickListener{
            val acceptDialogFragment = AcceptDialogFragment(this@ShoppingCartActivity)
            acceptDialogFragment.show(supportFragmentManager, "Expected Delivery in minutes")
        }
        bReject.setOnClickListener{
            val rejectDialogFragment = RejectDialogFragment(this@ShoppingCartActivity)
            rejectDialogFragment.show(supportFragmentManager, "Reason for rejection")
        }
        bDeliver.setOnClickListener{
            deliverItems()
        }
        bRefund.setOnClickListener{
            var refund = 0.00
            for(item in itemList){
                if(item.selected){
                    refund = refund.plus(item.deliverQty * item.price!!)
                }
            }
            val refundDialogFragment = RefundDialogFragment(refund, this@ShoppingCartActivity)
            refundDialogFragment.show(supportFragmentManager, "Refund reason")
        }
        updateView()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateView() {
        connection = dbAccess.connectToDatabase()
        itemList = ArrayList()
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        itemList = emptyList()
        if(orderID!! > 0) {
            items = orderID?.let { connection?.let { it1 -> dbAccess.getItemsInOrder(it1, it) } }!!
            for (item in items) {
                val notDelivered = item.quantity - item.delivered - item.refunded
                val price: Double? = connection?.let { dbAccess.getItemPrice(it, item.itemID) }
                if (notDelivered > 0) {
                    itemList = itemList + ShoppingcartItem(
                        item.id,
                        item.itemName,
                        item.quantity,
                        item.delivered,
                        notDelivered,
                        true,
                        notDelivered,
                        item.refunded,
                        price
                    )
                }
            }
            adapter = ShoppingCartItemAdapter(itemList)
            shoppingcartRecyclerView.layoutManager = layoutManager
            shoppingcartRecyclerView.setHasFixedSize(true)
            shoppingcartRecyclerView.adapter = adapter
            adapter.setOnItemClickListener(object : ShoppingCartItemAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                }
            })
            adapter.notifyDataSetChanged()
        }
    }

    private fun acceptOrder(){
        if(connection?.let { it1 -> dbAccess.acceptOrder(it1, orderID!!, waiterID!!, deliveryEstimate) }!! > 0) {
            Toast.makeText(
                this@ShoppingCartActivity,
                String.format("Order %s accepted", orderID.toString()),
                Toast.LENGTH_SHORT
            ).show()
            bAccept.isEnabled = false
            bReject.isEnabled = false
            bDeliver.isEnabled = true
            bRefund.isEnabled = true
        }
        else{
            Toast.makeText(
                this@ShoppingCartActivity,
                String.format("Failed to accept order %s", orderID.toString()),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun rejectOrder(){
        if(connection?.let { it1 -> dbAccess.rejectOrder(it1,
                orderID!!, waiterID!!, reason
            ) }!! > 0) {
            Toast.makeText(
                this@ShoppingCartActivity,
                String.format("Order %s rejected: %s", orderID.toString(), reason),
                Toast.LENGTH_SHORT
            ).show()
            bAccept.isEnabled = false
            bReject.isEnabled = false
            bDeliver.isEnabled = false
            bRefund.isEnabled = true
        }
        else{
            Toast.makeText(
                this@ShoppingCartActivity,
                String.format("Failed to reject order %s", orderID.toString()),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun deliverItems(){
        for(item in itemList){
            if(item.selected){
                connection?.let { it1 -> dbAccess.setItemDelivered(it1, item.id, item.deliverQty) }
            }
        }
        updateView()
        if(itemList.isEmpty()) {
            if (connection?.let { it1 -> dbAccess.fulfillOrder(it1, orderID!!) } != 0) {
                Toast.makeText(
                    this@ShoppingCartActivity,
                    String.format("Order %s fulfilled", orderID.toString()),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun refundItems(){
        for(item in itemList){
            if(item.selected){
                connection?.let { it1 -> dbAccess.setItemRefunded(it1, item.id, item.deliverQty) }
            }
        }
        connection?.let { orderID?.let { it1 ->
            dbAccess.refundOrder(it,
                it1, totalRefund, reasonForRefund)
        } }
        updateView()
        if(itemList.isEmpty()) {
            if (connection?.let { it1 -> dbAccess.fulfillOrder(it1, orderID!!) } != 0) {
                Toast.makeText(
                    this@ShoppingCartActivity,
                    String.format("Order %s fulfilled", orderID.toString()),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    class AcceptDialogFragment(private val estimatedDeliveryTimeSet: EstimatedDeliveryTimeSet) : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return activity?.let {
                val builder = AlertDialog.Builder(it)
                val inflater = requireActivity().layoutInflater
                val acceptView = inflater.inflate(R.layout.accept_dialog, null)
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
                    .setPositiveButton("OK") { _, _ ->
                        val estimatedDelivery: String = editTime.text.toString()
                        estimatedDeliveryTimeSet.receiveTime(estimatedDelivery)
                        dialog?.cancel()
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        dialog?.cancel()
                    }
                builder.create()
            } ?: throw java.lang.IllegalStateException("Activity cannot be null")
        }
    }

    override fun receiveTime(estimatedDelivery: String) {
        deliveryEstimate = estimatedDelivery.toInt()
        acceptOrder()
    }

    class RejectDialogFragment(private val rejectReasonSet: RejectReasonSet) : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return activity?.let {
                val builder = AlertDialog.Builder(it)
                val inflater = requireActivity().layoutInflater
                val rejectView = inflater.inflate(R.layout.reject_dialog, null)
                val reasons = listOf<RadioButton>(
                    rejectView.findViewById(R.id.rb_drunk),
                    rejectView.findViewById(R.id.rb_noid),
                    rejectView.findViewById(R.id.rb_other)
                )
                val edtReason = rejectView.findViewById<EditText>(R.id.edt_reject_reason)
                var rrBase = ""
                builder.setView(rejectView)
                    .setPositiveButton("OK") { _, _ ->
                        for (newReason in reasons){
                            if(newReason.isChecked){
                                rrBase = newReason.text.toString()
                                break
                            }
                        }
                        val rejectReason: String =
                            String.format("%s: %s", rrBase, edtReason.text.toString())
                        rejectReasonSet.receiveReason(rejectReason)
                        dialog?.cancel()
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        dialog?.cancel()
                    }
                builder.create()
            } ?: throw java.lang.IllegalStateException("Activity cannot be null")
        }
    }

    override fun receiveReason(rejectReason: String) {
        reason = rejectReason
        rejectOrder()
    }

    class RefundDialogFragment(private val refundAmount: Double, private val refundReasonSet: RefundReasonSet) : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return activity?.let {
                val builder = AlertDialog.Builder(it)
                val inflater = requireActivity().layoutInflater
                val refundView = inflater.inflate(R.layout.refund_dialog, null)
                val edtReason = refundView.findViewById<EditText>(R.id.edt_refund_reason)
                val edtAmount = refundView.findViewById<EditText>(R.id.edt_refund_amount)
                edtAmount.setText(String.format("%.2f€", refundAmount))
                builder.setView(refundView)
                    .setPositiveButton("OK") { _, _ ->
                        val refundReason: String = edtReason.text.toString()
                        val rfAmount: String = edtAmount.text.toString().dropLast(1)
                        refundReasonSet.receiveRefund(rfAmount, refundReason)
                        dialog?.cancel()
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        dialog?.cancel()
                    }
                builder.create()
            } ?: throw java.lang.IllegalStateException("Activity cannot be null")
        }
    }

    override fun receiveRefund(refundAmount: String, refundReason: String) {
        reasonForRefund = refundReason
        totalRefund = refundAmount.toDouble()
        refundItems()
    }
}

interface EstimatedDeliveryTimeSet{
    fun receiveTime(estimatedDelivery: String)
}

interface RejectReasonSet{
    fun receiveReason(rejectReason: String)
}

interface RefundReasonSet{
    fun receiveRefund(refundAmount: String, refundReason: String)
}

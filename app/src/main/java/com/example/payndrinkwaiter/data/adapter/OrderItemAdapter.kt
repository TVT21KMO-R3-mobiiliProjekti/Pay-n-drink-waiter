package com.example.payndrinkwaiter.data.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.payndrinkwaiter.R
import com.example.payndrinkwaiter.data.model.OrderItem
import java.sql.Time

class OrderItemAdapter (
    private val orderItemList: List<OrderItem>
): RecyclerView.Adapter<OrderItemAdapter.ViewHolder>(){
    private lateinit var mListener: OnItemClickListener
    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        mListener = listener
    }

    private val items: MutableList<CardView>
    init{
        this.items = ArrayList()
    }

    override fun getItemCount(): Int {
        return orderItemList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_item, parent, false)
        return ViewHolder(v, mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val orderTime = Time(orderItemList[position].placed)
        holder.tvOrderTable.text = orderItemList[position].seat.toString()
        holder.tvOrderTime.text = orderTime.toString()
        holder.tvOrderPrice.text = String.format("Price: %.2f€", orderItemList[position].price)
        if(orderItemList[position].accepted == true){
            holder.tvOrderAccepted.text = "Accepted"
        }
        else{
            holder.tvOrderAccepted.text = "New"
        }
        items.add(holder.card)
    }
    inner class ViewHolder
    internal constructor(
        itemView: View,
        listener: OnItemClickListener
    ):RecyclerView.ViewHolder(itemView){
        val tvOrderTime: TextView = itemView.findViewById(R.id.tv_order_time)
        val tvOrderTable: TextView = itemView.findViewById(R.id.tv_order_table)
        val tvOrderPrice: TextView = itemView.findViewById(R.id.tv_order_price)
        val tvOrderAccepted: TextView = itemView.findViewById(R.id.tv_order_accepted)
        val card: CardView = itemView.findViewById(R.id.cv_orders)
        init{
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }
}
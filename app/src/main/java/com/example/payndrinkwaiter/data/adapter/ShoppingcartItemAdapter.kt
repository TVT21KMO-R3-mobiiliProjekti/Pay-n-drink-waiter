package com.example.payndrinkwaiter.data.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.payndrinkwaiter.R
import com.example.payndrinkwaiter.data.model.ShoppingcartItem

class ShoppingcartItemAdapter (
    private val shoppingcartItemList: List<ShoppingcartItem>
): RecyclerView.Adapter<ShoppingcartItemAdapter.ViewHolder>() {
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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.shoppingcart_grid_item, parent, false)
        return ViewHolder(v, mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvName.text = shoppingcartItemList[position].itemName
        var price: Double? = shoppingcartItemList[position].itemQty.let {
            shoppingcartItemList[position].itemPrice?.times(it)
        }
        holder.tvPrice.text = String.format("%.2f %s", price, "€")
        holder.tvQty.setText(shoppingcartItemList[position].itemQty.toString())
        holder.bPlus.setOnClickListener{
            shoppingcartItemList[position].itemQty = shoppingcartItemList[position].itemQty.plus(1)
            holder.tvQty.setText(shoppingcartItemList[position].itemQty.toString())
            price = price?.plus(shoppingcartItemList[position].itemPrice!!)
            holder.tvPrice.text = String.format("%.2f %s", price, "€")
        }
        holder.bMinus.setOnClickListener{
            if(shoppingcartItemList[position].itemQty > 0) {
                shoppingcartItemList[position].itemQty =
                    shoppingcartItemList[position].itemQty.minus(1)
                holder.tvQty.setText(shoppingcartItemList[position].itemQty.toString())
                price = price?.minus(shoppingcartItemList[position].itemPrice!!)
                holder.tvPrice.text = String.format("%.2f %s", price, "€")
            }
        }
        items.add(holder.card)
    }
    override fun getItemCount(): Int {
        return shoppingcartItemList.size
    }

    inner class ViewHolder
    internal constructor(
        itemView: View,
        listener: OnItemClickListener
    ):RecyclerView.ViewHolder(itemView){
        val tvName: TextView = itemView.findViewById(R.id.name_text_view)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_price)
        val tvQty: EditText = itemView.findViewById(R.id.edtQTY)
        val bPlus: Button = itemView.findViewById(R.id.btnPlus)
        val bMinus: Button = itemView.findViewById(R.id.btnMinus)
        val cbDeliver: CheckBox = itemView.findViewById(R.id.cb_deliver)
        val card: CardView = itemView.findViewById(R.id.cv_menu_item)

        init{
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

}
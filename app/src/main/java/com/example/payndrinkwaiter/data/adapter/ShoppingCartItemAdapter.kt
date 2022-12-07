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

class ShoppingCartItemAdapter (
    private val shoppingCartItemList: List<ShoppingcartItem>
): RecyclerView.Adapter<ShoppingCartItemAdapter.ViewHolder>() {
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
        holder.tvName.text = shoppingCartItemList[position].itemName
        holder.tvQty.setText(shoppingCartItemList[position].deliverQty.toString())
        holder.tvPrice.text = String.format("%.2f€",
            shoppingCartItemList[position].price?.times(shoppingCartItemList[position].deliverQty) ?: Int
        )
        holder.bPlus.setOnClickListener{
            shoppingCartItemList[position].deliverQty = shoppingCartItemList[position].deliverQty.plus(1)
            if(shoppingCartItemList[position].deliverQty > shoppingCartItemList[position].notDeliveredQty){
                shoppingCartItemList[position].deliverQty = shoppingCartItemList[position].notDeliveredQty
            }
            holder.tvQty.setText(shoppingCartItemList[position].deliverQty.toString())
            holder.tvPrice.text = String.format("%.2f€",
                shoppingCartItemList[position].price?.times(shoppingCartItemList[position].deliverQty))
        }
        holder.bMinus.setOnClickListener{
            if(shoppingCartItemList[position].deliverQty > 0) {
                shoppingCartItemList[position].deliverQty =
                    shoppingCartItemList[position].deliverQty.minus(1)
                holder.tvQty.setText(shoppingCartItemList[position].deliverQty.toString())
                holder.tvPrice.text = String.format("%.2f€",
                    shoppingCartItemList[position].price?.times(shoppingCartItemList[position].deliverQty))
            }
        }
        holder.cbDeliver.setOnCheckedChangeListener { _, isChecked ->
            shoppingCartItemList[position].selected = isChecked
        }
        items.add(holder.card)
    }

    override fun getItemCount(): Int {
        return shoppingCartItemList.size
    }

    inner class ViewHolder
    internal constructor(
        itemView: View,
        listener: OnItemClickListener
    ):RecyclerView.ViewHolder(itemView){
        val tvName: TextView = itemView.findViewById(R.id.name_text_view)
        val tvQty: EditText = itemView.findViewById(R.id.edtQTY)
        val bPlus: Button = itemView.findViewById(R.id.btnPlus)
        val bMinus: Button = itemView.findViewById(R.id.btnMinus)
        val cbDeliver: CheckBox = itemView.findViewById(R.id.cb_deliver)
        val tvPrice: TextView = itemView.findViewById(R.id.price_text_view)
        val card: CardView = itemView.findViewById(R.id.cv_menu_item)

        init{
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

}
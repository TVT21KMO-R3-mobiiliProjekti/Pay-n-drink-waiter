package com.example.payndrinkwaiter.data.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.payndrinkwaiter.R
import com.example.payndrinkwaiter.data.model.FoodItem
import java.sql.Date

class FoodItemAdapter (
    private val foodItemList: List<FoodItem>
): RecyclerView.Adapter<FoodItemAdapter.ViewHolder>(){
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
        return foodItemList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_item, parent, false)
        return ViewHolder(v, mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvQuantity.text = foodItemList[position].quantity.toString()
        holder.tvFoodName.text = foodItemList[position].itemName
        holder.ivFood.setImageBitmap(foodItemList[position].picture)
        items.add(holder.card)
    }
    inner class ViewHolder
    internal constructor(
        itemView: View,
        listener: OnItemClickListener
    ): RecyclerView.ViewHolder(itemView){
        val tvQuantity: TextView = itemView.findViewById(R.id.tv_quantity)
        val tvFoodName: TextView = itemView.findViewById(R.id.tv_food_name)
        val ivFood: ImageView = itemView.findViewById(R.id.iv_food)
        val bPlus: Button = itemView.findViewById(R.id.b_plus)
        val bMinus: Button = itemView.findViewById(R.id.b_minus)
        val etQuantity: EditText = itemView.findViewById(R.id.et_quantity)
        val card: CardView = itemView.findViewById(R.id.cv_orders)

        init{
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }
}
package com.ajkerdeal.app.essential.ui.quick_order_scan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.api.models.quick_order.delivery_charge.DeliveryChargeResponse
import com.ajkerdeal.app.essential.databinding.ItemViewWeightButtonBinding
import timber.log.Timber

class WeightSelectionAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataList: MutableList<DeliveryChargeResponse> = mutableListOf()
    var onItemClick: ((position: Int, model: DeliveryChargeResponse) -> Unit)? = null
    private var selectedItem: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemViewWeightButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val model = dataList[position]
            val binding = holder.binding

            binding.timeSlot.text = model.weight
            val color = if (selectedItem == position) {
                ContextCompat.getColor(binding.timeSlot.context, R.color.white)
            } else {
                ContextCompat.getColor(binding.timeSlot.context, R.color.black_90)
            }
            val drawable = if (selectedItem == position) {
                ContextCompat.getDrawable(binding.timeSlot.context, R.drawable.bg_weight_selected)
            } else {
                ContextCompat.getDrawable(binding.timeSlot.context, R.drawable.bg_weight_unselected)
            }
            binding.timeSlot.background = drawable
            binding.timeSlot.setTextColor(color)
        }
    }

    inner class ViewHolder(val binding: ItemViewWeightButtonBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(position, dataList[position])
                    selectedItem = position
                    notifyDataSetChanged()
                }
            }
        }
    }

    fun initLoad(list: List<DeliveryChargeResponse>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
        Timber.d("deliveryChargeDebug $list")
    }

    fun clearList() {
        dataList.clear()
        notifyDataSetChanged()
    }

}
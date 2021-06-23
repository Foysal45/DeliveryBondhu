package com.ajkerdeal.app.essential.ui.quick_order_scan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request.WeightRangeWiseData
import com.ajkerdeal.app.essential.databinding.ItemViewDeliveryTypeBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import timber.log.Timber
import java.util.*

class DeliveryTypeAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataList: MutableList<WeightRangeWiseData> = mutableListOf()
    var onItemClick: ((position: Int, model: WeightRangeWiseData) -> Unit)? = null
    private var selectedItem: Int = -1

    private val options = RequestOptions().signature(ObjectKey(Calendar.getInstance().get(Calendar.DAY_OF_YEAR).toString()))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemViewDeliveryTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val model = dataList[position]
            val binding = holder.binding
            Timber.d("deliveryChargeDebug $model")

            if (position == selectedItem) {
                Glide.with(binding.deliveryTypeImage)
                    .load(model.onImageLink)
                    .apply(options)
                    .dontAnimate()
                    .into(binding.deliveryTypeImage)
            } else {
                Glide.with(binding.deliveryTypeImage)
                    .load(model.offImageLink)
                    .apply(options)
                    .dontAnimate()
                    .into(binding.deliveryTypeImage)
            }
        }
    }

    inner class ViewHolder(val binding: ItemViewDeliveryTypeBinding) : RecyclerView.ViewHolder(binding.root) {
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

    fun initLoad(list: List<WeightRangeWiseData>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
        Timber.d("deliveryChargeDebug $list")
    }

    fun selectPreSelection() {
        if (selectedItem != -1) {
            if (selectedItem in 0..dataList.lastIndex) {
                onItemClick?.invoke(selectedItem, dataList[selectedItem])
            }
        }
    }

    fun selectByDeliveryRangeId(rangeId: Int) {
        val index = dataList.indexOfFirst { it.deliveryRangeId == rangeId }
        if (index != -1) {
            selectedItem = index
            onItemClick?.invoke(selectedItem, dataList[selectedItem])
        }
    }

    fun clearList() {
        selectedItem = -1
        dataList.clear()
        notifyDataSetChanged()
    }

    fun clearSelection(){
        selectedItem = -1
    }

}
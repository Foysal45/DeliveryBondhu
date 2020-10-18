package com.ajkerdeal.app.essential.ui.print_dialog

import android.annotation.SuppressLint
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.api.models.order.OrderModel
import com.ajkerdeal.app.essential.databinding.ItemViewOrderPrintBinding
import com.ajkerdeal.app.essential.utils.DigitConverter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class OrderListPrintAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataList: MutableList<OrderModel> = mutableListOf()
    private val options = RequestOptions().placeholder(R.drawable.ic_logo_essentials)
    private val selectedItems: SparseBooleanArray = SparseBooleanArray()
    var onSelected: ((selectedCount: Int, totalItemCount: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ItemViewOrderPrintBinding = ItemViewOrderPrintBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val model = dataList[position]
            val binding = holder.binding

            Glide.with(binding.productImage)
                .load(model.imageUrl)
                .apply(options)
                .into(binding.productImage)

            binding.productName.text = "${model.productTitle}"
            binding.productCode.text = "কোড: ${model.couponId}"
            if (model.sizes.isNullOrEmpty()) {
                binding.productSize.visibility = View.GONE
            } else {
                binding.productSize.visibility = View.VISIBLE
                binding.productSize.text = "সাইজ: ${model.sizes}"
            }
            if (model.colors.isNullOrEmpty()) {
                binding.productColor.visibility = View.GONE
            } else {
                binding.productColor.visibility = View.VISIBLE
                binding.productColor.text = "কালার: ${model.colors}"
            }
            binding.productDeliveryType.text = "টাইপ: ${model.deliveryType}"

            val total = model.productPrice * model.productQtn + model.deliveryCharge
            val banglaPrice = DigitConverter.toBanglaDigit(model.productPrice)
            val banglaQuantity = DigitConverter.toBanglaDigit(model.productQtn)
            val deliveryCharge = if (model.deliveryCharge > 0) "+ ৳ ${DigitConverter.toBanglaDigit(model.deliveryCharge)}" else ""
            val banglaTotal = DigitConverter.toBanglaDigit(total)
            val price = "৳ $banglaPrice x $banglaQuantity $deliveryCharge = ৳ $banglaTotal"
            binding.productPrice.text = price

            binding.productQuantity.text = "পরিমান: $banglaQuantity"


            if (selectedItems.get(position, false)) {
                binding.card.isChecked = true
            } else {
                binding.card.isChecked = false
            }
        }
    }


    inner class ViewHolder(val binding: ItemViewOrderPrintBinding): RecyclerView.ViewHolder(binding.root) {

        init {
            binding.card.setOnLongClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    toggleSelection(adapterPosition)
                }
                true
            }
        }
    }

    fun toggleSelection(position: Int) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position)
        } else {
            selectedItems.put(position, true)
        }
        onSelected?.invoke(getSelectedItemCount(), dataList.size)
        notifyItemChanged(position)
    }

    fun selectAll() {
        for (i in 0..dataList.lastIndex) {
            selectedItems.put(i, true)
        }
        notifyDataSetChanged()
    }

    fun clearSelections() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItemCount(): Int {
        return selectedItems.size()
    }

    fun getSelectedItems(): List<OrderModel> {

        val list: MutableList<OrderModel> = mutableListOf()
        for (i in 0..dataList.lastIndex) {
            if (selectedItems.get(i, false)) {
                list.add(dataList[i])
            }
        }
        return list
    }


    fun initData(list: List<OrderModel>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

}
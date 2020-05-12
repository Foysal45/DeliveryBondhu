package com.ajkerdeal.app.essential.ui.dialog

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.api.models.collection.CollectionData
import com.ajkerdeal.app.essential.databinding.ItemViewCollectionProductBinding
import com.ajkerdeal.app.essential.utils.DigitConverter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class CollectionAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataList: MutableList<CollectionData> = mutableListOf()
    private val options = RequestOptions().placeholder(R.drawable.ic_logo_essentials)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemViewCollectionProductBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {

            val model = dataList[position]

            Glide.with(holder.binding.productImage)
                .load(model.imageUrl)
                .apply(options)
                .into(holder.binding.productImage)

            holder.binding.productName.text = "${model.productTitle}"

            val total = model.productPrice * model.productQtn
            val banglaPrice = DigitConverter.toBanglaDigit(model.productPrice)
            val banglaQuantity = DigitConverter.toBanglaDigit(model.productQtn)
            val banglaTotal = DigitConverter.toBanglaDigit(total)
            holder.binding.productPrice.text = "৳ $banglaPrice x $banglaQuantity = ৳ $banglaTotal"

            val isPay =  model.isPay
            val res = if (isPay == 0) {
                holder.binding.paymentType.visibility = View.VISIBLE
                R.drawable.ic_dont_pay
            } else if (isPay == 1) {
                holder.binding.paymentType.visibility = View.VISIBLE
                R.drawable.ic_pay
            } else {
                holder.binding.paymentType.visibility = View.GONE
                0
            }
            Glide.with(holder.binding.paymentType)
                .load(res)
                .apply(options)
                .into(holder.binding.paymentType)
        }
    }

    private inner class ViewHolder(val binding: ItemViewCollectionProductBinding): RecyclerView.ViewHolder(binding.root) {

        init {


        }

    }

    fun loadData(list: MutableList<CollectionData>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }
}
package com.ajkerdeal.app.essential.ui.home

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ajkerdeal.app.essential.api.models.order.Action
import com.ajkerdeal.app.essential.api.models.order.OrderCustomer
import com.ajkerdeal.app.essential.api.models.order.OrderModel
import com.ajkerdeal.app.essential.databinding.ItemViewOrderParentBinding

class OrderListParentAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataList: MutableList<OrderCustomer> = mutableListOf()
    var onCall: ((number: String?) -> Unit)? = null
    var onActionClicked: ((model: OrderCustomer, orderModel: OrderModel, actionModel: Action) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemViewOrderParentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {

            val model = dataList[position]

            holder.binding.customerName.text = model.customerName
            holder.binding.customerAddress.text = model.customerAddress

            if (model.state) {
                val currentRotation = holder.binding.indicator.rotation
                rotateView(holder.binding.indicator, currentRotation, 180f)
                holder.binding.recyclerView.visibility = View.VISIBLE
            } else {
                val currentRotation = holder.binding.indicator.rotation
                if (currentRotation != 0f) {
                    rotateView(holder.binding.indicator, currentRotation, 0f)
                }
                holder.binding.recyclerView.visibility = View.GONE
            }


            val dataAdapter = OrderListChildAdapter()
            dataAdapter.loadData(model.orderList as MutableList<OrderModel>)
            with(holder.binding.recyclerView) {
                setHasFixedSize(false)
                layoutManager = LinearLayoutManager(this.context)
                adapter = dataAdapter
                animation = null
            }
            dataAdapter.onActionClicked = { orderModel, actionModel ->
                onActionClicked?.invoke(model, orderModel, actionModel)
            }
            dataAdapter.onCall = { number ->
                onCall?.invoke(number)
            }

        }
    }

    private inner class ViewHolder(val binding: ItemViewOrderParentBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.parent.setOnClickListener {
                val state = dataList[adapterPosition].state
                dataList[adapterPosition].state = !state
                notifyItemChanged(adapterPosition)
            }

            binding.phone.setOnClickListener {
                onCall?.invoke(dataList[adapterPosition].customerMobileNumber)
            }


        }

    }

    fun loadInitData(list: MutableList<OrderCustomer>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

    fun loadMoreData(list: MutableList<OrderCustomer>) {
        val lastIndex = dataList.lastIndex
        dataList.addAll(list)
        notifyItemRangeInserted(lastIndex, list.size)
    }

    private fun rotateView(view: View, start: Float = 0f, end: Float = 0f, duration: Long = 200L) {
        val rotate = ObjectAnimator.ofFloat(view, "rotation", start, end)
        rotate.duration = duration
        rotate.start()
    }


}
package com.ajkerdeal.app.essential.ui.home

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.api.models.order.Action
import com.ajkerdeal.app.essential.api.models.order.OrderCustomer
import com.ajkerdeal.app.essential.api.models.order.OrderModel
import com.ajkerdeal.app.essential.databinding.ItemViewOrderParentBinding

class OrderListParentAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataList: MutableList<OrderCustomer> = mutableListOf()
    var onCall: ((number: String?) -> Unit)? = null
    var onActionClicked: ((model: OrderCustomer, actionModel: Action,  orderModel: OrderModel?) -> Unit)? = null
    var onPictureClicked: ((model: OrderModel) -> Unit)? = null
    var isChildView: Boolean = false

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

            if (model.actions.isNullOrEmpty()) {
                holder.binding.recyclerViewAction.visibility = View.GONE
            } else {
                holder.binding.recyclerViewAction.visibility = View.VISIBLE
                val dataAdapter = ActionAdapter()
                dataAdapter.loadData(model.actions!! as MutableList<Action>)
                with(holder.binding.recyclerViewAction) {
                    setHasFixedSize(false)
                    layoutManager = LinearLayoutManager(holder.binding.recyclerViewAction.context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = dataAdapter
                }
                dataAdapter.onActionClicked = { actionModel ->
                    onActionClicked?.invoke(model, actionModel, null)
                }
            }

            if (model.collectionSource != null) {
                if (model.collectionSource!!.sourceMessageData != null) {
                    val source = model.collectionSource!!.sourceMessageData
                    if (!source?.message.isNullOrEmpty()) {
                        holder.binding.collectionMsg.text = HtmlCompat.fromHtml(source?.message ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY)
                        holder.binding.collectionMsg.visibility = View.VISIBLE
                    } else {
                        if ( holder.binding.collectionMsg.visibility == View.VISIBLE){
                            holder.binding.collectionMsg.visibility = View.GONE
                        }
                    }
                } else {
                    if ( holder.binding.collectionMsg.visibility == View.VISIBLE){
                        holder.binding.collectionMsg.visibility = View.GONE
                    }
                }
            } else {
                if ( holder.binding.collectionMsg.visibility == View.VISIBLE){
                    holder.binding.collectionMsg.visibility = View.GONE
                }
            }

            val dataAdapter = OrderListChildAdapter()
            dataAdapter.loadData(model.orderList as MutableList<OrderModel>)
            with(holder.binding.recyclerView) {
                setHasFixedSize(false)
                isNestedScrollingEnabled = false
                layoutManager = LinearLayoutManager(this.context)
                adapter = dataAdapter
                animation = null
            }
            dataAdapter.onActionClicked = { orderModel, actionModel ->
                onActionClicked?.invoke(model,actionModel, orderModel)
            }
            dataAdapter.onCall = { number ->
                onCall?.invoke(number)
            }
            dataAdapter.onPictureClicked = {model ->
                onPictureClicked?.invoke(model)
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

            if (isChildView) {
                binding.parent.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.parent.context, R.color.childColor))
                binding.separator.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.separator.context, R.color.separator_gray))
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

    fun clearData() {
        dataList.clear()
        notifyDataSetChanged()
    }
}
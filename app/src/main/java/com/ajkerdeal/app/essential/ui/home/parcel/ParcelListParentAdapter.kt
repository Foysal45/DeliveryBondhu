package com.ajkerdeal.app.essential.ui.home.parcel

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ajkerdeal.app.essential.api.models.order.Action
import com.ajkerdeal.app.essential.api.models.order.OrderCustomer
import com.ajkerdeal.app.essential.api.models.order.OrderModel
import com.ajkerdeal.app.essential.api.models.pod.PodWiseData
import com.ajkerdeal.app.essential.databinding.ItemViewParcelParentBinding
import com.ajkerdeal.app.essential.ui.home.ActionAdapter
import com.ajkerdeal.app.essential.ui.home.OrderListParentAdapter
import com.ajkerdeal.app.essential.utils.DigitConverter

class ParcelListParentAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataList: MutableList<PodWiseData> = mutableListOf()
    var onCall: ((number: String?) -> Unit)? = null
    var onActionClicked: ((model: PodWiseData, orderCustomer: OrderCustomer, actionModel: Action, orderModel: OrderModel?) -> Unit)? = null
    var onActionClickedParent: ((model: PodWiseData, actionModel: Action) -> Unit)? = null
    var onActionClickedCustomer: ((model: PodWiseData, orderCustomer: OrderCustomer, actionModel: Action) -> Unit)? = null
    var onPictureClicked: ((model: OrderModel) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemViewParcelParentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {

            val model = dataList[position]

            holder.binding.customerName.text = "পার্সেল নং: ${model.podNumber}"
            holder.binding.customerAddress.text = "আয়: ৳ ${DigitConverter.toBanglaDigit(model.totalPodCommission)}  কাস্টমার: ${DigitConverter.toBanglaDigit(model.totalCustomer)}"

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
                    onActionClickedParent?.invoke(model, actionModel)
                }
            }

            if (model.customerMessageData != null) {

                val source = model.customerMessageData!!
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

            if (model.customerDataModel.isNullOrEmpty()) {
                holder.binding.recyclerView.visibility = View.GONE
            } else {
                val dataAdapter = OrderListParentAdapter()
                dataAdapter.isChildView = true
                dataAdapter.loadInitData(model.customerDataModel!! as MutableList<OrderCustomer>)
                with(holder.binding.recyclerView) {
                    setHasFixedSize(false)
                    layoutManager = LinearLayoutManager(this.context)
                    adapter = dataAdapter
                    animation = null
                }
                dataAdapter.onActionClicked = { orderCustomer: OrderCustomer, actionModel, orderModel ->
                    onActionClicked?.invoke(model, orderCustomer, actionModel, orderModel)
                }
                dataAdapter.onCall = { number ->
                    onCall?.invoke(number)
                }
                dataAdapter.onPictureClicked = { model ->
                    onPictureClicked?.invoke(model)
                }
                if (holder.binding.recyclerView.visibility == View.GONE) {
                    holder.binding.recyclerView.visibility == View.VISIBLE
                }
            }

        }
    }

    private inner class ViewHolder(val binding: ItemViewParcelParentBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.parent.setOnClickListener {
                val state = dataList[adapterPosition].state
                dataList[adapterPosition].state = !state
                notifyItemChanged(adapterPosition)
            }

        }

    }

    fun loadInitData(list: MutableList<PodWiseData>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

    fun loadMoreData(list: MutableList<PodWiseData>) {
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
package com.ajkerdeal.app.essential.ui.quick_order_lists

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request.ActionModel
import com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request.OrderRequest
import com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request.QuickOrderList
import com.ajkerdeal.app.essential.databinding.ItemViewQuickOrderParentBinding

import timber.log.Timber

class QuickOrderListParentAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataList: MutableList<QuickOrderList> = mutableListOf()
    var onActionClicked: ((model: QuickOrderList, actionModel: ActionModel, orderModel: OrderRequest?) -> Unit)? = null
    var onPictureClicked: ((model: QuickOrderList) -> Unit)? = null
    var onQRCodeClicked: ((model: QuickOrderList) -> Unit)? = null
    var onWeightUpdateClicked: ((model: OrderRequest, model2: QuickOrderList) -> Unit)? = null
    var onLocationReport: ((model: QuickOrderList) -> Unit)? = null
    var onLocationUpdate: ((model: QuickOrderList) -> Unit)? = null
    var onPrintClicked: ((model: QuickOrderList) -> Unit)? = null
    var onOrderListExpand: ((model: QuickOrderList, state: Boolean) -> Unit)? = null
    var onCall: ((number: String?, altNumber: String?) -> Unit)? = null

    var isCollectionTimerShow: Boolean = false
    var isWeightUpdateEnable: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemViewQuickOrderParentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {

            val model = dataList[position]
            val binding = holder.binding

            val nameWithDistrict = "${model.companyName} (<font color='#E86324'>${model.companyName}</font>)"
            binding.customerName.text = HtmlCompat.fromHtml(nameWithDistrict, HtmlCompat.FROM_HTML_MODE_LEGACY)
            if (model.address.isNullOrEmpty()){
                binding.customerAddress.text = "Address"
            }else{
                binding.customerAddress.text = model.address
            }


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

            val dataAdapter = QuickOrderListChildAdapter()
            dataAdapter.isCollectionTimerShow = isCollectionTimerShow
            dataAdapter.isWeightUpdateEnable = isWeightUpdateEnable
            dataAdapter.loadData(model.orderRequestList)
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

            /*dataAdapter.onPictureClicked = { model1 ->
                onPictureClicked?.invoke(model1)
            }

            dataAdapter.onQRCodeClicked = { model1 ->
                onQRCodeClicked?.invoke(model1)
            }*/
            dataAdapter.onWeightUpdateClicked = { model1 ->
                onWeightUpdateClicked?.invoke(model1, model)
            }

            if (model.actionModel.isNullOrEmpty()) {
                binding.recyclerViewAction.visibility = View.GONE
            } else {
                binding.recyclerViewAction.visibility = View.VISIBLE
                val dataAdapter = QuickOrderActionAdapter()
                 dataAdapter.loadData(model.actionModel)
                with(binding.recyclerViewAction) {
                    setHasFixedSize(false)
                    layoutManager = LinearLayoutManager(holder.binding.recyclerViewAction.context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = dataAdapter
                }
                dataAdapter.onActionClicked = { actionModel ->
                    onActionClicked?.invoke(model, actionModel, null)
                }
            }

        }
    }

    private inner class ViewHolder(val binding: ItemViewQuickOrderParentBinding) : RecyclerView.ViewHolder(binding.root) {

        init {

            binding.parent.setOnClickListener {
                if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                    val state = dataList[absoluteAdapterPosition].state
                    dataList[absoluteAdapterPosition].state = !state
                    notifyItemChanged(absoluteAdapterPosition)
                    onOrderListExpand?.invoke(dataList[absoluteAdapterPosition], dataList[absoluteAdapterPosition].state)
                }
            }

            binding.showLocation.setOnClickListener {
                onLocationReport?.invoke(dataList[absoluteAdapterPosition])
            }

            binding.addLocation.setOnClickListener {
                onLocationUpdate?.invoke(dataList[absoluteAdapterPosition])
            }

            binding.printBtn.setOnClickListener {
                onPrintClicked?.invoke(dataList[absoluteAdapterPosition])
            }

            binding.phone.setOnClickListener {
                if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                    onCall?.invoke(dataList[absoluteAdapterPosition].mobile, dataList[absoluteAdapterPosition].alterMobile)
                }
            }


            /*if (isCollectionPoint == 1) {
                binding.phone.setImageDrawable(ContextCompat.getDrawable(binding.phone.context, R.drawable.ic_call_1))
            }else {
                binding.phone.setImageDrawable(ContextCompat.getDrawable(binding.phone.context, R.drawable.ic_call))
            }*/
        }

    }

    fun loadInitData(list: List<QuickOrderList>) {
        Timber.d("debugData $list")
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

    fun loadMoreData(list: List<QuickOrderList>) {
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
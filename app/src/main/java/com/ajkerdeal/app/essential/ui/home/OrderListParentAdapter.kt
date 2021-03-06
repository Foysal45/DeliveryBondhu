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
import com.ajkerdeal.app.essential.utils.isValidCoordinate

class OrderListParentAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataList: MutableList<OrderCustomer> = mutableListOf()
    var onActionClicked: ((model: OrderCustomer, actionModel: Action,  orderModel: OrderModel?) -> Unit)? = null
    var onPictureClicked: ((model: OrderModel) -> Unit)? = null
    var onQRCodeClicked: ((model: OrderModel) -> Unit)? = null
    var onWeightUpdateClicked: ((model: OrderModel, model2: OrderCustomer) -> Unit)? = null

    var onLocationReport: ((model: OrderCustomer) -> Unit)? = null
    var onLocationUpdate: ((model: OrderCustomer) -> Unit)? = null
    var onPrintClicked: ((model: OrderCustomer) -> Unit)? = null
    var onUploadClicked: ((model: OrderCustomer, selectedModel: List<OrderModel>) -> Unit)? = null
    var onSingleImageUploadClicked: ((model: OrderModel) -> Unit)? = null
    var onCall: ((number: String?, altNumber: String?) -> Unit)? = null
    var onMerchantCall: ((number: String?) -> Unit)? = null
    var onChat: ((id: Int, name: String, number: String) -> Unit)? = null
    var onOrderListExpand: ((model: OrderCustomer, state: Boolean) -> Unit)? = null
    var onImageExistsToast: ((toast: String) -> Unit)? = null
    var onClearSelectionCalled: ((dataAdapter : OrderListChildAdapter) -> Unit)? = null
    var onClearSelectionCalledPosition: ((dataAdapter : Int) -> Unit)? = null

    var isChildView: Boolean = false
    var isCollectionPoint: Int = 0
    var isCollectionPointGroup: Int = -1
    var allowLocationAdd: Boolean = false
    var allowPrint: Boolean = false
    var isChatVisible: Boolean = false
    var allowImageUpload: Boolean = false
    var isCollectionTimerShow: Boolean = false
    var isWeightUpdateEnable: Boolean = false
    var isOrderFromAD: Boolean = false
    var isDelivery: Boolean = false
    var isSelectedEnable: Boolean = false
    var isExpandClose: Boolean = false

    private var selectedOrderLists: List<OrderModel> = listOf()

    //private val dataAdapter = OrderListChildAdapter()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemViewOrderParentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val dataAdapter = OrderListChildAdapter()
            val model = dataList[position]
            onClearSelectionCalledPosition?.invoke(position)
            val nameWithDistrict = "${model.name} (<font color='#E86324'>${model.district}</font>)"
            holder.binding.customerName.text = HtmlCompat.fromHtml(nameWithDistrict, HtmlCompat.FROM_HTML_MODE_LEGACY)
            holder.binding.customerAddress.text = model.address

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
                val innerdataAdapter = ActionAdapter()
                innerdataAdapter.loadData(model.actions!! as MutableList<Action>)
                with(holder.binding.recyclerViewAction) {
                    setHasFixedSize(false)
                    layoutManager = LinearLayoutManager(holder.binding.recyclerViewAction.context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = innerdataAdapter
                }
                innerdataAdapter.onActionClicked = { actionModel ->
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

            dataAdapter.isCollectionTimerShow = isCollectionTimerShow
            dataAdapter.isWeightUpdateEnable = isWeightUpdateEnable
            dataAdapter.isOrderFromAD = isOrderFromAD
            dataAdapter.isDelivery = isDelivery
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
            dataAdapter.onCall = { number, altNumber ->
                onCall?.invoke(number, altNumber)
            }

            dataAdapter.onMerchantCall = { number ->
                onMerchantCall?.invoke(number)
            }
            dataAdapter.onPictureClicked = { model1 ->
                onPictureClicked?.invoke(model1)
            }

            dataAdapter.onQRCodeClicked = { model1 ->
                onQRCodeClicked?.invoke(model1)
            }
            dataAdapter.onWeightUpdateClicked = { model1 ->
                onWeightUpdateClicked?.invoke(model1, model)
            }
            dataAdapter.onSingleUploadClicked = { model ->
                onSingleImageUploadClicked?.invoke( model)
            }
            dataAdapter.onItemSelected = { model, position ->
                if (isSelectedEnable){
                    if (model.documentUrl.isNullOrEmpty()){
                        dataAdapter.multipleSelection(model, position)
                        selectedOrderLists = dataAdapter.getSelectedItemModelList()
                    }else{
                     onImageExistsToast?.invoke("?????? ??????????????????????????? ???????????????????????? ??????????????? ????????? ???????????????")
                    }
                }
            }

            if (model.mobileNumber.isNullOrEmpty()) {
                holder.binding.phone.visibility = View.GONE
            } else {
                holder.binding.phone.visibility = View.VISIBLE
                if (isCollectionPoint == 1) {
                    holder.binding.phone.setImageDrawable(ContextCompat.getDrawable(holder.binding.phone.context, R.drawable.ic_call_1))
                }else {
                    holder.binding.phone.setImageDrawable(ContextCompat.getDrawable(holder.binding.phone.context, R.drawable.ic_call))
                }
            }

            if (isCollectionPointGroup == 1) {
                val lat = isValidCoordinate(model.latitude)
                val lng = isValidCoordinate(model.longitude)
                if (lat && lng) {
                    holder.binding.showLocation.visibility = View.VISIBLE
                } else {
                    holder.binding.showLocation.visibility = View.GONE
                }
                if (allowLocationAdd) {
                    holder.binding.addLocation.visibility = View.VISIBLE
                } else {
                    holder.binding.addLocation.visibility = View.GONE
                }
            } else {
                holder.binding.showLocation.visibility = View.GONE
                holder.binding.addLocation.visibility = View.GONE
            }

            if (allowPrint) {
                holder.binding.printBtn.visibility = View.VISIBLE
            } else {
                holder.binding.printBtn.visibility = View.GONE
            }

            if (isChatVisible) {
                holder.binding.chatBtn.visibility = View.VISIBLE
            } else {
                holder.binding.chatBtn.visibility = View.GONE
            }

            if (allowImageUpload) {
                holder.binding.uploadBtn.visibility = View.VISIBLE
            } else {
                holder.binding.uploadBtn.visibility = View.GONE
            }
        }
    }

    private inner class ViewHolder(val binding: ItemViewOrderParentBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.parent.setOnClickListener {
                val state = dataList[absoluteAdapterPosition].state
                dataList[absoluteAdapterPosition].state = !state
                if (isExpandClose){
                    for (item in 0 until dataList.size) {
                        if (item != absoluteAdapterPosition) {
                            dataList[item].state = false
                            notifyItemChanged(item)
                        }
                    }
                }
                onOrderListExpand?.invoke(dataList[absoluteAdapterPosition], dataList[absoluteAdapterPosition].state)
                notifyItemChanged(absoluteAdapterPosition)
            }

            binding.phone.setOnClickListener {
                onCall?.invoke(dataList[absoluteAdapterPosition].mobileNumber, dataList[absoluteAdapterPosition].alterMobile)
            }

            binding.chatBtn.setOnClickListener {
                onChat?.invoke(dataList[absoluteAdapterPosition].merchantId, dataList[absoluteAdapterPosition].name ?: "", dataList[absoluteAdapterPosition].mobileNumber ?: "")
            }

            if (isChildView) {
                binding.parent.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.parent.context, R.color.childColor))
                binding.separator.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.separator.context, R.color.separator_gray))
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

            binding.uploadBtn.setOnClickListener {
                var data = dataList[absoluteAdapterPosition].orderList?.filter { it.documentUrl!!.isNotEmpty() }
                onUploadClicked?.invoke(dataList[absoluteAdapterPosition], selectedOrderLists)
            }

            /*if (isCollectionPoint == 1) {
                binding.phone.setImageDrawable(ContextCompat.getDrawable(binding.phone.context, R.drawable.ic_call_1))
            }else {
                binding.phone.setImageDrawable(ContextCompat.getDrawable(binding.phone.context, R.drawable.ic_call))
            }*/
        }

    }

    /*fun clearSelection(){
       dataAdapter.clearSelections()
    }*/

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
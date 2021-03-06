package com.ajkerdeal.app.essential.ui.home

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.CountDownTimer
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.util.contains
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.api.models.order.Action
import com.ajkerdeal.app.essential.api.models.order.OrderCustomer
import com.ajkerdeal.app.essential.api.models.order.OrderModel
import com.ajkerdeal.app.essential.databinding.ItemViewOrderChildBinding
import com.ajkerdeal.app.essential.utils.DigitConverter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class OrderListChildAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataList: MutableList<OrderModel> = mutableListOf()
    private val options = RequestOptions().placeholder(R.drawable.ic_logo_essentials)
    var onActionClicked: ((model: OrderModel, actionModel: Action) -> Unit)? = null
    var onCall: ((number: String?, alternativeNumber: String?) -> Unit)? = null
    var onMerchantCall: ((number: String?) -> Unit)? = null
    var onPictureClicked: ((model: OrderModel) -> Unit)? = null
    var onQRCodeClicked: ((model: OrderModel) -> Unit)? = null
    var onWeightUpdateClicked: ((model: OrderModel) -> Unit)? = null
    var onItemSelected: ((model: OrderModel, position: Int) -> Unit)? = null
    var onSingleUploadClicked: ((selectedModel: OrderModel) -> Unit)? = null


    private var sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US)
    private var sdf1 = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    var isCollectionTimerShow: Boolean = false
    var isWeightUpdateEnable: Boolean = false
    var isOrderFromAD: Boolean = false
    var isDelivery: Boolean = false

    var allowImageUpload: Boolean = false
    private val selectedItems: SparseBooleanArray = SparseBooleanArray()
    private var currentSelectedIndex = -1
    private var reverseAllAnimations = false
    var enableSelection: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemViewOrderChildBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
            holder.binding.productCode.text = "?????????: ${model.couponId}"
            if (model.sizes.isNullOrEmpty()) {
                holder.binding.productSize.visibility = View.GONE
            } else {
                holder.binding.productSize.visibility = View.VISIBLE
                holder.binding.productSize.text = "????????????: ${model.sizes}"
            }
            if (model.colors.isNullOrEmpty()) {
                holder.binding.productColor.visibility = View.GONE
            } else {
                holder.binding.productColor.visibility = View.VISIBLE
                holder.binding.productColor.text = "???????????????: ${model.colors}"
            }
            if(isWeightUpdateEnable){
                holder.binding
            }
            holder.binding.productDeliveryType.text = "????????????: ${model.deliveryType}"

            if (isDelivery){
                if (!model.collectionSource?.sourcePersonName.isNullOrEmpty()){
                    holder.binding.merchantName.visibility = View.VISIBLE
                    holder.binding.merchantName.text = "????????????????????????????????? ????????? : ${model.collectionSource?.sourcePersonName}"
                }else{
                    holder.binding.merchantName.visibility = View.GONE
                }

                if (!model.collectionSource?.sourceMobile.isNullOrEmpty()){
                    holder.binding.merchantMobile.visibility = View.VISIBLE
                    holder.binding.merchantName.text = "?????????????????? ????????????????????? : ${model.collectionSource?.sourceMobile}"
                    holder.binding.merchantMobile.setOnClickListener{
                        onMerchantCall?.invoke(model.collectionSource?.sourceMobile)
                    }
                }else{
                    holder.binding.merchantMobile.visibility = View.GONE
                }
            }else{
                holder.binding.merchantName.visibility = View.GONE
                holder.binding.merchantMobile.visibility = View.GONE
            }

            val total = model.productPrice * model.productQtn + model.deliveryCharge
            val banglaPrice = DigitConverter.toBanglaDigit(model.productPrice)
            val banglaQuantity = DigitConverter.toBanglaDigit(model.productQtn)
            val deliveryCharge = if (model.deliveryCharge > 0) "+ ??? ${DigitConverter.toBanglaDigit(model.deliveryCharge)}" else ""
            val banglaTotal = DigitConverter.toBanglaDigit(total)
            val price = "??? $banglaPrice x $banglaQuantity $deliveryCharge = ??? $banglaTotal"
            holder.binding.productPrice.text = price

            holder.binding.productQuantity.text = "??????????????????: $banglaQuantity"

            if (position == dataList.lastIndex) {
                holder.binding.separator.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(holder.binding.separator.context, R.color.white))
            } else {
                holder.binding.separator.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(holder.binding.separator.context, R.color.separator_gray))
            }

            if (model.actions.isNullOrEmpty()) {
                holder.binding.recyclerView.visibility = View.GONE
            } else {
                holder.binding.recyclerView.visibility = View.VISIBLE
                val dataAdapter = ActionAdapter()
                dataAdapter.loadData(model.actions!! as MutableList<Action>)
                with(holder.binding.recyclerView) {
                    setHasFixedSize(false)
                    layoutManager = LinearLayoutManager(holder.binding.recyclerView.context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = dataAdapter
                }
                dataAdapter.onActionClicked = { actionModel ->
                    onActionClicked?.invoke(model, actionModel)
                }
            }

            if (model.priorityService > 0){
                holder.binding.priorityShowLayout.visibility = View.VISIBLE
                val msg = if (model.priorityService == 1) "Urgent" else model.priorityService.toString()
                holder.binding.priorityAmountText.text = msg
            }else{
                holder.binding.priorityShowLayout.visibility = View.GONE
            }

            if (model.collectionSource != null) {
                if (model.collectionSource!!.sourceMessageData != null) {
                    val source = model.collectionSource!!.sourceMessageData
                    if (source?.message.isNullOrEmpty()) {
                        holder.binding.collectionPointLayout.visibility = View.GONE
                    } else {
                        holder.binding.collectionAddress.text = HtmlCompat.fromHtml(source?.message ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY)
                        holder.binding.collectionPointLayout.visibility = View.VISIBLE
                    }

                    if (source?.status.isNullOrEmpty()){
                        holder.binding.orderStatus.visibility = View.GONE
                    } else {
                        holder.binding.orderStatus.text = HtmlCompat.fromHtml(source?.status ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY)
                        holder.binding.orderStatus.visibility = View.VISIBLE
                    }

                    val isPay =  model.collectionSource!!.sourceMessageData?.isPay
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

                } else {
                    holder.binding.collectionPointLayout.visibility = View.GONE
                }
            } else {
                holder.binding.collectionPointLayout.visibility = View.GONE
            }

            if (model?.collectionSource?.sourceMobile.isNullOrEmpty()) {
                holder.binding.phoneShop.visibility = View.GONE
            } else {
                holder.binding.phoneShop.visibility = View.VISIBLE
            }

            if(isWeightUpdateEnable && model.couponId.startsWith("DT-")){
                holder.binding.weightUpdateButton.visibility = View.VISIBLE
            }else{
                holder.binding.weightUpdateButton.visibility = View.GONE
            }

            Timber.d("isAdvancedPaymentDebug ${model.isAdvancePayment} $isOrderFromAD")
            if(isOrderFromAD && model.isAdvancePayment){
                holder.binding.isAdvancedPaymentIcon.visibility = View.VISIBLE
            }else{
                holder.binding.isAdvancedPaymentIcon.visibility = View.GONE
            }
            if(!isOrderFromAD && model.isHeavyWeight){
                holder.binding.isHeavyWeight.visibility = View.VISIBLE
            }else{
                holder.binding.isHeavyWeight.visibility = View.GONE
            }

            if(!model.documentUrl.isNullOrEmpty()){
                holder.binding.isDocumentUrl.visibility = View.VISIBLE
            }else{
                holder.binding.isDocumentUrl.visibility = View.GONE
            }

            if (selectedItems[position, false]) {
                holder.binding.parent.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor( holder.binding.parent.context, R.color.selection_color))
            } else {
                holder.binding.parent.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor( holder.binding.parent.context, R.color.white))
            }

            // Collection time
            if (isCollectionTimerShow) {
                collectionTimer(holder, model)
            }

        }
    }


    private fun collectionTimer(holder: ViewHolder, model: OrderModel) {
        if (holder.countDownTimer != null) {
            holder.countDownTimer?.cancel()
        }
        val endTime = model.collectionTimeSlot?.endTime ?: "00:00:00"
        if (endTime != "00:00:00") {
            try {
                val endDate = sdf.parse("${sdf1.format(Date().time)} $endTime")
                if (endDate != null) {
                    val timeDifference = endDate.time - Date().time
                    Timber.d("timeDifference $timeDifference ${endDate.time}")
                    Timber.d("timeDifference $endTime")
                    if (timeDifference > 0) {
                        holder.binding.timerLayout.visibility = View.VISIBLE
                        holder.countDownTimer = object: CountDownTimer(timeDifference,1000L) {
                            override fun onTick(millisUntilFinished: Long) {
                                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished).toInt() % 24
                                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished).toInt() % 60
                                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toInt() % 60

                                val message = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                                holder.binding.timeText.text = DigitConverter.toBanglaDigit(message)
                                if (hours < 1) {
                                    holder.binding.timeText.setTextColor(ContextCompat.getColor(holder.binding.timeText.context, R.color.crimson))
                                } else {
                                    holder.binding.timeText.setTextColor(ContextCompat.getColor(holder.binding.timeText.context, R.color.colorPrimary))
                                }
                            }

                            override fun onFinish() {
                                holder.binding.timeText.text = "????????????????????? ???????????? ?????????"
                            }
                        }.start()
                    } else {
                        holder.binding.timerLayout.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            holder.binding.timerLayout.visibility = View.GONE
        }
    }

    private inner class ViewHolder(val binding: ItemViewOrderChildBinding): RecyclerView.ViewHolder(binding.root) {

        var countDownTimer: CountDownTimer? = null

        init {

            binding.phoneShop.setOnClickListener {
                val mobile = dataList[absoluteAdapterPosition]?.collectionSource?.sourceMobile
                val alternativeMobile = dataList[absoluteAdapterPosition]?.collectionSource?.sourceMobile
                onCall?.invoke(mobile, alternativeMobile)
            }

            binding.productImage.setOnClickListener {
                onPictureClicked?.invoke(dataList[absoluteAdapterPosition])
            }
            binding.qrcodeBtn.setOnClickListener {
                onQRCodeClicked?.invoke(dataList[absoluteAdapterPosition])
            }
            binding.root.setOnLongClickListener {
                onItemSelected?.invoke(dataList[absoluteAdapterPosition], absoluteAdapterPosition)
                enableSelection = true
                return@setOnLongClickListener true
            }
            binding.root.setOnClickListener {
                if (enableSelection){
                    onItemSelected?.invoke(dataList[absoluteAdapterPosition], absoluteAdapterPosition)
                }
            }
            binding.weightUpdateButton.setOnClickListener {
                onWeightUpdateClicked?.invoke(dataList[absoluteAdapterPosition])
            }

            binding.isDocumentUrl.setOnClickListener{
                onSingleUploadClicked?.invoke(dataList[absoluteAdapterPosition])
            }
        }

    }

    fun multipleSelection(model : OrderModel, pos: Int) {
        if (model.documentUrl.isNullOrEmpty()){
            this.currentSelectedIndex = pos
            if (selectedItems.contains(pos)){
                selectedItems.delete(pos)
            }else{
                selectedItems.put(pos, true)
            }
            notifyItemChanged(pos)
        }else{
        }

    }

    fun clearSelections() {
        enableSelection = false
        reverseAllAnimations = true
        selectedItems.clear()
        Timber.d("Cleared data")
        notifyDataSetChanged()
    }

    fun getSelectedItemCount(): Int {
        return selectedItems.size()
    }

    fun getSelectedItemModelList(): List<OrderModel> {
        val items: MutableList<OrderModel> = ArrayList<OrderModel>(selectedItems.size())
        for (i in 0 until selectedItems.size()) {
            items.add(dataList[selectedItems.keyAt(i)])
        }
        return items
    }

    fun loadData(list: MutableList<OrderModel>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

}
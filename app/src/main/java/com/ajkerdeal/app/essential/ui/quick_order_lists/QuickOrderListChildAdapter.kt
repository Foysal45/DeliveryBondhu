package com.ajkerdeal.app.essential.ui.quick_order_lists

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request.ActionModel
import com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request.OrderRequest
import com.ajkerdeal.app.essential.databinding.ItemViewQuickOrderChildBinding
import com.ajkerdeal.app.essential.utils.DigitConverter
import com.bumptech.glide.request.RequestOptions
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class QuickOrderListChildAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataList: MutableList<OrderRequest> = mutableListOf()
    private val options = RequestOptions().placeholder(R.drawable.ic_logo_essentials)
    var onActionClicked: ((model: OrderRequest, actionModel: ActionModel) -> Unit)? = null
    var onCall: ((number: String?, alternativeNumber: String?) -> Unit)? = null
    var onPictureClicked: ((model: OrderRequest) -> Unit)? = null
    var onQRCodeClicked: ((model: OrderRequest) -> Unit)? = null
    var onWeightUpdateClicked: ((model: OrderRequest) -> Unit)? = null

    private var sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US)
    private var sdf1 = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    var isCollectionTimerShow: Boolean = true
    var isWeightUpdateEnable: Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemViewQuickOrderChildBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {

            val model = dataList[position]
            val binding = holder.binding


            binding.parcelCountText.text = "${model.requestOrderAmount} ??????"
            //binding.timeText.text = model.collectionTimeSlot.endTime
            binding.orderStatus.text = model.courierUsersView
            binding.collectionAddress.text = model.requestDate
           /* holder.binding.productCode.text = "?????????: ${model.}"
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
            }*/

            if (model.actionModel.isNullOrEmpty()) {
                holder.binding.recyclerView.visibility = View.GONE
            } else {
                holder.binding.recyclerView.visibility = View.VISIBLE
                val dataAdapter = QuickOrderActionAdapter()
                dataAdapter.loadData(model.actionModel)
                with(holder.binding.recyclerView) {
                    setHasFixedSize(false)
                    layoutManager = LinearLayoutManager(holder.binding.recyclerView.context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = dataAdapter
                }
                dataAdapter.onActionClicked = { actionModel ->
                    onActionClicked?.invoke(model, actionModel)
                }
            }

            /*if (model.priorityService > 0){
                holder.binding.priorityShowLayout.visibility = View.VISIBLE
                holder.binding.priorityAmountText.text = model.priorityService.toString()
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

                    val isPay =  1
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

*//*            if (modelsourceMobile.isNullOrEmpty()) {
                holder.binding.phoneShop.visibility = View.GONE
            } else {
                holder.binding.phoneShop.visibility = View.VISIBLE
            }

            if(isWeightUpdateEnable && model.couponId.startsWith("DT-")){
                holder.binding.weightUpdateButton.visibility = View.VISIBLE
            }else{
                holder.binding.weightUpdateButton.visibility = View.GONE
            }*/

            // Collection time
            collectionTimer(holder, model)
            if (isCollectionTimerShow) {
                Timber.d("timeDebug ${model.collectionTimeSlot.endTime}")
                collectionTimer(holder, model)
            }

        }
    }


    private fun collectionTimer(holder: ViewHolder, model: OrderRequest) {
        if (holder.countDownTimer != null) {
            holder.countDownTimer?.cancel()
        }
        val endTime = model.collectionTimeSlot.endTime ?: "00:00:00"
        Timber.d("timeDebug $endTime")
        if (endTime != "00:00:00") {
            try {
                val endDate = sdf.parse("${sdf1.format(Date().time)} $endTime")
                Timber.d("timeDebug $endTime")
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

    private inner class ViewHolder(val binding: ItemViewQuickOrderChildBinding): RecyclerView.ViewHolder(binding.root) {

        var countDownTimer: CountDownTimer? = null

        init {

            binding.phoneShop.setOnClickListener {
                val mobile = dataList[absoluteAdapterPosition].deliveryUserId.toString()
                val alternativeMobile = dataList[absoluteAdapterPosition].deliveryUserId.toString()
                onCall?.invoke(mobile, alternativeMobile)
            }

            binding.qrcodeBtn.setOnClickListener {
                onQRCodeClicked?.invoke(dataList[absoluteAdapterPosition])
            }
            binding.weightUpdateButton.setOnClickListener {
                onWeightUpdateClicked?.invoke(dataList[absoluteAdapterPosition])
            }
        }

    }

    fun loadData(list: List<OrderRequest>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

}
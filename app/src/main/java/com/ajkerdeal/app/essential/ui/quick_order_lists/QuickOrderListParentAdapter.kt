package com.ajkerdeal.app.essential.ui.quick_order_lists

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request.ActionModel
import com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request.OrderRequest
import com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request.QuickOrderList
import com.ajkerdeal.app.essential.databinding.ItemViewQuickOrderParentBinding
import com.ajkerdeal.app.essential.utils.DigitConverter

import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

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

    private var sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US)
    private var sdf1 = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    var currentStatus: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemViewQuickOrderParentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {

            val model = dataList[position]
            val binding = holder.binding

            val nameWithDistrict = "${model.companyName} (<font color='#E86324'>${model.districtsViewModel.thana}</font>)"
            binding.customerName.text = HtmlCompat.fromHtml(nameWithDistrict, HtmlCompat.FROM_HTML_MODE_LEGACY)
            if (model.address.isNullOrEmpty()){
                binding.customerAddress.text = "Address"
            }else{
                binding.customerAddress.text = model.address
            }

            binding.parcelCountText.text = "${DigitConverter.toBanglaDigit(model.orderRequestList.first().requestOrderAmount)} টি"
            val requestDate = model.orderRequestList.last().requestDate?.split("T")?.first()
            binding.requestDate.text = DigitConverter.toBanglaDate(requestDate, "yyyy-MM-dd")

            if (currentStatus == 44) {
                holder.binding.timerLayout.visibility = View.GONE
                if (holder.countDownTimer != null) {
                    holder.countDownTimer?.cancel()
                }
            } else {
                holder.binding.timerLayout.visibility = View.VISIBLE
                collectionTimer(holder, model.orderRequestList.first())
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

            /*if (model.state) {
                val currentRotation = holder.binding.indicator.rotation
                rotateView(holder.binding.indicator, currentRotation, 180f)
                holder.binding.recyclerView.visibility = View.VISIBLE
            } else {
                val currentRotation = holder.binding.indicator.rotation
                if (currentRotation != 0f) {
                    rotateView(holder.binding.indicator, currentRotation, 0f)
                }
                holder.binding.recyclerView.visibility = View.GONE
            }*/

            /*val dataAdapter = QuickOrderListChildAdapter()
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

            *//*dataAdapter.onPictureClicked = { model1 ->
                onPictureClicked?.invoke(model1)
            }

            dataAdapter.onQRCodeClicked = { model1 ->
                onQRCodeClicked?.invoke(model1)
            }*//*
            dataAdapter.onWeightUpdateClicked = { model1 ->
                onWeightUpdateClicked?.invoke(model1, model)
            }*/
        }
    }

    private inner class ViewHolder(val binding: ItemViewQuickOrderParentBinding) : RecyclerView.ViewHolder(binding.root) {
        var countDownTimer: CountDownTimer? = null
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
                                /*if (hours < 1) {
                                    holder.binding.timeText.setTextColor(ContextCompat.getColor(holder.binding.timeText.context, R.color.crimson))
                                } else {
                                    holder.binding.timeText.setTextColor(ContextCompat.getColor(holder.binding.timeText.context, R.color.colorPrimary))
                                }*/
                            }

                            override fun onFinish() {
                                holder.binding.timeText.text = "টাইম আউট"
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
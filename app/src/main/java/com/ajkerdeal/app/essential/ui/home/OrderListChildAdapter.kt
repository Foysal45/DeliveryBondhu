package com.ajkerdeal.app.essential.ui.home

import android.annotation.SuppressLint
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
import com.ajkerdeal.app.essential.api.models.order.OrderModel
import com.ajkerdeal.app.essential.databinding.ItemViewOrderChildBinding
import com.ajkerdeal.app.essential.utils.DigitConverter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class OrderListChildAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataList: MutableList<OrderModel> = mutableListOf()
    private val options = RequestOptions().placeholder(R.drawable.ic_logo_essentials)
    var onActionClicked: ((model: OrderModel, actionModel: Action) -> Unit)? = null
    var onCall: ((number: String?) -> Unit)? = null
    var onPictureClicked: ((model: OrderModel) -> Unit)? = null

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

            holder.binding.productName.text = "${model.productTitle} (${model.couponId})"

            val total = model.productPrice * model.productQtn
            val banglaPrice = DigitConverter.toBanglaDigit(model.productPrice)
            val banglaQuantity = DigitConverter.toBanglaDigit(model.productQtn)
            val banglaTotal = DigitConverter.toBanglaDigit(total)
            holder.binding.productPrice.text = "৳ $banglaPrice x $banglaQuantity = ৳ $banglaTotal"

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

                } else {
                    holder.binding.collectionPointLayout.visibility = View.GONE
                }
            } else {
                holder.binding.collectionPointLayout.visibility = View.GONE
            }


            /*holder.binding.actionContainer.removeAllViews()
            model.actions?.forEach {action ->

                if (action.actionType == 1) {

                    val theme = ContextThemeWrapper(holder.binding.actionContainer.context, R.style.ActionButtonPositive)
                    val positiveBtn = MaterialButton(theme).apply {
                        layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                        text = action.actionMessage
                        //icon = ContextCompat.getDrawable(holder.binding.actionContainer.context, R.drawable.ic_done)
                        //backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(holder.binding.actionContainer.context, R.color.button_action_accept))

                    }
                    positiveBtn.setOnClickListener {
                        onActionClicked?.invoke(model)
                    }
                    holder.binding.actionContainer.addView(positiveBtn)
                }

                if (action.actionType == 2) {

                    //val theme = ContextThemeWrapper(holder.binding.actionContainer.context, R.style.ActionButtonNegative)
                    val negativeBtn = MaterialButton(holder.binding.actionContainer.context).apply {
                        layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                        text = action.actionMessage
                        icon = ContextCompat.getDrawable(holder.binding.actionContainer.context, R.drawable.ic_close)
                        val colorText = ColorStateList.valueOf(ContextCompat.getColor(holder.binding.actionContainer.context, R.color.button_action_text_gry))
                        iconTint = colorText
                        setTextColor(colorText)
                        backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(holder.binding.actionContainer.context, R.color.button_action_reject))
                        textSize = 12.0f
                    }
                    negativeBtn.setOnClickListener {
                        onActionClicked?.invoke(model)
                    }
                    holder.binding.actionContainer.addView(negativeBtn)


                }

                if (action.actionType == 3) {
                    val positiveMsg = TextView(holder.binding.actionContainer.context, null, R.style.ActionMessagePositive).apply {
                        text = action.actionMessage
                        textSize = 12.0f
                        val colorText = ColorStateList.valueOf(ContextCompat.getColor(holder.binding.actionContainer.context, R.color.button_action_accept))
                        setTextColor(colorText)
                    }
                    holder.binding.actionContainer.addView(positiveMsg)
                }

                if (action.actionType == 4) {
                    val negativeMsg = TextView(holder.binding.actionContainer.context, null, R.style.ActionMessagePositive).apply {
                        text = action.actionMessage
                        textSize = 12.0f
                        val colorText = ColorStateList.valueOf(ContextCompat.getColor(holder.binding.actionContainer.context, R.color.button_action_text_gry))
                        setTextColor(colorText)
                    }
                    holder.binding.actionContainer.addView(negativeMsg)
                }

            }*/
        }
    }

    private inner class ViewHolder(val binding: ItemViewOrderChildBinding): RecyclerView.ViewHolder(binding.root) {

        init {

            binding.phoneShop.setOnClickListener {
                val mobile = dataList[adapterPosition]?.collectionSource?.sourceMobile
                onCall?.invoke(mobile)
            }

            binding.productImage.setOnClickListener {
                onPictureClicked?.invoke(dataList[adapterPosition])
            }
        }

    }

    fun loadData(list: MutableList<OrderModel>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }
}
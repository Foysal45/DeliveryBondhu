package com.ajkerdeal.app.essential.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private val options = RequestOptions().placeholder(R.drawable.ad_logo)
    var onActionClicked: ((model: OrderModel, actionModel: Action) -> Unit)? = null

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

            holder.binding.productName.text = model.productTitle
            holder.binding.productPrice.text = "৳ ${DigitConverter.toBanglaDigit(model.productPrice)}"
            holder.binding.productQuantity.text = "x ${DigitConverter.toBanglaDigit(model.productQtn)}"

            if (position == dataList.lastIndex) {
                holder.binding.separator.visibility = View.GONE
            } else {
                holder.binding.separator.visibility = View.VISIBLE
            }

            if (model.actions.isNullOrEmpty()) {
                holder.binding.recyclerView.visibility = View.GONE
            } else {
                holder.binding.recyclerView.visibility = View.VISIBLE
                val dataAdapter = ActionAdapter()
                dataAdapter.loadData(model.actions!! as MutableList<Action>)
                with(holder.binding.recyclerView) {
                    setHasFixedSize(false)
                    layoutManager = LinearLayoutManager(holder.binding.recyclerView.context)
                    adapter = dataAdapter
                }
                dataAdapter.onActionClicked = { actionModel ->
                    onActionClicked?.invoke(model, actionModel)
                }
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


        }

    }

    fun loadData(list: MutableList<OrderModel>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }
}
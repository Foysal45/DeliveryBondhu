package com.ajkerdeal.app.essential.ui.home

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.api.models.order.Action
import com.ajkerdeal.app.essential.databinding.ItemViewActionBtnPositiveBinding
import com.ajkerdeal.app.essential.databinding.ItemViewActionMessageBinding

class ActionAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataList: MutableList<Action> = mutableListOf()
    var onActionClicked: ((model: Action) -> Unit)? = null

    override fun getItemViewType(position: Int): Int {
        return dataList[position].actionType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> ViewHolder(ItemViewActionBtnPositiveBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            2 -> ViewHolder(ItemViewActionBtnPositiveBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            3 -> ViewHolder2(ItemViewActionMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            4 -> ViewHolder2(ItemViewActionMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> ViewHolder(ItemViewActionBtnPositiveBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = dataList[position]
        when (holder) {
            is ViewHolder -> {
                holder.binding.actionBtn.text = model.actionMessage
                holder.binding.actionBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor(model.colorCode))

                //val colorText = ColorStateList.valueOf(ContextCompat.getColor(holder.binding.actionBtn.context, R.color.white))
                if (model.actionType == 1) {

                    with(holder.binding.actionBtn) {
                        //setTextColor(colorText)
                        icon = ContextCompat.getDrawable(holder.binding.actionBtn.context, R.drawable.ic_done)
                        //iconTint = colorText
                    }

                } else if (model.actionType == 2) {

                    //val colorText = ColorStateList.valueOf(ContextCompat.getColor(holder.binding.actionBtn.context, R.color.button_action_text_gry))
                    with(holder.binding.actionBtn) {
                        //setTextColor(colorText)
                        icon = ContextCompat.getDrawable(holder.binding.actionBtn.context, R.drawable.ic_close)
                        //iconTint = colorText
                    }

                }
            }
            is ViewHolder2 -> {

                if (model.actionType == 3) {
                    val colorText = ColorStateList.valueOf(ContextCompat.getColor(holder.binding.actionBtn.context, R.color.button_action_accept))
                    holder.binding.actionBtn.setTextColor(colorText)
                } else if (model.actionType == 4) {
                    val colorText = ColorStateList.valueOf(ContextCompat.getColor(holder.binding.actionBtn.context, R.color.button_action_text_gry))
                    holder.binding.actionBtn.setTextColor(colorText)
                }

                holder.binding.actionBtn.text = model.actionMessage
            }
        }
    }

    private inner class ViewHolder(val binding: ItemViewActionBtnPositiveBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.actionBtn.setOnClickListener {
                onActionClicked?.invoke(dataList[adapterPosition])
            }
        }
    }


    private inner class ViewHolder2(val binding: ItemViewActionMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        init {

        }
    }

    fun loadData(list: MutableList<Action>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }
}
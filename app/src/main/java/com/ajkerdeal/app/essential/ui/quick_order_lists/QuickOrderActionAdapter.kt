package com.ajkerdeal.app.essential.ui.quick_order_lists

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.api.models.quick_order.fetch_quick_order_request.ActionModel
import com.ajkerdeal.app.essential.databinding.ItemViewActionBtnPositiveBinding
import com.ajkerdeal.app.essential.databinding.ItemViewActionMessageBinding
import com.bumptech.glide.Glide

class QuickOrderActionAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataList: MutableList<ActionModel> = mutableListOf()
    var onActionClicked: ((model: ActionModel) -> Unit)? = null

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
                holder.binding.title.text = model.buttonName
               // holder.binding.parent.backgroundTintList = ColorStateList.valueOf(Color.parseColor(model.colorCode))
                Glide.with(holder.binding.icon)
                    .load(R.drawable.ic_done)
                    .into(holder.binding.icon)

            }
            is ViewHolder2 -> {
                    val colorText = ColorStateList.valueOf(ContextCompat.getColor(holder.binding.actionBtn.context, R.color.button_action_accept))
                    holder.binding.actionBtn.setTextColor(colorText)


                holder.binding.actionBtn.text = model.statusMessage
            }
        }
    }

    private inner class ViewHolder(val binding: ItemViewActionBtnPositiveBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.parent.setOnClickListener {
                onActionClicked?.invoke(dataList[adapterPosition])
            }
        }
    }


    private inner class ViewHolder2(val binding: ItemViewActionMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        init {

        }
    }

    fun loadData(list: List<ActionModel>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }
}
package com.ajkerdeal.app.essential.ui.home.weight_selection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ajkerdeal.app.essential.api.models.weight.WeightRangeDataModel
import com.ajkerdeal.app.essential.databinding.ItemViewWeightListBinding


class WeightSelectionAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataList: MutableList<WeightRangeDataModel> = mutableListOf()
    var onItemClicked: ((model: WeightRangeDataModel) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemViewWeightListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewModel(binding)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewModel) {
            val model = dataList[position]

            holder.binding.weightLists.text = model.weight
        }
    }

    inner class ViewModel(val binding: ItemViewWeightListBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClicked?.invoke(dataList[adapterPosition])
                }
            }
        }
    }

    fun loadInitData(list: List<WeightRangeDataModel>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

    fun clearData() {
        dataList.clear()
        notifyDataSetChanged()
    }
}
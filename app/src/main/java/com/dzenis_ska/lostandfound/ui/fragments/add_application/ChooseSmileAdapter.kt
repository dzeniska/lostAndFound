package com.dzenis_ska.lostandfound.ui.fragments.add_application

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dzenis_ska.lostandfound.databinding.ItemChooseSmileAdapterBinding

typealias ChooseSmile = (Int)->Unit

class ChooseSmileAdapter(val chooseSmile: ChooseSmile) :
    RecyclerView.Adapter<ChooseSmileAdapter.SmileViewHolder>(),
    View.OnClickListener {

    var smiles: List<String> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    class SmileViewHolder(val binding: ItemChooseSmileAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmileViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemChooseSmileAdapterBinding.inflate(inflater, parent, false)
        binding.smileId.setOnClickListener(this)
        return SmileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SmileViewHolder, position: Int) {
        val smile = smiles[position]
        with(holder.binding) {
            holder.itemView.tag = smile
            if (smile.isNotBlank()) {
                smileId.text = smile
            }
        }
    }

    override fun getItemCount(): Int {
        return smiles.size
    }

    override fun onClick(view: View) {
        val smile = view.tag as String
        val position = smiles.indexOfFirst { it == smile }
        chooseSmile(position)
    }
}
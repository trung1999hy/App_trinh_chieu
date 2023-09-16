package com.thn.videoconstruction.adapter

import android.graphics.Color
import android.view.View
import com.thn.videoconstruction.R
import com.thn.videoconstruction.base.FEBaseAdapter
import com.thn.videoconstruction.base.FEBaseViewHolder
import kotlinx.android.synthetic.main.item_color_list.view.*

class FEListColorsAdapterFE(val callback: (Int) -> Unit) : FEBaseAdapter<String>() {
    override fun doGetViewType(position: Int): Int = R.layout.item_color_list
    private var mColorSelected = ""
    override fun onBindViewHolder(holder: FEBaseViewHolder, position: Int) {
        val view = holder.itemView
        val item = mItemList[position]
        try {
            view.colorPreview.setBackgroundColor(Color.parseColor(item))
            view.setOnClickListener {
                mColorSelected = item
                callback.invoke(Color.parseColor(item))
                notifyDataSetChanged()
            }
            if (mColorSelected == item) {
                view.translationY = -20f
                view.strokeInColor.visibility = View.VISIBLE
            } else {
                view.translationY = 0f
                view.strokeInColor.visibility = View.GONE
            }
        } catch (e: Exception) {

        }
    }
}
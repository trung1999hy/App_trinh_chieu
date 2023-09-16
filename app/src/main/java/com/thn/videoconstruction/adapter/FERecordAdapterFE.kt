package com.thn.videoconstruction.adapter

import android.view.View
import com.thn.videoconstruction.R
import com.thn.videoconstruction.base.FEBaseAdapter
import com.thn.videoconstruction.base.FEBaseViewHolder
import com.thn.videoconstruction.models.RecordedModels
import kotlinx.android.synthetic.main.item_recorded.view.*

class FERecordAdapterFE : FEBaseAdapter<RecordedModels>() {
    var onSelect: ((RecordedModels) -> Unit)? = null
    override fun doGetViewType(position: Int): Int = R.layout.item_recorded

    override fun onBindViewHolder(holder: FEBaseViewHolder, position: Int) {
        val view = holder.itemView
        val item = mItemList[position]
        if (item.isSelect) {
            view.grayBg.visibility = View.VISIBLE
        } else {
            view.grayBg.visibility = View.GONE
        }

        view.setOnClickListener {
            setOffAll()
            item.isSelect = true
            onSelect?.invoke(item)
            notifyDataSetChanged()
        }
        view.recordName.text = "Record_${position}"
    }

    fun checkRecordExist(timeMs: Int): RecordedModels? {
        for (item in itemList) {
            if (item.checkTime(timeMs)) {

                return item
            }
        }
        return null
    }

    fun deleteRecord(path: String) {
        getItemByPath(path)?.let {
            mItemList.remove(it)
            setOffAll()
            notifyDataSetChanged()
        }
    }

    fun selectRecord(path: String) {
        getItemByPath(path)?.let {
            setOffAll()
            it.isSelect = true
            notifyDataSetChanged()
        }
    }

    private fun getItemByPath(path: String): RecordedModels? {
        for (item in mItemList) {
            if (item.path == path) {

                return item
            }
        }
        return null
    }

    fun setOffAll() {
        for (item in mItemList) {
            item.isSelect = false
        }
        notifyDataSetChanged()
    }
}
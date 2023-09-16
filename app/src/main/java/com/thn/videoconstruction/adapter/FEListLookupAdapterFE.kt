package com.thn.videoconstruction.adapter

import android.view.View
import com.thn.videoconstruction.R
import com.thn.videoconstruction.base.FEBaseAdapter
import com.thn.videoconstruction.base.FEBaseViewHolder
import com.thn.videoconstruction.models.LookupModel
import com.thn.videoconstruction.utils.Utils
import kotlinx.android.synthetic.main.item_lookup.view.*

class FEListLookupAdapterFE(
    val onSelectLookup: (Utils.LookupType) -> Unit, val
    openInapp: () -> Unit
) :
    FEBaseAdapter<LookupModel>() {
    private var mCurrentPosition = -1

    init {
        mItemList.clear()
        val lookupDataList = Utils.getLookupDataList()
        for (item in lookupDataList) {
            mItemList.add(LookupModel(item))
        }
        notifyDataSetChanged()
    }

    override fun doGetViewType(position: Int): Int = R.layout.item_lookup

    override fun onBindViewHolder(holder: FEBaseViewHolder, position: Int) {
        val view = holder.itemView
        val item = mItemList[position]
        view.lock.visibility = if (item.lock) View.VISIBLE else View.GONE
        view.setOnClickListener {

                onSelectLookup.invoke(item.lookupType)
                mCurrentPosition = position
                notifyDataSetChanged()

        }
        if (mCurrentPosition == position) {
            view.strokeBg.visibility = View.VISIBLE
        } else {
            view.strokeBg.visibility = View.GONE
        }
        view.imageThumb.setImageBitmap(Utils.getBitmapFromAsset("preview/${item.lookupType}.jpg"))
        view.lookupNameLabel.text = item.name
    }

    fun highlightItem(lookupType: Utils.LookupType) {
        for (index in 0 until mItemList.size) {
            val item = mItemList[index]
            if (lookupType == item.lookupType) {
                mCurrentPosition = index
                notifyDataSetChanged()
                break
            }
        }
    }
}
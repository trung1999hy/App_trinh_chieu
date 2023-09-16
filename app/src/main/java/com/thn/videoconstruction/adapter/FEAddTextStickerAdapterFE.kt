package com.thn.videoconstruction.adapter

import android.view.View
import com.thn.videoconstruction.R
import com.thn.videoconstruction.base.FEBaseAdapter
import com.thn.videoconstruction.base.FEBaseViewHolder
import com.thn.videoconstruction.models.TextStickerAddedModel
import kotlinx.android.synthetic.main.item_text_sticker_added.view.*

class FEAddTextStickerAdapterFE(private val onChange: OnChange) :
    FEBaseAdapter<TextStickerAddedModel>() {
    override fun doGetViewType(position: Int): Int = R.layout.item_text_sticker_added

    override fun onBindViewHolder(holder: FEBaseViewHolder, position: Int) {
        val view = holder.itemView
        val item = mItemList[position]

        view.setOnClickListener {
            setOffAll()
            mCurrentItem?.inEdit = false
            item.inEdit = true
            mCurrentItem = item
            notifyDataSetChanged()
            onChange.onClickTextSticker(item)
        }

        if (item.inEdit) {
            view.grayBg.visibility = View.VISIBLE
        } else {
            view.grayBg.visibility = View.GONE
        }
        view.textContent.text = item.text
    }

    fun setOffAll() {
        for (item in mItemList) {
            item.inEdit = false
        }
        mCurrentItem = null
        notifyDataSetChanged()
    }

    fun addNewText(textStickerAddedModel: TextStickerAddedModel) {
        mCurrentItem?.inEdit = false
        mItemList.add(textStickerAddedModel)
        mCurrentItem = textStickerAddedModel
        notifyDataSetChanged()
    }

    fun deleteItem(textStickerAddedModel: TextStickerAddedModel) {
        mItemList.remove(textStickerAddedModel)
        notifyDataSetChanged()
    }

    fun deleteAllItem() {
        mItemList.clear()
        notifyDataSetChanged()
    }

    fun getItemBytViewId(viewId: Int): TextStickerAddedModel? {
        for (item in mItemList) {
            if (item.viewId == viewId) {
                return item
            }
        }
        return null
    }

    interface OnChange {
        fun onClickTextSticker(textStickerAddedModel: TextStickerAddedModel)
    }

}
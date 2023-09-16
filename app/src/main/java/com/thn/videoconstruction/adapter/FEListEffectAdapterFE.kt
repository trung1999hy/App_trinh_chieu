package com.thn.videoconstruction.adapter

import android.view.View
import com.thn.videoconstruction.R
import com.thn.videoconstruction.base.FEBaseAdapter
import com.thn.videoconstruction.base.FEBaseViewHolder
import com.thn.videoconstruction.ui_effect.UIEffectUtils
import com.thn.videoconstruction.models.UIEffectModel
import com.thn.videoconstruction.utils.Utils
import kotlinx.android.synthetic.main.item_gs_transition_list.view.*

class FEListEffectAdapterFE : FEBaseAdapter<UIEffectModel>() {
    var onSelectEffectCallback: ((Int, UIEffectUtils.EffectType) -> Unit)? = null

    init {
        val effectDataList = UIEffectUtils.getAllGSEffectData()
        for (item in effectDataList) {
            mItemList.add(UIEffectModel(item).apply {
                if (item.effectType == UIEffectUtils.EffectType.NONE) {
                    isSelect = true
                }
            })
        }
        notifyDataSetChanged()
    }

    override fun doGetViewType(position: Int): Int = R.layout.item_gs_transition_list

    override fun onBindViewHolder(holder: FEBaseViewHolder, position: Int) {
        val item = mItemList[position]
        val view = holder.itemView
        view.transitionNameLabel.text = item.name
        if (item.isSelect) {
            view.strokeBg.visibility = View.VISIBLE
        } else {
            view.strokeBg.visibility = View.GONE
        }
        view.setOnClickListener {
            setOffAll()
            item.isSelect = true
            notifyDataSetChanged()
            onSelectEffectCallback?.invoke(position, item.UIEffectData.effectType)
        }

        view.imagePreview.setImageBitmap(Utils.getBitmapFromAsset("video-preview/${item.UIEffectData.effectType}.jpg"))
    }

    private fun setOffAll() {
        for (item in mItemList) {
            item.isSelect = false
        }
    }

    fun selectEffect(effectType: UIEffectUtils.EffectType) {
        setOffAll()
        for (item in mItemList) {
            if (item.UIEffectData.effectType == effectType) {
                item.isSelect = true
                notifyDataSetChanged()
                return
            }
        }
    }
}
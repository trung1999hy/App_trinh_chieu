package com.thn.videoconstruction.adapter

import android.net.Uri
import android.view.View
import com.thn.videoconstruction.R
import com.thn.videoconstruction.base.FEBaseAdapter
import com.thn.videoconstruction.base.FEBaseViewHolder
import com.thn.videoconstruction.models.UIModel
import com.thn.videoconstruction.utils.Utils
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_gs_transition_list.view.*

class FEListTransitionAdapterFE(
    private val onSelectTransition: (UIModel) -> Unit, val
    openInapp: () -> Unit
) : FEBaseAdapter<UIModel>() {

    init {
        addGSTransitionData(Utils.getGSTransitionList())
    }

    override fun doGetViewType(position: Int): Int = R.layout.item_gs_transition_list

    override fun onBindViewHolder(holder: FEBaseViewHolder, position: Int) {
        val view = holder.itemView
        val item = mItemList[position]

        view.transitionNameLabel.text = item.FETransition.transitionName
        if (item.selected) {
            view.strokeBg.visibility = View.VISIBLE
        } else {
            view.strokeBg.visibility = View.GONE
        }

        view.setOnClickListener {
            highlightItem(item.FETransition)
            onSelectTransition.invoke(item)
            notifyDataSetChanged()

        }
        Glide.with(view.context)
            .load(Uri.parse("file:///android_asset/transition/${item.FETransition.transitionName}.jpg"))
            .into(view.imagePreview)
    }

    fun addGSTransitionData(FETransitionList: ArrayList<com.thn.videoconstruction.transition.FETransition>) {
        mItemList.clear()
        notifyDataSetChanged()
        for (gsTransition in FETransitionList) {
            mItemList.add(UIModel(gsTransition))
        }
        notifyDataSetChanged()
    }

    fun highlightItem(FETransition: com.thn.videoconstruction.transition.FETransition) {
        for (item in mItemList) {
            item.selected = item.FETransition.transitionCodeId == FETransition.transitionCodeId
        }
        notifyDataSetChanged()
    }

}
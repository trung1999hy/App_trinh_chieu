package com.thn.videoconstruction.adapter

import android.view.View
import com.thn.videoconstruction.R
import com.thn.videoconstruction.base.FEBaseAdapter
import com.thn.videoconstruction.base.FEBaseViewHolder
import com.thn.videoconstruction.models.SlideSourceModel
import com.thn.videoconstruction.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_image_list_in_slide_show.view.*

class FESlideAdapterFE : FEBaseAdapter<SlideSourceModel>() {

    var onClickItem: ((Int) -> Unit)? = null

    override fun doGetViewType(position: Int): Int = R.layout.item_image_list_in_slide_show

    override fun onBindViewHolder(holder: FEBaseViewHolder, position: Int) {
        val view = holder.itemView
        val item = mItemList[position]
        val imageSize = Utils.density(view.context) * 64
        if (item.isSelect) {
            view.strokeBg.visibility = View.VISIBLE
        } else {
            view.strokeBg.visibility = View.GONE
        }
        Glide.with(view.context).load(item.path).apply(RequestOptions().override(imageSize.toInt()))
            .into(view.imagePreview)
        view.setOnClickListener {
            setOffAll()

            item.isSelect = true
            notifyDataSetChanged()
            onClickItem?.invoke(position)
        }
    }

    private fun setOffAll() {
        for (item in mItemList) item.isSelect = false

    }

    fun addImagePathList(arrayList: ArrayList<String>) {
        mItemList.clear()
        notifyDataSetChanged()
        for (item in arrayList) {
            mItemList.add(SlideSourceModel(item))
        }
        notifyDataSetChanged()
    }

    fun changeVideo(position: Int) {
        if (position >= 0 && position < mItemList.size) {

            setOffAll()
            mItemList[position].isSelect = true
            notifyDataSetChanged()
        }
    }

    fun changeHighlightItem(position: Int) {
        if (position >= 0 && position < mItemList.size) {
            setOffAll()
            mItemList[position].isSelect = true
            notifyDataSetChanged()
        }
    }
}
package com.thn.videoconstruction.adapter

import android.view.View
import com.thn.videoconstruction.R
import com.thn.videoconstruction.application.FEMainApp
import com.thn.videoconstruction.base.FEBaseAdapter
import com.thn.videoconstruction.base.FEBaseViewHolder
import com.thn.videoconstruction.models.Theme
import com.thn.videoconstruction.models.ThemeModel
import com.thn.videoconstruction.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_slide_theme.view.*

class FESlideThemeAdapterFE(val callback: (Theme) -> Unit) : FEBaseAdapter<ThemeModel>() {

    init {
        getAllThemeOnDevice()
    }


    override fun doGetViewType(position: Int): Int = R.layout.item_slide_theme

    override fun onBindViewHolder(holder: FEBaseViewHolder, position: Int) {
        val view = holder.itemView
        val item = mItemList[position]
        view.setOnClickListener {
            callback.invoke(item.theme)
            highlightItem(item.theme.themeVideoFilePath)
        }
        view.themeNameLabel.text = item.name
        if (item.name == "none") {
            view.themeIcon.setImageResource(R.drawable.ic_none)
        } else {
            Glide.with(view.context).load(item.videoPath).apply(RequestOptions().override(200))
                .into(view.themeIcon)
        }
        if (item.selected) {
            view.strokeBg.visibility = View.VISIBLE
            view.blackBgOfTitleView.setBackgroundColor(FEMainApp.getContext().resources.getColor(R.color.orangeA02))
        } else {
            view.strokeBg.visibility = View.GONE
            view.blackBgOfTitleView.setBackgroundColor(FEMainApp.getContext().resources.getColor(R.color.blackAlpha45))
        }


    }

    fun highlightItem(path: String) {
        for (item in mItemList) {
            item.selected = item.theme.themeVideoFilePath == path
        }
        notifyDataSetChanged()
    }

    private fun getAllThemeOnDevice() {
        for (themeData in Utils.getThemeDataList()) {
            mItemList.add(ThemeModel(themeData))
        }
        notifyDataSetChanged()
    }

}
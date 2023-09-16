package com.thn.videoconstruction.adapter

import android.view.View
import com.thn.videoconstruction.R
import com.thn.videoconstruction.base.FEBaseAdapter
import com.thn.videoconstruction.base.FEBaseViewHolder
import com.thn.videoconstruction.models.ThemeLink
import com.thn.videoconstruction.utils.Utils
import kotlinx.android.synthetic.main.item_theme_in_home.view.*
import java.io.File

class FEThemeHomeAdapterFE : FEBaseAdapter<ThemeLink>() {

    var onItemClick: ((ThemeLink) -> Unit)? = null
    private var mCurrentThemeFileName = "None"
    var rewardIsLoaded = false
    override fun doGetViewType(position: Int): Int = R.layout.item_theme_in_home

    override fun onBindViewHolder(holder: FEBaseViewHolder, position: Int) {
        val view = holder.itemView
        val item = mItemList[position]
        view.themeName.text = item.name
        if (item.link == "none") {
            view.themeIcon.setImageResource(R.drawable.ic_none)
            view.iconDownload.visibility = View.GONE
        } else {
            if (File(Utils.themeFolderPath + "/${item.fileName}.mp4").exists()) {
                view.iconDownload.visibility = View.GONE
                view.bgAlpha.visibility = View.GONE
            } else {
                view.iconDownload.visibility = View.VISIBLE
                if (rewardIsLoaded) {
                    view.bgAlpha.visibility = View.GONE
                } else {
                    view.bgAlpha.visibility = View.VISIBLE
                }
            }
        }

        if (mCurrentThemeFileName == item.fileName) {
            view.strokeBg.visibility = View.VISIBLE
        } else {
            view.strokeBg.visibility = View.GONE
        }

        view.setOnClickListener {
            onItemClick?.invoke(item)
        }


    }

    fun changeCurrentThemeName(themeFileName: String) {
        mCurrentThemeFileName = themeFileName
        notifyDataSetChanged()
    }

}
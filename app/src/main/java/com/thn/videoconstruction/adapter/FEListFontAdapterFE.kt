package com.thn.videoconstruction.adapter

import android.graphics.Color
import androidx.core.content.res.ResourcesCompat
import com.thn.videoconstruction.R
import com.thn.videoconstruction.base.FEBaseAdapter
import com.thn.videoconstruction.base.FEBaseViewHolder
import com.thn.videoconstruction.models.FontModels
import com.thn.videoconstruction.models.FontsDatas
import kotlinx.android.synthetic.main.item_fonts_list.view.*

class FEListFontAdapterFE(val callback: (fontId: Int) -> Unit) : FEBaseAdapter<FontModels>() {

    init {
        mItemList.add(FontModels(FontsDatas(R.font.doubledecker_demo, "Double")))
        mItemList.add(FontModels(FontsDatas(R.font.doubledecker_dots, "Double Dots")))
        mItemList.add(FontModels(FontsDatas(R.font.fonseca_grande, "Fonseca")))
        mItemList.add(FontModels(FontsDatas(R.font.youth_power, "Youth Power")))
        mItemList.add(FontModels(FontsDatas(R.font.fun_sized,"Fun sized")))

    }

    private var selectedFontId = -1
    override fun doGetViewType(position: Int): Int = R.layout.item_fonts_list

    override fun onBindViewHolder(holder: FEBaseViewHolder, position: Int) {
        val view = holder.itemView
        val item = mItemList[position]

        view.fontPreview.typeface = ResourcesCompat.getFont(view.context, item.fontId)
        view.fontPreview.text = item.fontName

        if (item.fontId == selectedFontId) {
            view.setBackgroundColor(Color.parseColor("#33000000"))
        } else {
            view.setBackgroundColor(Color.TRANSPARENT)
        }

        view.setOnClickListener {
            callback.invoke(item.fontId)
            selectedFontId = item.fontId
            notifyDataSetChanged()
        }
    }
}
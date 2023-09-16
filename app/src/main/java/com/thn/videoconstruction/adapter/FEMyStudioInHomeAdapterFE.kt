package com.thn.videoconstruction.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.View
import com.thn.videoconstruction.R
import com.thn.videoconstruction.application.FEMainApp
import com.thn.videoconstruction.base.FEBaseAdapter
import com.thn.videoconstruction.base.FEBaseViewHolder
import com.thn.videoconstruction.models.MyStudioModel
import com.thn.videoconstruction.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_my_studio_in_home.view.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class FEMyStudioInHomeAdapterFE : FEBaseAdapter<MyStudioModel>() {
    override fun doGetViewType(position: Int): Int = R.layout.item_my_studio_in_home

    var onClickItem: ((MyStudioModel) -> Unit)? = null

    @SuppressLint("DefaultLocale")
    override fun onBindViewHolder(holder: FEBaseViewHolder, position: Int) {
        val view = holder.itemView
        val item = mItemList[position]
        val size = Utils.density(view.context) * 98
        Glide.with(view.context).load(item.filePath).placeholder(R.drawable.ic_load_thumb).apply(
            RequestOptions().override(size.toInt())
        ).into(view.imageThumb)

        if (item.filePath.lowercase(Locale.getDefault()).contains(".mp4")) {
            view.grayBg.visibility = View.VISIBLE
            try {
                val duration = Utils.getVideoDuration(item.filePath)
                view.durationLabel.text = Utils.convertSecToTimeString(duration / 1000)
            } catch (e: Exception) {
                File(item.filePath).delete()
                FEMainApp.getContext().sendBroadcast(
                    Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(File(item.filePath))
                    )
                )
            }


        } else {
            view.grayBg.visibility = View.GONE
            view.durationLabel.visibility = View.GONE
        }
        view.setOnClickListener {
            onClickItem?.invoke(item)
        }

    }

    override fun setItemList(arrayList: ArrayList<MyStudioModel>) {
        arrayList.sort()
        mItemList.clear()
        mItemList.addAll(arrayList)
        notifyDataSetChanged()
    }
}
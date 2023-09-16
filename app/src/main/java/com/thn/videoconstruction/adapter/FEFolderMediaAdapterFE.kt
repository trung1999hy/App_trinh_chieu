package com.thn.videoconstruction.adapter

import android.annotation.SuppressLint
import com.thn.videoconstruction.R
import com.thn.videoconstruction.base.FEBaseAdapter
import com.thn.videoconstruction.base.FEBaseViewHolder
import com.thn.videoconstruction.models.MediaAlbumModel
import com.thn.videoconstruction.models.Media
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_media_album.view.*

class FEFolderMediaAdapterFE : FEBaseAdapter<MediaAlbumModel>() {
    override fun doGetViewType(position: Int): Int = R.layout.item_media_album

    var onClickItem: ((MediaAlbumModel) -> Unit)? = null

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FEBaseViewHolder, position: Int) {
        val view = holder.itemView
        val item = mItemList[position]
        view.albumDescription.text = "${item.albumName}(${item.mediaItemPaths.size})"
        if (item.mediaItemPaths.size > 0) {
            Glide.with(view.context).load(item.mediaItemPaths[0].filePath)
                .apply(RequestOptions().override(/*DimenUtils.screenWidth(view.context)/3*/200))
                .into(view.albumCover)
        }
        view.setOnClickListener {
            onClickItem?.invoke(item)
        }

    }

    fun setItemListFromData(itemsInput: ArrayList<Media>) {
        if (itemsInput.size < 1) return
        val finalItems = arrayListOf<MediaAlbumModel>()
        finalItems.add(MediaAlbumModel(itemsInput[0].folderName, itemsInput[0].folderId))
        finalItems[0].mediaItemPaths.add(itemsInput[0])

        for (index in 1 until itemsInput.size) {
            val itemInput = itemsInput[index]
            var flag = true
            for (j in 0 until finalItems.size) {
                if (finalItems[j].folderId == itemInput.folderId) {
                    finalItems[j].mediaItemPaths.add(itemInput)
                    flag = false
                    break
                }
            }
            if (flag) {
                finalItems.add(MediaAlbumModel(itemInput.folderName, itemInput.folderId))
                finalItems[finalItems.size - 1].mediaItemPaths.add(itemInput)
            }
        }
        mItemList.clear()
        mItemList.addAll(finalItems)
        notifyDataSetChanged()
    }

    fun addItemToAlbum(media: Media) {
        for (folder in mItemList) {
            if (folder.folderId == media.folderId) {
                folder.mediaItemPaths.add(media)
                break
            }
        }
        notifyDataSetChanged()
    }
}
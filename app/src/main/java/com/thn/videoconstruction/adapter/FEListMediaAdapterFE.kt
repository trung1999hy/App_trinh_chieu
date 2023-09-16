package com.thn.videoconstruction.adapter

import android.annotation.SuppressLint
import com.thn.videoconstruction.R
import com.thn.videoconstruction.application.FEMainApp
import com.thn.videoconstruction.base.FEBaseAdapter
import com.thn.videoconstruction.base.FEBaseViewHolder
import com.thn.videoconstruction.models.Media
import com.thn.videoconstruction.models.MediaModel
import com.thn.videoconstruction.fe_ui.pick_media.MediaPickActivityFE
import com.thn.videoconstruction.utils.Utils
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_header_view_date.view.*
import kotlinx.android.synthetic.main.item_media_with_text_count.view.*
import java.util.*

class FEListMediaAdapterFE(val callback: (MediaModel) -> Unit) : FEBaseAdapter<MediaModel>() {

    private val mImageSize: Int
    var activeCounter = true

    init {
        val context = FEMainApp.getContext()
        val density = Utils.density(context)
        val numberCols =
            (Utils.screenWidth(context) / (MediaPickActivityFE.COLS_IMAGE_LIST_SIZE * density)).toInt()
        mImageSize = Utils.screenWidth(context) / numberCols
        mItemList.clear()
        notifyDataSetChanged()
    }

    override fun doGetViewType(position: Int): Int {
        return if (mItemList[position].filePath.isEmpty()) {
            R.layout.item_header_view_date
        } else {
            R.layout.item_media_with_text_count
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FEBaseViewHolder, position: Int) {
        val view = holder.itemView
        val item = mItemList[position]
        if (getItemViewType(position) == R.layout.item_header_view_date) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = item.dateAdded

            val today = Calendar.getInstance()

            if (calendar.timeInMillis >= System.currentTimeMillis()) {
                view.dateAddedLabel.text =
                    "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${
                        calendar.get(Calendar.YEAR)
                    }"
            } else {
                if (calendar.get(Calendar.YEAR) != today.get(Calendar.YEAR)) {
                    view.dateAddedLabel.text =
                        "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${
                            calendar.get(Calendar.YEAR)
                        }"
                } else {
                    if (calendar.get(Calendar.MONTH) != today.get(Calendar.MONTH)) {
                        view.dateAddedLabel.text =
                            "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${
                                calendar.get(Calendar.YEAR)
                            }"
                    } else {
                        if (calendar.get(Calendar.DAY_OF_MONTH) != today.get(Calendar.DAY_OF_MONTH)) {
                            if (today.timeInMillis - calendar.timeInMillis < (24 * 60 * 60 * 1000)) {
                                view.dateAddedLabel.text =
                                    view.context.getString(R.string.yesterday)
                            } else {
                                view.dateAddedLabel.text =
                                    "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${
                                        calendar.get(Calendar.YEAR)
                                    }"
                            }
                        } else {
                            view.dateAddedLabel.text = view.context.getString(R.string.today)
                        }
                    }
                }
            }


        } else {
            if (activeCounter) {
                view.mediaThumb.activeCounter()
            } else {
                view.mediaThumb.disableCounter()
            }
            view.mediaThumb.setCount(item.count)

            Glide.with(view.context).load(item.filePath).placeholder(R.drawable.ic_load_thumb).into(view.mediaThumb)
            view.setOnClickListener {
                item.count++
                view.mediaThumb.setCount(item.count)
                callback.invoke(item)
            }
        }

    }

    private val originMediaList = ArrayList<MediaModel>()
    override fun setItemList(arrayList: ArrayList<MediaModel>) {
        originMediaList.clear()
        originMediaList.addAll(arrayList)
        if (arrayList.size < 1) return
        val finalItems = getFinalItem(arrayList)
        mItemList.clear()
        mItemList.addAll(finalItems)
        notifyDataSetChanged()
    }

    fun addNewItem(media: Media) {
        originMediaList.add(MediaModel(media))
        originMediaList.sort()
        val finalItems = getFinalItem(originMediaList)
        mItemList.clear()
        mItemList.addAll(finalItems)
        notifyDataSetChanged()
    }

    private fun getFinalItem(arrayList: ArrayList<MediaModel>): ArrayList<MediaModel> {
        val finalItems = arrayListOf<MediaModel>()

        finalItems.add(MediaModel(Media(arrayList[0].dateAdded)))
        finalItems.add(arrayList[0])

        val preItemCalendar = Calendar.getInstance()
        val currentItemCalendar = Calendar.getInstance()
        for (index in 1 until arrayList.size) {
            val preItem = arrayList[index - 1]
            val item = arrayList[index]

            preItemCalendar.timeInMillis = preItem.dateAdded
            currentItemCalendar.timeInMillis = item.dateAdded

            if (preItemCalendar.get(Calendar.YEAR) != currentItemCalendar.get(Calendar.YEAR)) {
                finalItems.add(MediaModel(Media(item.dateAdded)))
                finalItems.add(item)
            } else {
                if (preItemCalendar.get(Calendar.MONTH) != currentItemCalendar.get(Calendar.MONTH)) {
                    finalItems.add(MediaModel(Media(item.dateAdded)))
                    finalItems.add(item)
                } else {
                    if (preItemCalendar.get(Calendar.DAY_OF_MONTH) != currentItemCalendar.get(
                            Calendar.DAY_OF_MONTH
                        )
                    ) {
                        finalItems.add(MediaModel(Media(item.dateAdded)))
                        finalItems.add(item)
                    } else {
                        finalItems.add(item)
                    }
                }
            }
        }
        for (item in mItemList) {
            for (finalItem in finalItems) {
                if (item.filePath.isNotEmpty() && item.filePath == finalItem.filePath) {
                    finalItem.count = item.count
                    break
                }
            }
        }
        return finalItems
    }

    fun updateCount(mediaCount: HashMap<String, Int>) {
        for (item in mItemList) {
            mediaCount[item.filePath]?.let {
                item.count = it
            }
        }
        notifyDataSetChanged()
    }

    fun updateCount(pathList: ArrayList<String>) {
        for (path in pathList) {
            for (item in mItemList) {
                if (path == item.filePath) {
                    item.count++

                    break
                }
            }
        }
        notifyDataSetChanged()
    }

    fun deleteByPath(path: String) {
        for (index in 0 until mItemList.size) {
            val item = mItemList[index]
            if (item.filePath == path) {
                mItemList.removeAt(index)
                return
            }

        }
    }

    fun deleteEmptyDay() {
        val dateList = ArrayList<Long>()
        for (index in 0 until mItemList.size) {
            val item = mItemList[index]
            if (item.filePath.isEmpty()) {
                if (index == mItemList.size - 1) {
                    mItemList.removeAt(index)
                } else {
                    val nextItem = mItemList[index + 1]
                    if (nextItem.filePath.isEmpty()) {
                        dateList.add(item.dateAdded)
                    }
                }
            }
        }

        dateList.forEach {
            for (index in 0 until mItemList.size) {
                val item = mItemList[index]
                if (item.filePath.isEmpty() && item.dateAdded == it) {
                    mItemList.removeAt(index)
                    break
                }
            }
        }

    }

}
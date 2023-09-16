package com.thn.videoconstruction.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class FEBaseAdapter <T> : RecyclerView.Adapter<FEBaseViewHolder>() {
    protected val mItemList = ArrayList<T>()
    val itemList get() = mItemList

    protected var mCurrentItem:T? = null
    val currentItem get() = mCurrentItem

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FEBaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return FEBaseViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mItemList.size
    }

    open fun setItemList(arrayList: ArrayList<T>) {
        mItemList.clear()
        mItemList.addAll(arrayList)
        notifyDataSetChanged()
    }

    fun addItem(item:T) {
        mItemList.add(item)
        notifyDataSetChanged()
    }

    fun clear() {
        mItemList.clear()
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = doGetViewType(position)

    abstract fun doGetViewType(position:Int):Int

}
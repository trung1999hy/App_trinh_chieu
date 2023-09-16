package com.thn.videoconstruction.adapter

import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.thn.videoconstruction.R
import com.thn.videoconstruction.databinding.CustomRvEditSlideshowBinding
import com.thn.videoconstruction.models.IconModels
import com.bumptech.glide.Glide

@Suppress("DEPRECATION")
class FEImageSlideAdapter(
    val ItemClick: (Int) -> Unit
) : RecyclerView.Adapter<FEImageSlideAdapter.SlideImageViewHolder>() {
    var previousItemClicked = -1
    var currentItemClicked = -1
    private var listItem: ArrayList<IconModels> = arrayListOf()

    inner class SlideImageViewHolder(var binding: CustomRvEditSlideshowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.M)
        fun bindData(item: IconModels) {
            if (item.image != null)
                Glide.with(binding.root).load(item.image).into(binding.itemMusic)
            binding.tvMusic.text = item.textIcon
            checkColorImage()
            binding.root.setOnClickListener {
                if (item.image!= null) {
                    checkColorImage()
                    previousItemClicked = currentItemClicked
                    currentItemClicked = adapterPosition
                    notifyItemChanged(adapterPosition)
                    if (previousItemClicked != -1) {
                        notifyItemChanged(previousItemClicked)
                    }
                    ItemClick.invoke(adapterPosition)
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.M)
        private fun checkColorImage() {
            var position = adapterPosition
            if (currentItemClicked == position) {
                binding.itemMusic.setColorFilter(binding.root.context.getColor(R.color.greenA01))
                binding.tvMusic.setTextColor(Color.parseColor("#434BFA"))
            } else {
                binding.itemMusic.setColorFilter(binding.root.context.getColor(R.color.grayA01))
                binding.tvMusic.setTextColor(Color.parseColor("#BDBDBD"))
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideImageViewHolder {
        return SlideImageViewHolder(CustomRvEditSlideshowBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun getItemCount(): Int = listItem.size


    override fun onBindViewHolder(holder: SlideImageViewHolder, position: Int) {
        holder.bindData(listItem[position])
    }

    fun updateData(mListItem: ArrayList<IconModels>) {
        this.listItem = mListItem
        notifyDataSetChanged()
    }
}
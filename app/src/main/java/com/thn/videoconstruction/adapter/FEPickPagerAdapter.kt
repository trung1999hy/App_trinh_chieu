package com.thn.videoconstruction.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.thn.videoconstruction.R
import com.thn.videoconstruction.fe_ui.pick_media.FEFolderMediaFragment
import com.thn.videoconstruction.fe_ui.pick_media.FEListMediaFragment
import java.util.*

class FEPickPagerAdapter(val context: Context, fragmentManager: FragmentManager) :
    FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                FEListMediaFragment()
            }

            else -> {
                FEFolderMediaFragment()
            }

        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> {
                context.getString(R.string.gallery).uppercase(Locale.getDefault())
            }

            else -> {
                context.getString(R.string.albums).uppercase(Locale.getDefault())
            }
        }
    }

    override fun getCount(): Int = 2

}
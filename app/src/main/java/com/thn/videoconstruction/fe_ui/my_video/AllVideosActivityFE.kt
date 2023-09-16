package com.thn.videoconstruction.fe_ui.my_video

import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.thn.videoconstruction.R
import com.thn.videoconstruction.adapter.AllAdapterFE
import com.thn.videoconstruction.base.FEBaseActivity
import com.thn.videoconstruction.models.MyStudioModel
import com.thn.videoconstruction.fe_ui.share_video.VideoShareActivityFE
import com.thn.videoconstruction.utils.*
import kotlinx.android.synthetic.main.activity_base_layout.*
import kotlinx.android.synthetic.main.activity_my_video.*
import kotlinx.android.synthetic.main.base_header_view.view.*
import java.io.File

class AllVideosActivityFE : FEBaseActivity() {
    override fun getContentResId(): Int = R.layout.activity_my_video

    private val mAllAdapter = AllAdapterFE()

    override fun initViews() {
        setRightButton(R.drawable.ic_delete_white) {
            Loggers.e("delete")
            if (mAllAdapter.getNumberItemSelected() < 1) {
                showToast(getString(R.string.nothing_item_selected))
                return@setRightButton
            }
            showYesNoDialog(getString(R.string.do_you_want_delete_items)) {
                deleteItemSelected()
            }
        }

        setSubRightButton(R.drawable.ic_check_all_none) {
            Loggers.e("check all")
            var allItemChecked = true
            for (item in mAllAdapter.itemList) {
                if (!item.checked && item.filePath.length > 5) {
                    allItemChecked = false
                    break
                }
            }
            Loggers.e("allItemChecked = $allItemChecked")
            if (allItemChecked) {
                mAllAdapter.setOffAll()
                headerView.subRightButton.setImageResource(R.drawable.ic_check_all_none)
            } else {
                selectAll()
                headerView.subRightButton.setImageResource(R.drawable.ic_check_all)
            }

        }

        hideButton()
        setScreenTitle(getString(R.string.my_studio))
        val colSize = 110 * Utils.density(this)
        val numberCols = Utils.screenWidth(this) / colSize
        allMyStudioListView.apply {
            adapter = mAllAdapter
            layoutManager = GridLayoutManager(
                context,
                numberCols.toInt(),
                LinearLayoutManager.VERTICAL,
                false
            ).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (mAllAdapter.getItemViewType(position) == R.layout.item_all_my_studio) {
                            1
                        } else {
                            numberCols.toInt()
                        }
                    }

                }
            }
        }
        //getAllMyStudioItem()
    }

    private var mSelectMode = false
    override fun initActions() {
        mAllAdapter.onLongPress = {
            if (!mSelectMode) {
                openSelectMode()
            }
        }
        mAllAdapter.onSelectChange = {
            Thread {

                val number = mAllAdapter.getNumberItemSelected()
                val total = mAllAdapter.getTotalItem()

                if (number == total) {
                    runOnUiThread {
                        headerView.subRightButton.setImageResource(R.drawable.ic_check_all)
                    }

                } else {
                    runOnUiThread {
                        headerView.subRightButton.setImageResource(R.drawable.ic_check_all_none)
                    }
                }
            }.start()
        }
        mAllAdapter.onClickItem = {
            if (!mSelectMode)
                VideoShareActivityFE.gotoActivity(this, it.filePath)
        }

        mAllAdapter.onClickOpenMenu = { view, myStudioDataModel ->
            val popupMenu = PopupMenu(this@AllVideosActivityFE, view)
            popupMenu.menuInflater.inflate(R.menu.item_my_studio_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {

                    when (item?.itemId) {

                        R.id.delete -> {
                            onDeleteItem(myStudioDataModel.filePath)
                        }


                        R.id.share -> {
                            shareVideoFile(myStudioDataModel.filePath)

                        }
                    }
                    popupMenu.dismiss()
                    return true
                }

            })
            popupMenu.show()
        }
    }

    private fun onDeleteItem(path: String) {
        showYesNoDialog(getString(R.string.do_you_want_delete_item)) {
            val file = File(path)
            if (file.exists()) {

                try {
                    file.delete()
                    mAllAdapter.onDeleteItem(path)
                    updateEmptyIcon()
                    doSendBroadcast(path)
                } catch (e: Exception) {

                }


            }
        }

    }

    private fun openSelectMode() {
        mSelectMode = true
        mAllAdapter.selectMode = true
        mAllAdapter.notifyDataSetChanged()
        showButton()
    }

    private fun closeSelectMode() {
        mSelectMode = false
        mAllAdapter.selectMode = false
        mAllAdapter.notifyDataSetChanged()
        hideButton()
        mAllAdapter.setOffAll()

    }

    private fun showButton() {
        showRightButton()
        showSubRightButton()
    }

    private fun hideButton() {
        hideRightButton()
        hideSubRightButton()
    }

    private fun selectAll() {
        mAllAdapter.selectAll()
    }

    private fun deleteItemSelected() {
        showProgressDialog()
        Thread {
            val selectedItems = ArrayList<MyStudioModel>()
            for (item in mAllAdapter.itemList) {
                if (item.checked && item.filePath.isNotEmpty()) {
                    selectedItems.add(item)
                }
            }
            for (item in selectedItems) {
                val file = File(item.filePath)
                file.delete()
                doSendBroadcast(item.filePath)
                runOnUiThread {
                    mAllAdapter.onDeleteItem(item.filePath)
                }

            }

            runOnUiThread {
                updateEmptyIcon()
                closeSelectMode()
                dismissProgressDialog()
            }
        }.start()

    }

    private fun getAllMyStudioItem() {

        Thread {
            if (mAllAdapter.itemCount > 0) {
                val deletePathList = ArrayList<String>()
                mAllAdapter.itemList.forEachIndexed { index, myStudioDataModel ->

                    if (myStudioDataModel.filePath.length > 5 && !File(myStudioDataModel.filePath).exists()) {
                        deletePathList.add(myStudioDataModel.filePath)
                    }

                }

                runOnUiThread {
                    deletePathList.forEach {
                        mAllAdapter.onDeleteItem(it)
                    }
                }

            } else {
                runOnUiThread {
                    showProgressDialog()
                }
                val folder = File(Utils.myStuioFolderPath)
                val myStudioDataList = ArrayList<MyStudioModel>()
                if (folder.exists() && folder.isDirectory) {
                    for (item in folder.listFiles()) {
                        try {
                            val duration = Utils.getVideoDuration(item.absolutePath)
                            if (item.exists())
                                myStudioDataList.add(
                                    MyStudioModel(
                                        item.absolutePath,
                                        item.lastModified(),
                                        duration
                                    )
                                )

                        } catch (e: java.lang.Exception) {
                            item.delete()
                            doSendBroadcast(item.absolutePath)
                            continue
                        }

                    }
                }
                myStudioDataList.sort()

                runOnUiThread {
                    mAllAdapter.setItemList(myStudioDataList)
                    if (myStudioDataList.size > 0) {
                        mAllAdapter.notifyDataSetChanged()
                        iconNoItem.visibility = View.GONE
                    } else {
                        iconNoItem.visibility = View.VISIBLE
                    }
                    dismissProgressDialog()
                }
            }


        }.start()


    }

    private fun updateEmptyIcon() {
        Thread {
            val total = mAllAdapter.getTotalItem()
            if (total <= 0) {
                runOnUiThread {
                    iconNoItem.visibility = View.VISIBLE
                    allMyStudioListView.visibility = View.GONE
                }

            } else {
                runOnUiThread {
                    iconNoItem.visibility = View.GONE
                    allMyStudioListView.visibility = View.VISIBLE
                }

            }
        }.start()
    }

    override fun onBackPressed() {
        if (mYesNoDialogShowing) {
            dismissYesNoDialog()
            return
        }
        if (mSelectMode) {
            closeSelectMode()
        } else {
            super.onBackPressed()
        }
    }


    override fun onResume() {
        super.onResume()
        getAllMyStudioItem()
    }

}

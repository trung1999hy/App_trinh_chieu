package com.thn.videoconstruction.fe_ui.pick_media


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager

import com.thn.videoconstruction.R
import com.thn.videoconstruction.adapter.FEListMediaAdapterFE
import com.thn.videoconstruction.adapter.FEFolderMediaAdapterFE
import com.thn.videoconstruction.models.MediaModel
import com.thn.videoconstruction.utils.Loggers
import com.thn.videoconstruction.utils.Utils
import kotlinx.android.synthetic.main.fragment_media_folder.*
import kotlinx.android.synthetic.main.fragment_media_list.allMediaListView
import kotlinx.android.synthetic.main.fragment_media_list.iv_no
import kotlinx.android.synthetic.main.fragment_media_list.tv_no
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import java.io.File

class FEFolderMediaFragment : Fragment(), KodeinAware {
    override lateinit var kodein: Kodein

    private val mFeMediaPickViewModelFactory: FeMediaPickViewModelFactory by instance<FeMediaPickViewModelFactory>()
    private lateinit var mFEMediaPickViewModel: FE_MediaPickViewModel

    private val mFEFolderMediaAdapter = FEFolderMediaAdapterFE()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_media_folder, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        kodein = (context as KodeinAware).kodein

        mFEMediaPickViewModel = ViewModelProvider(
            activity!!,
            mFeMediaPickViewModelFactory
        ).get(FE_MediaPickViewModel::class.java)

        initView()
        listen()
    }

    private var mFEListMediaAdapter = FEListMediaAdapterFE {

    }

    private fun initView() {

        setFolderListView()


        mFEFolderMediaAdapter.onClickItem = {
            Loggers.e("name = ${it.albumName}")

            mFEMediaPickViewModel.onShowFolder()
            val mediaItems = ArrayList<MediaModel>()
            for (item in it.mediaItemPaths) {
                mediaItems.add(MediaModel(item))
            }

            mediaItems.sort()
            mFEListMediaAdapter = FEListMediaAdapterFE { mediaDataModel ->
                if (mIsActionTrim) {
                    return@FEListMediaAdapterFE
                }
                mFEMediaPickViewModel.onPickImage(mediaDataModel)
            }
            val colSize = MediaPickActivityFE.COLS_IMAGE_LIST_SIZE * Utils.density(context!!)
            val numberCols = Utils.screenWidth(context!!) / colSize
            mFEListMediaAdapter.setItemList(mediaItems)
            if (mIsActionTrim) {
                mFEListMediaAdapter.activeCounter = false
                mFEListMediaAdapter.notifyDataSetChanged()
            }
            mediaFolderListView.apply {
                adapter = mFEListMediaAdapter
                layoutManager = GridLayoutManager(
                    context,
                    numberCols.toInt(),
                    LinearLayoutManager.VERTICAL,
                    false
                ).apply {
                    spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            return if (mFEListMediaAdapter.getItemViewType(position) == R.layout.item_header_view_date) {
                                numberCols.toInt()
                            } else {
                                1
                            }
                        }

                    }
                }
            }
            mFEListMediaAdapter.updateCount(mFEMediaPickViewModel.mediaPickedCount)
        }
    }

    override fun onResume() {
        super.onResume()
        val listItem = ArrayList<String>()
        mFEListMediaAdapter.itemList.forEach {
            if (it.filePath.isNotEmpty() && !File(it.filePath).exists()) {
                listItem.add(it.filePath)
            }
        }

        listItem.forEach {
            mFEListMediaAdapter.deleteByPath(it)
        }

        mFEListMediaAdapter.notifyDataSetChanged()
        if (mFEFolderMediaAdapter.itemCount <= 0) {
            if (mFEMediaPickViewModel.folderIsShowing) {
                mFEMediaPickViewModel.hideFolder()
            }
        }

        Thread {
            if (mFEListMediaAdapter.itemCount > 0) {
                mFEListMediaAdapter.deleteEmptyDay()
                activity?.runOnUiThread {
                    mFEListMediaAdapter.notifyDataSetChanged()
                }
            }
        }.start()

    }


    private fun setFolderListView() {
        val colSize = MediaPickActivityFE.COLS_ALBUM_LIST_SIZE * Utils.density(context!!)
        val numberCols = Utils.screenWidth(context!!) / colSize

        mediaFolderListView.adapter = mFEFolderMediaAdapter
        mediaFolderListView.layoutManager =
            GridLayoutManager(context, numberCols.toInt(), LinearLayoutManager.VERTICAL, false)
    }

    private var mIsActionTrim = false
    private fun listen() {
        mFEMediaPickViewModel.FELocalStorage.mediaResponse.observe(
            viewLifecycleOwner,
            Observer {
                if (it == null || it.size == 0) {
                    tv_no.visibility = View.VISIBLE
                    iv_no.visibility = View.VISIBLE
                    mediaFolderListView.visibility = View.GONE
                }else{
                    tv_no.visibility = View.GONE
                    iv_no.visibility = View.GONE
                    mediaFolderListView.visibility = View.VISIBLE
                }
                mFEFolderMediaAdapter.setItemListFromData(it)
            })

        mFEMediaPickViewModel.folderIsShowingLiveData.observe(viewLifecycleOwner, Observer {
            if (mediaFolderListView.adapter is FEListMediaAdapterFE) setFolderListView()
        })

        mFEMediaPickViewModel.itemJustPicked.observe(viewLifecycleOwner, Observer {
            mFEListMediaAdapter.updateCount(mFEMediaPickViewModel.mediaPickedCount)
        })

        mFEMediaPickViewModel.itemJustDeleted.observe(viewLifecycleOwner, Observer {
            mFEListMediaAdapter.updateCount(mFEMediaPickViewModel.mediaPickedCount)
        })

        mFEMediaPickViewModel.acctiveCounter.observe(viewLifecycleOwner, Observer {
            if (it == false) {
                mIsActionTrim = true

            }
        })

        mFEMediaPickViewModel.newMediaItem.observe(viewLifecycleOwner, Observer {
            mFEFolderMediaAdapter.addItemToAlbum(it)
        })
    }

}

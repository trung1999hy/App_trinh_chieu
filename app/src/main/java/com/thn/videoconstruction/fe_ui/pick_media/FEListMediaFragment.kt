package com.thn.videoconstruction.fe_ui.pick_media


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.thn.videoconstruction.R
import com.thn.videoconstruction.adapter.FEListMediaAdapterFE
import com.thn.videoconstruction.models.MediaModel
import com.thn.videoconstruction.models.MediaPickedModel
import com.thn.videoconstruction.utils.Loggers
import com.thn.videoconstruction.utils.Utils
import kotlinx.android.synthetic.main.fragment_media_list.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import java.io.File


class FEListMediaFragment : Fragment(), KodeinAware {
    override lateinit var kodein: Kodein

    private val mFeMediaPickViewModelFactory: FeMediaPickViewModelFactory by instance()
    private lateinit var mFEMediaPickViewModel: FE_MediaPickViewModel

    private var mIsActionTrim = false

    private val mFEListMediaAdapter = FEListMediaAdapterFE {
        if (mIsActionTrim) {
            return@FEListMediaAdapterFE
        }
        mFEMediaPickViewModel.onPickImage(it)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_media_list, container, false)
    }

    val extraPathList = ArrayList<String>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        kodein = (context as KodeinAware).kodein

        mFEMediaPickViewModel = ViewModelProvider(
            activity!!,
            mFeMediaPickViewModelFactory
        ).get(FE_MediaPickViewModel::class.java)
        listen()
        initView()

        activity!!.intent.getStringArrayListExtra("list-photo")?.let {
            for (path in it) {
                extraPathList.add(path)
            }
            Loggers.e("add more count fragment = ${it.size}")
        }

        activity!!.intent.getStringArrayListExtra("list-video")?.let {
            for (path in it) {
                extraPathList.add(path)
            }
            Loggers.e("add more count fragment = ${it.size}")
        }
    }


    private fun initView() {
        val colSize = MediaPickActivityFE.COLS_IMAGE_LIST_SIZE * Utils.density(context!!)
        val numberCols = Utils.screenWidth(context!!) / colSize

        allMediaListView.apply {
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

    }

    private val mMediaList = ArrayList<MediaModel>()
    private fun listen() {
        mFEMediaPickViewModel.FELocalStorage.mediaResponse.observe(
            viewLifecycleOwner,
            Observer {
                Loggers.e("media size = ${it.size}")
                if (it.size == 0) {
                    mMediaList.clear()
                    mFEListMediaAdapter.clear()
                    return@Observer
                }
                val mediaList = ArrayList<MediaModel>()
                for (item in it) {
                    if (File(item.filePath).exists()) {
                        val mediaModel = MediaModel(item)
                        mediaList.add(mediaModel)
                    }

                }

                mMediaList.clear()
                mMediaList.addAll(mediaList)
                mMediaList.sort()
                mFEListMediaAdapter.setItemList(mMediaList)
                mFEListMediaAdapter.updateCount(extraPathList)
                mFEMediaPickViewModel.updateCount(extraPathList)
                extraPathList.clear()
            })

        mFEMediaPickViewModel.itemJustDeleted.observe(viewLifecycleOwner, Observer {

            onDeleteItem(it)
        })
        mFEMediaPickViewModel.itemJustPicked.observe(viewLifecycleOwner, Observer {
            mFEListMediaAdapter.updateCount(mFEMediaPickViewModel.mediaPickedCount)
        })
        mFEMediaPickViewModel.acctiveCounter.observe(viewLifecycleOwner, Observer {
            if (it == false) {
                mIsActionTrim = true
                mFEListMediaAdapter.activeCounter = false
                mFEListMediaAdapter.notifyDataSetChanged()
            }
        })
        mFEMediaPickViewModel.newMediaItem.observe(viewLifecycleOwner, Observer {
            mFEListMediaAdapter.addNewItem(it)
            updateCount(it.filePath)
        })
    }

    private fun updateCount(filePath: String) {
        for (item in mFEListMediaAdapter.itemList) {
            if (item.filePath == filePath) {
                item.count++
                break
            }
        }
        mFEListMediaAdapter.notifyDataSetChanged()
    }

    private fun onDeleteItem(mediaPickedDataModel: MediaPickedModel) {
        for (item in mFEListMediaAdapter.itemList) {
            if (item.filePath == mediaPickedDataModel.path) {
                item.count--
                break
            }
        }
        mFEListMediaAdapter.notifyDataSetChanged()
    }
}

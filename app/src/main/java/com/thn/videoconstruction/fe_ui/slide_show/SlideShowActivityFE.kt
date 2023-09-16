package com.thn.videoconstruction.fe_ui.slide_show

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.thn.videoconstruction.R
import com.thn.videoconstruction.adapter.*
import com.thn.videoconstruction.base.FEBaseSlideShow
import com.thn.videoconstruction.view_customers.FEEditTextSticker
import com.thn.videoconstruction.view_customers.FEVideoControllerView
import com.thn.videoconstruction.view_customers.model_image_view.FEDataContainer
import com.thn.videoconstruction.view_customers.model_image_view.ImageSlideGLView
import com.thn.videoconstruction.view_customers.model_image_view.FERenderer
import com.thn.videoconstruction.models.*
import com.thn.videoconstruction.fe_ui.FEHomeViewModel
import com.thn.videoconstruction.fe_ui.inapp.FEPurchaseInAppActivity
import com.thn.videoconstruction.fe_ui.pick_media.MediaPickActivityFE
import com.thn.videoconstruction.fe_ui.fe_process_video.VideosProcessActivityFE
import com.thn.videoconstruction.utils.Loggers
import com.thn.videoconstruction.utils.Utils
import kotlinx.android.synthetic.main.activity_base_edit.*
import kotlinx.android.synthetic.main.layout_change_duration_tools.view.*
import kotlinx.android.synthetic.main.layout_change_filter_tools.view.*
import kotlinx.android.synthetic.main.layout_change_transition_tools.view.*
import java.io.File

class SlideShowActivityFE : FEBaseSlideShow() {

    private lateinit var mImageGLView: ImageSlideGLView
    private lateinit var mFERenderer: FERenderer

    companion object {
        val imagePickedListKey = "Image picked list"
    }

    private val viewModel: FEHomeViewModel by viewModels {
        FEHomeViewModel.HomeViewModelFactory(this)
    }
    private var save: VideoSaves? = null

    @Volatile
    private lateinit var mFEDataContainer: FEDataContainer

    private val mImageList = ArrayList<String>()
    private var mTimer: CountDownTimer? = null
    private var mCurrentTimeMs = 0
    private var mIsPlaying = false
    private var mShouldReload = false
    private val mFESlideAdapter = FESlideAdapterFE()
    private var mTheme = Theme()
    private var mGsTransition = getRandomTransition()

    private fun getRandomTransition(): com.thn.videoconstruction.transition.FETransition {
        val randomType = Utils.TransitionType.values().random()
        Loggers.e("random type = $randomType")
        return Utils.getTransitionByType(randomType)
    }


    private val mNewThemeListAdapter = FEThemeHomeAdapterFE()

    private val mFEListTransitionAdapter = FEListTransitionAdapterFE({
        mGsTransition = it.FETransition
        performChangeTransition(it.FETransition)
    }, {
        val intent = Intent(this, FEPurchaseInAppActivity::class.java)
        startActivity(intent)
    })


    private val mImageWithLookupAdapter = FEImageLookupAdapterFE {
        doSeekById(it)
    }


    private val mLookupListAdapter = FEListLookupAdapterFE( {
        mImageWithLookupAdapter.changeLookupOfCurretItem(it)
        reloadInTime(mCurrentTimeMs)
    },{
        val intent = Intent(this, FEPurchaseInAppActivity::class.java)
        startActivity(intent)
    })

    override fun isImageSlideShow(): Boolean = true

    override fun doInitViews() {
        useDefaultMusic()
        setScreenTitle(getString(R.string.slide_show))
        needShowDialog = true
        FEImageSlideAdapter.updateData(getListItem())
        rv_edit.adapter = FEImageSlideAdapter
        rv_edit.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val themeFileName = intent.getStringExtra("themeFileName") ?: ""
        if (themeFileName.isNotEmpty()) {
            mTheme = Theme(
                Utils.themeFolderPath + "/${themeFileName}.mp4",
                Theme.ThemType.NOT_REPEAT,
                themeFileName
            )
        }
        mImageGLView = ImageSlideGLView(this, null)
        mFERenderer = FERenderer(mGsTransition)
        mImageGLView.doSetRenderer(mFERenderer)
        setGLView(mImageGLView)
        showProgressDialog()
        val imageList = intent.getStringArrayListExtra(imagePickedListKey)
        if (imageList == null || imageList.size < 1) {
            finishAfterTransition()
        } else {
            onInitSlide(imageList)
        }
        toolType = ToolType.THEME

    }

    private val FEImageSlideAdapter = FEImageSlideAdapter() {
        when (it) {
            0 -> {
                if (toolType == ToolType.DURATION || !mTouchEnable) return@FEImageSlideAdapter
                toolType = ToolType.DURATION
                showLayoutChangeDuration()
            }

            1 -> {
                if (toolType == ToolType.TRANSITION || !mTouchEnable) return@FEImageSlideAdapter
                toolType = ToolType.TRANSITION
                showLayoutChangeTransition()
            }

            4 -> {
                if (toolType == ToolType.FILTER || !mTouchEnable) return@FEImageSlideAdapter
                toolType = ToolType.FILTER
                showLayoutChangeFilter()
            }

            2 -> {
                if (toolType == ToolType.MUSIC || !mTouchEnable) return@FEImageSlideAdapter
                toolType = ToolType.MUSIC
                showLayoutChangeMusic()
            }

            3 -> {
                if (toolType == ToolType.TEXT || !mTouchEnable) return@FEImageSlideAdapter
                toolType = ToolType.TEXT
                showLayoutChangeText(this)
            }
        }
    }

    private fun onInitSlide(pathList: ArrayList<String>) {
        mImageList.clear()
        mCurrentTimeMs = 0
        mImageList.addAll(pathList)
        mFESlideAdapter.addImagePathList(mImageList)
        Thread {
            mFEDataContainer = FEDataContainer(mImageList)
            runOnUiThread {
                showLayoutChangeDuration()
                setMaxTime(mFEDataContainer.getMaxDurationMs())
                dismissProgressDialog()
                doPlayVideo()
                playVideo()
                if (mTheme.themeVideoFilePath != "none")
                    performChangeTheme(mTheme)
            }
        }.start()

    }

    private fun getListItem(): ArrayList<IconModels> {
        val listItem: ArrayList<IconModels> = arrayListOf()
        listItem.add(IconModels(2, R.drawable.ic_btn_duration, "Duration"))
        listItem.add(IconModels(1, R.drawable.ic_btn_transition, "Transition"))
        listItem.add(IconModels(3, R.drawable.ic_btn_add_music, "Music"))
        listItem.add(IconModels(4, R.drawable.type, "Text"))
        listItem.add(IconModels(5, R.drawable.filter, "Filter"))

        return listItem
    }

    private var mCurrentFrameId = 0L
    private fun playVideo() {
        mTimer = object : CountDownTimer(4000000, 40) {
            override fun onFinish() {
                start()
            }

            override fun onTick(millisUntilFinished: Long) {
                if (mIsPlaying) {
                    mCurrentTimeMs += 40
                    if (mCurrentTimeMs >= mFEDataContainer.getMaxDurationMs()) {

                        doRepeat()
                    } else {
                        updateTimeline()
                        val frameData =
                            mFEDataContainer.getFrameDataByTime(mCurrentTimeMs)
                        if (frameData.slideId != mCurrentFrameId) {
                            mFERenderer.resetData()
                            mCurrentFrameId = frameData.slideId
                        }
                        mFERenderer.changeFrameData(frameData)
                        onStick()

                    }

                }
            }
        }.start()
    }

    private var mCurrentLookupType = Utils.LookupType.NONE
    private fun onStick() {
        val position =
            mCurrentTimeMs / (mFEDataContainer.transitionTimeMs + mFEDataContainer.delayTimeMs)
        mFESlideAdapter.changeHighlightItem(position)
        mCurrentLookupType = mImageWithLookupAdapter.changeHighlightItem(position)
        mLookupListAdapter.highlightItem(mCurrentLookupType)

    }

    override fun doInitActions() {
        setRightButton(R.drawable.ic_save_vector) {
            doExportVideo()
        }

        videoControllerView.onChangeListener = object : FEVideoControllerView.OnChangeListener {
            override fun onUp(timeMilSec: Int) {
                doSeekTo(timeMilSec)
            }

            override fun onMove(progress: Float) {

            }

        }

        mImageGLView.setOnClickListener {
            if (onEditSticker) return@setOnClickListener
            if (mShouldReload) {
                mCurrentTimeMs = 0
                mShouldReload = false
            }
            if (mIsPlaying) {
                doPauseVideo()
            } else {
                doPlayVideo()
            }
        }


        mFESlideAdapter.onClickItem = {
            doSeekTo(it * (mFEDataContainer.delayTimeMs + mFEDataContainer.transitionTimeMs))
        }
    }

    override fun getCurrentVideoTimeMs(): Int = mCurrentTimeMs
    override fun performPlayVideo() {
        doPlayVideo()
    }

    override fun onBackPressed() {
        showYesNoDialog("Bạn có muốn lưu video nháp không ? ", {
            saveVideo()
            finish()

        }, {
            finish()
        })
    }

    fun saveVideo() {
        if (save == null) {
            save = VideoSaves(
                name = "project",
                pathList = mImageList.toString(),
                pathMusic = getMusicData(),
                pathText = getTextAddedList().toString(),
                mCurrentLookupType = mCurrentLookupType,
                time = mFEDataContainer.getMaxDurationMs(),
                transitionName = mGsTransition.transitionName,
                transitionCodeId = mGsTransition.transitionCodeId
            )
            viewModel.add(save!!)
        } else {
            save?.apply {
                name = "project"
                pathList = mImageList.toString()
                pathMusic = getMusicData()
                pathText = getTextAddedList().toString()
                mCurrentLookupType = mCurrentLookupType
                time = mFEDataContainer.getMaxDurationMs()
                transitionName = mGsTransition.transitionName
                transitionCodeId = mGsTransition.transitionCodeId
            }
            viewModel.update(save!!)
        }
    }


    override fun performPauseVideo() {
        doPauseVideo()
    }

    override fun getMaxDuration(): Int = mFEDataContainer.getMaxDurationMs()

    override fun performSeekTo(timeMs: Int, showProgress: Boolean) {
        Loggers.e("timeMs = $timeMs")
        if (timeMs >= mFEDataContainer.getMaxDurationMs()) {
            doRepeat()
        } else {
            doSeekTo(timeMs)
        }

    }

    override fun performSeekTo(timeMs: Int) {
        if (timeMs >= mFEDataContainer.getMaxDurationMs()) {
            doRepeat()
            Loggers.e("performSeekTo -> doRepeat()")
            return
        }
        Loggers.e("performSeekTo -> doSeekTo(timeMs)")
        doSeekTo(timeMs)
    }

    override fun isPlaying(): Boolean = mIsPlaying
    override fun getSourcePathList(): ArrayList<String> = mImageList
    override fun getScreenTitle(): String = getString(R.string.slide_show)

    override fun performExportVideo() {
        doExportVideo()
    }

    override fun performChangeVideoVolume(volume: Float) {

    }

    private fun showLayoutChangeTransition() {
        val view = View.inflate(this, R.layout.layout_change_transition_tools, null)
        showToolsActionLayout(view)

        view.imageOfSlideShowListViewInChangeTransition.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        view.imageOfSlideShowListViewInChangeTransition.adapter = mFESlideAdapter
        view.gsTransitionListView.adapter = mFEListTransitionAdapter
        view.gsTransitionListView.layoutManager =
            GridLayoutManager(this@SlideShowActivityFE, 2, GridLayoutManager.HORIZONTAL, false)
        mFEListTransitionAdapter.highlightItem(mGsTransition)
        view.icAddPhotoInChangeTransition.setOnClickListener {
            doAddMoreImage()
        }
    }

    private var addMoreAvailable = true
    private fun doAddMoreImage() {
        if (addMoreAvailable) {
            addMoreAvailable = false
            val intent = Intent(this, MediaPickActivityFE::class.java).apply {
                putExtra("action", MediaPickActivityFE.ADD_MORE_PHOTO)
                putStringArrayListExtra("list-photo", mImageList)
            }
            startActivityForResult(intent, MediaPickActivityFE.ADD_MORE_PHOTO_REQUEST_CODE)
            object : CountDownTimer(1000, 1000) {
                override fun onFinish() {
                    addMoreAvailable = true
                }

                override fun onTick(millisUntilFinished: Long) {

                }

            }.start()
        }

    }

    private fun showLayoutChangeDuration() {
        val view = View.inflate(this, R.layout.layout_change_duration_tools, null)
        showToolsActionLayout(view)
        val totalTimeMs =
            (mFEDataContainer.getCurrentDelayTimeMs() + mFEDataContainer.transitionTimeMs)
        view.changeDurationSeekBar.setCurrentDuration(totalTimeMs / 1000)
        view.totalDurationLabel.text =
            Utils.convertSecToTimeString(mFEDataContainer.getMaxDurationMs() / 1000)

        view.changeDurationSeekBar.setDurationChangeListener({
            doPauseVideo()
            doChangeDelayTime(it)
            mShouldReload = true
            videoControllerView.setCurrentDuration(0)
            view.totalDurationLabel.text =
                Utils.convertSecToTimeString(mFEDataContainer.getMaxDurationMs() / 1000)
            videoControllerView.setMaxDuration(mFEDataContainer.getMaxDurationMs())
        }, {
            doRepeat()


        })
    }

    private fun showLayoutChangeFilter() {
        doPauseVideo()
        val view = View.inflate(this, R.layout.layout_change_filter_tools, null)
        showToolsActionLayout(view)

        view.lookupListView.adapter = mLookupListAdapter
        view.lookupListView.layoutManager =
            GridLayoutManager(this@SlideShowActivityFE, 2, GridLayoutManager.HORIZONTAL, false)

        view.imageListView.adapter = mImageWithLookupAdapter.apply {
            setItemList(mFEDataContainer.getSlideList())
        }
        view.numberImageLabel.text =
            "${mImageWithLookupAdapter.itemCount} ${getString(R.string.photos)}"
        view.imageListView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        onStick()
    }

    private fun doChangeDelayTime(time: Int) {
        mFEDataContainer.delayTimeMs =
            time * 1000 - mFEDataContainer.transitionTimeMs
    }

    private fun performChangeTheme(theme: Theme) {

        doPauseVideo()
        mNewThemeListAdapter.changeCurrentThemeName(theme.themeName)
        mImageGLView.changeTheme(theme)
        doRepeat()
        object : CountDownTimer(100, 100) {
            override fun onFinish() {
                doPlayVideo()
            }

            override fun onTick(millisUntilFinished: Long) {

            }

        }.start()

    }

    private fun performChangeTransition(FETransition: com.thn.videoconstruction.transition.FETransition) {
        mImageGLView.changeTransition(FETransition)
    }

    private fun doPauseVideo() {
        if (mIsPlaying == false) return
        mIsPlaying = false
        mFERenderer.onPause()
        onPauseVideo()
    }

    private fun doPlayVideo() {
        mIsPlaying = true
        mFERenderer.onPlay()
        onPlayVideo()
    }

    private fun doSeekTo(timeMs: Int, showProgress: Boolean = true) {
        val autoPlay = mIsPlaying
        doPauseVideo()

        mFERenderer.setUpdateTexture(true)
        mCurrentTimeMs = timeMs
        mFERenderer.seekTheme(mCurrentTimeMs)
        if (showProgress)
            showProgressDialog()
        Thread {
            val frameData = mFEDataContainer.seekTo(timeMs)
            mCurrentFrameId = 1L
            mFERenderer.resetData()
            runOnUiThread {
                dismissProgressDialog()

                mFERenderer.changeFrameData(frameData)

                onSeekTo(timeMs)
                if (autoPlay) doPlayVideo()
                else doPauseVideo()
            }
        }.start()

    }

    private fun reloadInTime(timeMs: Int) {
        val autoPlay = mIsPlaying
        doPauseVideo()
        Thread {
            val frameData = mFEDataContainer.seekTo(timeMs, true)
            mCurrentTimeMs = timeMs
            runOnUiThread {
                mFERenderer.changeFrameData(frameData)
                doSeekTo(timeMs, false)
                if (autoPlay) doPlayVideo()
                else doPauseVideo()
            }
        }.start()
    }

    private fun doSeekById(id: Long) {
        doPauseVideo()
        val timeMs = mFEDataContainer.getStartTimeById(id)
        doSeekTo(timeMs)
        onStick()
    }

    private fun doRepeat() {
        doPauseVideo()
        mFEDataContainer.onRepeat()
        mFERenderer.resetData()
        doSeekTo(0)
        mCurrentTimeMs = 0
        Loggers.e("doRepeat")
        onRepeat()
    }

    override fun onPause() {
        super.onPause()
        mImageGLView.onPause()
        doPauseVideo()
    }

    override fun onResume() {
        super.onResume()
        mImageGLView.onResume()

        Thread {
            mImageList.forEach {
                if (!File(it).exists()) {
                    runOnUiThread {
                        finish()
                    }

                }
            }
        }.start()


    }

    override fun onDestroy() {
        super.onDestroy()
        mFERenderer.onDestroy()
    }

    private fun doExportVideo() {
        doPauseVideo()

        showExportDialog() { quality, ratio ->
            if (quality < 1) {
                showToast(getString(R.string.please_choose_video_quality))
            } else {
                dismissExportDialog()
                prepareForExport(quality)
            }
        }
    }

    private fun prepareForExport(quality: Int) {
        showProgressDialog()
        Thread {
            val stickerAddedForRender = ArrayList<StickerForRender>()
            for (item in getTextAddedList()) {
                val bitmap = Bitmap.createBitmap(
                    stickerContainer.width,
                    stickerContainer.height,
                    Bitmap.Config.ARGB_8888
                )
                val view = findViewById<View>(item.viewId)

                if (view is FEEditTextSticker) view.getOutBitmap(Canvas(bitmap))
                val outPath = Utils.saveStickerToTemp(bitmap)
                stickerAddedForRender.add(
                    StickerForRender(
                        outPath,
                        item.startTimeMilSec,
                        item.endTimeMilSec
                    )
                )
            }

            val imageSlideDataList = mFEDataContainer.getSlideList()
            val delayTime = mFEDataContainer.delayTimeMs
            val musicPath = getMusicData()
            val musicVolume = getMusicVolume()
            val themeData = mTheme

            val intent = Intent(this, VideosProcessActivityFE::class.java)
            Bundle().apply {
                putSerializable("stickerDataList", stickerAddedForRender)
                putSerializable("imageDataList", imageSlideDataList)
                putInt("delayTime", delayTime)
                putString("musicPath", musicPath)
                putFloat("musicVolume", musicVolume)
                putSerializable("themeData", themeData)
                putInt("videoQuality", quality)
                putSerializable("gsTransition", mGsTransition)
                intent.putExtra("bundle", this)
                intent.putExtra(
                    VideosProcessActivityFE.action,
                    VideosProcessActivityFE.renderSlideAction
                )
            }
            runOnUiThread {
                dismissProgressDialog()
                startActivity(intent)
            }
        }.start()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Loggers.e("request code = $requestCode")
        if (requestCode == MediaPickActivityFE.ADD_MORE_PHOTO_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    val pathList = it.getStringArrayListExtra("Image picked list")

                    pathList?.let { paths ->
                        Loggers.e("size result= ${paths.size}")
                        showProgressDialog()
                        Thread {
                            mFEDataContainer.setNewImageList(paths)
                            runOnUiThread {
                                mFERenderer.setUpdateTexture(true)
                                setMaxTime(mFEDataContainer.getMaxDurationMs())
                                doRepeat()
                                // doPlayVideo()
                                mFESlideAdapter.addImagePathList(paths)
                                dismissProgressDialog()
                            }

                        }.start()


                    }
                }
            }
        }
    }


}

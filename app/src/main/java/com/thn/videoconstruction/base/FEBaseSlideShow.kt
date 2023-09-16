package com.thn.videoconstruction.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.thn.videoconstruction.R
import com.thn.videoconstruction.adapter.FERecordAdapterFE
import com.thn.videoconstruction.adapter.FEAddTextStickerAdapterFE
import com.thn.videoconstruction.view_customers.FETextLayoutAdd
import com.thn.videoconstruction.view_customers.FEVideoTimeCropView
import com.thn.videoconstruction.view_customers.FEEditTextSticker
import com.thn.videoconstruction.view_customers.FEViewSticker
import com.thn.videoconstruction.models.MusicReturn
import com.thn.videoconstruction.models.TextStickerAddedModel
import com.thn.videoconstruction.modules.audio_manager.AudioManagers
import com.thn.videoconstruction.fe_ui.fe_select_music.MusicSelectActivityFE
import com.thn.videoconstruction.utils.Loggers
import com.thn.videoconstruction.utils.Utils
import com.daasuu.gpuv.player.GPUPlayerView
import kotlinx.android.synthetic.main.activity_base_edit.*
import kotlinx.android.synthetic.main.activity_base_layout.*
import kotlinx.android.synthetic.main.layout_change_music_tools.view.*
import kotlinx.android.synthetic.main.layout_change_text_tools.view.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import kotlin.math.roundToInt

abstract class FEBaseSlideShow : FEBaseActivity(), KodeinAware {
    override val kodein by closestKodein()
    override fun getContentResId(): Int = R.layout.activity_base_edit
    protected var onEditSticker = false
    private val mAudioManagers: AudioManagers by instance<AudioManagers>()
    private var mCurrentMusicData: MusicReturn? = null
    protected var toolType = ToolType.NONE
    private var mCurrentVideoVolume = 1f

    @Volatile
    protected var mTouchEnable = true


    private val mFEAddTextStickerAdapter =
        FEAddTextStickerAdapterFE(object : FEAddTextStickerAdapterFE.OnChange {
            override fun onClickTextSticker(textStickerAddedModel: TextStickerAddedModel) {
                updateChangeTextStickerLayout(textStickerAddedModel, true)
            }

        })


    private val mRecoredAdapter = FERecordAdapterFE()
    override fun initViews() {
        doInitViews()
        val screenW = Utils.screenWidth(this)
        val videoPreviewScale = Utils.videoPreviewScale()
        Loggers.e("scale = $videoPreviewScale")
        slideBgPreview.layoutParams.width = (screenW * videoPreviewScale).toInt()
        slideBgPreview.layoutParams.height = (screenW * videoPreviewScale).toInt()


        baseRootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            baseRootView.getWindowVisibleDisplayFrame(rect)
            if (baseRootView.rootView.height - (rect.bottom - rect.top) > 500) {
                FETextLayoutAdd?.translationY = -56 * Utils.density()
            } else {
                FETextLayoutAdd?.translationY = 0f
            }
        }

    }


    override fun initActions() {
        setRightButton(R.drawable.ic_save_vector) {
            performExportVideo()
            hideKeyboard()
        }
        doInitActions()
    }

    fun useDefaultMusic() {
        mAudioManagers.useDefault()
    }

    var clickSelectMusicAvailable = true
    fun showLayoutChangeMusic() {
        val view = View.inflate(this, R.layout.layout_change_music_tools, null)
        showToolsActionLayout(view)

        view.soundNameLabel.setClick {
            if (clickSelectMusicAvailable) {
                clickSelectMusicAvailable = false
                val intent = Intent(this, MusicSelectActivityFE::class.java)
                mCurrentMusicData?.let {
                    Bundle().apply {
                        putSerializable("CurrentMusic", it)
                        intent.putExtra("bundle", this)
                    }
                }

                startActivityForResult(intent, MusicSelectActivityFE.SELECT_MUSIC_REQUEST_CODE)

                object : CountDownTimer(1000, 1000) {
                    override fun onFinish() {
                        clickSelectMusicAvailable = true
                    }

                    override fun onTick(millisUntilFinished: Long) {

                    }

                }.start()
            }

        }
        view.icDelete.setOnClickListener {
            view.icDelete.visibility = View.INVISIBLE
            mAudioManagers.returnToDefault(getCurrentVideoTimeMs())
            mCurrentMusicData = null
            updateChangeMusicLayout()
        }
        updateChangeMusicLayout()
        view.musicVolumeSeekBar.setProgressChangeListener {
            mAudioManagers.setVolume(it / 100f)
        }
        view.videoVolumeSeekBar.setProgressChangeListener {
            performChangeVideoVolume(it / 100f)
            mCurrentVideoVolume = it / 100f
        }
        if (isImageSlideShow()) {
            view.videoVolumeSeekBar.visibility = View.GONE
            view.icVideoVolume.visibility = View.INVISIBLE
        }
    }

    private fun updateChangeMusicLayout() {
        val view = toolsAction.getChildAt(toolsAction.childCount - 1)
        if (mAudioManagers.getAudioName() == "none") {
            view.icDelete.visibility = View.INVISIBLE
            view.soundNameLabel.text = getString(R.string.default_)
        } else {
            view.icDelete.visibility = View.VISIBLE
            view.soundNameLabel.text = mAudioManagers.getAudioName()

        }
        view.musicVolumeSeekBar.setProgress(mAudioManagers.getVolume() * 100)
        view.videoVolumeSeekBar.setProgress(mCurrentVideoVolume * 100)
    }

    protected fun getMusicData(): String = mAudioManagers.getOutMusicPath()
    protected fun getMusicVolume(): Float = mAudioManagers.getVolume()


    fun setOffAllSticker() {
        for (index in 0 until stickerContainer.childCount) {
            val view = stickerContainer.getChildAt(index)
            if (view is FEViewSticker) {
                view.setInEdit(false)
            }
        }
    }

    private var FETextLayoutAdd: FETextLayoutAdd? = null
    fun showLayoutChangeText(context: Context) {
        setOffAllTextSticker()
        mFEAddTextStickerAdapter.setOffAll()
        val view = View.inflate(context, R.layout.layout_change_text_tools, null)
        showToolsActionLayout(view)

        view.buttonAddText.setClick {
            setOffAllTextSticker()
            getTopViewInToolAction().cropTimeViewInText.visibility = View.INVISIBLE
            getTopViewInToolAction().buttonPlayAndPauseInText.visibility = View.INVISIBLE
            showAddTextLayout(null, true, context)
        }

        view.confirmAddText.setOnClickListener {
            setOffAllTextSticker()
            mFEAddTextStickerAdapter.setOffAll()
            view.cropTimeViewInText.visibility = View.INVISIBLE
            view.buttonPlayAndPauseInText.visibility = View.INVISIBLE
            showVideoController()
        }
        if (mFEAddTextStickerAdapter.itemCount < 1) {
            view.cancelAddTextSticker.visibility = View.GONE
        }
        view.cancelAddTextSticker.setOnClickListener {
            showYesNoDialog(getString(R.string.do_you_want_delete_all_text)) {
                getTopViewInToolAction().cropTimeViewInText.visibility = View.INVISIBLE
                getTopViewInToolAction().buttonPlayAndPauseInText.visibility = View.INVISIBLE

                deleteAllTextSticker()
                showVideoController()
                hideKeyboard()
                view.cancelAddTextSticker.visibility = View.GONE
            }

        }
        view.textStickerAddedListView.apply {
            adapter = mFEAddTextStickerAdapter
            layoutManager = LinearLayoutManager(
                this@FEBaseSlideShow,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
    }

    private fun deleteAllTextSticker() {
        val listView = ArrayList<View>()
        for (i in 0 until stickerContainer.childCount) {
            val view = stickerContainer.getChildAt(i)
            if (view is FEEditTextSticker) {
                listView.add(view)
            }
        }
        listView.forEach {
            stickerContainer.removeView(it)
        }
        mFEAddTextStickerAdapter.deleteAllItem()
        setOffAllTextSticker()
        mFEAddTextStickerAdapter.setOffAll()
    }

    protected fun getTextAddedList(): ArrayList<TextStickerAddedModel> =
        mFEAddTextStickerAdapter.itemList

    private fun showAddTextLayout(
        FEEditTextSticker: FEEditTextSticker? = null,
        isEdit: Boolean = false,
        context: Context
    ) {
        mTouchEnable = false

        setOffAllTextSticker()
        mFEAddTextStickerAdapter.setOffAll()
        FEEditTextSticker?.let {
            it.changeIsAdded(false)
            stickerContainer.removeView(it)
            it.setInEdit(true)

        }

        FETextLayoutAdd = FETextLayoutAdd(context, FEEditTextSticker)
        performPauseVideo()
        fullScreenOtherLayoutContainer.apply {
            removeAllViews()
            addView(FETextLayoutAdd)
            playTranslationYAnimation(this)

        }

        setRightButton(R.drawable.ic_check) {
            FETextLayoutAdd?.hideKeyboard()
            FETextLayoutAdd?.getEditTextView()?.let {

                performAddText(it, context)
            }

        }
        setScreenTitle(getString(R.string.text_editor))
        onPauseVideo()
        if (isEdit) FETextLayoutAdd?.showKeyboard()
        activeTouch()
    }

    private fun activeTouch() {
        Thread {
            Thread.sleep(500)
            mTouchEnable = true
        }.start()
    }

    private fun performAddText(FEEditTextSticker: FEEditTextSticker, context: Context) {
        stickerContainer.addView(FEEditTextSticker)
        FEEditTextSticker.changeIsAdded(true)
        getTopViewInToolAction().cancelAddTextSticker.visibility = View.VISIBLE
        val textStickerAddedModel: TextStickerAddedModel
        if (mFEAddTextStickerAdapter.getItemBytViewId(FEEditTextSticker.id) == null) {
            textStickerAddedModel = TextStickerAddedModel(
                FEEditTextSticker.getMainText(),
                true,
                0,
                getMaxDuration(),
                FEEditTextSticker.id
            )
            mFEAddTextStickerAdapter.addNewText(textStickerAddedModel)
        } else {
            textStickerAddedModel =
                mFEAddTextStickerAdapter.getItemBytViewId(FEEditTextSticker.id)!!
            textStickerAddedModel.inEdit = true
            mFEAddTextStickerAdapter.notifyDataSetChanged()
        }

        updateChangeTextStickerLayout(textStickerAddedModel, false)
        FEEditTextSticker.deleteCallback = {
            getTopViewInToolAction().cropTimeViewInText.visibility = View.INVISIBLE
            getTopViewInToolAction().buttonPlayAndPauseInText.visibility = View.INVISIBLE



            stickerContainer.removeView(FEEditTextSticker)
            mFEAddTextStickerAdapter.deleteItem(textStickerAddedModel)
            setOffAllTextSticker()
            mFEAddTextStickerAdapter.setOffAll()
            showVideoController()
            hideKeyboard()
            if (mFEAddTextStickerAdapter.itemCount < 1) {
                getTopViewInToolAction().cancelAddTextSticker.visibility = View.GONE
            }
        }
        FEEditTextSticker.editCallback = { textSticker ->
            showAddTextLayout(textSticker, true, context)
            Loggers.e("onEdit")
        }
        hideAllViewInFullScreenLayout()
    }

    private fun updateChangeTextStickerLayout(
        textStickerAddedModel: TextStickerAddedModel,
        autoSeek: Boolean
    ) {
        val view = toolsAction.getChildAt(toolsAction.childCount - 1)
        if (autoSeek) {
            performSeekTo(textStickerAddedModel.startTimeMilSec)
        }
        view.cropTimeViewInText.visibility = View.VISIBLE
        view.buttonPlayAndPauseInText.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                changeVideoStateInAddStickerInText(view)
            }
        }

        view.cropTimeViewInText.apply {
            if (!isImageSlideShow()) {
                loadVideoImagePreview(
                    getSourcePathList(),
                    Utils.screenWidth(this@FEBaseSlideShow) - (76 * Utils.density(this@FEBaseSlideShow)).roundToInt()
                )
            } else {
                loadImage(getSourcePathList())
            }
            setMax(getMaxDuration())
            setStartAndEnd(
                textStickerAddedModel.startTimeMilSec,
                textStickerAddedModel.endTimeMilSec
            )
        }
        view.cropTimeViewInText.onChangeListener = object : FEVideoTimeCropView.OnChangeListener {
            override fun onSwipeLeft(startTimeMilSec: Float) {
                changeVideoStateToPauseInAddStickerInText(view)
                textStickerAddedModel.startTimeMilSec = startTimeMilSec.toInt()
            }

            override fun onUpLeft(startTimeMilSec: Float) {
                changeVideoStateToPauseInAddStickerInText(view)
                textStickerAddedModel.startTimeMilSec = startTimeMilSec.toInt()
                performSeekTo(textStickerAddedModel.startTimeMilSec)
            }

            override fun onSwipeRight(endTimeMilSec: Float) {
                changeVideoStateToPauseInAddStickerInText(view)
                textStickerAddedModel.endTimeMilSec = endTimeMilSec.toInt()
            }

            override fun onUpRight(endTimeMilSec: Float) {
                changeVideoStateToPauseInAddStickerInText(view)
                textStickerAddedModel.endTimeMilSec = endTimeMilSec.toInt()
            }

        }
        hideVideoController()
        setOffAllTextSticker()
        detectInEdit(textStickerAddedModel)
        changeVideoStateToPauseInAddStickerInText(view)
        onPauseVideo()
    }

    private fun changeVideoStateInAddStickerInText(view: View) {
        view.buttonPlayAndPauseInText.apply {
            if (isPlaying()) {
                setImageResource(R.drawable.ic_play)
                performPauseVideo()
            } else {
                setImageResource(R.drawable.ic_pause)
                performPlayVideo()
            }
        }
    }

    private fun changeVideoStateToPauseInAddStickerInText(view: View) {
        view.buttonPlayAndPauseInText.apply {
            setImageResource(R.drawable.ic_play)
            performPauseVideo()
        }
    }

    private fun setOffAllTextSticker() {
        for (index in 0 until stickerContainer.childCount) {
            val view = stickerContainer.getChildAt(index)
            if (view is FEEditTextSticker) {
                view.setInEdit(false)
            }
        }
    }

    private fun detectInEdit(textStickerAddedModel: TextStickerAddedModel) {
        for (index in 0 until stickerContainer.childCount) {
            val view = stickerContainer.getChildAt(index)
            if (view is FEEditTextSticker) {
                if (view.id == textStickerAddedModel.viewId) {
                    view.setInEdit(true)
                    stickerContainer.removeView(view)
                    stickerContainer.addView(view)
                    return
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        FETextLayoutAdd?.onResume()
    }


    protected fun setGLView(glSurfaceView: GLSurfaceView) {

        slideGlViewContainer.addView(
            glSurfaceView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
    }

    protected fun setExoPlayerView(playerView: GPUPlayerView) {
        videoGlViewContainer.removeAllViews()
        videoGlViewContainer.addView(
            playerView, FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
        )
    }

    protected fun releaseExoPlayerView() {
        slideGlViewContainer.removeAllViews()

    }

    protected fun removeGLiew() {
        slideGlViewContainer.removeAllViews()
    }

    fun updateTimeline() {
        videoControllerView.setCurrentDuration(getCurrentVideoTimeMs())
        checkInTime(getCurrentVideoTimeMs())
    }

    protected fun checkInTime(timeMs: Int) {
        checkTextInTime(timeMs)
    }


    private fun checkTextInTime(timeMilSec: Int) {
        for (item in getTextAddedList()) {
            if (timeMilSec >= item.startTimeMilSec && timeMilSec <= item.endTimeMilSec) {
                val view = findViewById<View>(item.viewId)
                if (view.visibility != View.VISIBLE) view.visibility = View.VISIBLE
            } else {
                val view = findViewById<View>(item.viewId)
                if (view.visibility == View.VISIBLE) view.visibility = View.INVISIBLE
            }
        }
    }

    fun setMaxTime(timeMs: Int) {
        videoControllerView.setMaxDuration(timeMs)
    }

    protected fun showToolsActionLayout(view: View) {
        showVideoController()
        setOffAllSticker()
        setOffAllTextSticker()

        mFEAddTextStickerAdapter.setOffAll()
        toolsAction.removeAllViews()
        toolsAction.addView(
            view,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        playTranslationYAnimation(view)
    }

    protected fun onPauseVideo() {
        if (toolType == ToolType.TEXT) {
            val view = getTopViewInToolAction()
            view.buttonPlayAndPauseInText.setImageResource(R.drawable.ic_play)
        }
        mAudioManagers.pauseAudio()
        icPlay.visibility = View.VISIBLE
    }

    protected fun onPlayVideo() {
        if (toolType == ToolType.TEXT) {
            val view = getTopViewInToolAction()
            view.buttonPlayAndPauseInText.setImageResource(R.drawable.ic_pause)
        }
        mAudioManagers.playAudio()
        icPlay.visibility = View.GONE
    }

    protected fun onSeekTo(timeMs: Int) {
        Loggers.e("seek to $timeMs")
        mAudioManagers.seekTo(timeMs)
        updateTimeline()
    }

    protected fun onRepeat() {
        mAudioManagers.repeat()
    }

    private fun hideVideoController() {
        onEditSticker = true
        performPauseVideo()
        videoControllerView.visibility = View.GONE
        icPlay.alpha = 0f
    }

    private fun showVideoController() {
        onEditSticker = false
        videoControllerView.visibility = View.VISIBLE
        icPlay.alpha = 1f
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         if (resultCode == Activity.RESULT_OK && requestCode == MusicSelectActivityFE.SELECT_MUSIC_REQUEST_CODE) {
            if (data != null) {
                val bundle = data.getBundleExtra("bundle")
                val musicReturn =
                    (bundle?.getSerializable(MusicSelectActivityFE.MUSIC_RETURN_DATA_KEY)) as MusicReturn
                changeMusicData(musicReturn)
            }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    private fun changeMusicData(musicReturn: MusicReturn) {

        if (mCurrentMusicData == null || mCurrentMusicData?.audioFilePath != musicReturn.audioFilePath || mCurrentMusicData?.startOffset != musicReturn.startOffset || mCurrentMusicData?.length != musicReturn.length) {
            mCurrentMusicData = musicReturn
            mAudioManagers.changeAudio(musicReturn, getCurrentVideoTimeMs())
        }

        updateChangeMusicLayout()
    }

    private fun getTopViewInToolAction(): View = toolsAction.getChildAt(toolsAction.childCount - 1)

    abstract fun isImageSlideShow(): Boolean

    abstract fun doInitViews()
    abstract fun doInitActions()
    abstract fun getCurrentVideoTimeMs(): Int

    abstract fun performPlayVideo()
    abstract fun performPauseVideo()
    abstract fun getMaxDuration(): Int
    abstract fun performSeekTo(timeMs: Int)
    abstract fun performSeekTo(timeMs: Int, showProgress: Boolean)
    abstract fun isPlaying(): Boolean
    abstract fun getSourcePathList(): ArrayList<String>
    abstract fun getScreenTitle(): String
    abstract fun performExportVideo()
    enum class ToolType {
        NONE, TRIM, EFFECT, THEME, TRANSITION, DURATION, MUSIC, STICKER, TEXT, FILTER, RECORDER
    }

    abstract fun performChangeVideoVolume(volume: Float)
    private fun hideKeyboard() {

        FETextLayoutAdd?.hideKeyboard()
    }

    override fun onBackPressed() {

        FETextLayoutAdd?.hideKeyboard()
        when {
            otherLayoutContainer.childCount > 0 -> {
                otherLayoutContainer.removeAllViews()
                return
            }

            fullScreenOtherLayoutContainer.childCount > 0 -> {
                showYesNoDialog(getString(R.string.do_you_want_to_save), {

                    if (toolType == ToolType.TEXT) {
                        FETextLayoutAdd?.getEditTextView()?.let {
                            performAddText(it, it.context)
                        }
                    }
                }, {
                    hideAllViewInFullScreenLayout()
                    if (toolType == ToolType.TEXT) {
                        FETextLayoutAdd?.onCancelEdit()?.let {
                            performAddText(it, it.context)
                            Loggers.e("on cancel edit text")

                        }

                        FETextLayoutAdd = null
                    }
                })

                return
            }

            else -> {
                super.onBackPressed()
            }
        }
    }

    private fun hideAllViewInFullScreenLayout() {

        fullScreenOtherLayoutContainer.removeAllViews()
        setScreenTitle(screenTitle())
        setRightButton(R.drawable.ic_save_vector) {
            performExportVideo()
            hideKeyboard()
        }
        setScreenTitle(getScreenTitle())

    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard()
    }

    protected fun setOffAllStickerAndText() {
        setOffAllSticker()
        setOffAllTextSticker()
        mFEAddTextStickerAdapter.setOffAll()
    }


}
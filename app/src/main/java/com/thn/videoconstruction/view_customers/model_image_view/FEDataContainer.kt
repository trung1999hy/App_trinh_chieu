package com.thn.videoconstruction.view_customers.model_image_view

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import com.thn.videoconstruction.utils.Utils
import java.io.File
import kotlin.math.max

class FEDataContainer(val mImageList: ArrayList<String> = ArrayList()) {

    private val mFESlideDataList = ArrayList<FESlideData>()

    @Volatile
    var delayTimeMs = 2500
    val transitionTimeMs = 500
    private val fps = 25
    val imageList get() = mImageList
    private var mCurrentSlideIndex = 0

    @Volatile
    private lateinit var mCurrentBitmap: Bitmap

    @Volatile
    private lateinit var mNextBitmap: Bitmap

    @Volatile
    private lateinit var mBackupBitmap: Bitmap

    private var mFromLookupBitmap = Utils.getBitmapFromAsset("lut/NONE.jpg")
    private var mToLookupBitmap = Utils.getBitmapFromAsset("lut/NONE.jpg")

    init {
        initData()
    }

    fun initData() {
        mFESlideDataList.clear()
        if (mImageList.size > 0) {
            for (index in 0 until mImageList.size) {
                val nextImagePath = if (index < mImageList.size - 1) {
                    mImageList[index + 1]
                } else {
                    mImageList[mImageList.size - 1]
                }
                val FESlideData =
                    FESlideData(
                        View.generateViewId() + System.currentTimeMillis(),
                        mImageList[index],
                        nextImagePath
                    )
                mFESlideDataList.add(FESlideData)
            }

            val firstSlide = mFESlideDataList[0]
            mCurrentBitmap = getBitmapResized(firstSlide.fromImagePath)
            mCurrentSlideId = firstSlide.slideId
            val secondSlide = mFESlideDataList[1]
            mNextBitmap = getBitmapResized(secondSlide.fromImagePath)

        }
        updateBackupSlide()

    }

    private val noneLutPath = "lut/NONE.jpg"
    fun onRepeat() {
        mCurrentSlideId = 1L
        val firstSlide = mFESlideDataList[0]
        mCurrentBitmap = getBitmapResized(firstSlide.fromImagePath)
        mNextBitmap = getBitmapResized(firstSlide.toImagePath)
        mCurrentSlideId = firstSlide.slideId
        mCurrentSlideIndex = 0
        mFromLookupBitmap =
            Utils.getBitmapFromAsset("lut/${mFESlideDataList[mCurrentSlideIndex].lookupType}.jpg")

        mToLookupBitmap = if (mCurrentSlideIndex < mFESlideDataList.size - 1)
            Utils.getBitmapFromAsset("lut/${mFESlideDataList[mCurrentSlideIndex + 1].lookupType}.jpg")
        else Utils.getBitmapFromAsset(noneLutPath)

    }

    private fun getBitmapResized(path: String): Bitmap {
        val imageFile = File(path)
        return if (imageFile.exists()) {
            val outName = "${imageFile.parentFile?.name}${imageFile.name}"
            val tempImageFile = File("${Utils.tempImageFolderPath}/$outName")
            if (tempImageFile.exists()) {
                Utils.getBitmapFromFilePath(tempImageFile.absolutePath)
            } else {
                val resizedBitmap = drawResizedBitmap(path)
                Utils.saveBitmapToTempData(resizedBitmap, outName)
                resizedBitmap
            }
        } else {
            Utils.getBlackBitmap()
        }


    }

    private var mCurrentSlideId = 1L
    fun getFrameDataByTime(timeMs: Int, needReload: Boolean = false): FEFrame {

        var slideIndex = ((timeMs) / (delayTimeMs + transitionTimeMs))
        if (slideIndex == -1) slideIndex = mFESlideDataList.size - 1
        val targetSlide = mFESlideDataList[slideIndex]
        mCurrentSlideIndex = slideIndex

        val delta = timeMs - slideIndex * (delayTimeMs + transitionTimeMs)
        var progress: Float
        progress = when {
            delta in 0..delayTimeMs -> 0f
            slideIndex == mFESlideDataList.size - 1 -> 0f
            else -> {
                ((delta - delayTimeMs).toFloat() / transitionTimeMs)
            }
        }

        calculateZoom(timeMs)

        if (needReload || targetSlide.slideId != mCurrentSlideId) {
            if (targetSlide.slideId != mCurrentSlideId) {
                val f = zoom
                zoom = zoom1
                zoom1 = f

                val f2 = zoomF
                zoomF = zoomT
                zoomT = f2
            }
            mFromLookupBitmap =
                Utils.getBitmapFromAsset("lut/${mFESlideDataList[mCurrentSlideIndex].lookupType}.jpg")

            mToLookupBitmap = if (mCurrentSlideIndex < mFESlideDataList.size - 1)
                Utils.getBitmapFromAsset("lut/${mFESlideDataList[mCurrentSlideIndex + 1].lookupType}.jpg")
            else Utils.getBitmapFromAsset(noneLutPath)

            mCurrentBitmap = getBitmapResized(targetSlide.fromImagePath)
            mNextBitmap = getBitmapResized(targetSlide.toImagePath)
            mCurrentSlideId = targetSlide.slideId

        }
        return FEFrame(
            mCurrentBitmap,
            mNextBitmap,
            mFromLookupBitmap,
            mToLookupBitmap,
            progress,
            mCurrentSlideId,
            zoom,
            zoom1
        )
    }

    private var zoom = 1f
    private var zoom1 = 1f

    private val zoomDuration = 5000

    private var zoomF = 1f
    private var zoomT = 0.95f

    private fun calculateZoom(timeMs: Int) {


    }

    fun seekTo(timeMs: Int, needReload: Boolean = false): FEFrame {

        var slideIndex = ((timeMs) / (delayTimeMs + transitionTimeMs))

        if (slideIndex == mFESlideDataList.size) slideIndex = 0
        mCurrentSlideIndex = slideIndex
        val targetSlide = mFESlideDataList[slideIndex]

        val surplus = timeMs % (delayTimeMs + transitionTimeMs)
        val progress: Float
        progress = if (surplus <= delayTimeMs) {
            0f
        } else {
            (surplus - delayTimeMs) / (transitionTimeMs.toFloat())
        }

        calculateZoom(timeMs)
        if (needReload || targetSlide.slideId != mCurrentSlideId) {

            mFromLookupBitmap =
                Utils.getBitmapFromAsset("lut/${mFESlideDataList[mCurrentSlideIndex].lookupType}.jpg")
            mToLookupBitmap = if (mCurrentSlideIndex < mFESlideDataList.size - 1)
                Utils.getBitmapFromAsset("lut/${mFESlideDataList[mCurrentSlideIndex + 1].lookupType}.jpg")
            else
                Utils.getBitmapFromAsset(noneLutPath)
            mCurrentBitmap = getBitmapResized(targetSlide.fromImagePath)
            mNextBitmap = getBitmapResized(targetSlide.toImagePath)
            mCurrentSlideId = targetSlide.slideId

        }
        return FEFrame(
            mCurrentBitmap,
            mNextBitmap,
            mFromLookupBitmap,
            mToLookupBitmap,
            progress,
            mCurrentSlideId,
            zoom,
            zoom1
        )
    }

    private fun updateBackupSlide() {
        Thread {

            for (index in 0 until mFESlideDataList.size) {
                val item = mFESlideDataList[index]


                getBitmapResized(item.fromImagePath)


            }


        }.start()
    }

    private fun drawResizedBitmap(imagePath: String): Bitmap {
        val screenW = 1080
        val rawBitmap = Utils.getBitmapFromFilePath(imagePath)
        val outBitmapSize: Int
        if (rawBitmap.width < screenW && rawBitmap.height < screenW) {
            outBitmapSize = max(rawBitmap.width, rawBitmap.height)
        } else {
            outBitmapSize = screenW
        }

        val blurBgBitmap = Utils.blurBitmapV2(
            Utils.resizeMatchBitmap(
                rawBitmap,
                outBitmapSize.toFloat() + 100
            ), 20
        )

        val rawBitmapResized = Utils.resizeWrapBitmap(rawBitmap, outBitmapSize.toFloat())

        val resizedBitmapWithBg =
            Bitmap.createBitmap(outBitmapSize, outBitmapSize, Bitmap.Config.ARGB_8888)

        Canvas(resizedBitmapWithBg).apply {
            drawARGB(255, 0, 0, 0)
            blurBgBitmap?.let {
                drawBitmap(
                    it,
                    (outBitmapSize - it.width) / 2f,
                    (outBitmapSize - it.height) / 2f,
                    null
                )
            }
            drawBitmap(
                rawBitmapResized,
                (outBitmapSize - rawBitmapResized.width) / 2f,
                (outBitmapSize - rawBitmapResized.height) / 2f,
                null
            )
        }

        return resizedBitmapWithBg
    }

    fun getMaxDurationMs(): Int {
        return (delayTimeMs + transitionTimeMs) * mFESlideDataList.size
    }

    fun getStartTimeById(slideId: Long): Int {

        for (index in 0 until mFESlideDataList.size) {
            val item = mFESlideDataList[index]
            if (slideId == item.slideId) {
                return index * (delayTimeMs + transitionTimeMs)
            }
        }
        return 0
    }

    fun getCurrentDelayTimeMs(): Int = delayTimeMs

    fun getSlideList(): ArrayList<FESlideData> = mFESlideDataList

    fun prepareForRender(FESlideDataList: ArrayList<FESlideData>, delayTime: Int) {
        mFESlideDataList.clear()
        mFESlideDataList.addAll(FESlideDataList)
        mCurrentSlideIndex = 0
        this.delayTimeMs = delayTime
        val firstSlide = mFESlideDataList[0]
        mCurrentBitmap = getBitmapResized(firstSlide.fromImagePath)
        mNextBitmap = getBitmapResized(firstSlide.toImagePath)
        mCurrentSlideId = firstSlide.slideId
        val secondSlide = mFESlideDataList[1]
        mBackupBitmap = getBitmapResized(secondSlide.toImagePath)

        mFromLookupBitmap =
            Utils.getBitmapFromAsset("lut/${mFESlideDataList[mCurrentSlideIndex].lookupType}.jpg")

        mToLookupBitmap = if (mCurrentSlideIndex < mFESlideDataList.size - 1)
            Utils.getBitmapFromAsset("lut/${mFESlideDataList[mCurrentSlideIndex + 1].lookupType}.jpg")
        else Utils.getBitmapFromAsset(noneLutPath)
    }

    fun getFrameByTimeForRender(timeMs: Int): FEFrame {
        var slideIndex = ((timeMs - 1) / (delayTimeMs + transitionTimeMs))

        if (slideIndex == mFESlideDataList.size) slideIndex--
        val targetSlide = mFESlideDataList[slideIndex]
        mCurrentSlideIndex = slideIndex
        val surplus = (timeMs - 1) % (delayTimeMs + transitionTimeMs)
        val progress: Float
        progress = if (surplus <= delayTimeMs) {
            0f
        } else {
            (surplus + 1 - delayTimeMs) / transitionTimeMs.toFloat()
        }
        calculateZoom(timeMs)
        if (targetSlide.slideId != mCurrentSlideId) {

            val f = zoom
            zoom = zoom1
            zoom1 = f

            val f2 = zoomF
            zoomF = zoomT
            zoomT = f2

            mFromLookupBitmap =
                Utils.getBitmapFromAsset("lut/${mFESlideDataList[mCurrentSlideIndex].lookupType}.jpg")

            mToLookupBitmap = if (mCurrentSlideIndex < mFESlideDataList.size - 1)
                Utils.getBitmapFromAsset("lut/${mFESlideDataList[mCurrentSlideIndex + 1].lookupType}.jpg")
            else Utils.getBitmapFromAsset(noneLutPath)

            mCurrentBitmap = getBitmapResized(targetSlide.fromImagePath)
            mNextBitmap = getBitmapResized(targetSlide.toImagePath)
            mCurrentSlideId = targetSlide.slideId


        }

        return FEFrame(
            mCurrentBitmap,
            mNextBitmap,
            mFromLookupBitmap,
            mToLookupBitmap,
            progress,
            mCurrentSlideId,
            zoom,
            zoom1
        )
    }

    fun setNewImageList(pathList: ArrayList<String>) {
        mImageList.clear()
        mImageList.addAll(pathList)
        initData()
    }

    fun changeCurrentSlideId(id: Long) {
        mCurrentSlideId = id
    }
}
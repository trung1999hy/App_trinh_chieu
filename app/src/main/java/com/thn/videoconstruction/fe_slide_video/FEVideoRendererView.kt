package com.thn.videoconstruction.fe_slide_video

import android.graphics.Point
import android.media.MediaMetadataRetriever
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Size
import com.thn.videoconstruction.view_customers.fe_renderer_slider.FEViewDrawer
import com.thn.videoconstruction.fe_slide_theme.FEThemeSlideDrawer
import com.thn.videoconstruction.models.Theme
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class FEVideoRendererView(val mVideoSlideGLView: FEVideoSlideGLView)  : GLSurfaceView.Renderer {



    private var mThemeDrawer: FEThemeSlideDrawer? = null
    private var mDrawer: FEViewDrawer? = null

    private var mTheme = Theme()
    val themeData get() = mTheme

    private val mFEVideoDataViewList = ArrayList<FEVideoDataView>()

    private var mTotalDuration = 0L

    init {
        mThemeDrawer = FEThemeSlideDrawer(mTheme)
        mDrawer = FEViewDrawer()
    }

    fun initData(videoList: ArrayList<String>) {
        mFEVideoDataViewList.clear()
        for(path in videoList) {
            val videoDataForSlide = getDataForSlide(path)
            mFEVideoDataViewList.add(videoDataForSlide)
            mTotalDuration+=(videoDataForSlide.endOffset-videoDataForSlide.startOffset)
        }
    }

    fun playByProgress(percent:Float) {
        val tartTime = percent*mTotalDuration

    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        mDrawer?.prepare()
        mThemeDrawer?.prepare()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

    }

    override fun onDrawFrame(gl: GL10?) {

        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        mDrawer?.drawFrame()
        mThemeDrawer?.drawFrame()
    }

    fun changeTheme(theme: Theme) {
        mTheme = theme
        mThemeDrawer?.changeTheme(theme)
    }

    fun changeVideo(videoPath:String, size: Size, point:Point) {
        mDrawer?.changeVideo(videoPath, size, point)
    }

    private fun getDataForSlide(videoPath: String):FEVideoDataView {
        val media = MediaMetadataRetriever()
        media.setDataSource(videoPath)
        val rotation = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toInt()
        val videoW:Int
        val videoH:Int
        if (rotation == 90) {
            videoW = (media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT) ?:"-1").toInt()
            videoH = (media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) ?: "-1").toInt()
        } else {
            videoW = (media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) ?: "-1").toInt()
            videoH = (media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT) ?: "-1").toInt()
        }
        val size =Size(videoW, videoH)
        val length = (media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION) ?: "-1").toLong()
        return FEVideoDataView(videoPath, 0, length, size)
    }

}
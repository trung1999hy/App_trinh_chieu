package com.thn.videoconstruction.fe_slide_show.fe_slide_gl

import android.opengl.GLES20
import com.thn.videoconstruction.application.FEMainApp
import com.thn.videoconstruction.models.SlideShow
import com.thn.videoconstruction.fe_slide_show.FESlideShowDrawers
import com.thn.videoconstruction.utils.Utils

class FEFERendererSlideShowImageView : FERendererSlideShowView() {

    private var mFESlideShowDrawers: FESlideShowDrawers? = null
    private lateinit var mSlideShow: SlideShow

    init {
        mFESlideShowDrawers = FESlideShowDrawers()
    }

    override fun initData(filePathList: ArrayList<String>) {
        mSlideShow = SlideShow(filePathList)
    }

    override fun performPrepare() {
        mFESlideShowDrawers?.prepare()
    }

    override fun performDraw() {
        mFESlideShowDrawers?.drawFrame()
    }

    override fun onChangeTheme() {
        mFESlideShowDrawers?.setUpdateTexture(true)
    }

    override fun changeTransition(FETransition: com.thn.videoconstruction.transition.FETransition) {
        val handle = Utils.compileShader(
            GLES20.GL_FRAGMENT_SHADER,
            getFragmentShader(FETransition.transitionCodeId)
        )
        mFESlideShowDrawers?.changeTransition(FETransition, handle)
    }

    override fun drawSlideByTime(timeMilSec: Int) {
        val frameData = mSlideShow.getFrameByVideoTime(timeMilSec)
        mFESlideShowDrawers?.changeFrameData(frameData)
    }

    override fun getDuration(): Int {
        return mSlideShow.getTotalDuration()
    }

    override fun getDelayTimeSec(): Int {
        return mSlideShow.delayTimeSec
    }

    override fun setDelayTimeSec(timeSec: Int): Boolean {
        if (mSlideShow.delayTimeSec == timeSec) return false
        mSlideShow.updateTime(timeSec)
        return true
    }

    override fun repeat() {
        mSlideShow.repeat()
    }

    private fun getFragmentShader(transitionCodeId: Int): String {
        val transitionCode =
            Utils.readTextFileFromRawResource(FEMainApp.getContext(), transitionCodeId)
        return "precision mediump float;\n" +
                "varying vec2 _uv;\n" +
                "uniform sampler2D from, to;\n" +
                "uniform float progress, ratio, _fromR, _toR, _zoomProgress;\n" +
                "\n" +
                "vec4 getFromColor(vec2 uv){\n" +
                "    return texture2D(from, vec2(1.0, -1.0)*uv*_zoomProgress);\n" +
                "}\n" +
                "vec4 getToColor(vec2 uv){\n" +
                "    return texture2D(to, vec2(1.0, -1.0)*uv*_zoomProgress);\n" +
                "}" +
                transitionCode +
                "void main(){gl_FragColor=transition(_uv);}"
    }

}
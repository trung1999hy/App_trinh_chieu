package com.thn.videoconstruction.view_customers.fe_renderer_slider

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.thn.videoconstruction.application.FEMainApp
import com.thn.videoconstruction.models.FrameDatassss
import com.thn.videoconstruction.models.Theme
import com.thn.videoconstruction.utils.Utils


class FEViewGL : GLSurfaceView {

    private var mRenderer: FeViewRenderer? =
        null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attributes: AttributeSet) : super(context, attributes) {
        init()
    }

    private fun init() {
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
    }

    fun setSlideRenderer(render: FeViewRenderer) {
        mRenderer = render
        setRenderer(render)
    }

    fun drawSlide(frameDatassss: FrameDatassss) {
        mRenderer?.changeFrameData(frameDatassss)
    }

    fun changeTransition(FETransition: com.thn.videoconstruction.transition.FETransition) {
        queueEvent(Runnable {
            val handle = Utils.compileShader(
                GLES20.GL_FRAGMENT_SHADER,
                getFragmentShader(FETransition.transitionCodeId)
            )
            mRenderer?.changeTransition(FETransition, handle)
        })
    }

    fun changeTheme(theme: Theme) {
        queueEvent(Runnable {
            mRenderer?.changeTheme(theme)

        })
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
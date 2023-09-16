package com.thn.videoconstruction.fe_slide_show

import android.opengl.GLES20
import com.thn.videoconstruction.application.FEMainApp
import com.thn.videoconstruction.models.FrameDatassss
import com.thn.videoconstruction.utils.Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class FESlideShowDrawers {
    private var mFrameDatassss: FrameDatassss? = null

    private val mBytesPerFloat = 4

    private var mUpdateTexture = true

    private val mVertexData = floatArrayOf(
        -1f, -1f, 0.0f,
        -1f, 1f, 0.0f,
        1f, -1f, 0.0f,
        -1f, 1f, 0.0f,
        1f, 1f, 0.0f,
        1f, -1f, 0.0f
    )
    private var mVertexBuffer: FloatBuffer

    private val mPositionDataSize = 3
    private val mColorDataSize = 4
    private val mNormalDataSize = 3
    private val mTextureCoordinateDataSize = 2


    private var mProgramHandle = 0

    private var mTextureFromDataHandle = 0
    private var mTextureToDataHandle = 0

    private var mTransition = com.thn.videoconstruction.transition.FETransition()

    private var mFragmentShaderHandle = 0
    private var mVertexShaderHandle = 0

    init {
        mVertexBuffer = ByteBuffer.allocateDirect(mVertexData.size * mBytesPerFloat)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mVertexBuffer.put(mVertexData).position(0)
    }

    fun prepare() {
        mVertexShaderHandle = Utils.compileShader(GLES20.GL_VERTEX_SHADER, getVertexShader())
        mFragmentShaderHandle = Utils.compileShader(
            GLES20.GL_FRAGMENT_SHADER,
            getFragmentShader(mTransition.transitionCodeId)
        )

        mProgramHandle = Utils.createAndLinkProgram(
            mVertexShaderHandle, mFragmentShaderHandle,
            arrayOf("_p")
        )
    }

    fun prepare(FETransition: com.thn.videoconstruction.transition.FETransition) {
        mTransition = FETransition
        mVertexShaderHandle = Utils.compileShader(GLES20.GL_VERTEX_SHADER, getVertexShader())
        mFragmentShaderHandle = Utils.compileShader(
            GLES20.GL_FRAGMENT_SHADER,
            getFragmentShader(mTransition.transitionCodeId)
        )

        mProgramHandle = Utils.createAndLinkProgram(
            mVertexShaderHandle, mFragmentShaderHandle,
            arrayOf("_p")
        )
    }

    fun drawFrame() {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        mFrameDatassss?.let {
            if (mUpdateTexture) {
                GLES20.glDeleteTextures(
                    2,
                    intArrayOf(mTextureFromDataHandle, mTextureToDataHandle),
                    0
                )
                mTextureFromDataHandle = Utils.loadTexture(it.fromBitmap)
                mTextureToDataHandle = Utils.loadTexture(it.toBitmap)
                mUpdateTexture = false
            }
            drawSlide(it)
        }
    }

    private fun drawSlide(frameDatassss: FrameDatassss) {
        GLES20.glUseProgram(mProgramHandle)

        val positionAttr = GLES20.glGetAttribLocation(mProgramHandle, "_p")
        GLES20.glEnableVertexAttribArray(positionAttr)
        GLES20.glVertexAttribPointer(positionAttr, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer)

        val textureFromLocate = GLES20.glGetUniformLocation(mProgramHandle, "from")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureFromDataHandle)
        GLES20.glUniform1i(textureFromLocate, 0)

        val textureToLocate = GLES20.glGetUniformLocation(mProgramHandle, "to")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureToDataHandle)
        GLES20.glUniform1i(textureToLocate, 1)

        val zoomProgressLocation = GLES20.glGetUniformLocation(mProgramHandle, "_zoomProgress")
        GLES20.glUniform1f(zoomProgressLocation, frameDatassss.zoomProgress)

        val progressLocation = GLES20.glGetUniformLocation(mProgramHandle, "progress")
        GLES20.glUniform1f(progressLocation, frameDatassss.progress)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)

    }

    fun changeFrameData(frameDatassss: FrameDatassss?) {
        if (mFrameDatassss?.slideId == frameDatassss?.slideId) {
            mUpdateTexture = false
            mFrameDatassss = frameDatassss
        } else {
            mFrameDatassss = frameDatassss
            mUpdateTexture = true
        }
    }

    fun setUpdateTexture(b: Boolean) {
        mUpdateTexture = b
    }

    fun changeTransition(
        FETransition: com.thn.videoconstruction.transition.FETransition,
        fragmentShaderHandle: Int
    ) {
        if (mTransition.transitionCodeId == FETransition.transitionCodeId) return
        mTransition = FETransition
        mFragmentShaderHandle = fragmentShaderHandle
        GLES20.glDeleteProgram(mProgramHandle)
        mProgramHandle = Utils.createAndLinkProgram(
            mVertexShaderHandle, mFragmentShaderHandle,
            arrayOf("_p")
        )
    }

    private fun getVertexShader(): String {
        return "attribute vec2 _p;\n" +
                "varying vec2 _uv;\n" +
                "void main() {\n" +
                "gl_Position = vec4(_p,0.0,1.0);\n" +
                "_uv = vec2(0.5, 0.5) * (_p+vec2(1.0, 1.0));\n" +
                "}"
    }

    private fun getFragmentShader(transitionCodeId: Int): String {

        val transitionCode = Utils.readTextFileFromRawResource(
            FEMainApp.getContext(),
            transitionCodeId
        )

        return "precision mediump float;\n" +
                "varying vec2 _uv;\n" +
                "uniform sampler2D from, to;\n" +
                "uniform float progress, ratio, _fromR, _toR;\n" +
                "uniform highp float _zoomProgress;" +
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
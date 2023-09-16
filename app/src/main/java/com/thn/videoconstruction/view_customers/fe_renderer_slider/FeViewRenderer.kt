package com.thn.videoconstruction.view_customers.fe_renderer_slider

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.thn.videoconstruction.fe_slide_show.FESlideShowDrawers
import com.thn.videoconstruction.models.FrameDatassss
import com.thn.videoconstruction.fe_slide_theme.FEThemeSlideDrawer
import com.thn.videoconstruction.models.Theme
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class FeViewRenderer(val theme: Theme) : GLSurfaceView.Renderer {

    private var mFESlideShowDrawers:FESlideShowDrawers? = null
    private var mFEThemeSlideDrawer:FEThemeSlideDrawer? = null

    init {
        mFESlideShowDrawers = FESlideShowDrawers()
        mFEThemeSlideDrawer = FEThemeSlideDrawer(theme)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        mFEThemeSlideDrawer?.prepare()
        mFESlideShowDrawers?.prepare()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

    }

    override fun onDrawFrame(gl: GL10?) {

        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        mFESlideShowDrawers?.drawFrame()
        mFEThemeSlideDrawer?.drawFrame()
    }

    fun changeTheme(theme: Theme) {
        mFEThemeSlideDrawer?.changeTheme(theme)
        mFESlideShowDrawers?.setUpdateTexture(true)
    }

    fun changeFrameData(frameDatassss: FrameDatassss) {
        mFESlideShowDrawers?.changeFrameData(frameDatassss)
    }

    fun changeTransition(FETransition: com.thn.videoconstruction.transition.FETransition, fragmentShaderHandle: Int) {
        mFESlideShowDrawers?.changeTransition(FETransition, fragmentShaderHandle)
    }
}
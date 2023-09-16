package com.thn.videoconstruction.view_customers.model_image_view

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.thn.videoconstruction.models.Theme

class ImageSlideGLView(context: Context, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {

    private lateinit var mFERenderer: com.thn.videoconstruction.view_customers.model_image_view.FERenderer

    init {
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
    }

    fun doSetRenderer(FERenderer: com.thn.videoconstruction.view_customers.model_image_view.FERenderer) {
        mFERenderer = FERenderer

        setRenderer(FERenderer)
    }


    fun changeTransition(FETransition: com.thn.videoconstruction.transition.FETransition) {

        queueEvent(Runnable {
            mFERenderer.changeTransition(FETransition)
        })
    }




    fun changeTheme(theme: Theme) {
        queueEvent(Runnable {
            mFERenderer.changeTheme(theme)
        })
    }

}
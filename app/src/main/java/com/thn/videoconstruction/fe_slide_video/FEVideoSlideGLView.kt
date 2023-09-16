package com.thn.videoconstruction.fe_slide_video

import android.content.Context
import android.graphics.Point
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Size
import com.thn.videoconstruction.models.Theme

class FEVideoSlideGLView (context: Context, attributes: AttributeSet?) : GLSurfaceView(context, attributes) {

    private lateinit var mFEVideoRendererView: FEVideoRendererView


    init {
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
    }

    fun performSetRenderer(FEVideoRendererView: FEVideoRendererView) {
        mFEVideoRendererView = FEVideoRendererView
        setRenderer(mFEVideoRendererView)
    }


    fun changeTheme(theme: Theme) {
        queueEvent(Runnable {
            mFEVideoRendererView.changeTheme(theme)
        })
    }

    fun changeVideo(FEVideoDataView: FEVideoDataView) {
        queueEvent(Runnable {
            val size = FEVideoDataView.size
            val viewPortX:Int
            val viewPortY:Int
            val viewPortW:Int
            val viewPortH:Int
            if(size.width > size.height) {
                viewPortW = width
                viewPortH = width*size.height/size.width
                viewPortY = (height-viewPortH)/2
                viewPortX = 0
            } else {
                viewPortH = height
                viewPortW = height*size.width/size.height
                viewPortY = 0
                viewPortX = (width-viewPortW)/2
            }
            mFEVideoRendererView.changeVideo(FEVideoDataView.path, Size(viewPortW, viewPortH), Point(viewPortX, viewPortY))
        })

    }




    fun seekTo(timeMilSec:Int) {

    }

}
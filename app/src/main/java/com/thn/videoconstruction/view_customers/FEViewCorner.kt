package com.thn.videoconstruction.view_customers

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.thn.videoconstruction.utils.Utils

class FEViewCorner(context: Context, attrs: AttributeSet?) :
    AppCompatImageView(context, attrs) {
    private var mCorner = 6f
    private fun getClipPath(): Path {
        val cornerRadius = mCorner * Utils.density(context)
        val path = Path()
        path.reset()
        path.addRoundRect(
            RectF(0f, 0f, width.toFloat(), height.toFloat()),
            cornerRadius,
            cornerRadius,
            Path.Direction.CW
        )
        path.close()
        return path
    }

    fun changeCorner(corner: Float) {
        mCorner = corner
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.clipPath(getClipPath())
        super.onDraw(canvas)
    }


}
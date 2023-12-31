package com.thn.videoconstruction.view_customers

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import com.thn.videoconstruction.R
import kotlin.math.max
import kotlin.math.sqrt

open class FERippleView :AppCompatImageView {

    constructor(context: Context) : super(context) {
        initAttrs(null)
    }

    constructor(context: Context, attributes: AttributeSet) : super(context, attributes) {
        initAttrs(attributes)
    }
    private var mRippleColor = Color.parseColor("#4DFFFFFF")
    protected var onClick:(()->Unit)? = null
    protected var instanClick:(()->Unit)? = null

    private var mCurrentRadius = 0

    private var mMaxRadius = 0f
    private var mCurrentX = 0f
    private var mCurrentY = 0f

    private var isPress = false

    private var mCornerRadius = 0f


    private val mFillPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private fun initAttrs(attrs:AttributeSet?) {
        if(attrs == null) return
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RippleImageView)
        mCornerRadius = typedArray.getDimension(R.styleable.RippleView_cornerRadius, 0f)
        mRippleColor = typedArray.getColor(R.styleable.RippleImageView_rippleColor, Color.parseColor("#4DFFFFFF"))
        typedArray.recycle()

        mFillPaint.color = mRippleColor
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if(event?.action == MotionEvent.ACTION_DOWN || event?.action == MotionEvent.ACTION_POINTER_DOWN) {
            instanClick?.invoke()
            mCurrentX = event.x
            mCurrentY = event.y
            mCurrentRadius = 0
            mMaxRadius = max((sqrt(mCurrentX*mCurrentX+mCurrentY*mCurrentY)), sqrt((width-mCurrentX) *(width-mCurrentX)+(height-mCurrentY)*(height-mCurrentY))) + 100f
            drawRipple()
        }



        return true
    }

    fun getClipPath(): Path {
        val path = Path()
        path.reset()
        path.addRoundRect(RectF(0f,0f,width.toFloat(), height.toFloat()), mCornerRadius, mCornerRadius, Path.Direction.CW)
        path.close()
        return path
    }


    override fun onDraw(canvas: Canvas?) {
        canvas?.clipPath(getClipPath())
        super.onDraw(canvas)

        if(isPress) {
            canvas?.drawCircle(mCurrentX, mCurrentY, mCurrentRadius.toFloat(), mFillPaint)
        }
    }

    private fun drawRipple() {
        isPress = true
        val animator = ValueAnimator.ofFloat(0f,mMaxRadius)
        animator.addUpdateListener {
            it.animatedFraction
            mCurrentRadius = (mMaxRadius*it.animatedFraction).toInt()
            invalidate()
        }
        animator.duration = 200
        animator.addListener(object : Animator.AnimatorListener{


            override fun onAnimationStart(p0: Animator) {

            }

            override fun onAnimationEnd(p0: Animator) {
                isPress = false
            }

            override fun onAnimationCancel(p0: Animator) {

            }

            override fun onAnimationRepeat(p0: Animator) {
            }

        })
        animator.start()
    }

    fun setClick(onClick:()->Unit) {
        this.onClick = onClick
    }
    fun setInstanceClick(onClick:()->Unit) {
        this.instanClick = onClick
    }
}
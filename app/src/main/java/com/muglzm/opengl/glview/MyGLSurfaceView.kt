package com.muglzm.opengl.glview

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import com.muglzm.opengl.getMatrixCallback

class MyGLSurfaceView constructor(context: Context?, attrs: AttributeSet? = null) : GLSurfaceView(context, attrs),OnScaleGestureListener {
    private val TOUCH_SCALE_FACTOR = 180.0f / 320
    private var mPreviousY = 0f
    private var mPreviousX = 0f
    private var mXAngle = 0
    private var mYAngle = 0
    private var mRatioWidth = 0
    private var mRatioHeight = 0
    private val mScaleGestureDetector: ScaleGestureDetector
    private var mPreScale = 1.0f
    private var mCurScale = 1.0f
    private var mLastMultiTouchTime: Long = 0
    private var mOnGestureCallback: OnGestureCallback? = null
    private var mGetMatrixCallback:getMatrixCallback?=null

    fun addOnGestureCallback(callback: getMatrixCallback) {
        mGetMatrixCallback = callback
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height)
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth)
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height)
            }
        }
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val preSpan = detector.previousSpan
        val curSpan = detector.currentSpan
        mCurScale = if (curSpan < preSpan) {
            mPreScale - (preSpan - curSpan) / 200
        } else {
            mPreScale + (curSpan - preSpan) / 200
        }
        mCurScale = Math.max(0.05f, Math.min(mCurScale, 80.0f))
        if (mGetMatrixCallback != null) {
            Log.d(TAG, "onScale: 缩放比例")
            mGetMatrixCallback!!.setAngleX(mXAngle)
            mGetMatrixCallback!!.setAngleY(mYAngle)
            mGetMatrixCallback!!.setScale(mCurScale*2)
            //requestRender()
        }
        return false
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        mPreScale = mCurScale
        mLastMultiTouchTime = System.currentTimeMillis()
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.pointerCount == 1) {
            consumeClickEvent(e)
            val currentTimeMillis = System.currentTimeMillis()
            if (currentTimeMillis - mLastMultiTouchTime > 200) {
                val y = e.y
                val x = e.x
                when (e.action) {
                    MotionEvent.ACTION_MOVE -> {
                        val dy = y - mPreviousY
                        val dx = x - mPreviousX
                        mYAngle += (dx * TOUCH_SCALE_FACTOR).toInt()
                        mXAngle += (dy * TOUCH_SCALE_FACTOR).toInt()
                    }
                }
                mPreviousY = y
                mPreviousX = x
                Log.d(TAG, "onTouchEvent: 处理手势")
                if (mGetMatrixCallback != null) {
                    mGetMatrixCallback!!.setAngleX(mXAngle)
                    mGetMatrixCallback!!.setAngleY(mYAngle)
                    mGetMatrixCallback!!.setScale(mCurScale)
                    //requestRender()
                }
            }
        } else {
            mScaleGestureDetector.onTouchEvent(e)
        }
        return true
    }

    fun setAspectRatio(width: Int, height: Int) {
        Log.d(TAG, "setAspectRatio() called with: width = [$width], height = [$height]")
        require(!(width < 0 || height < 0)) { "Size cannot be negative." }
        mRatioWidth = width
        mRatioHeight = height
        requestLayout()
    }

    fun addOnGestureCallback(callback: OnGestureCallback?) {
        mOnGestureCallback = callback
    }
    public fun addOnMatrixCallback(callback: getMatrixCallback?) {
        mGetMatrixCallback = callback
    }

    private fun consumeClickEvent(event: MotionEvent) {
        var touchX = -1f
        var touchY = -1f
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                touchX = event.x
                touchY = event.y
                run {
                    //点击
                    if (mOnGestureCallback != null) mOnGestureCallback!!.onTouchLoc(touchX, touchY)
                }
            }
            else -> {
            }
        }
    }


    interface OnGestureCallback {
        fun onGesture(xRotateAngle: Int, yRotateAngle: Int, scale: Float)
        fun onTouchLoc(touchX: Float, touchY: Float)
    }



    companion object {
        const val TAG = "MyGLSurfaceView"
    }

    init {
        mScaleGestureDetector = ScaleGestureDetector(context, this)
    }
}
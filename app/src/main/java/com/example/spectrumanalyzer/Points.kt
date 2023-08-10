package com.example.spectrumanalyzer

class Points internal constructor(private val mMaxNum: Int) {
    private var mNum: Int = 0
    private val mX: FloatArray
    private val mY: FloatArray

    init {
        mX = FloatArray(mMaxNum)
        mY = FloatArray(mMaxNum)
    }

    fun init() {
        mNum = 0
    }

    fun add(x: Float, y: Float) {
        if (mNum < mMaxNum) {
            mX[mNum] = x
            mY[mNum] = y
            mNum++
        }
    }

    fun getX(index: Int): Float {
        val ret: Float
        ret = if (index >= 0 && index < mNum) {
            mX[index]
        } else {
            0f
        }
        return ret
    }

    fun getY(index: Int): Float {
        val ret: Float
        ret = if (index >= 0 && index < mNum) {
            mY[index]
        } else {
            0f
        }
        return ret
    }

    fun getNum(): Int {
        return mNum
    }
}

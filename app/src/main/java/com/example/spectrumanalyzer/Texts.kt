package com.example.spectrumanalyzer

class Texts internal constructor(private val mMaxNum: Int) {
    private var mNum: Int = 0
    private val mText: Array<String>
    private val mX: FloatArray
    private val mY: FloatArray

    init {
        mText = Array(mMaxNum){""}
        mX = FloatArray(mMaxNum)
        mY = FloatArray(mMaxNum)
    }

    fun init() {
        for (i in 0 until mNum) {
            mText[i] = ""
            mX[i] = 0f
            mY[i] = 0f
        }
        mNum = 0
    }

    fun add(text: String, x: Float, y: Float) {
        if (mNum < mMaxNum) {
            mText[mNum] = text
            mX[mNum] = x
            mY[mNum] = y
            mNum++
        }
    }

    fun getText(index: Int): String {
        val ret: String
        ret = if (index >= 0 && index < mNum) {
            mText[index]
        } else {
            ""
        }
        return ret
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

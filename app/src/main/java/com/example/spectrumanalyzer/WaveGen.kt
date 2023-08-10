package com.example.spectrumanalyzer

import android.util.Log

class WaveGen internal constructor(
    private var mFreq: Int,
    private val mSampleRate: Int,
    private val mSize: Int
) {
    private var mTime = 0.0
    private val mBuf: ShortArray
    private var mDbgCnt: Int

    init {
        mBuf = ShortArray(mSize)
        mDbgCnt = 0
    }

    fun changeParam(freq: Int) {
        mFreq = freq
    }

    val buf: ShortArray
        get() {
            for (i in 0 until mSize) {
                val value = Math.sin(2.0 * Math.PI * mFreq * mTime)
                mBuf[i] = (value * Short.MAX_VALUE).toInt().toShort()
                mTime = mTime + 1.0 / mSampleRate.toDouble()
            }
            if (mDbgCnt == 1) {
                for (i in 10000 until mSize) {
                    Log.d(TAG, "mBuf[" + i + "] :" + mBuf[i])
                }
            }
            mDbgCnt++
            return mBuf
        }

    companion object {
        private const val TAG = "WaveGen"
    }
}

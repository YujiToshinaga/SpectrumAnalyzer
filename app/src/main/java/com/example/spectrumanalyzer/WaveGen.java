package com.example.spectrumanalyzer;

import android.util.Log;

/**
 * Created by usr1 on 2017/05/20.
 */

public class WaveGen {
    private static final String TAG = "WaveGen";
    private int mFreq;
    private int mSampleRate;
    private int mSize;
    private double mTime;
    private short[] mBuf;

    private int mDbgCnt;

    WaveGen(int freq, int sampleRate, int size) {
        mFreq = freq;
        mSampleRate = sampleRate;
        mSize = size;
        mTime = 0.0;
        mBuf = new short[size];

        mDbgCnt = 0;
    }

    void changeParam(int freq) {
        mFreq = freq;
    }

    public short[] getBuf() {
        for (int i = 0; i < mSize; i++) {
            double val = Math.sin(2.0 * Math.PI * mFreq * mTime);
            mBuf[i] = (short)(val * Short.MAX_VALUE);
            mTime = mTime + 1.0 / (double)mSampleRate;
        }
        if (mDbgCnt == 1) {
            for (int i = 10000; i < mSize; i++) {
                Log.d(TAG, "mBuf[" + i + "] :" + mBuf[i]);
            }
        }
        mDbgCnt++;
        return mBuf;
    }
}

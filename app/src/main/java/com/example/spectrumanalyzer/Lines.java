package com.example.spectrumanalyzer;

/**
 * Created by usr1 on 2017/05/28.
 */

public class Lines {
    private int mMaxNum;
    private int mNum;
    private float[] mX1;
    private float[] mY1;
    private float[] mX2;
    private float[] mY2;

    Lines(int maxNum) {
        mMaxNum = maxNum;
        mNum = 0;
        mX1 = new float[maxNum];
        mY1 = new float[maxNum];
        mX2 = new float[maxNum];
        mY2 = new float[maxNum];
    }

    public void init() {
        mNum = 0;
    }

    public void add(float x1, float y1, float x2, float y2) {
        if (mNum < mMaxNum) {
            mX1[mNum] = x1;
            mY1[mNum] = y1;
            mX2[mNum] = x2;
            mY2[mNum] = y2;
            mNum++;
        }
    }

    public float getX1(int index) {
        float ret;
        if ((index >= 0) && (index < mNum)) {
            ret = mX1[index];
        } else {
            ret = 0;
        }
        return ret;
    }

    public float getY1(int index) {
        float ret;
        if ((index >= 0) && (index < mNum)) {
            ret = mY1[index];
        } else {
            ret = 0;
        }
        return ret;
    }

    public float getX2(int index) {
        float ret;
        if ((index >= 0) && (index < mNum)) {
            ret = mX2[index];
        } else {
            ret = 0;
        }
        return ret;
    }

    public float getY2(int index) {
        float ret;
        if ((index >= 0) && (index < mNum)) {
            ret = mY2[index];
        } else {
            ret = 0;
        }
        return ret;
    }

    public float getNum() {
        return mNum;
    }
}

package com.example.spectrumanalyzer;

/**
 * Created by usr1 on 2017/05/28.
 */

public class Points {
    private int mMaxNum;
    private int mNum;
    private float[] mX;
    private float[] mY;

    Points(int maxNum) {
        mMaxNum = maxNum;
        mNum = 0;
        mX = new float[maxNum];
        mY = new float[maxNum];
    }

    public void init() {
        mNum = 0;
    }

    public void add(float x, float y) {
        if (mNum < mMaxNum) {
            mX[mNum] = x;
            mY[mNum] = y;
            mNum++;
        }
    }

    public float getX(int index) {
        float ret;
        if ((index >= 0) && (index < mNum)) {
            ret = mX[index];
        } else {
            ret = 0;
        }
        return ret;
    }

    public float getY(int index) {
        float ret;
        if ((index >= 0) && (index < mNum)) {
            ret = mY[index];
        } else {
            ret = 0;
        }
        return ret;
    }

    public int getNum() {
        return mNum;
    }
}

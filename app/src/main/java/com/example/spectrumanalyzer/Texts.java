package com.example.spectrumanalyzer;

/**
 * Created by usr1 on 2017/05/28.
 */

public class Texts {
    private int mMaxNum;
    private int mNum;
    private String[] mText;
    private float[] mX;
    private float[] mY;

    Texts(int maxNum) {
        mMaxNum = maxNum;
        mNum = 0;
        mText = new String[maxNum];
        mX = new float[maxNum];
        mY = new float[maxNum];
    }

    public void init() {
        for (int i = 0; i < mNum; i++) {
            mText[i] = null;
            mX[i] = 0;
            mY[i] = 0;
        }
        mNum = 0;
    }

    public void add(String text, float x, float y) {
        if (mNum < mMaxNum) {
            mText[mNum] = text;
            mX[mNum] = x;
            mY[mNum] = y;
            mNum++;
        }
    }

    public String getText(int index) {
        String ret;
        if ((index >= 0) && (index < mNum)) {
            ret = mText[index];
        } else {
            ret = "";
        }
        return ret;
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

package com.example.spectrumanalyzer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class SpectrumView extends View {
    private static final String TAG = "SpectrumView";
    private static int MAX_SPECTRUM_NUM = 8192;
    private static double FREQ_HZ_MIN = 20;          // 表示する最小周波数
    private static double FREQ_HZ_MAX = 22050;       // 表示する最大周波数
    private static double AMP_DB_MIN = (-120);       // 表示する最小デシベル数
    private static double AMP_DB_GRID_UNIT = 20;     // 表示するデシベル数のグリッド間隔
    private static int GRAPH_PAD_LEFT = 60;
    private static int GRAPH_PAD_RIGHT = 10;
    private static int GRAPH_PAD_TOP = 20;
    private static int GRAPH_PAD_BOTTOM = 80;
    private int mGridColor = Color.RED;
    private int mScaleColor = Color.GREEN;
    private int mSpectrumColor = Color.GREEN;
    private int mResultColor = Color.GREEN;
    private Paint mGridPaint;
    private Paint mScalePaint;
    private Paint mSpectrumPaint;
    private Paint mResultPaint;
    private int mSampleRate;
    private double[] mSpectrum;
    private int mSpectrumNum;
    private int mFreqLogMin;
    private int mFreqLogMax;
    private int mGridXNum;
    private int mGridYNum;
    private int mScaleXNum;
    private int mScaleYNum;
    private double[] mGridX;
    private double[] mGridY;
    private double[] mScaleX;
    private double[] mScaleY;
    private double[] mSpectrumX;
    private double[] mSpectrumY;
    private double mPeakFreq;
    private int mViewWidth;
    private int mViewHeight;
    private int mGraphWidth;
    private int mGraphHeight;
    private int mGraphLeft;
    private int mGraphRight;
    private int mGraphTop;
    private int mGraphBottom;
    private double mFreqLogUnitWidth;
    private double mFreqLogOffset;
    private double mAmpDbUnitHeight;

    public SpectrumView(Context context) {
        super(context);
        init(null, 0);
    }

    public SpectrumView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SpectrumView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        Log.d(TAG, "init");

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.SpectrumView, defStyle, 0);
        mGridColor = a.getColor(R.styleable.SpectrumView_gridColor, mGridColor);
        mScaleColor = a.getColor(R.styleable.SpectrumView_scaleColor, mScaleColor);
        mSpectrumColor = a.getColor(R.styleable.SpectrumView_spectrumColor, mSpectrumColor);
        mResultColor = a.getColor(R.styleable.SpectrumView_spectrumColor, mSpectrumColor);
        a.recycle();

        mGridPaint = new Paint();
        mGridPaint.setStrokeWidth(1f);
        mGridPaint.setStyle(Paint.Style.STROKE);
        mGridPaint.setAntiAlias(true);
        mGridPaint.setColor(mGridColor);
        mScalePaint = new Paint();
        mScalePaint.setTextSize(20);
        mScalePaint.setAntiAlias(true);
        mScalePaint.setColor(mScaleColor);
        mSpectrumPaint = new Paint();
        mSpectrumPaint.setStrokeWidth(1f);
        mSpectrumPaint.setAntiAlias(true);
        mSpectrumPaint.setColor(mSpectrumColor);
        mResultPaint = new Paint();
        mResultPaint.setTextSize(20);
        mResultPaint.setAntiAlias(true);
        mResultPaint.setColor(mResultColor);
        mSampleRate = 0;
        mSpectrum = null;
        mSpectrumNum = 0;

        // 周波数の対数の範囲を計算する
        mFreqLogMin = (int)Math.floor(Math.log10(FREQ_HZ_MIN));
        mFreqLogMax = (int)Math.ceil(Math.log10(FREQ_HZ_MAX));
        Log.d(TAG, "mFreqLogMin: " + mFreqLogMin); // 1
        Log.d(TAG, "mFreqLogMax: " + mFreqLogMax); // 5

        // グリッド線を初期化する
        mGridXNum = 9 - (int)(FREQ_HZ_MIN / Math.pow(10, mFreqLogMin));
        mGridXNum += 9 * (mFreqLogMax - mFreqLogMin - 2);
        mGridXNum += (int)(FREQ_HZ_MAX / Math.pow(10, mFreqLogMax - 1));
        mGridXNum++;
        mGridX = new double[mGridXNum];
        for (int i = 0; i < mGridXNum; i++) {
            mGridX[i] = 0;
        }
        mGridYNum = (int)(Math.ceil((-AMP_DB_MIN) / AMP_DB_GRID_UNIT)) + 1;
        mGridY = new double[mGridYNum];
        for (int i = 0; i < mGridYNum; i++) {
            mGridY[i] = 0;
        }

        // 目盛り表示を初期化する
        mScaleXNum = mFreqLogMax - mFreqLogMin;
        mScaleX = new double[mScaleXNum];
        for (int i = 0; i < mScaleXNum; i++) {
            mScaleX[i] = 0;
        }
        mScaleYNum = (int)(Math.ceil((-AMP_DB_MIN) / AMP_DB_GRID_UNIT)) + 1;
        mScaleY = new double[mScaleYNum];
        for (int i = 0; i < mScaleYNum; i++) {
            mScaleY[i] = 0;
        }
        Log.d(TAG, "mScaleXNum: " + mScaleXNum); // 4

        // スペクトラムデータを初期化する
        mSpectrumX = new double[MAX_SPECTRUM_NUM];
        mSpectrumY = new double[MAX_SPECTRUM_NUM];
        for (int i = 0; i < MAX_SPECTRUM_NUM; i++) {
            mSpectrumX[i] = 0;
            mSpectrumY[i] = 0;
        }

        mPeakFreq = 0;

        // 各種データを初期化する
        calcParam();
        calcGrid();
        calcScale();
        calcSpectrum();
        calcResult();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Viewのサイズが変わったら座標の再計算する
        if ((mViewWidth != getWidth()) || (mViewHeight != getHeight())) {
            calcParam();
            calcGrid();
            calcScale();
        }

        // スペクトラムの座標を計算する
        calcSpectrum();
        calcResult();

        // 描画する
        drawGrid(canvas);
        drawScale(canvas);
        drawSpectrum(canvas);
        drawResult(canvas);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

        // 各座標を計算する
        calcParam();
        calcGrid();
        calcScale();
        calcSpectrum();
    }

    // パラメータの計算
    private void calcParam() {
        // Viewのサイズを計算する
        mViewWidth = getWidth();
        mViewHeight = getHeight();
        mGraphWidth = getWidth() - GRAPH_PAD_LEFT - GRAPH_PAD_RIGHT;
        mGraphHeight = getHeight() - GRAPH_PAD_TOP - GRAPH_PAD_BOTTOM;
        mGraphLeft = getLeft() + GRAPH_PAD_LEFT;
        mGraphRight = getRight() - GRAPH_PAD_RIGHT;
        mGraphTop = getTop() + GRAPH_PAD_TOP;
        mGraphBottom = getBottom() - GRAPH_PAD_BOTTOM;

        // 周波数の対数の単位区間幅を計算する
        mFreqLogUnitWidth = (double)mGraphWidth / (Math.log10(FREQ_HZ_MAX) - Math.log10(FREQ_HZ_MIN));

        // 周波数の対数のオフセットを計算する
        mFreqLogOffset = Math.log10(FREQ_HZ_MIN) * mFreqLogUnitWidth;

        // 振幅の単位高さを計算する
        mAmpDbUnitHeight = (double)mGraphHeight / (-AMP_DB_MIN);
    }

    // グリッドの計算
    private void calcGrid() {
        // 縦のグリッド線の座標を計算する
        int gridCountX = 0;
        for (int i = mFreqLogMin; i < mFreqLogMax; i++) {
            for (int j = 1; j < 10; j++) {
                double x = mGraphLeft + Math.log10(Math.pow(10, i) * j) * mFreqLogUnitWidth - mFreqLogOffset;
                if ((x >= mGraphLeft) && (x <= mGraphRight)) {
                    mGridX[gridCountX] = x;
                    gridCountX++;
                }
            }
        }

        // 横のグリッド線の座標を計算する
        for (int i = 0; i < mGridYNum; ++i) {
            mGridY[i] = mGraphTop + mAmpDbUnitHeight * AMP_DB_GRID_UNIT * i;
        }
    }

    // グリッドの計算
    private void calcScale() {
        // aaa
        for (int i = 0; i < mScaleXNum; i++) {
            mScaleX[i] = mGraphLeft + Math.log10(Math.pow(10, i + mFreqLogMin)) * mFreqLogUnitWidth - mFreqLogOffset;
        }

        // aaa
        for (int i = 0; i < mScaleYNum; ++i) {
            mScaleY[i] = mGraphTop + mAmpDbUnitHeight * AMP_DB_GRID_UNIT * i;
        }
    }

    // スペクトラムの計算
    private void calcSpectrum() {
        // スペクトルデータがなかったら計算しない
        if (mSpectrum == null) {
            return;
        }

        // スペクトラムの座標を計算する
        for (int i = 0; i < mSpectrumNum; ++i) {
            double freqHz = (((double) mSampleRate / 2) / mSpectrumNum) * i;
            double x = mGraphLeft + (Math.log10(freqHz) * mFreqLogUnitWidth) - mFreqLogOffset;
            double spectrumDb = 20.0 * Math.log10(mSpectrum[i]);
            double y = mGraphTop + mGraphHeight - (((-AMP_DB_MIN) - (-spectrumDb)) * mAmpDbUnitHeight);
            mSpectrumX[i] = x;
            if ((y >= mGraphTop) && (y <= mGraphBottom)) { // TODO
                mSpectrumY[i] = y;
            } else {
                mSpectrumY[i] = mGraphBottom;
            }
        }
    }

    private void calcResult() {
        double peakAmp = 0;
        double peakFreq = 0;
        for (int i = 0; i < mSpectrumNum; ++i) {
            if (mSpectrum[i] > peakAmp) {
                peakAmp = mSpectrum[i];
                peakFreq = (((double) mSampleRate / 2) / mSpectrumNum) * i;
            }
        }
        mPeakFreq = peakFreq;
    }

    // グリッドの描画
    private void drawGrid(Canvas canvas) {
        // 枠を描画する
        canvas.drawRect((float)mGraphLeft, (float)mGraphTop, (float)mGraphRight, (float)mGraphBottom, mGridPaint);

        // 縦線を描画する
        for (int i = 0; i < mGridXNum; i++) {
            double x = mGridX[i];
            canvas.drawLine((float)x, (float)mGraphBottom, (float)x, (float)mGraphTop, mGridPaint);
        }

        // 横線を描画する
        for (int i = 0; i < mGridYNum; i++) {
            double y = mGridY[i];
            canvas.drawLine((float)mGraphLeft, (float)y, (float)mGraphRight, (float)y, mGridPaint);
        }
    }

    // 目盛り表示の描画
    private void drawScale(Canvas canvas) {
        for (int i = 0; i < mScaleXNum; i++) {
            canvas.drawText("0dB", (float)mScaleX[i], mGraphBottom + 10, mScalePaint);
        }

        for (int i = 0; i < mScaleYNum; i++) {
            canvas.drawText("0dB", (float)10, (float)mScaleY[i], mScalePaint);
        }
    }

    // スペクトラムの描画
    private void drawSpectrum(Canvas canvas) {
        // スペクトルデータがなかったら描画しない
        if (mSpectrum == null) {
            return;
        }

        // スペクトラムを描画する
//        for(int i = 0; i < mSpectrumNum; ++i) {
//            canvas.drawLine((float)mSpectrumX[i], (float)mGraphBottom, (float)mSpectrumX[i], (float)mSpectrumY[i], mSpectrumPaint);
//        }
        for(int i = 0; i < mSpectrumNum - 1; ++i) {
            canvas.drawLine((float)mSpectrumX[i], (float)mSpectrumY[i], (float)mSpectrumX[i + 1], (float)mSpectrumY[i + 1], mSpectrumPaint);
        }
    }

    private void drawResult(Canvas canvas) {
        canvas.drawText("Peak " + mPeakFreq + " Hz", (float)mGraphLeft + 20, mGraphBottom + 50, mResultPaint);
    }

    // スペクトラムデータ設定
    public void setSpectrum(double[] spectrum, int spectrumNum, int sampleRate) {
        mSpectrum = spectrum;
        mSpectrumNum = spectrumNum;
        mSampleRate = sampleRate;
    }

    // 描画の更新
    public void update() {
        invalidate();
    }
}


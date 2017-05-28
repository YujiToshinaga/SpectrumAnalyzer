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
    private static int FREQ_HZ_MIN = 20;          // 表示する最小周波数
    private static int FREQ_HZ_MAX = 22050;       // 表示する最大周波数
    private static int AMP_DB_MIN = (-120);       // 表示する最小デシベル数
    private static int AMP_DB_GRID_UNIT = 20;     // 表示するデシベル数のグリッド間隔
    private static int GRAPH_PAD_LEFT = 60;
    private static int GRAPH_PAD_RIGHT = 10;
    private static int GRAPH_PAD_TOP = 20;
    private static int GRAPH_PAD_BOTTOM = 80;
    private int mGridColor = Color.RED;
    private int mScaleColor = Color.GREEN;
    private int mSpectrumColor = Color.GREEN;
    private int mResultColor = Color.GREEN;
    private Paint mSpectrumPaint;
    private int mSampleRate;
    private double[] mAmp;
    private int mAmpNum;
    private int mFreqHzLogMin;
    private int mFreqHzLogMax;
    private Lines mGridX;
    private Lines mGridY;
    private Texts mScaleX;
    private Texts mScaleY;
    private Lines mSpectrum;
    private Texts mInfo;
    private int mViewWidth;
    private int mViewHeight;
    private int mGraphWidth;
    private int mGraphHeight;
    private int mGraphLeft;
    private int mGraphRight;
    private int mGraphTop;
    private int mGraphBottom;
    private double mFreqHzLogUnitWidth;
    private double mFreqHzLogOffset;
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
        mResultColor = a.getColor(R.styleable.SpectrumView_spectrumColor, mResultColor);
        a.recycle();

        mSpectrumPaint = new Paint();
        mSpectrumPaint.setTextSize(20);
//        mSpectrumPaint.setStrokeWidth(1f);
        mSpectrumPaint.setAntiAlias(true);

        mSampleRate = 0;
        mAmp = null;
        mAmpNum = 0;

        // 周波数の対数の範囲を計算する
        mFreqHzLogMin = (int)Math.floor(Math.log10(FREQ_HZ_MIN));
        mFreqHzLogMax = (int)Math.ceil(Math.log10(FREQ_HZ_MAX));
        Log.d(TAG, "mFreqHzLogMin: " + mFreqHzLogMin); // 1
        Log.d(TAG, "mFreqHzLogMax: " + mFreqHzLogMax); // 5

        // 描画オブジェクトのメモリを確保する
        mGridX = new Lines(100);
        mGridY = new Lines(100);
        mScaleX = new Texts(10);
        mScaleY = new Texts(10);
        mSpectrum = new Lines(MAX_SPECTRUM_NUM);
        mInfo = new Texts(100);

        // いったんグラフを計算して初期化する
        calcGrid();
        calcSpectrum();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Viewのサイズが変わったら座標の再計算する
        if ((mViewWidth != getWidth()) || (mViewHeight != getHeight())) {
            calcGrid();
        }

        // スペクトラムの座標を計算する
        calcSpectrum();

        // 描画する
        drawGraph(canvas);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

        // 各座標を計算する
        calcGrid();
        calcSpectrum();
    }

    // グラフの計算
    private void calcGrid() {
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
        mFreqHzLogUnitWidth = (double)mGraphWidth / (Math.log10(FREQ_HZ_MAX) - Math.log10(FREQ_HZ_MIN));

        // 周波数の対数のオフセットを計算する
        mFreqHzLogOffset = Math.log10(FREQ_HZ_MIN) * mFreqHzLogUnitWidth;

        // 振幅の単位高さを計算する
        mAmpDbUnitHeight = (double)mGraphHeight / (-AMP_DB_MIN);

        // 周波数のグリッドの座標を計算する
        mGridX.init();
        for (int i = mFreqHzLogMin; i < mFreqHzLogMax; i++) {
            for (int j = 1; j < 10; j++) {
                double x = mGraphLeft + Math.log10(Math.pow(10, i) * j) * mFreqHzLogUnitWidth - mFreqHzLogOffset;
                if ((x >= mGraphLeft) && (x <= mGraphRight)) {
                    mGridX.add((float)x, (float)mGraphTop, (float)x, (float)mGraphBottom);
                }
            }
        }

        // 振幅のグリッドの座標を計算する
        mGridY.init();
        int gridYNum = (int)(Math.ceil((-AMP_DB_MIN) / AMP_DB_GRID_UNIT)) + 1;
        for (int i = 0; i < gridYNum; ++i) {
            double y = mGraphTop + mAmpDbUnitHeight * AMP_DB_GRID_UNIT * i;
            if ((y >= mGraphTop) && (y <= mGraphBottom)) {
                mGridY.add((float)mGraphLeft, (float)y, (float)mGraphRight, (float)y);
            }
        }

        // 周波数の目盛り表示を計算する
        mScaleX.init();
        for (int i = mFreqHzLogMin; i < mFreqHzLogMax; i++) {
            double x = mGraphLeft + Math.log10(Math.pow(10, i)) * mFreqHzLogUnitWidth - mFreqHzLogOffset;
            if ((x >= mGraphLeft) && (x <= mGraphRight)) {
                mScaleX.add((int)Math.pow(10, i) + "Hz", (float)x - 20, (float)mGraphBottom + 20);
            }
        }

        // 振幅の目盛り表示を計算する
        mScaleY.init();
        int scaleYNum = (int)(Math.ceil((-AMP_DB_MIN) / AMP_DB_GRID_UNIT)) + 1;
        for (int i = 0; i < scaleYNum; ++i) {
            double y = mGraphTop + mAmpDbUnitHeight * AMP_DB_GRID_UNIT * i;
            mScaleX.add((-AMP_DB_GRID_UNIT) * i + "dB", (float)10, (float)y + 5);
        }
    }

    // スペクトラムの計算
    private void calcSpectrum() {
        // スペクトルを計算する
        mSpectrum.init();
        if (mAmp != null) {
            Points spectrumPoint = new Points(MAX_SPECTRUM_NUM);

            // スペクトルの座標を計算する
            for (int i = 0; i < mAmpNum; ++i) {
                double freqHz = (((double) mSampleRate / 2) / mAmpNum) * i;
                double x = mGraphLeft + (Math.log10(freqHz) * mFreqHzLogUnitWidth) - mFreqHzLogOffset;
                double spectrumDb = 20.0 * Math.log10(mAmp[i]);
                double y = mGraphTop + mGraphHeight - (((-AMP_DB_MIN) - (-spectrumDb)) * mAmpDbUnitHeight);
                if ((x >= mGraphLeft) && (x <= mGraphRight)) {
                    if ((y >= mGraphTop) && (y <= mGraphBottom)) { // TODO
                        spectrumPoint.add((float)x, (float)y);
                    } else {
                        spectrumPoint.add((float)x, mGraphBottom);
                    }
                }
            }

            // 折れ線グラフの座標を計算する
            for (int i = 0; i < spectrumPoint.getNum() - 1; i++) {
                mSpectrum.add(spectrumPoint.getX(i), spectrumPoint.getY(i), spectrumPoint.getX(i + 1), spectrumPoint.getY(i + 1));
            }
        }

        // 値情報を計算する
        mInfo.init();
        double peakAmp = 0;
        double peakFreq = 0;
        for (int i = 0; i < mAmpNum; ++i) {
            if (mAmp[i] > peakAmp) {
                peakAmp = mAmp[i];
                peakFreq = (((double) mSampleRate / 2) / mAmpNum) * i;
            }
        }
        mInfo.add("Peak : " + peakFreq, mGraphLeft + 20, mGraphBottom + 40);
    }

    // グラフの描画
    private void drawGraph(Canvas canvas) {
        // 枠を描画する
        mSpectrumPaint.setColor(mGridColor);
        canvas.drawLine(mGraphLeft, mGraphTop, mGraphRight, mGraphTop, mSpectrumPaint);
        canvas.drawLine(mGraphLeft, mGraphTop, mGraphLeft, mGraphBottom, mSpectrumPaint);
        canvas.drawLine(mGraphRight, mGraphTop, mGraphRight, mGraphBottom, mSpectrumPaint);
        canvas.drawLine(mGraphLeft, mGraphBottom, mGraphRight, mGraphBottom, mSpectrumPaint);

        // 周波数のグリッドを描画する
        mSpectrumPaint.setColor(mGridColor);
        for (int i = 0; i < mGridX.getNum(); i++) {
            canvas.drawLine(mGridX.getX1(i), mGridX.getY1(i), mGridX.getX2(i), mGridX.getY2(i), mSpectrumPaint);
        }

        // 振幅のグリッドを描画する
        mSpectrumPaint.setColor(mGridColor);
        for (int i = 0; i < mGridY.getNum(); i++) {
            canvas.drawLine(mGridY.getX1(i), mGridY.getY1(i), mGridY.getX2(i), mGridY.getY2(i), mSpectrumPaint);
        }

        // 周波数の目盛りを描画する
        mSpectrumPaint.setColor(mScaleColor);
        for (int i = 0; i < mScaleX.getNum(); i++) {
            canvas.drawText(mScaleX.getText(i), mScaleX.getX(i), mScaleX.getY(i), mSpectrumPaint);
        }

        // 振幅のグリッドを描画する
        mSpectrumPaint.setColor(mScaleColor);
        for (int i = 0; i < mScaleY.getNum(); i++) {
            canvas.drawText(mScaleY.getText(i), mScaleY.getX(i), mScaleY.getY(i), mSpectrumPaint);
        }

        // スペクトラムを描画する
        mSpectrumPaint.setColor(mSpectrumColor);
        for(int i = 0; i < mSpectrum.getNum(); ++i) {
            canvas.drawLine(mSpectrum.getX1(i), mSpectrum.getY1(i), mSpectrum.getX2(i), mSpectrum.getY2(i), mSpectrumPaint);
        }

        // 値情報を描画する
        mSpectrumPaint.setColor(mResultColor);
        canvas.drawText(mInfo.getText(0), mInfo.getX(0), mInfo.getY(0), mSpectrumPaint);
    }

    // スペクトラムデータ設定
    public void setAmp(double[] spectrum, int spectrumNum, int sampleRate) {
        mAmp = spectrum;
        mAmpNum = spectrumNum;
        mSampleRate = sampleRate;
    }

    // 描画の更新
    public void update() {
        invalidate();
    }
}


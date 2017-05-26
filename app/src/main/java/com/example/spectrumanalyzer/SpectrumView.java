package com.example.spectrumanalyzer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class SpectrumView extends View {
    private static final String TAG = "SpectrumView";
    private static double FREQ_MIN = 20;             // 表示する最小周波数
    private static double FREQ_MAX = 22050;          // 表示する最大周波数
    private static double AMP_DB_MIN = (-120);       // 表示する最小デシベル数
    private static double AMP_DB_GRID_UNIT = 10;     // 表示するデシベル数のグリッド間隔
    private int mContentWidth;
    private int mContentHeight;
    private Paint mSpectrumPaint;
    private Paint mGridPaint;
    private int mSampleRate;
    private double[] mSpectrum;
    private int mSpectrumNum;
    private int mFreqLogMin;
    private int mFreqLogMax;
    private double mFreqLogUnitWidth;
    private double mFreqLogOffset;
    private double[] mGridX;
    private double[] mGridY;

    private String mExampleString; // TODO: use a default from R.string...
    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
    private double mExampleDimension = 0; // TODO: use a default from R.dimen...
    private TextPaint mTextPaint;
    private double mTextWidth;
    private double mTextHeight;

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
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.SpectrumView, defStyle, 0);

        mExampleString = a.getString(
                R.styleable.SpectrumView_exampleString);
        mExampleColor = a.getColor(
                R.styleable.SpectrumView_exampleColor,
                mExampleColor);
        mExampleDimension = a.getDimension(
                R.styleable.SpectrumView_exampleDimension,
                (float)mExampleDimension);

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();

        mSpectrumPaint = new Paint();
        mSpectrumPaint.setStrokeWidth(1f);
        mSpectrumPaint.setAntiAlias(true);
        mGridPaint = new Paint();
        mGridPaint.setStrokeWidth(1f);
        mGridPaint.setAntiAlias(true);
    }

    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize((float)mExampleDimension);
        mTextPaint.setColor(mExampleColor);
        mTextWidth = mTextPaint.measureText(mExampleString);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int contentWidth = getWidth();
        int contentHeight = getHeight();

        // Viewのサイズが変わったら再計算する
        if((mContentWidth != contentWidth) || (mContentHeight != contentHeight)) {
            calculateGraph();
        }

//        // Draw the text.
//        canvas.drawText(mExampleString,
//                paddingLeft + (contentWidth - mTextWidth) / 2,
//                paddingTop + (contentHeight + mTextHeight) / 2,
//                mTextPaint);

        // グリッドを描画する
        drawGrid(canvas);

        // スペクトラムを描画する
        if (mSpectrum != null){
            drawSpectrum(canvas);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

        mContentHeight = getHeight();
        mContentWidth = getWidth();
        calculateGraph();
    }

    // グラフ情報の計算
    private void calculateGraph() {
        int left = getLeft();
        int right = getRight();
        int top = getTop();
        int bottom = getBottom();
        int width = getWidth();
        int height = getHeight();

        // 周波数の対数の範囲を計算する
        mFreqLogMin = (int)Math.floor(Math.log10(FREQ_MIN));
        mFreqLogMax = (int)Math.ceil(Math.log10(FREQ_MAX));

        // 周波数の対数の単位区間幅を計算する
        mFreqLogUnitWidth = width / (Math.log10(FREQ_MAX) - Math.log10(FREQ_MIN));

        // 周波数の対数のオフセット
        mFreqLogOffset = (double)(Math.log10(FREQ_MIN) * mFreqLogUnitWidth);

        // 縦のグリッド線の数を数える
        int gridNumX;
        gridNumX = 9 - (int)(FREQ_MIN / Math.pow(10, mFreqLogMin));
        gridNumX += 9 * (mFreqLogMax - mFreqLogMin - 2);
        gridNumX += (int)(FREQ_MAX / Math.pow(10, mFreqLogMax - 1));
        gridNumX++;

        // 縦のグリッド線の座標を計算する
        mGridX = new double[gridNumX];
        int gridCountX = 0;
        for (int i = mFreqLogMin; i < mFreqLogMax; i++) {
            for (int j = 1; j < 10; j++) {
                double x = Math.log10(Math.pow(10, i) * j) * mFreqLogUnitWidth - mFreqLogOffset;
                if ((x >= left) && (x <= right)) {
                    mGridX[gridCountX] = x;
                    gridCountX++;
                }
            }
        }

        // 振幅の単位高さを計算する
        double ampDbUnitHeight = height / (-AMP_DB_MIN);

        // 横のグリッド線の数を数える
        int gridNumY = (int)(Math.ceil((-AMP_DB_MIN) / AMP_DB_GRID_UNIT));

        // 横のグリッド線の座標を計算する
        mGridY = new double[gridNumY];
        for(int i = 0; i < gridNumY; ++i){
            mGridY[i] = top + ampDbUnitHeight * AMP_DB_GRID_UNIT * i;
        }
    }

    // スペクトラムの描画
    private void drawSpectrum(Canvas canvas) {
        // Viewのサイズ情報
        int left = getLeft();
        int right = getRight();
        int top = getTop();
        int bottom = getBottom();
        int height = getHeight();

        // スペクトラムを描画する
        for(int i = 0; i < mSpectrumNum; ++i) {
            double frequency = (((double)mSampleRate / 2) / mSpectrumNum) * i;
            double x = (Math.log10(frequency) * mFreqLogUnitWidth) - mFreqLogOffset;
            double spectrum_db = 20.0 * Math.log10(mSpectrum[i]);
            double ampDbUnitHeight = (double)(height / -AMP_DB_MIN);
            double y = height - (((-AMP_DB_MIN) - (-spectrum_db)) * ampDbUnitHeight);
            if ((x >= left) && (x <= right) && (y >= top) && (y <= height)) {
                canvas.drawLine((float)x, (float)bottom, (float)x, (float)y, mSpectrumPaint);
            }
        }
    }

    // グリッドの描画
    private void drawGrid(Canvas canvas) {
        // Viewのサイズ情報
        int bottom = getBottom();
        int top = getTop();
        int left = getLeft();
        int right = getRight();

        // 縦線を描画する
        for (int i = 0; i < mGridX.length; i++) {
            double x = mGridX[i];
            canvas.drawLine((float)x, (float)bottom, (float)x, (float)top, mGridPaint);
        }

        // 横線を描画する
        for (int i = 0; i < mGridY.length; i++) {
            double y = mGridY[i];
            canvas.drawLine((float)left, (float)y, (float)right, (float)y, mGridPaint);
        }
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

    public String getExampleString() {
        return mExampleString;
    }

    public void setExampleString(String exampleString) {
        mExampleString = exampleString;
        invalidateTextPaintAndMeasurements();
    }

    public int getExampleColor() {
        return mExampleColor;
    }

    public void setExampleColor(int exampleColor) {
        mExampleColor = exampleColor;
        invalidateTextPaintAndMeasurements();
    }

    public double getExampleDimension() {
        return mExampleDimension;
    }

    public void setExampleDimension(double exampleDimension) {
        mExampleDimension = exampleDimension;
        invalidateTextPaintAndMeasurements();
    }
}


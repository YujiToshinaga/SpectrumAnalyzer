package com.example.spectrumanalyzer

import Lines
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View


class SpectrumView : View {
    private var mGridColor = Color.RED
    private var mScaleColor = Color.GREEN
    private var mSpectrumColor = Color.GREEN
    private var mResultColor = Color.GREEN
    private var mSpectrumPaint: Paint = Paint()
    private var mSampleRate = 0
    private var mAmp: DoubleArray? = null
    private var mAmpNum = 0
    private var mFreqHzLogMin = 0
    private var mFreqHzLogMax = 0

    private var mGridX: Lines = Lines(100)
    private var mGridY: Lines = Lines(100)
    private var mScaleX: Texts = Texts(10)
    private var mScaleY: Texts = Texts(10)
    private var mSpectrum: Lines = Lines(MAX_SPECTRUM_NUM)
    private var mInfo: Texts = Texts(100)
    private var mViewWidth: Int = 0
    private var mViewHeight: Int = 0
    private var mGraphWidth: Int = 0
    private var mGraphHeight: Int = 0
    private var mGraphLeft: Int = 0
    private var mGraphRight: Int = 0
    private var mGraphTop: Int = 0
    private var mGraphBottom: Int = 0
    private var mFreqHzLogUnitWidth = 0.0
    private var mFreqHzLogOffset: Double = 0.0
    private var mAmpDbUnitHeight: Double = 0.0

    constructor(context: Context?) : super(context) {
        init(null, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        Log.d(TAG, "init")

        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.SpectrumView, defStyle, 0
        )
        mGridColor = a.getColor(R.styleable.SpectrumView_gridColor, mGridColor)
        mScaleColor = a.getColor(R.styleable.SpectrumView_scaleColor, mScaleColor)
        mSpectrumColor = a.getColor(R.styleable.SpectrumView_spectrumColor, mSpectrumColor)
        mResultColor = a.getColor(R.styleable.SpectrumView_spectrumColor, mResultColor)
        a.recycle()
//        mSpectrumPaint = Paint()
        mSpectrumPaint!!.textSize = 20f
        //        mSpectrumPaint.setStrokeWidth(1f);
        mSpectrumPaint!!.isAntiAlias = true
        mSampleRate = 0
//        mAmp = null
//        mAmpNum = 0

        // 周波数の対数の範囲を計算する
        mFreqHzLogMin = Math.floor(Math.log10(FREQ_HZ_MIN.toDouble())).toInt()
        mFreqHzLogMax = Math.ceil(Math.log10(FREQ_HZ_MAX.toDouble())).toInt()
        Log.d(TAG, "mFreqHzLogMin: $mFreqHzLogMin") // 1
        Log.d(TAG, "mFreqHzLogMax: $mFreqHzLogMax") // 5

        // 描画オブジェクトのメモリを確保する
//        mGridX = Lines(100)
//        mGridY = Lines(100)
//        mScaleX = Texts(10)
//        mScaleY = Texts(10)
//        mSpectrum = Lines(MAX_SPECTRUM_NUM)
//        mInfo = Texts(100)

        // いったんグラフを計算して初期化する
        calcGrid()
        calcSpectrum()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Viewのサイズが変わったら座標の再計算する
        if (mViewWidth != width || mViewHeight != height) {
            calcGrid()
        }

        // スペクトラムの座標を計算する
        calcSpectrum()

        // 描画する
        drawGraph(canvas)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)

        // 各座標を計算する
        calcGrid()
        calcSpectrum()
    }

    // グラフの計算
    private fun calcGrid() {

        // Viewのサイズを計算する
        mViewWidth = width
        mViewHeight = height
        mGraphWidth = width - GRAPH_PAD_LEFT - GRAPH_PAD_RIGHT
        mGraphHeight = height - GRAPH_PAD_TOP - GRAPH_PAD_BOTTOM
        mGraphLeft = left + GRAPH_PAD_LEFT
        mGraphRight = right - GRAPH_PAD_RIGHT
        mGraphTop = top + GRAPH_PAD_TOP
        mGraphBottom = bottom - GRAPH_PAD_BOTTOM

        // 周波数の対数の単位区間幅を計算する
        mFreqHzLogUnitWidth =
            mGraphWidth.toDouble() / (Math.log10(FREQ_HZ_MAX.toDouble()) - Math.log10(
                FREQ_HZ_MIN.toDouble()
            ))

        // 周波数の対数のオフセットを計算する
        mFreqHzLogOffset = Math.log10(FREQ_HZ_MIN.toDouble()) * mFreqHzLogUnitWidth

        // 振幅の単位高さを計算する
        mAmpDbUnitHeight = mGraphHeight.toDouble() / -AMP_DB_MIN

        // 周波数のグリッドの座標を計算する
        mGridX.init()
        for (i in mFreqHzLogMin until mFreqHzLogMax) {
            for (j in 1..9) {
                val x = mGraphLeft + Math.log10(
                    Math.pow(
                        10.0,
                        i.toDouble()
                    ) * j
                ) * mFreqHzLogUnitWidth - mFreqHzLogOffset
                if (x >= mGraphLeft && x <= mGraphRight) {
                    mGridX.add(
                        x.toFloat(),
                        mGraphTop.toFloat(),
                        x.toFloat(),
                        mGraphBottom.toFloat()
                    )
                }
            }
        }

        // 振幅のグリッドの座標を計算する
        mGridY.init()
        val gridYNum = Math.ceil((-AMP_DB_MIN / AMP_DB_GRID_UNIT).toDouble())
            .toInt() + 1
        for (i in 0 until gridYNum) {
            val y = mGraphTop + mAmpDbUnitHeight * AMP_DB_GRID_UNIT * i
            if (y >= mGraphTop && y <= mGraphBottom) {
                mGridY.add(mGraphLeft.toFloat(), y.toFloat(), mGraphRight.toFloat(), y.toFloat())
            }
        }

        // 周波数の目盛り表示を計算する
        mScaleX.init()
        for (i in mFreqHzLogMin until mFreqHzLogMax) {
            val x = mGraphLeft + Math.log10(
                Math.pow(
                    10.0,
                    i.toDouble()
                )
            ) * mFreqHzLogUnitWidth - mFreqHzLogOffset
            if (x >= mGraphLeft && x <= mGraphRight) {
                mScaleX.add(
                    Math.pow(10.0, i.toDouble()).toInt().toString() + "Hz",
                    x.toFloat() - 20,
                    mGraphBottom.toFloat() + 20
                )
            }
        }

        // 振幅の目盛り表示を計算する
        mScaleY.init()
        val scaleYNum = Math.ceil((-AMP_DB_MIN / AMP_DB_GRID_UNIT).toDouble())
            .toInt() + 1
        for (i in 0 until scaleYNum) {
            val y = mGraphTop + mAmpDbUnitHeight * AMP_DB_GRID_UNIT * i
            mScaleX.add((-AMP_DB_GRID_UNIT * i).toString() + "dB", 10f, y.toFloat() + 5)
        }
    }

    // スペクトラムの計算
    private fun calcSpectrum() {
        // スペクトルを計算する
        mSpectrum.init()
        if (mAmp != null) {
            val spectrumPoint = Points(MAX_SPECTRUM_NUM)

            // スペクトルの座標を計算する
            for (i in 0 until mAmpNum) {
                val freqHz = mSampleRate.toDouble() / 2 / mAmpNum * i
                val x = mGraphLeft + Math.log10(freqHz) * mFreqHzLogUnitWidth - mFreqHzLogOffset
                val spectrumDb = 20.0 * Math.log10(mAmp!![i])
                val y = mGraphTop + mGraphHeight - (-AMP_DB_MIN - -spectrumDb) * mAmpDbUnitHeight
                if (x >= mGraphLeft && x <= mGraphRight) {
                    if (y >= mGraphTop && y <= mGraphBottom) { // TODO
                        spectrumPoint.add(x.toFloat(), y.toFloat())
                    } else {
                        spectrumPoint.add(x.toFloat(), mGraphBottom.toFloat())
                    }
                }
            }

            // 折れ線グラフの座標を計算する
            for (i in 0 until spectrumPoint.getNum() - 1) {
                mSpectrum.add(
                    spectrumPoint.getX(i),
                    spectrumPoint.getY(i),
                    spectrumPoint.getX(i + 1),
                    spectrumPoint.getY(i + 1)
                )
            }
        }

        // 値情報を計算する
        mInfo.init()
        var peakAmp = 0.0
        var peakFreq = 0.0
        for (i in 0 until mAmpNum) {
            if (mAmp!![i] > peakAmp) {
                peakAmp = mAmp!![i]
                peakFreq = mSampleRate.toDouble() / 2 / mAmpNum * i
            }
        }
        mInfo.add("Peak : $peakFreq", (mGraphLeft + 20).toFloat(), (mGraphBottom + 40).toFloat())
    }

    // グラフの描画
    private fun drawGraph(canvas: Canvas) {
        // 枠を描画する
        mSpectrumPaint!!.color = mGridColor
        canvas.drawLine(
            mGraphLeft.toFloat(), mGraphTop.toFloat(), mGraphRight.toFloat(), mGraphTop.toFloat(),
            mSpectrumPaint!!
        )
        canvas.drawLine(
            mGraphLeft.toFloat(), mGraphTop.toFloat(), mGraphLeft.toFloat(), mGraphBottom.toFloat(),
            mSpectrumPaint!!
        )
        canvas.drawLine(
            mGraphRight.toFloat(),
            mGraphTop.toFloat(),
            mGraphRight.toFloat(),
            mGraphBottom.toFloat(),
            mSpectrumPaint!!
        )
        canvas.drawLine(
            mGraphLeft.toFloat(),
            mGraphBottom.toFloat(),
            mGraphRight.toFloat(),
            mGraphBottom.toFloat(),
            mSpectrumPaint!!
        )

        // 周波数のグリッドを描画する
        mSpectrumPaint!!.color = mGridColor
        for (i in 0 until mGridX.getNum()) {
            canvas.drawLine(
                mGridX.getX1(i), mGridX.getY1(i), mGridX.getX2(i), mGridX.getY2(i),
                mSpectrumPaint!!
            )
        }

        // 振幅のグリッドを描画する
        mSpectrumPaint!!.color = mGridColor
        for (i in 0 until mGridY.getNum()) {
            canvas.drawLine(
                mGridY.getX1(i), mGridY.getY1(i), mGridY.getX2(i), mGridY.getY2(i),
                mSpectrumPaint!!
            )
        }

        // 周波数の目盛りを描画する
        mSpectrumPaint!!.color = mScaleColor
        for (i in 0 until mScaleX.getNum()) {
            canvas.drawText(
                mScaleX.getText(i), mScaleX.getX(i), mScaleX.getY(i),
                mSpectrumPaint!!
            )
        }

        // 振幅のグリッドを描画する
        mSpectrumPaint!!.color = mScaleColor
        for (i in 0 until mScaleY.getNum()) {
            canvas.drawText(
                mScaleY.getText(i), mScaleY.getX(i), mScaleY.getY(i),
                mSpectrumPaint!!
            )
        }

        // スペクトラムを描画する
        mSpectrumPaint!!.color = mSpectrumColor
        for (i in 0 until mSpectrum.getNum()) {
            canvas.drawLine(
                mSpectrum.getX1(i), mSpectrum.getY1(i), mSpectrum.getX2(i), mSpectrum.getY2(i),
                mSpectrumPaint!!
            )
        }

        // 値情報を描画する
        mSpectrumPaint!!.color = mResultColor
        canvas.drawText(mInfo.getText(0), mInfo.getX(0), mInfo.getY(0), mSpectrumPaint!!)
    }

    // スペクトラムデータ設定
    fun setAmp(spectrum: DoubleArray?, spectrumNum: Int, sampleRate: Int) {
        mAmp = spectrum
        mAmpNum = spectrumNum
        mSampleRate = sampleRate
    }

    // 描画の更新
    fun update() {
        invalidate()
    }

    companion object {
        private const val TAG = "SpectrumView"
        private const val MAX_SPECTRUM_NUM = 8192
        private const val FREQ_HZ_MIN = 20 // 表示する最小周波数
        private const val FREQ_HZ_MAX = 22050 // 表示する最大周波数
        private const val AMP_DB_MIN = -120 // 表示する最小デシベル数
        private const val AMP_DB_GRID_UNIT = 20 // 表示するデシベル数のグリッド間隔
        private const val GRAPH_PAD_LEFT = 60
        private const val GRAPH_PAD_RIGHT = 10
        private const val GRAPH_PAD_TOP = 20
        private const val GRAPH_PAD_BOTTOM = 80
    }
}


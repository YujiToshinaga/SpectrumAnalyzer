package com.example.spectrumanalyzer

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.app.ActivityCompat


class SpectrumRecorder internal constructor(sampleRate: Int, samples: Int) {
    private var mRecordPositionUpdateListener: OnRecordPositionUpdateListener?
    private var mAudioRecord: AudioRecord?
    private val mSampleRate: Int
    var samples: Int
    private var mAmp: DoubleArray?
    private var mAmpNum: Int

    init {
        Log.d(TAG, "SpectrumRecorder")
        mRecordPositionUpdateListener = null
        mAudioRecord = null
        mSampleRate = sampleRate
        this.samples = samples
        mAmp = null
        mAmpNum = 0
    }

    interface OnRecordPositionUpdateListener {
        fun onPeriodicNotification(amp: DoubleArray?, ampNum: Int)
    }

    fun setRecordPositionUpdateListener(listener: OnRecordPositionUpdateListener?) {
        mRecordPositionUpdateListener = listener
    }

    fun start() {
        val minBufferSize: Int
        mAmpNum = samples / 2
        mAmp = DoubleArray(mAmpNum)
        for (i in 0 until mAmpNum) {
            mAmp!![i] = 0.0
        }

        // 必要となるバッファサイズを計算する
        minBufferSize = AudioRecord.getMinBufferSize(
            mSampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        Log.d(TAG, "bufferSize: $minBufferSize")

        // AudioRecordを初期化する
        mAudioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            mSampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            samples * 2 // 16bit = 2byteだから
        )

        // AudioRecord録音の設定をする
        mAudioRecord!!.setRecordPositionUpdateListener(object :
            AudioRecord.OnRecordPositionUpdateListener {
            override fun onPeriodicNotification(recorder: AudioRecord) {
                val buf: ShortArray
                val fft: Fft4g
                val fftData: DoubleArray

                // エラー処理
                if (mAudioRecord == null) {
                    return
                }

                // バッファの初期化をする
                buf = ShortArray(samples)
                fft = Fft4g(samples)
                fftData = DoubleArray(samples)

                // 録音データを読み出す
                mAudioRecord!!.read(buf, 0, buf.size)

                // FFTへの入力データを作成する
                for (i in 0 until samples) {
                    fftData[i] = buf[i].toDouble()
                }

                // FFTを実行する
                fft.rdft(1, fftData)

                // 正規化された振幅を計算する
                for (i in 0 until mAmpNum) {
                    mAmp!![i] = (Math.sqrt(
                        Math.pow(fftData[i * 2] / mAmpNum, 2.0)
                                + Math.pow(fftData[i * 2 + 1] / mAmpNum, 2.0)
                    ) / Math.pow(2.0, BIT_PER_SAMPLE.toDouble())) / Math.sqrt(2.0)

//                    if (i == 40) {
//                        Log.d(TAG, "freq " + (((double)44100 / 2) / mAmpNum) * i);
//                        Log.d(TAG, "mAmp " + mAmp[i]);
//                        Log.d(TAG, "fftData " + fftData[i * 2]);
//                        Log.d(TAG, "fftDataNorm " + fftData[i * 2] / mAmpNum);
//                        Log.d(TAG, "fftDataNorm " + fftData[i * 2 + 1] / mAmpNum);
//                    }
                }

                // コールバック関数を呼び出す
                mRecordPositionUpdateListener!!.onPeriodicNotification(mAmp, mAmpNum)
            }

            override fun onMarkerReached(recorder: AudioRecord) {}
        })

        // 通知間隔を受信周期にする
        mAudioRecord!!.positionNotificationPeriod = samples

        // 録音する
        mAudioRecord!!.startRecording()
    }

    fun stop() {
        // エラー処理
        if (mAudioRecord == null) {
            Log.d(TAG, "stop : not initialized")
            return
        }

        // 停止する
        mAudioRecord!!.stop()

        // 変数を初期化する
        mAudioRecord = null
        mAmpNum = 0
        mAmp = null
    }

    companion object {
        private val TAG = "SpectrumRecorder"
        private val BIT_PER_SAMPLE = 16
    }
}

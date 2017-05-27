package com.example.spectrumanalyzer;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * Created by usr1 on 2017/05/21.
 */

public class SpectrumRecorder {
    private static final String TAG = "SpectrumRecorder";
    private static final int BIT_PER_SAMPLE = 16;
    private SpectrumRecorder.OnRecordPositionUpdateListener mRecordPositionUpdateListener;
    private AudioRecord mAudioRecord;
    private int mSampleRate;
    private int mSamples;
    private int mFreqNum;
    private double[] mSpectrum;

    SpectrumRecorder(int sampleRate) {
        Log.d(TAG, "SpectrumRecorder");
        mRecordPositionUpdateListener = null;
        mAudioRecord = null;
        mSampleRate = sampleRate;
        mSamples = 0;
        mFreqNum = 0;
        mSpectrum = null;
    }

    public interface OnRecordPositionUpdateListener {
        public void onPeriodicNotification(double[] spectrum, int spectrumNum);
    }

    public void setRecordPositionUpdateListener(SpectrumRecorder.OnRecordPositionUpdateListener listener) {
        mRecordPositionUpdateListener = listener;
    }

    public void start(int samples) {
        int minBufferSize;

        mSamples = samples;
        mFreqNum = mSamples / 2;
        mSpectrum = new double[mFreqNum];
        for (int i = 0; i < mFreqNum; i++) {
            mSpectrum[i] = 0.0;
        }

        // 必要となるバッファサイズを計算する
        minBufferSize = AudioRecord.getMinBufferSize(
                mSampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );
        Log.d(TAG, "bufferSize: " + minBufferSize);

        // AudioRecordを初期化する
        mAudioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                mSampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                mSamples * 2                        // 16bit = 2byteだから
        );

        // AudioRecord録音の設定をする
        mAudioRecord.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
            @Override
            public void onPeriodicNotification(AudioRecord recorder) {
                short buf[];
                Fft4g fft;
                double[] fftData;

                // エラー処理
                if (mAudioRecord == null) {
                    return;
                }

                // バッファの初期化をする
                buf = new short[mSamples];
                fft = new Fft4g(mSamples);
                fftData = new double[mSamples];

                // 録音データを読み出す
                mAudioRecord.read(buf, 0, buf.length);

                // FFTへの入力データを作成する
                for (int i = 0; i < mSamples; i++) {
                    fftData[i] = (double)buf[i];
                }

                // FFTを実行する
                fft.rdft(1, fftData);

                // 正規化された振幅を計算する
                for (int i = 0; i < mFreqNum; i++) {
                    mSpectrum[i] = (Math.sqrt(
                            Math.pow(fftData[i * 2] / mFreqNum, 2)
                                    + Math.pow(fftData[i * 2 + 1] / mFreqNum, 2)
                    ) / Math.pow(2, BIT_PER_SAMPLE)) / Math.sqrt(2);

//                    if (i == 40) {
//                        Log.d(TAG, "freq " + (((double)44100 / 2) / mFreqNum) * i);
//                        Log.d(TAG, "mSpectrum " + mSpectrum[i]);
//                        Log.d(TAG, "fftData " + fftData[i * 2]);
//                        Log.d(TAG, "fftDataNorm " + fftData[i * 2] / mFreqNum);
//                        Log.d(TAG, "fftDataNorm " + fftData[i * 2 + 1] / mFreqNum);
//                    }
                }

                // コールバック関数を呼び出す
                mRecordPositionUpdateListener.onPeriodicNotification(mSpectrum, mFreqNum);
            }

            @Override
            public void onMarkerReached(AudioRecord recorder) {
            }
        });

        // 通知間隔を受信周期にする
        mAudioRecord.setPositionNotificationPeriod(mSamples);

        // 録音する
        mAudioRecord.startRecording();
    }

    public void stop() {
        // エラー処理
        if (mAudioRecord == null) {
            Log.d(TAG, "stop : not initialized");
            return;
        }

        // 停止する
        mAudioRecord.stop();

        // 変数を初期化する
        mAudioRecord = null;
        mSamples = 0;
        mFreqNum = 0;
        mSpectrum = null;
    }
}

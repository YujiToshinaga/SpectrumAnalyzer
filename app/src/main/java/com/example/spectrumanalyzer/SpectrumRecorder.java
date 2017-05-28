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
    private double[] mAmp;
    private int mAmpNum;

    SpectrumRecorder(int sampleRate, int samples) {
        Log.d(TAG, "SpectrumRecorder");
        mRecordPositionUpdateListener = null;
        mAudioRecord = null;
        mSampleRate = sampleRate;
        mSamples = samples;
        mAmp = null;
        mAmpNum = 0;
    }

    public interface OnRecordPositionUpdateListener {
        public void onPeriodicNotification(double[] amp, int ampNum);
    }

    public void setRecordPositionUpdateListener(SpectrumRecorder.OnRecordPositionUpdateListener listener) {
        mRecordPositionUpdateListener = listener;
    }

    public void start() {
        int minBufferSize;

        mAmpNum = mSamples / 2;
        mAmp = new double[mAmpNum];
        for (int i = 0; i < mAmpNum; i++) {
            mAmp[i] = 0.0;
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
                for (int i = 0; i < mAmpNum; i++) {
                    mAmp[i] = (Math.sqrt(
                            Math.pow(fftData[i * 2] / mAmpNum, 2)
                                    + Math.pow(fftData[i * 2 + 1] / mAmpNum, 2)
                    ) / Math.pow(2, BIT_PER_SAMPLE)) / Math.sqrt(2);

//                    if (i == 40) {
//                        Log.d(TAG, "freq " + (((double)44100 / 2) / mAmpNum) * i);
//                        Log.d(TAG, "mAmp " + mAmp[i]);
//                        Log.d(TAG, "fftData " + fftData[i * 2]);
//                        Log.d(TAG, "fftDataNorm " + fftData[i * 2] / mAmpNum);
//                        Log.d(TAG, "fftDataNorm " + fftData[i * 2 + 1] / mAmpNum);
//                    }
                }

                // コールバック関数を呼び出す
                mRecordPositionUpdateListener.onPeriodicNotification(mAmp, mAmpNum);
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
        mAmpNum = 0;
        mAmp = null;
    }

    public void setSamples(int samples) {
        mSamples = samples;
    }

    public int getSamples() {
        return mSamples;
    }
}

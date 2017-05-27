package com.example.spectrumanalyzer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * Created by usr1 on 2017/05/20.
 */

public class WavePlayer {
    private static final String TAG = "WavePlayer";
    private AudioTrack mAudioTrack;
    private WaveGen mWaveGen;
    private int mSampleRate;
    private int mFreq;
    private int mSamples;

    WavePlayer(int sampleRate) {
        int bufferSize;

        Log.d(TAG, "Initialize WavePlayer");

        mSampleRate = sampleRate;
        mFreq = 0;

        // 必要となるバッファサイズを計算する
        bufferSize = AudioTrack.getMinBufferSize(
                mSampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        // AudioTrackを初期化する
        mAudioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                mSampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM
        );

        // 一度に書き込むサンプル数を計算する
        mSamples = bufferSize / 4;

        // WaveGenを初期化する
        mWaveGen = new WaveGen(mFreq, mSampleRate, mSamples);

        // AudioTrack再生の設定をする
        mAudioTrack.setPlaybackPositionUpdateListener(
                new AudioTrack.OnPlaybackPositionUpdateListener() {
                    public void onPeriodicNotification(AudioTrack track) {
                        short[] buf;
                        buf = mWaveGen.getBuf();
                        mAudioTrack.write(buf, 0, buf.length);
                    }

                    public void onMarkerReached(AudioTrack track) {
                    }
                }
        );

        // Listnerを呼び出す周期を設定する
        mAudioTrack.setPositionNotificationPeriod(mSamples);
    }

    public void changeParam(int freq) {
        // エラー処理
        if (mAudioTrack == null) {
            Log.d(TAG, "start : not initialized");
            return;
        }

        // パラメータを変更する
        mWaveGen.changeParam(freq);
    }

    public void play() {
        // エラー処理
        if (mAudioTrack == null) {
            Log.d(TAG, "start : not initialized");
            return;
        }

        // 再生する
        short[] buf;
        buf = mWaveGen.getBuf();
        mAudioTrack.write(buf, 0, buf.length);
        buf = mWaveGen.getBuf();
        mAudioTrack.write(buf, 0, buf.length);
        mAudioTrack.play();

    }

    public void stop() {
        // エラー処理
        if (mAudioTrack == null) {
            Log.d(TAG, "start : not initialized");
            return;
        }

        // 停止する
        mAudioTrack.stop();
    }
}

package com.example.spectrumanalyzer

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log


class WavePlayer internal constructor(sampleRate: Int) {
    private val mAudioTrack: AudioTrack?
    private val mWaveGen: WaveGen
    private val mSampleRate: Int
    private val mFreq: Int
    private val mSamples: Int

    init {
        val bufferSize: Int
        Log.d(TAG, "Initialize WavePlayer")
        mSampleRate = sampleRate
        mFreq = 0

        // 必要となるバッファサイズを計算する
        bufferSize = AudioTrack.getMinBufferSize(
            mSampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        // AudioTrackを初期化する
        mAudioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            mSampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            AudioTrack.MODE_STREAM
        )

        // 一度に書き込むサンプル数を計算する
        mSamples = bufferSize / 4 // 16bit=2byteで割って、さらに1/2ずつバッファ書き込みする

        // WaveGenを初期化する
        mWaveGen = WaveGen(mFreq, mSampleRate, mSamples)

        // AudioTrack再生の設定をする
        mAudioTrack.setPlaybackPositionUpdateListener(
            object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onPeriodicNotification(track: AudioTrack) {
                    val buf: ShortArray
                    buf = mWaveGen.buf
                    mAudioTrack.write(buf, 0, buf.size)
                }

                override fun onMarkerReached(track: AudioTrack) {}
            }
        )

        // Listnerを呼び出す周期を設定する
        mAudioTrack.positionNotificationPeriod = mSamples
    }

    fun changeParam(freq: Int) {
        // エラー処理
        if (mAudioTrack == null) {
            Log.d(TAG, "start : not initialized")
            return
        }

        // パラメータを変更する
        mWaveGen.changeParam(freq)
    }

    fun play() {
        // エラー処理
        if (mAudioTrack == null) {
            Log.d(TAG, "start : not initialized")
            return
        }

        // 再生する
        var buf: ShortArray
        buf = mWaveGen.buf
        mAudioTrack.write(buf, 0, buf.size)
        buf = mWaveGen.buf
        mAudioTrack.write(buf, 0, buf.size)
        mAudioTrack.play()
    }

    fun stop() {
        // エラー処理
        if (mAudioTrack == null) {
            Log.d(TAG, "start : not initialized")
            return
        }

        // 停止する
        mAudioTrack.stop()
    }

    companion object {
        private const val TAG = "WavePlayer"
    }
}
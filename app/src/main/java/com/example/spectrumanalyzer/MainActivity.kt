package com.example.spectrumanalyzer

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.spectrumanalyzer.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    // 定数
    private val RECORD_AUDIO_PERMISSION = 0x1
    private val TAG = "MainActivity"
    private val SAMPLE_RATE = 44100

    // メンバ変数
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mAudioManager: AudioManager
    private lateinit var mSpectrumRecorder: SpectrumRecorder
    private lateinit var mWavePlayer: WavePlayer
    private var mSaveVolume = 0
    private var mVolume = 0
    private var mSamples = 0
    private var mFreq1 = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)

        // View Bindingを用いてactivity_main.xmlの部品にアクセスする
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mBinding.root
        setContentView(view)

        // システムの音量にアクセスするためのインスタンスを生成する
        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        // 各インスタンスを生成する
        mSpectrumRecorder = SpectrumRecorder(SAMPLE_RATE, 2048)
        mWavePlayer = WavePlayer(SAMPLE_RATE)

        // メンバ変数を初期化する
        mSaveVolume = 0
        mVolume = mSaveVolume
        mSamples = 2048
        mFreq1 = 1000

        // AUDIO_RECORDのアクセス権を取得する
        permitAudioRecord()

        // Analyzeボタンが押された時の処理をする
        mBinding.toggleButtonAnalyze.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                Log.d(TAG, "Analyze")
                val idx = mBinding.spinnerSamples.selectedItemPosition
                mSamples = when (idx) {
                    0 -> 2048
                    1 -> 4096
                    2 -> 8192
                    else -> 2048
                }
                mSpectrumRecorder.samples = mSamples
                mSpectrumRecorder.start()
            } else {
                Log.d(TAG, "Stop")
                mSpectrumRecorder.stop()
            }
        })

        // -------- Play --------
        mBinding.toggleButtonPlay.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                Log.d(TAG, "Play")
                mFreq1 = mBinding.editTextFreq1.text.toString().toInt()
                mWavePlayer.changeParam(mFreq1)
                mWavePlayer.play()
            } else {
                Log.d(TAG, "Stop")
                mWavePlayer.stop()
            }
        })

        mSpectrumRecorder.setRecordPositionUpdateListener(object :
            SpectrumRecorder.OnRecordPositionUpdateListener {
            override fun onPeriodicNotification(amp: DoubleArray?, ampNum: Int) {
                mBinding.spectrumView.setAmp(amp, ampNum, SAMPLE_RATE)
                mBinding.spectrumView.update()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
        mSaveVolume = mAudioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
        mAudioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, mVolume, 0)
    }

    override fun onPause() {
        super.onPause()
        mVolume = mAudioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
        mAudioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, mSaveVolume, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        mAudioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, mSaveVolume, 0)
    }

    // RECORD_AUDIOのアクセス権を取得する
    private fun permitAudioRecord() {

        // ユーザーがAUDIO_RECORDへのアクセスを許可したかどうか表示する
        // コールバック関数のようなものを定義する
        val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this@MainActivity,
                        "AUDIO_RECORD OK!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@MainActivity,
                        "AUDIO_RECORD NG!", Toast.LENGTH_LONG).show()
            }
        }

        // RECORD_AUDIOへのアクセスが許可されているかどうかを取得する
        val audioPermission = ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.RECORD_AUDIO)

        // アクセスが許可されていない場合は、許可を求める
        if (audioPermission != PackageManager.PERMISSION_GRANTED) {
            // 過去にRECORD_AUDIOへのアクセスの許可を求めるダイアログで、
            // 許可せずに今後表示しない選択をしたかどうかを取得する
            val audioRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.RECORD_AUDIO)

            // 今後表示しないを選択していた場合は、設定からアクセスを許可するようダイアログ表示する
            if (audioRationale == true) {
                AlertDialog.Builder(this)
                    .setMessage("「設定」でAUDIO_RECORDへのアクセスを許可してください")
                    .setPositiveButton("OK") { dialog, which ->
                        // TODO: OKが押された時の処理を書く
                    }
                    .create().show()

            // 今後表示しないを選択していなかった場合は、アクセス許可を求めるダイアログを表示する
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
            }
        } else {
            // 過去に許可されているので何もしない
        }
    }
}
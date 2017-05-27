package com.example.spectrumanalyzer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int RECORD_AUDIO_PERMISSION = 0x1;
    private static final int SAMPLE_RATE = 44100;
    private SpectrumView mSpectrumView;
    private ToggleButton mToggleButtonAnalyze;
    private Spinner mSpinnerSamples;
    private ToggleButton mToggleButtonPlay;
    private EditText mEditTextFreq1;
    private AudioManager mAudioManager;
    private SpectrumRecorder mSpectrumRecorder;
    private WavePlayer mWavePlayer;
    private int mSaveVolume;
    private int mVolume;
    private int mSamples;
    private int mFreq1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permitAudioRecord();

        // 描画用変数を初期化する
        mSpectrumView = (SpectrumView)findViewById(R.id.spectrumView);
        mToggleButtonAnalyze = (ToggleButton)findViewById(R.id.toggleButtonAnalyze);
        mSpinnerSamples = (Spinner)findViewById(R.id.spinnerSamples);
        mToggleButtonPlay = (ToggleButton)findViewById(R.id.toggleButtonPlay);
        mEditTextFreq1 = (EditText)findViewById(R.id.editTextFreq1);

        // インスタンスと変数を初期化する
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mSpectrumRecorder = new SpectrumRecorder(SAMPLE_RATE);
        mWavePlayer = new WavePlayer(SAMPLE_RATE);
        mSaveVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mVolume = mSaveVolume;
        mSamples = 0;
        mFreq1 = 0;

        // -------- Analyze --------
        mToggleButtonAnalyze.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "Analyze");
                    int idx = mSpinnerSamples.getSelectedItemPosition();
                    switch (idx) {
                        case 0: mSamples = 2048; break;
                        case 1: mSamples = 4096; break;
                        case 2: mSamples = 8192; break;
                        default: mSamples = 2048; break;
                    }
                    mSpectrumRecorder.start(mSamples);
                } else {
                    Log.d(TAG, "Stop");
                    mSpectrumRecorder.stop();
                }
            }
        });

        // -------- Play --------
        mToggleButtonPlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "Play");
                    mFreq1 = Integer.parseInt(mEditTextFreq1.getText().toString());
                    mWavePlayer.changeParam(mFreq1);
                    mWavePlayer.play();
                } else {
                    Log.d(TAG, "Stop");
                    mWavePlayer.stop();
                }
            }
        });

        mSpectrumRecorder.setRecordPositionUpdateListener(new SpectrumRecorder.OnRecordPositionUpdateListener() {
            @Override
            public void onPeriodicNotification(double[] spectrum, int spectrumNum) {
                mSpectrumView.setSpectrum(spectrum, spectrumNum, SAMPLE_RATE);
                mSpectrumView.update();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mSaveVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolume, 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mSaveVolume, 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mSaveVolume, 0);
    }

    // RECORD_AUDIOの許可
    private void permitAudioRecord() {
        // 以前に許可されたかどうかチェックする
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // 許可を求める
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION);
            }
        } else {
            // 以前に許可されているので何もしない
        }
    }

    // RECORD_AUDIOの許可結果を受け取る
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RECORD_AUDIO_PERMISSION:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(MainActivity.this, "AUDIO_RECORD OK!",Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "AUDIO_RECORD NG!",Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

}

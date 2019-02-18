package com.iflytek.aiui.demo.chatsdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.Window;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.iflytek.aiui.demo.chatsdk.speech.abstracts.BaseSpeechCallback;

import java.io.File;

/**
 * 语义理解demo。
 *
 * @author ydong
 */
public class NlpDemo extends Activity {


    private static final String TAG = "ydong";
    private TextView displaytext;

    @Override
    @SuppressLint("ShowToast")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.nlpdemo);
        displaytext = findViewById(R.id.displaytext);

        initlog();
        initAIUI();
    }


    private void initAIUI() {
        SpeechManager.getInstance().createAgent();
        SpeechManager.getInstance().setBaseSpeechCallback(speechCallback);
    }

    BaseSpeechCallback speechCallback = new BaseSpeechCallback() {
        @Override
        public void recognizeResult(String text) {
            LogUtils.d(TAG, "recognizeResult::" + text);
        }

        @Override
        public void nlpResult(String json) {
            displaytext.setText(json);
            LogUtils.d(TAG, "nlpResult::" + json);
        }

        @Override
        public void onRecognizeProgress(String text, boolean isSecondRecognize) {
            super.onRecognizeProgress(text, isSecondRecognize);
        }

        @Override
        public void onVolumeChanged(int volume, byte[] arg1) {
            super.onVolumeChanged(volume, arg1);
        }

        @Override
        public void onEndOfSpeech() {
            super.onEndOfSpeech();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SpeechManager.getInstance().destroyAgent();
    }

    /**
     * log to /sdcard/anzerTTS/
     */
    private void initlog() {
        Utils.init(getApplicationContext());
        LogUtils.Builder builder = new LogUtils.Builder().setBorderSwitch(false).setLog2FileSwitch(true)
                .setDir(new File(Environment.getExternalStorageDirectory().getPath() + "/AIUINEW"))
                .setLogSwitch(true);
    }


}

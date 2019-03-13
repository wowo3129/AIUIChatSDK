package com.iflytek.aiui.demo.chatsdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.aiuisdk.BaseSpeechCallback;
import com.aiuisdk.SpeechManager;

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
        setContentView(R.layout.nlpdemo);
        initView();
        initChatSDK();
    }

    private void initView() {
        displaytext = findViewById(R.id.displaytext);
    }


    private void initChatSDK() {
        SpeechManager.CreateInstance(getApplicationContext());
        SpeechManager.getInstance().setBaseSpeechCallback(speechCallback);
    }

    BaseSpeechCallback speechCallback = new BaseSpeechCallback() {
        @Override
        public void recognizeResult(String text) {
            Log.d(TAG, "recognizeResult::" + text);
            displaytext.setText(text);

        }

        @Override
        public void nlpResult(String text, String json) {
            Log.d(TAG, "nlpResult::" + text);
            SpeechManager.onSpeaking(text);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SpeechManager.getInstance().destroyAgent();
    }

}

package com.iflytek.aiui.demo.chatsdk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.aiuisdk.BaseSpeechCallback;
import com.aiuisdk.SpeechManager;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.disposables.Disposable;

/**
 * 语义理解demo。
 *
 * @author ydong
 */
public class NlpDemo extends Activity {


    private static final String TAG = "ydong";
    private TextView question;
    private TextView answer;

    @Override
    @SuppressLint("ShowToast")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nlpdemo);
        initPermission();
        initView();
    }

    private void initView() {
        question = findViewById(R.id.question);
        answer = findViewById(R.id.answer);
    }


    private void initChatSDK() {
        SpeechManager.CreateInstance(getApplicationContext());
        SpeechManager.getInstance().setBaseSpeechCallback(speechCallback);
    }

    BaseSpeechCallback speechCallback = new BaseSpeechCallback() {
        @Override
        public void recognizeResult(String text) {
            Log.d(TAG, "recognizeResult::" + text);
            question.setText(text);

        }

        @Override
        public void nlpResult(String text, String json) {
            Log.d(TAG, "nlpResult::" + text);
            answer.setText(text);
            SpeechManager.onSpeaking(text);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SpeechManager.getInstance().onDestroy();
    }

    @SuppressLint("CheckResult")
    private void initPermission() {
        Disposable subscribe = new RxPermissions(this).request(Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CHANGE_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE)
                .subscribe(granted -> {
                    if (granted) {
                        Toast.makeText(this, "获取权限成功", Toast.LENGTH_SHORT).show();
                        initChatSDK();
                    } else {
                        Toast.makeText(this, "请先获取权限后使用", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

}

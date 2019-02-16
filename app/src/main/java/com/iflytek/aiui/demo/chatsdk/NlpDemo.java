package com.iflytek.aiui.demo.chatsdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.Window;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;

import java.io.File;

/**
 * 语义理解demo。
 *
 * @author ydong
 */
public class NlpDemo extends Activity {


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
        SpeechManager.CreateSpeechUtility(getApplication(), "5b614ca0");
        SpeechManager.getInstance().createAgent();
    }

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

package com.huimin.baidulib;


import android.content.Context;

import com.huimin.baidulib.listener.IRecogListener;
import com.huimin.baidulib.listener.ISpeakListener;

/**
 * Created by kermitye
 * Date: 2018/5/24 17:20
 * Desc:
 */
public class BaiduSpeechManager {
    private static final String TAG = BaiduSpeechManager.class.getSimpleName();


    private BaiduSpeechManager() {}

    private static class SingletonHolder {
        public static final BaiduSpeechManager INSTANCE = new BaiduSpeechManager();
    }

    public static BaiduSpeechManager getInstance() {
        return SingletonHolder.INSTANCE;
    }


    public void init(Context context) {
        TtsHelper.getInstance().init(context);
        RecogHelper.getInstance().init(context);
    }


    public void setRecogListener(IRecogListener listener) {
        RecogHelper.getInstance().setRecogListener(listener);
    }

    public void setSpeakListener(ISpeakListener listener) {
        TtsHelper.getInstance().setSpeakListener(listener);
    }

    public void startRecog() {
        RecogHelper.getInstance().start();
    }

    public void stopRecog() {
        RecogHelper.getInstance().stop();
    }

    public void startSpeak(String text) {
        this.startSpeak(text, false);
    }

    public void startSpeak(String text, boolean isInterrup) {
        if (isInterrup)
            TtsHelper.getInstance().stopSpeak();
        TtsHelper.getInstance().startSpeak(text);
    }

    public void stopSpeak() {
        TtsHelper.getInstance().stopSpeak();
    }

    public boolean isSpeaking() {
        return TtsHelper.getInstance().isSpeaking();
    }

    public void destory() {
        RecogHelper.getInstance().destory();
        TtsHelper.getInstance().destory();
    }

}

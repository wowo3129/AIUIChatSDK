package com.ydong.iflylib;

import android.content.Context;

import com.ydong.iflylib.helper.RecognizerHelper;
import com.ydong.iflylib.helper.TtsHelper;
import com.ydong.iflylib.listener.IRecognizerListener;
import com.ydong.iflylib.listener.ISpeakListener;
import com.ydong.iflylib.listener.SpeakListener;
import com.iflytek.cloud.SpeechUtility;

/**
 * 讯飞语音管理类唯一入口,语音识别合成的管理
 *
 * @author ydong
 */
public class SpeechManager {

    private static SpeechManager speechManager;

    public static SpeechManager getInstance() {
        if (speechManager == null) {
            speechManager = new SpeechManager();
        }
        return speechManager;
    }

    /**
     * 在application 中初始化语音基础模块
     *
     * @param context 上下文
     */
    public void init(Context context) {
        SpeechUtility.createUtility(context, "appid=" + context.getString(R.string.appid));
    }

    /**
     * 初始化语音识别和合成模块
     *
     * @param context 上下文
     */
    public void initStart(Context context) {
        RecognizerHelper.getInstance().init(context);
        TtsHelper.getInstance().init(context);
    }

    //=====================语音听写=================================

    /**
     * 设置语音识别回调接口
     *
     * @param listener 回调接口
     */
    public void setRecognizerListener(IRecognizerListener listener) {
        RecognizerHelper.getInstance().setIRecognizerListener(listener);
    }

    /**
     * 开始识别
     */
    public void startReco() {
        RecognizerHelper.getInstance().startRecognizer();
    }

    /**
     * 暂停识别
     */
    public void stopReco() {
        RecognizerHelper.getInstance().stopRecognizer();
    }

    /**
     * 销毁识别
     */
    public void destoryReco() {
        RecognizerHelper.getInstance().destory();
    }


    //======================语音合成=======================================

    /**
     * 设置语音合成接口回调
     *
     * @param listener 回调接口
     */
    public void setSpeakListener(final ISpeakListener listener) {
        TtsHelper.getInstance().setSpeakListener(new SpeakListener() {
            @Override
            public void onSpeakBegin(String text) {
                listener.onSpeakBegin(text);
            }

            @Override
            public void onSpeakOver(String msg) {
                listener.onSpeakOver(msg);
            }

            @Override
            public void onInterrupted() {
                listener.onInterrupted();
            }
        });
    }

    /**
     * 开始合成
     *
     * @param text     合成的文字
     * @param listener 合成结束后的回调
     */
    public void startSpeak(String text, final ISpeakListener listener) {
        TtsHelper.getInstance().startSpeak(text, new SpeakListener() {
            @Override
            public void onSpeakBegin(String text) {
                listener.onSpeakBegin(text);
            }

            @Override
            public void onSpeakOver(String msg) {
                listener.onSpeakOver(msg);
            }

            @Override
            public void onInterrupted() {
                listener.onInterrupted();
            }
        });
    }

    /**
     * 开始合成
     *
     * @param text 待合成文字
     */
    public void startSpeak(String text) {
        TtsHelper.getInstance().startSpeak(text);
    }

    /**
     * @return 是否正在合成的状态
     */
    public boolean isSpeaking() {
        return TtsHelper.getInstance().isSpeaking();
    }

    /**
     * 停止合成
     */
    public void stopSpeak() {
        TtsHelper.getInstance().stopSpeak();
    }

    /**
     * 退出时销毁合成对象
     */
    public void destorySpeak() {
        TtsHelper.getInstance().destory();
    }

    /**
     * 销毁识别和合成
     */
    public void release() {
        destoryReco();
        destorySpeak();
    }


}

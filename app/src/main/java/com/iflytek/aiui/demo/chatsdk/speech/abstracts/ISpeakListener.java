package com.iflytek.aiui.demo.chatsdk.speech.abstracts;

import com.iflytek.cloud.SpeechError;

/**
 * 合成回调
 *
 * @author ydong
 */
public interface ISpeakListener {

    /**
     * 合成播放完毕回调
     *
     * @param errorCode 错误号
     */
    public void onSpeakOver(SpeechError errorCode);

    /**
     * 合成播放被打断
     */
    public void onInterrupted();

    public void onSpeakBegin(String vo);

}

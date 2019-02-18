package com.iflytek.aiui.demo.chatsdk.speech.abstracts;

/**
 * Project Name：AnzerPlatServiceDemo
 * Description ：
 *
 * @author ：ZouWeiLin on 2018.08.24
 */
public abstract class BaseSpeechCallback {

    /**
     * 通过识别语音得到文字，允许外部调用
     *
     * @param text 识别后的文本
     */
    public void recognizeResult(String text) {

    }

    /**
     * @param json 返回三方语义结果
     */
    public void nlpResult(String json) {

    }

    /**
     * 识别过程，该过程为识别过程，不应拿该文字进行语义识别
     *
     * @param text              识别过程中的文字
     * @param isSecondRecognize {@code true}表示是指令引擎识别到结果，上层不需要对为true的回调作出识别
     */
    public void onRecognizeProgress(String text, boolean isSecondRecognize) {

    }

    /**
     * 识别过程中的音量
     *
     * @param volume 音量
     * @param arg1   进行识别的数据
     */
    public void onVolumeChanged(int volume, byte[] arg1) {

    }

    /**
     * 结束说话
     */
    public void onEndOfSpeech() {

    }

}

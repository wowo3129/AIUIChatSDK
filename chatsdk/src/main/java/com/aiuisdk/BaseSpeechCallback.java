package com.aiuisdk;

/**
 * Project Name：AnzerPlatServiceDemo
 * Description ：
 *
 * @author ：ydong on 2018.08.24
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
     * @param text 返回三方语义结果，可直接speak出来
     * @param json 返回三方语义结果json串，可根据需求自行解析
     */
    public void nlpResult(String text, String json) {

    }

}

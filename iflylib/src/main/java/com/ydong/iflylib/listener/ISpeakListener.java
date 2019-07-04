package com.ydong.iflylib.listener;

/**
 * @author ydong
 */
public interface ISpeakListener {
    /**
     * @param text 开始合成
     */
    void onSpeakBegin(String text);

    /**
     * @param msg 结束合成
     */
    void onSpeakOver(String msg);

    /**
     * 中断合成
     */
    void onInterrupted();
}

package com.huimin.baidulib.listener;

/**
 * Created by kermitye
 * Date: 2018/5/24 10:33
 * Desc:
 */
public interface ISpeakListener {
    void onSpeakBegin(String text);

    void onSpeakOver(String msg);

    void onInterrupted();
}

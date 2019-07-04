package com.ydong.iflylib.listener;

/**
 * 功能：语音识别回调
 *
 * @author ydong
 */
public interface IRecognizerListener {
    /**
     * 当前正在说话的音量大小
     *
     * @param volume 音量大小
     */
    void onVolumeChanged(int volume);

    /**
     * 返回问题所对应的听写结果
     *
     * @param result 听写结果
     */
    void onResult(String result);

    /**
     * 错误信息反馈
     *
     * @param msg 错误信息
     */
    void onError(String msg);
}

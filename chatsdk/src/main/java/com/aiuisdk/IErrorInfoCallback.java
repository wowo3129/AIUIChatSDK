package com.aiuisdk;

/**
 * 错误信息回调接口
 *
 * @author ydong
 * @see SpeechManager#getInstance().setErrorInfoCallback();方法来设置错误回调，根据返回的信息自行处理错误
 */
public interface IErrorInfoCallback {
    /**
     * @param code 错误码
     * @param info 错误原因
     */
    void errorCallback(int code, String info);
}

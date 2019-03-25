package com.huimin.baidulib.listener;

/**
 * Created by kermitye
 * Date: 2018/5/24 18:12
 * Desc:
 */
public interface IRecogListener {
    void onVolumeChanged(int volume);

    void onResult(String result);

    void onError(String msg);
}

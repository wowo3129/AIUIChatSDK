package com.huimin.baidulib.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by kermitye
 * Date: 2018/5/25 14:31
 * Desc:
 */
public class VolumeBean {

    /**
     * volume : 56
     * volume-percent : 1
     */

    public int volume;
    @SerializedName("volume-percent")
    public int volumepercent;
}

package com.aiuisdk.config;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * AIUI语音合成发音人
 */
@StringDef(value = {
        ITTSLanguage.jiajia,
        ITTSLanguage.xiaoyan,
        ITTSLanguage.xiaoyu,
        ITTSLanguage.vixy,
        ITTSLanguage.vixq,
        ITTSLanguage.vixl,
        ITTSLanguage.vinn,
        ITTSLanguage.vils,
        ITTSLanguage.vixx,
        ITTSLanguage.vixr,
        ITTSLanguage.vixm,
        ITTSLanguage.vixyun,
        ITTSLanguage.vixk,
        ITTSLanguage.vixqa,
        ITTSLanguage.vixying,
        ITTSLanguage.xiaofeng,
        ITTSLanguage.x_chongchong
})
@Retention(RetentionPolicy.SOURCE)
public @interface ITTSLanguage {
    /**
     * 嘉嘉	jiajia 普通话
     */
    String jiajia = "jiajia";
    /**
     * 小燕	xiaoyan 普通话
     */
    String xiaoyan = "xiaoyan";
    /**
     * 晓峰	xiaofeng 普通话
     */
    String xiaofeng = "xiaofeng";
    /**
     * 小宇	xiaoyu 普通话
     */
    String xiaoyu = "xiaoyu";
    /**
     * 小研	vixy 普通话
     */
    String vixy = "vixy";
    /**
     * 小琪	vixq 普通话
     */
    String vixq = "vixq";
    /**
     * 小莉	vixl 普通话
     */
    String vixl = "vixl";
    /**
     * 楠楠	vinn 普通话
     */
    String vinn = "vinn";
    /**
     * 老孙	vils 普通话
     */
    String vils = "vils";
    /**
     * 小新	vixx 普通话
     */
    String vixx = "vixx";
    /**
     * 小蓉	vixr 四川话
     */
    String vixr = "vixr";
    /**
     * 小梅	vixm 广东话
     */
    String vixm = "vixm";
    /**
     * 小芸	vixyun 东北话
     */
    String vixyun = "vixyun";
    /**
     * 小坤	vixk 河南话
     */
    String vixk = "vixk";
    /**
     * 小强	vixqa 湖南话
     */
    String vixqa = "vixqa";
    /**
     * 小莹	vixying 山西话
     */
    String vixying = "vixying";
    /**
     * 虫虫(ent参数需设置为xtts)	x_chongchong 精品普通话
     */
    String x_chongchong = "x_chongchong";

}

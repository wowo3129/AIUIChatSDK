package com.huimin.baidulib;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.huimin.baidulib.control.InitConfig;
import com.huimin.baidulib.control.MySyntherizer;
import com.huimin.baidulib.control.NonBlockSyntherizer;
import com.huimin.baidulib.listener.ISpeakListener;
import com.huimin.baidulib.listener.MessageListener;
import com.huimin.baidulib.util.AutoCheck;
import com.huimin.baidulib.util.AutoTtsCheck;
import com.huimin.baidulib.util.OfflineResource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kermitye
 * Date: 2018/5/24 17:33
 * Desc:
 */
public class TtsHelper {

    private Context mContext;
    protected String appId = "11222135";

    protected String appKey = "qGVgjQzmMsRe1jY1ugtYxiwT";

    protected String secretKey = "zVvMeR1BFgHuvLuKAfub6NoGZqlNhh0R";

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    private TtsMode ttsMode = TtsMode.ONLINE;

    // ================选择TtsMode.ONLINE  不需要设置以下参数; 选择TtsMode.MIX 需要设置下面2个离线资源文件的路径
    private static final String TEMP_DIR = "/sdcard/baiduTTS"; // 重要！请手动将assets目录下的3个dat 文件复制到该目录

    // 请确保该PATH下有这个文件
    private static final String TEXT_FILENAME = TEMP_DIR + "/" + "bd_etts_text.dat";
    // 请确保该PATH下有这个文件 ，m15是离线男声
    private static final String MODEL_FILENAME =
            TEMP_DIR + "/" + "bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat";


    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat为离线男声模型；
    // assets目录下bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat为离线女声模型
    protected String offlineVoice = OfflineResource.VOICE_MALE;

    // ===============初始化参数设置完毕，更多合成参数请至getParams()方法中设置 =================


    protected SpeechSynthesizer mSpeechSynthesizer;
    // 主控制类，所有合成控制方法从这个类开始
    protected MySyntherizer synthesizer;
    private ISpeakListener mSpeakListener;

    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };
    private InitConfig mInitConfig;
    private MessageListener mListener;

    private TtsHelper() {}

    private static class SingletonHolder {
        public static final TtsHelper INSTANCE = new TtsHelper();
    }

    public static TtsHelper getInstance() {
        return SingletonHolder.INSTANCE;
    }


    public void init(Context context) {
        this.mContext = context;
        LoggerProxy.printable(true); // 日志打印在logcat中

        // 设置初始化参数
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        mListener = new MessageListener(mSpeakListener);
        Map<String, String> params = getParams();


        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        mInitConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, mListener);

        // 如果您集成中出错，请将下面一段代码放在和demo中相同的位置，并复制InitConfig 和 AutoCheck到您的项目中
        // 上线时请删除AutoCheck的调用
        AutoTtsCheck.getInstance(context).check(mInitConfig, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoTtsCheck autoCheck = (AutoTtsCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainDebugMessage();
                        Logger.error(message);
//                        toPrint(message); // 可以用下面一行替代，在logcat中查看代码
                        // Log.w("AutoCheckMessage", message);
                    }
                }
            }

        });

        synthesizer = new NonBlockSyntherizer(mContext, mInitConfig, mHandler); // 此处可以改为MySyntherizer 了解调用过程

    }

    public void setSpeakListener(ISpeakListener speakListener) {
        this.mSpeakListener = speakListener;
        mListener.setSpeakLisstener(mSpeakListener);
    }


    public void startSpeak(String text) {
        // 需要合成的文本text的长度不能超过1024个GBK字节。

        // 合成前可以修改参数：
        // Map<String, String> params = getParams();
        // synthesizer.setParams(params);
        if (mSpeakListener != null)
            mSpeakListener.onSpeakBegin(text);

        int result = synthesizer.speak(text);
        checkResult(result, "speak");
    }

    public void stopSpeak() {
        int result = synthesizer.stop();
        if (mSpeakListener != null && isSpeaking())
            mSpeakListener.onInterrupted();
        mListener.mIsSpeaking = false;
//            mSpeakListener.(result == 0 ? "" : "error code:" + result);
        checkResult(result, "stop");
    }


    private void checkResult(int result, String method) {
        if (result != 0) {
            Logger.error("error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }

    public boolean isSpeaking() {
        if (mListener != null) {
            return mListener.mIsSpeaking;
        }
        return false;
    }


    public void destory() {
        if (synthesizer != null) {
            synthesizer.release();
        }
    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     */
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");

        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,
                offlineResource.getModelFilename());
        return params;
    }

    protected OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(mContext, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
//            toPrint("【error】:copy files from assets failed." + e.getMessage());
        }
        return offlineResource;
    }
}

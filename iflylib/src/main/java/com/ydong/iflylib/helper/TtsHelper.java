package com.ydong.iflylib.helper;

import android.content.Context;
import android.util.Log;

import com.ydong.iflylib.listener.SpeakListener;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;

/**
 * 讯飞合成辅助类
 *
 * @author ydong
 */
public class TtsHelper {
    private static final String TAG = TtsHelper.class.getSimpleName();

    private Context mContext;
    /**
     * 语音合成对象
     */
    private SpeechSynthesizer mTts;
    /**
     * 默认发音人
     */
    private String voicer = "xiaoyan";
    /**
     * 引擎类型
     */
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private SpeakListener mSpeakListener;
    private static TtsHelper ttsHelper;

    public static TtsHelper getInstance() {
        if (ttsHelper == null) {
            ttsHelper = new TtsHelper();
        }
        return ttsHelper;
    }

    /**
     * 初始化语音合成模块
     *
     * @param context 上下文
     */
    public void init(Context context) {
        this.mContext = context;
        mTts = SpeechSynthesizer.createSynthesizer(context, mTtsInitListener);
    }

    public void setSpeakListener(SpeakListener listener) {
        this.mSpeakListener = listener;
    }

    public void startSpeak(String text, SpeakListener listener) {
        if (mTts == null) {
            if (mSpeakListener != null) {
                mSpeakListener.onSpeakOver("创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化");
            }
            return;
        }
        // 设置参数
        setParam();
        int code = mTts.startSpeaking(text, listener);
        listener.onSpeakBegin(text);
        if (code != ErrorCode.SUCCESS) {
            Log.e(TAG, "语音合成失败,错误码: " + code);
        }
    }

    public void startSpeak(String text) {
        if (mSpeakListener == null) {
            mSpeakListener = new SpeakListener() {
                @Override
                public void onSpeakBegin(String text) {

                }

                @Override
                public void onSpeakOver(String msg) {

                }

                @Override
                public void onInterrupted() {

                }
            };
        }
        this.startSpeak(text, mSpeakListener);
    }


    public void stopSpeak() {
        if (mTts == null) {
            return;
        }
        mTts.stopSpeaking();
    }


    public boolean isSpeaking() {
        if (mTts != null) {
            return mTts.isSpeaking();
        }
        return false;
    }

    /**
     * 退出时停止合成，释放连接
     */
    public void destory() {
        if (null != mTts) {
            mTts.stopSpeaking();
            mTts.destroy();
        }
    }


    /**
     * 初始化合成对象监听
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.e(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Log.e(TAG, "初始化失败,错误码：" + code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };


    /**
     * 参数设置
     */
    private void setParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            // 设置在线合成发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
            //设置合成语速
            mTts.setParameter(SpeechConstant.SPEED, "50");
            //设置合成音调
            mTts.setParameter(SpeechConstant.PITCH, "50");
            //设置合成音量
            mTts.setParameter(SpeechConstant.VOLUME, "50");
        } else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            // 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
            mTts.setParameter(SpeechConstant.VOICE_NAME, "");
            /*
             * TODO 本地合成不设置语速、音调、音量，默认使用语记设置
             * 开发者如需自定义参数，请参考在线合成参数设置
             */
        }
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "false");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
//        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
//        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/tts.wav");
    }

}

package com.iflytek.aiui.demo.chatsdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.iflytek.aiui.demo.chatsdk.speech.abstracts.ISpeakListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;

/**
 * @author ydong
 */
public class SpeechHandler extends Handler {

    private final String TAG = "SpeechHandler";

    private Context mContext;

    private ISpeakListener speakListener;
    private SpeechSynthesizer mSpeechSynthesizer;

    private SpeechHandler sHandler;

    public static final String SPEAKTXT = "SpeakTxt";

    public SpeechHandler(Context context, SpeechSynthesizer mSpeechSynthesizer) {
        this.mContext = context;
        this.mSpeechSynthesizer = mSpeechSynthesizer;

        if (sHandler == null) {
            sHandler = this;
        }
    }

    @Override
    public void handleMessage(Message msg) {
        Log.d(TAG, "handleMessage msg=" + (msg == null ? "null" : msg.what));
        if (msg == null) {
            Log.i(TAG, "msg is null ");
            return;
        }
        switch (msg.what) {
            case MessageType.CALLBACK_SpeechSynthesizer_INIT:
//                setSpeakParams();
                setTtsParam();
                break;
            case MessageType.CALL_STOP_SPEAK:
                stopSpeak();
                break;
            case MessageType.CALL_START_SPEAK:
                Intent ttsParams = (Intent) msg.obj;
                if (ttsParams == null) {
                    Log.i(TAG, "speak txt is null!");
                    return;
                }
                String txt = ttsParams.getStringExtra(SPEAKTXT);
                startSpeak(txt);
                break;
            default:
                break;
        }

    }

    public void setSpeakListener(ISpeakListener listener) {
        this.speakListener = listener;
    }

    /**
     * 合成
     *
     * @param text
     */
    public void startSpeak(String text) {
        stopSpeak();
        if (mSpeechSynthesizer == null) {
            Log.e(TAG, "synthesizer not inited yet");
            return;
        }

        Log.v(TAG, "onSpeakBegin");
        if (speakListener != null) {
            speakListener.onSpeakBegin(text);
        }
        mSpeechSynthesizer.startSpeaking(text, mSynthesizerListener);
    }

    private void stopSpeak() {
        if (mSpeechSynthesizer != null && mSpeechSynthesizer.isSpeaking()) {
            Log.v(TAG, "stopSpeak");
            mSpeechSynthesizer.stopSpeaking();
        }
    }

    public boolean isSpeaking() {
        if (mSpeechSynthesizer != null) {
            Log.v(TAG, "isSpeaking");
            return mSpeechSynthesizer.isSpeaking();
        }
        return false;
    }


    /**
     * 设置语音合成参数
     */
    private void setSpeakParams() {
        /*String voicer = SpeechManager.getInstance().getSpeaker();
        //选择是否用本地合成
        boolean isLocal = SpeechManager.getInstance().isLocalSpeaker();
        String speaker = SpeechManager.getInstance().getSpeaker();
        Log.i(TAG, "isLocal= " + isLocal);
        // 根据合成引擎设置相应参数
        if (SpeechUtility.getUtility().checkServiceInstalled() && isLocal) {
            Log.i(TAG, "当前为本地合成发音人： " + voicer);
            mSpeechSynthesizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            // 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
            mSpeechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, voicer);
        } else {
        }*/
        String speaker = SpeechManager.getInstance().getSpeaker();
        mSpeechSynthesizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        // 设置在线合成发音人
        mSpeechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "");
        // 设置合成语速
        mSpeechSynthesizer.setParameter(SpeechConstant.SPEED, "50");
        // 设置合成音调
        mSpeechSynthesizer.setParameter(SpeechConstant.PITCH, "50");
        // 设置合成音量
        mSpeechSynthesizer.setParameter(SpeechConstant.VOLUME, "50");
        // 设置播放器音频流类型
        mSpeechSynthesizer.setParameter(SpeechConstant.STREAM_TYPE, "3");

        // 设置播放合成音频打断音乐播放，默认为true
        mSpeechSynthesizer.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        // mSpeechSynthesizer.setParameter(SpeechConstant.AUDIO_FORMAT,
        // "wav");
        // mSpeechSynthesizer.setParameter(SpeechConstant.TTS_AUDIO_PATH,
        // Environment.getExternalStorageDirectory() + "/msc/tts.wav");
    }

    /**
     * 语音合成设置
     */
    private void setTtsParam() {
        if (mSpeechSynthesizer == null) {
            return;
        }
        mSpeechSynthesizer.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        mSpeechSynthesizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        // 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
        mSpeechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        // 设置播放器音频流类型
        mSpeechSynthesizer.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mSpeechSynthesizer.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "false");
        mSpeechSynthesizer.setParameter(SpeechConstant.AUDIO_FORMAT, "raw");
        // 设置合成音量
        mSpeechSynthesizer.setParameter(SpeechConstant.VOLUME, "50");
    }

    /**
     * 语音合成回调监听
     */
    private SynthesizerListener mSynthesizerListener = new SynthesizerListener() {

        @Override
        public void onSpeakResumed() {
            // 继续播放
            Log.v(TAG, "onSpeakResumed");
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            Log.v(TAG, "onSpeakProgress");
        }

        @Override
        public void onSpeakPaused() {
            // 暂停播放
            Log.v(TAG, "onSpeakPaused");
        }

        @Override
        public void onSpeakBegin() {
            // 开始播放

        }

        @Override
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
            Log.v(TAG, "----onEvent:" + arg0);
            if (arg0 == 21002) {
                if (speakListener != null) {
                    speakListener.onInterrupted();
                }
            }

        }

        @Override
        public void onCompleted(SpeechError errorCode) {
            Log.v(TAG, "----onCompleted");
            // 如果有新的监听则不传给全局监听，否则传入
            if (speakListener != null) {
                speakListener.onSpeakOver(errorCode);
            }
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            // 合成进度
            Log.v(TAG, "onBufferProgress percent: " + percent);
        }
    };


}

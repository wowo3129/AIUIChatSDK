package com.ydong.iflylib.helper;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;
import com.ydong.iflylib.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author ydong
 */
public class WakeUpHelper {
    private static final String TAG = WakeUpHelper.class.getSimpleName();

    private Context mContext;
    /**
     * 唤醒对象
     */
    private VoiceWakeuper mIvw;

    private String keep_alive = "1";
    private String ivwNetMode = "0";
    private int curThresh = 1450;


    private WakeUpHelper() {
    }

    private static class SingletonHolder {
        public static final WakeUpHelper INSTANCE = new WakeUpHelper();
    }

    public static WakeUpHelper getInstance() {
        return SingletonHolder.INSTANCE;
    }


    public void init(Context context) {
        this.mContext = context;
        StringBuffer param = new StringBuffer();
        param.append("appid=" + context.getString(R.string.appid));
        param.append(",");
        // 设置使用v5+
        param.append(SpeechConstant.ENGINE_MODE + "=" + SpeechConstant.MODE_MSC);
        SpeechUtility.createUtility(context, param.toString());
        // 初始化唤醒对象
        mIvw = VoiceWakeuper.createWakeuper(context, null);
        initParam();
    }


    private void initParam() {
        //非空判断，防止因空指针使程序崩溃
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            // 清空参数
            mIvw.setParameter(SpeechConstant.PARAMS, null);
            // 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
            // 新唤醒引擎门限的推荐默认值为1450，取值范围为非负数，一般可在0-3000之间调节。
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:" + curThresh);
            // 设置唤醒模式
            mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
            // 设置持续进行唤醒
            mIvw.setParameter(SpeechConstant.KEEP_ALIVE, keep_alive);
            // 设置闭环优化网络模式
            mIvw.setParameter(SpeechConstant.IVW_NET_MODE, ivwNetMode);
            // 设置唤醒资源路径
            mIvw.setParameter(SpeechConstant.IVW_RES_PATH, getResource());
            // 设置唤醒录音保存路径，保存最近一分钟的音频
            mIvw.setParameter(SpeechConstant.IVW_AUDIO_PATH, Environment.getExternalStorageDirectory().getPath() + "/msc/ivw.wav");
            mIvw.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
            // 如有需要，设置 NOTIFY_RECORD_DATA 以实时通过 onEvent 返回录音音频流字节
            //mIvw.setParameter( SpeechConstant.NOTIFY_RECORD_DATA, "1" );

            // 启动唤醒
            mIvw.startListening(mWakeuperListener);
        } else {
            Log.d(TAG, "初始化失败");
        }
    }

    private WakeuperListener mWakeuperListener = new WakeuperListener() {

        @Override
        public void onResult(WakeuperResult result) {
            //唤醒完成
            Log.d(TAG, "onResult");

            mIvw = VoiceWakeuper.getWakeuper();
            if (mIvw != null) {
//                mIvw.startListening(mWakeuperListener);
                mIvw.stopListening();
                return;
            }

            if (!"1".equalsIgnoreCase(keep_alive)) {
//                setRadioEnable(true);
            }
            try {
                String text = result.getResultString();
                JSONObject object;
                object = new JSONObject(text);
                StringBuffer buffer = new StringBuffer();
                buffer.append("【RAW】 " + text);
                buffer.append("\n");
                buffer.append("【操作类型】" + object.optString("sst"));
                buffer.append("\n");
                buffer.append("【唤醒词id】" + object.optString("id"));
                buffer.append("\n");
                buffer.append("【得分】" + object.optString("score"));
                buffer.append("\n");
                buffer.append("【前端点】" + object.optString("bos"));
                buffer.append("\n");
                buffer.append("【尾端点】" + object.optString("eos"));
//                resultString =buffer.toString();
            } catch (JSONException e) {
//                resultString = "结果解析出错";
                e.printStackTrace();
            }
//            textView.setText(resultString);
        }

        @Override
        public void onError(SpeechError error) {
            Log.e(TAG, "唤醒失败:" + error.getErrorCode() + " / " + error.getErrorDescription());
            mIvw = VoiceWakeuper.getWakeuper();
            if (mIvw != null) {
                mIvw.startListening(mWakeuperListener);
            }
//            showTip(error.getPlainDescription(true));
//            setRadioEnable(true);
        }

        /**
         * 唤醒开始
         */
        @Override
        public void onBeginOfSpeech() {
            Log.e(TAG, "唤醒开始");
        }

        @Override
        public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
            Log.e(TAG, "onEvent");
            switch (eventType) {
                // EVENT_RECORD_DATA 事件仅在 NOTIFY_RECORD_DATA 参数值为 真 时返回
                case SpeechEvent.EVENT_RECORD_DATA:
                    final byte[] audio = obj.getByteArray(SpeechEvent.KEY_EVENT_RECORD_DATA);
                    Log.i(TAG, "ivw audio length: " + audio.length);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onVolumeChanged(int volume) {
            Log.e(TAG, "当前音量:" + volume);
        }
    };


    private String getResource() {
        final String resPath = ResourceUtil.generateResourcePath(mContext, ResourceUtil.RESOURCE_TYPE.assets, "ivw/" + mContext.getString(R.string.appid) + ".jet");
        Log.d(TAG, "resPath: " + resPath);
        return resPath;
    }

    private void destory() {
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            mIvw.destroy();
        }
    }


}

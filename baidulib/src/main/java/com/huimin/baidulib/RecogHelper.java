package com.huimin.baidulib;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.google.gson.Gson;
import com.huimin.baidulib.bean.PartialBean;
import com.huimin.baidulib.bean.VolumeBean;
import com.huimin.baidulib.listener.IRecogListener;
import com.huimin.baidulib.util.AutoCheck;
import com.huimin.baidulib.util.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Created by kermitye
 * Date: 2018/5/24 17:33
 * Desc:
 */
public class RecogHelper implements EventListener {

    private Context mContext;
    private EventManager mAsr;
    private boolean enableOffline;
    private IRecogListener mRecogListener;

    private RecogHelper() {}


    private static class SingletonHolder {
        public static final RecogHelper INSTANCE = new RecogHelper();
    }

    public static RecogHelper getInstance() {
        return SingletonHolder.INSTANCE;
    }


    public void init(Context context) {
        this.mContext = context;
        mAsr = EventManagerFactory.create(context, "asr");
//        mAsr = EventManagerFactory.create(context, "wp");
        mAsr.registerListener(this);
    }

    public void setRecogListener(IRecogListener listener) {
        mRecogListener = listener;
    }

    public void start() {
        Logger.error("开始识别：ASR_START");
//        txtLog.setText("");
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        if (enableOffline) {
            params.put(SpeechConstant.DECODER, 2);
        }
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, true);
        // params.put(SpeechConstant.NLU, "enable");
        params.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 0); // 长语音
        // params.put(SpeechConstant.IN_FILE, "res:///com/baidu/android/voicedemo/16k_test.pcm");
        // params.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);
        // params.put(SpeechConstant.PROP ,20000);
        // params.put(SpeechConstant.PID, 1537); // 中文输入法模型，有逗号
        // 请先使用如‘在线识别’界面测试和生成识别参数。 params同ActivityRecog类中myRecognizer.start(params);
//        params.put(SpeechConstant.AUDIO_MILLS, System.currentTimeMillis() - 1000);  //唤醒词说完后，中间有停顿，然后接句子。推荐4个字 1500ms

        start(params);
    }

    public void start(Map<String, Object> params) {
        // 复制此段可以自动检测错误
        (new AutoCheck(mContext, new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainErrorMessage(); // autoCheck.obtainAllMessage();
                        Logger.error(message);
//                        txtLog.append(message + "\n");
                        ; // 可以用下面一行替代，在logcat中查看代码
                        // Log.w("AutoCheckMessage", message);
                    }
                }
            }
        }, enableOffline)).checkAsr(params);
        String json = null; // 可以替换成自己的json
        json = new JSONObject(params).toString(); // 这里可以替换成你需要测试的json
        mAsr.send(SpeechConstant.ASR_START, json, null, 0, 0);
        Logger.error("输入参数：" + json);
    }


    /**
     * 提前结束录音等待识别结果。
     */
    public void stop() {
        Logger.error("停止识别：ASR_STOP");
        mAsr.send(SpeechConstant.ASR_STOP, null, null, 0, 0); //
    }


    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        String logTxt = "name: " + name;
        if (params != null && !params.isEmpty()) {
            logTxt += " ;params :" + params;
        }

        switch (name) {
            case SpeechConstant.CALLBACK_EVENT_ASR_VOLUME:
                Logger.error("=====volume:" + params);
                VolumeBean volumeBean = JsonUtils.deserialize(params, VolumeBean.class);
                if (mRecogListener != null) {
                    Logger.error("=====volume get... :" + volumeBean.volumepercent);
                    mRecogListener.onVolumeChanged(volumeBean.volumepercent);
                }

                break;
            case SpeechConstant.CALLBACK_EVENT_WAKEUP_SUCCESS:
                /*isWeakUp = true;
                releaseWekeup();
                release();
                mAsr = EventManagerFactory.create(mContext, "asr");
                mAsr.registerListener(this);
                start();*/
                break;
            case SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL:
                PartialBean resultBean = JsonUtils.deserialize(params, PartialBean.class);
                if (resultBean != null && "final_result".equals(resultBean.result_type)) {
                    if (mRecogListener != null) {
                        mRecogListener.onResult(resultBean.best_result);
                    }
                }

//                    EventBus.getDefault().post(new MyEvents.ChatEvent(EventCodes.CHAT_SPEECH_RESULT, resultBean.best_result));
                /*stop();
                release();
                mAsr = EventManagerFactory.create(mContext, "wp");
                mAsr.registerListener(this); //  EventListener 中 onEvent方法*/
                break;
        }
        Logger.error(logTxt);
    }

    public void destory() {
        if (mAsr == null) {
            return;
        }
        mAsr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        mAsr.unregisterListener(this);
    }

}

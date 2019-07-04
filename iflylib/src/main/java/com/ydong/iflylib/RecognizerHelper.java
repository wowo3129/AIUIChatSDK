package com.ydong.iflylib;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.ydong.iflylib.listener.IRecognizerListener;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by kermitye
 * Date: 2018/5/23 15:05
 * Desc: 讯飞听写辅助类
 *
 * @author ydong
 */
public class RecognizerHelper {
    private static final String TAG = RecognizerHelper.class.getSimpleName();

    /**
     * 语音听写对象
     */
    private SpeechRecognizer mIat;
    /**
     * 引擎类型
     */
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    /**
     * 用HashMap存储听写结果
     */
    private HashMap<String, String> mIatResults = new LinkedHashMap<>();
    private Context mContext;
    private boolean mIsRec;
    /**
     * 函数调用返回值
     */
    int ret = 0;
    private IRecognizerListener mIRecognizerListener;


    private RecognizerHelper() {
    }

    private static class SingletonHolder {
        public static final RecognizerHelper INSTANCE = new RecognizerHelper();
    }

    public static RecognizerHelper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 初始化语音识别功能模块
     *
     * @param context
     */
    public void init(Context context) {
        this.mContext = context;
        mIat = SpeechRecognizer.createRecognizer(context, mInitListener);
    }

    /**
     * 听写回调
     *
     * @param listener
     */
    public void setIRecognizerListener(IRecognizerListener listener) {
        this.mIRecognizerListener = listener;
    }


    /**
     * 开始识别
     */
    public void startRecognizer() {
        if (null == mIat) {
            if (mIRecognizerListener != null) {
                mIRecognizerListener.onError("创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化");
            }
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            Logger.error("创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化");
            return;
        }
        mIatResults.clear();
        // 设置参数
        setParam();
        mIsRec = true;
        ret = mIat.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            if (mIRecognizerListener != null) {
                mIRecognizerListener.onError("听写失败,错误码：" + ret);
            }
            Logger.error("听写失败,错误码：" + ret);
        }
    }

    /**
     * 停止识别
     */
    public void stopRecognizer() {
        if (mIat == null)
            return;
        mIsRec = false;
        mIat.cancel();
//        mIat.stopListening();
        Logger.error("停止听写");
    }


    public void destory() {
        if (null != mIat) {
            Logger.error("退出释放");
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                if (mIRecognizerListener != null) {
                    mIRecognizerListener.onError("初始化失败,错误码:" + code);
                }
                Logger.error("初始化失败,错误码:" + code);
//                showTip("初始化失败，错误码：" + code);
            }
        }
    };


    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            Logger.error("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            /*if(mTranslateEnable && error.getErrorCode() == 14002) {
                Logger.error( error.getPlainDescription(true)+"\n请确认是否已开通翻译功能" );
            } else {

            }*/
            try {
                String msg = error.getPlainDescription(true);
                Logger.error(msg);
                if (mIRecognizerListener != null) {
                    mIRecognizerListener.onError(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mIsRec) {
                startRecognizer();
            }
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            Logger.error("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Logger.error(results.getResultString());
//            startRecognizer();

            printResult(results);
            if (isLast) {
                // TODO 最后的结果
            }
            startRecognizer();
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
//            Logger.error("当前正在说话，音量大小：" + volume);
            if (mIRecognizerListener != null) {
                mIRecognizerListener.onVolumeChanged(volume);
            }
//            Log.d(TAG, "返回音频数据："+data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };


    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        String result = resultBuffer.toString();
        Logger.error("听写结果:" + result);
        if (mIRecognizerListener != null) {
            mIRecognizerListener.onResult(resultBuffer.toString());
        }
    }


    /**
     * 参数设置
     *
     * @return
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

       /* this.mTranslateEnable = mSharedPreferences.getBoolean( this.getString(R.string.pref_key_translate), false );
        if( mTranslateEnable ){
            Log.i( TAG, "translate enable" );
            mIat.setParameter( SpeechConstant.ASR_SCH, "1" );
            mIat.setParameter( SpeechConstant.ADD_CAP, "translate" );
            mIat.setParameter( SpeechConstant.TRS_SRC, "its" );
        }*/

        String lag = "mandarin";
        if (lag.equals("en_us")) {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
            mIat.setParameter(SpeechConstant.ACCENT, null);

            /*if( mTranslateEnable ){
                mIat.setParameter( SpeechConstant.ORI_LANG, "en" );
                mIat.setParameter( SpeechConstant.TRANS_LANG, "cn" );
            }*/
        } else {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);

           /* if( mTranslateEnable ){
                mIat.setParameter( SpeechConstant.ORI_LANG, "cn" );
                mIat.setParameter( SpeechConstant.TRANS_LANG, "en" );
            }*/
        }
        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "4000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "0");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

}

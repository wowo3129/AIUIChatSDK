package com.iflytek.aiui.demo.chatsdk;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.aiui.demo.chatsdk.speech.abstracts.ISpeakListener;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * 识别、合成、唤醒、热词上传、AIUI离线命令词识别
 */
public class SpeechManager {
    private final static String TAG = "SpeechManager";
    private static SpeechManager mInstance;
    public static Context context;
    private SpeechSynthesizer mSpeechSynthesizer;
    private SpeechHandler mSpeechHandler;
    private String speaker = "jiajia";
    private AIUIAgent mAIUIAgent = null;
    private int mAIUIState = AIUIConstant.STATE_IDLE;
    /**
     * 在线合成还是离线合成，默认是在线
     */
    private boolean isLocalSpeaker = true;

    public SpeechManager(Context context) {
        this.context = context;
        // 初始化合成引擎
        mSpeechSynthesizer = SpeechSynthesizer.createSynthesizer(context, mSynthersizerInit);
        //处理合成需要的参数设置
        mSpeechHandler = new SpeechHandler(context, mSpeechSynthesizer);
    }


    /**
     * 获取已经初始化的实例对象
     *
     * @return 识别管理
     */
    public static SpeechManager getInstance() {
        if (mInstance == null) {
            mInstance = new SpeechManager(App.getContext());
        }
        return mInstance;
    }

    public static void CreateSpeechUtility(Application app, String appid) {
        SpeechUtility.createUtility(app, "appid=" + appid);
    }

    /**
     * 初始化创建AIUI代理
     */
    public void createAgent() {
        if (null == mAIUIAgent) {
            Log.i(TAG, "ydong create aiui agent");
            mAIUIAgent = AIUIAgent.createAgent(context, getAIUIParams(), mAIUIListener);
            AIUIMessage startMsg = new AIUIMessage(AIUIConstant.CMD_START, 0, 0, null, null);
            mAIUIAgent.sendMessage(startMsg);
        }

        if (null == mAIUIAgent) {
            final String strErrorTip = "创建AIUIAgent失败！";
            Log.i(TAG, "ydong strErrorTip");
        } else {
            Log.i(TAG, "ydong AIUIAgent已创建");
        }
    }

    /**
     * 销毁AIUI代理
     */
    public void destroyAgent() {
        if (null != mAIUIAgent) {
            Log.d(TAG, "AIUIAgent已销毁");
            mAIUIAgent.destroy();
            mAIUIAgent = null;
        } else {
            Log.d(TAG, "AIUIAgent为空");
        }
    }


    /**
     * 语音配置
     */
    private String getAIUIParams() {
        String params = "";

        AssetManager assetManager = context.getResources().getAssets();
        try {
            InputStream ins = assetManager.open("cfg/aiui_phone.cfg");
            byte[] buffer = new byte[ins.available()];

            ins.read(buffer);
            ins.close();

            params = new String(buffer);

            JSONObject paramsJson = new JSONObject(params);

            params = paramsJson.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return params;
    }


    /**
     * 开启语音语义开关
     */
    private void startVoiceNlp() {
        Log.d(TAG, "ydong startVoiceNlp 开启语音语义开关");
        if (null == mAIUIAgent) {
            Log.d(TAG, "AIUIAgent为空，请先创建");
            return;
        }

        Log.i(TAG, "start voice nlp");
        // 先发送唤醒消息，改变AIUI内部状态，只有唤醒状态才能接收语音输入
        // 默认为oneshot模式，即一次唤醒后就进入休眠。可以修改aiui_phone.cfg中speech参数的interact_mode为continuous以支持持续交互
        if (AIUIConstant.STATE_WORKING != mAIUIState) {
            AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
            mAIUIAgent.sendMessage(wakeupMsg);
        }

        // 打开AIUI内部录音机，开始录音。若要使用上传的个性化资源增强识别效果，则在参数中添加pers_param设置
        // 个性化资源使用方法可参见http://doc.xfyun.cn/aiui_mobile/的用户个性化章节
        // 在输入参数中设置tag，则对应结果中也将携带该tag，可用于关联输入输出
        String params = "sample_rate=16000,data_type=audio,pers_param={\"uid\":\"\"},tag=audio-tag";
        AIUIMessage startRecord = new AIUIMessage(AIUIConstant.CMD_START_RECORD, 0, 0, params, null);

        mAIUIAgent.sendMessage(startRecord);
    }

    /**
     * 停止录音
     */
    private void stopVoiceNlp() {
        if (null == mAIUIAgent) {
            Log.i(TAG, "AIUIAgent 为空，请先创建");
            return;
        }

        Log.i(TAG, "stop voice nlp");
        // 停止录音
        String params = "sample_rate=16000,data_type=audio";
        AIUIMessage stopRecord = new AIUIMessage(AIUIConstant.CMD_STOP_RECORD, 0, 0, params, null);

        mAIUIAgent.sendMessage(stopRecord);
    }


    /**
     * 获取语音语义的返回结果
     */
    private AIUIListener mAIUIListener = event -> {

        switch (event.eventType) {
            case AIUIConstant.EVENT_CONNECTED_TO_SERVER:
                LogUtils.i(TAG, "on event: " + event.eventType + "<--已连接服务器");
                startVoiceNlp();
                break;

            case AIUIConstant.EVENT_SERVER_DISCONNECTED:
                LogUtils.i(TAG, "on event: " + event.eventType + "<--与服务器断连");
                break;

            case AIUIConstant.EVENT_WAKEUP:
                LogUtils.i(TAG, "on event: " + event.eventType + "<--进入识别状态");
                break;
            //通过EVENT_RESULT解析AIUI返回的听写和语义结果
            case AIUIConstant.EVENT_RESULT:
                Log.i(TAG, "on event: " + event.eventType + "<--解析AIUI返回的听写和语义结果");
                processResult(event);
                break;

            case AIUIConstant.EVENT_ERROR: {
                LogUtils.e(TAG, "---->ERROR on event: " + event.eventType + "错误: " + event.arg1 + "\n" + event.info);
            }
            break;

            case AIUIConstant.EVENT_VAD: {
                if (AIUIConstant.VAD_BOS == event.arg1) {
                    LogUtils.i(TAG, "找到vad_bos");
                } else if (AIUIConstant.VAD_EOS == event.arg1) {
                    LogUtils.i(TAG, "找到vad_eos");
                } else {
                    LogUtils.i("" + event.arg2);
                }
            }
            break;

            case AIUIConstant.EVENT_START_RECORD: {
                LogUtils.i(TAG, "on event: " + event.eventType + "<--已开始录音");
            }
            break;

            case AIUIConstant.EVENT_STOP_RECORD: {
                LogUtils.i(TAG, "on event: " + event.eventType + "<--已停止录音");
            }
            break;

            case AIUIConstant.EVENT_STATE: {
                // 状态事件
                mAIUIState = event.arg1;

                if (AIUIConstant.STATE_IDLE == mAIUIState) {
                    // 闲置状态，AIUI未开启
                    LogUtils.i(TAG, "on event STATE_IDLE: " + event.eventType + "<--闲置状态，AIUI未开启[0]");
                } else if (AIUIConstant.STATE_READY == mAIUIState) {
                    // AIUI已就绪，等待唤醒
                    LogUtils.i(TAG, "on event STATE_READY: " + event.eventType + "<--AIUI已就绪，等待唤醒[1]");
                } else if (AIUIConstant.STATE_WORKING == mAIUIState) {
                    // AIUI工作中，可进行交互
                    LogUtils.i(TAG, "on event STATE_WORKING: " + event.eventType + "<--AIUI工作中，可进行交互[2]");
                }
            }
            break;

            case AIUIConstant.EVENT_CMD_RETURN:
                // TODO: 2019/2/14  数据同步的返回 待处理
//                    eventCmdResult(event);
                break;
            case AIUIConstant.EVENT_TTS: {
                switch (event.arg1) {
                    case AIUIConstant.TTS_SPEAK_BEGIN:
                        LogUtils.d(TAG, "开始播放");
                        break;

                    case AIUIConstant.TTS_SPEAK_PROGRESS:
                        LogUtils.d(" 播放进度为" + event.data.getInt("percent"));     // 播放进度
                        break;

                    case AIUIConstant.TTS_SPEAK_PAUSED:
                        LogUtils.d(TAG, "暂停播放");
                        break;

                    case AIUIConstant.TTS_SPEAK_RESUMED:
                        LogUtils.d(TAG, "恢复播放");
                        break;

                    case AIUIConstant.TTS_SPEAK_COMPLETED:
                        LogUtils.d(TAG, "播放完成");
                        break;

                    default:
                        break;
                }
            }
            break;

            default:
                break;
        }
    };


    /**
     * 处理AIUI结果事件（听写结果和语义结果）
     *
     * @param event 结果事件
     */
    private void processResult(AIUIEvent event) {
        try {
            JSONObject bizParamJson = new JSONObject(event.info);
            JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
            JSONObject params = data.getJSONObject("params");
            JSONObject content = data.getJSONArray("content").getJSONObject(0);
            long rspTime = event.data.getLong("eos_rslt", -1);  //响应时间
            String sub = params.optString("sub");
            if (content.has("cnt_id") && !sub.equals("tts")) {
                String cnt_id = content.getString("cnt_id");
                JSONObject cntJson = new JSONObject(new String(event.data.getByteArray(cnt_id), StandardCharsets.UTF_8));
                LogUtils.d(TAG, "识别结果【1】-->" + cntJson.toString());
                if ("nlp".equals(sub)) {
                    //语义结果(nlp)
                    JSONObject semanticResult = cntJson.optJSONObject("intent");
                    if (semanticResult != null && semanticResult.length() != 0) {
                        //解析得到语义结果，将语义结果作为消息插入到消息列表中
                        String s = semanticResult.toString();
                        // TODO: 2019/2/16 语义结果
                        LogUtils.d(TAG, "wowo3129 语义结果---->" + s);
                    }
                } else if ("iat".equals(sub)) {
                    //听写结果(iat)
                    processIATResult(cntJson);
                }
            }
        } catch (Throwable e) {
            LogUtils.d(TAG, "---->Throwable" + e.toString());
            e.printStackTrace();
        }
    }

    private String[] mIATPGSStack = new String[256];
    long allcount = 0;

    /**
     * 解析听写结果更新当前语音消息的听写内容
     */
    private void processIATResult(JSONObject cntJson) throws JSONException {

        JSONObject text = cntJson.optJSONObject("text");
        // 解析拼接此次听写结果
        StringBuilder iatText = new StringBuilder();
        JSONArray words = text.optJSONArray("ws");
        boolean lastResult = text.optBoolean("ls");
        for (int index = 0; index < words.length(); index++) {
            JSONArray charWord = words.optJSONObject(index).optJSONArray("cw");
            for (int cIndex = 0; cIndex < charWord.length(); cIndex++) {
                iatText.append(charWord.optJSONObject(cIndex).opt("w"));
            }
        }

        String pgsMode = text.optString("pgs");
        //非PGS模式结果
        if (TextUtils.isEmpty(pgsMode)) {
            if (TextUtils.isEmpty(iatText)) return;
        } else {
            int serialNumber = text.optInt("sn");
            mIATPGSStack[serialNumber] = iatText.toString();
            //pgs结果两种模式rpl和apd模式（替换和追加模式）
            if ("rpl".equals(pgsMode)) {
                //根据replace指定的range，清空stack中对应位置值
                JSONArray replaceRange = text.optJSONArray("rg");
                int start = replaceRange.getInt(0);
                int end = replaceRange.getInt(1);

                for (int index = start; index <= end; index++) {
                    mIATPGSStack[index] = null;
                }
            }

            StringBuilder PGSResult = new StringBuilder();
            //汇总stack经过操作后的剩余的有效结果信息
            for (int index = 0; index < mIATPGSStack.length; index++) {
                if (TextUtils.isEmpty(mIATPGSStack[index])) continue;
                if (!TextUtils.isEmpty(PGSResult.toString())) PGSResult.append("\n");
                PGSResult.append(mIATPGSStack[index]);
                //如果是最后一条听写结果，则清空stack便于下次使用
                if (lastResult) {
                    mIATPGSStack[index] = null;
                }
            }

            if (lastResult) {
                // TODO: 2019/2/16 最终听写到的结果内容

                String ttsStr = PGSResult.toString();   //得到待合成文本
//                SpeechManager.getInstance().startSpeak(ttsStr);
                byte[] ttsData = new byte[0];  //转为二进制数据
                try {
                    ttsData = ttsStr.getBytes("utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                StringBuffer params = new StringBuffer();  //构建合成参数
                params.append("vcn=xiaoyan");  //合成发音人
                params.append(",speed=50");  //合成速度
                params.append(",pitch=50");  //合成音调
                params.append(",volume=50");  //合成音量

                //开始合成
                AIUIMessage startTts = new AIUIMessage(AIUIConstant.CMD_TTS, AIUIConstant.START, 0, params.toString(), ttsData);
                mAIUIAgent.sendMessage(startTts);

                LogUtils.d(TAG, "wowo3129 FINAL_RESULT---->" + PGSResult.toString() + "<----【" + allcount + "】");
            }
        }
    }

    /**
     * 初始化语音合成
     */
    private InitListener mSynthersizerInit = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechSynthesizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Log.d(TAG, "初始化SpeechSynthesizer失败,错误码：" + code);
                return;
            }
            sendMessage(mSpeechHandler, MessageType.CALLBACK_SpeechSynthesizer_INIT, 0, 0, null);
        }
    };

    /**
     * 消息发送
     *
     * @param handler target
     * @param what    消息类型
     * @param arg0    消息参数
     * @param arg1    消息参数
     * @param obj     消息参数
     */
    private void sendMessage(Handler handler, int what, int arg0, int arg1, Object obj) {
        Log.d(TAG, "sendMessage what=" + what);
        if (handler != null) {
            Message msg = handler.obtainMessage(what, arg0, arg1, obj);
            msg.sendToTarget();
        }
    }

    public String getSpeaker() {
        return speaker;
    }

    /**
     * 设置发音人
     *
     * @param speaker
     */
    public void setSpeaker(String speaker) {
        this.speaker = speaker;
        sendMessage(mSpeechHandler,
                MessageType.CALLBACK_SpeechSynthesizer_INIT, 0, 0, null);
    }

    public boolean isLocalSpeaker() {
        return isLocalSpeaker;
    }

    /**
     * 设置当前用本地还是在线
     *
     * @param isLocalSpeaker
     */
    public void setLocalSpeaker(boolean isLocalSpeaker) {
        this.isLocalSpeaker = isLocalSpeaker;
        sendMessage(mSpeechHandler,
                MessageType.CALLBACK_SpeechSynthesizer_INIT, 0, 0, null);
    }

    public void startSpeak(String text) {
        if (text == null || text.length() < 1)
            return;
        startSpeak(text, null, null);
    }

    public void startSpeak(String text, ISpeakListener listener) {
        if (text == null || text.length() < 1) {
            return;
        }
        if (mSpeechHandler != null) {
            mSpeechHandler.setSpeakListener(listener);
        }
        Intent intent = new Intent();
        intent.putExtra(SpeechHandler.SPEAKTXT, text);
        sendMessage(mSpeechHandler, MessageType.CALL_START_SPEAK, 0, 0, intent);
    }

    public void startSpeak(String text, ISpeakListener listener, Intent i) {
        stopSpeak();
        if (text == null || text.length() < 1) {
            return;
        }
        if (mSpeechHandler != null) {
            mSpeechHandler.setSpeakListener(listener);
        }
        Intent intent = new Intent();
        intent.putExtra(SpeechHandler.SPEAKTXT, text);
        sendMessage(mSpeechHandler, MessageType.CALL_START_SPEAK, 0, 0, intent);
    }

    public void stopSpeak() {
        Log.v(TAG, "stopSpeak");
        sendMessage(mSpeechHandler, MessageType.CALL_STOP_SPEAK, 0, 0, null);
    }

    /**
     * 是否在播放TTS
     */
    public boolean isSpeaking() {
        Log.v(TAG, "isSpeaking");
        if (mSpeechHandler != null) {
            return mSpeechHandler.isSpeaking();
        }
        return false;
    }
}

package com.aiuisdk;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.aiuisdk.config.ITTSLanguage;
import com.aiuisdk.config.ResponseJson;
import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.SpeechUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 识别、合成、切换情景模式
 * 全双工：效果：持续识别一整天不间断，方案：每次收到STATE_READY休眠状态就发送唤醒指令CMD_WAKEUP再次拉起识别，从逻辑上改为全双工,可行
 * {@code 参考讯飞文档:https://doc.iflyos.cn/aiui/sdk/mobile_doc/aiui_state.html}
 *
 * @author ydong
 */
public class SpeechManager {
    private final static String TAG = "SpeechManager";

    private static Context context;
    private static SpeechManager mInstance;
    private static AIUIAgent mAIUIAgent = null;
    private static int mAIUIState = AIUIConstant.STATE_IDLE;
    private boolean mIVWWakeup = false;
    private BaseSpeechCallback speechCallback;
    private JSONObject mParamsJson;
    private IErrorInfoCallback iErrorInfoCallback;
    /**
     * 全双工识别
     */
    public static boolean DUPLEX = false;

    /**
     * 设置true表示不休眠的全双工识别
     *
     * @param b true 一直识别 false:会自动休眠
     */
    public void setDuplex(boolean b) {
        DUPLEX = b;
    }

    /**
     * TTS完成回调
     */
    private static Runnable mTTSCallback;
    private String appid;

    private SpeechManager() {
        createAgent();
//        initializeMSCIfExist(context, getAppid());
    }

    public String getAppid() {
        return appid;
    }

    /**
     * 设置你自己的项目里的appId
     *
     * @param appId 应用的appId
     */
    public void setAppid(String appId) {
        this.appid = appId;
    }

    public static void CreateInstance(Context cmt) {
        context = cmt;
        if (mInstance == null) {
            mInstance = new SpeechManager();
            Log.i(TAG, "SpeechManager init success");
        }
    }

    /**
     * 初始化SpeechManager实例,
     * 注入上下文
     * 初始化后，自动进入对话监听状态
     */
    public static SpeechManager getInstance() {
        if (mInstance == null) {
            Log.d(TAG, "SpeechManager is not init yet!");
        }
        return mInstance;
    }


    /**
     * 初始化创建AIUI代理
     */
    public void createAgent() {
        if (null == mAIUIAgent) {
            Log.i(TAG, "create agent");
            String aiuiParams = getAIUIParams();
            Log.i(TAG, "create agent aiuiParams" + aiuiParams);
            mAIUIAgent = AIUIAgent.createAgent(context, aiuiParams, mAIUIListener);
            AIUIMessage startMsg = new AIUIMessage(AIUIConstant.CMD_START, 0, 0, null, null);
            mAIUIAgent.sendMessage(startMsg);
            //语音合成的监听
            getmAIUIEvent().observeForever(ttsObserver);
        }

        if (null == mAIUIAgent) {
            final String strErrorTip = "创建AIUIAgent失败！";
            Log.i(TAG, "strErrorTip" + strErrorTip);
        } else {
            Log.i(TAG, "AIUIAgent have create");
        }
    }

    /**
     * 销毁AIUI代理
     */
    public void destroyAgent() {
        if (null != mAIUIAgent) {
            Log.i(TAG, "AIUIAgent已销毁");
            mAIUIAgent.destroy();
            mAIUIAgent = null;
        } else {
            Log.i(TAG, "AIUIAgent is null");
        }
    }

    /**
     * 语音合成监听器
     */
    private Observer ttsObserver = new Observer<AIUIEvent>() {
        @Override
        public void onChanged(@Nullable AIUIEvent event) {
            if (event.eventType == AIUIConstant.EVENT_TTS) {
                switch (event.arg1) {
                    case AIUIConstant.TTS_SPEAK_BEGIN:
                        Log.i(TAG, "开始播放");
                        break;
                    case AIUIConstant.TTS_SPEAK_PROGRESS:
                        //播放进度
                        //Log.i(TAG, "播放进度为" + event.data.getInt("percent"));
                        break;

                    case AIUIConstant.TTS_SPEAK_PAUSED:
                        Log.i(TAG, "暂停播放");
                        break;

                    case AIUIConstant.TTS_SPEAK_RESUMED:
                        Log.i(TAG, "恢复播放");
                        break;
                    case AIUIConstant.TTS_SPEAK_COMPLETED: {
                        Log.i(TAG, "TTS Complete");
                        if (mTTSCallback != null) {
                            mTTSCallback.run();
                            mTTSCallback = null;
                        }
                    }
                    break;
                    default:
                        break;

                }
            }
        }
    };

    /**
     * @see #createAgent() 创建时所需要的语音参数配置
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
            mParamsJson = new JSONObject(params);
            JSONObject login = mParamsJson.getJSONObject("login");
            appid = login.optString("appid");
            setAppid(appid);
            params = mParamsJson.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return params;
    }


    /**
     * 开启录音
     * 进入对话监听状态
     */
    public void startVoice() {
        if (null == mAIUIAgent) {
            Log.i(TAG, "AIUIAgent is null,then create AIUIAgent");
            createAgent();
        }

        Log.i(TAG, "start voice nlp 开启录音");
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
     * 停止对话监听状态，并且停止合成播放
     */
    public void stopVoice() {
        if (null == mAIUIAgent) {
            Log.i(TAG, "AIUIAgent 为空，请先创建");
            return;
        }
        Log.i(TAG, "停止对话监听状态，并且停止合成播放 stop voice nlp");
        String params = "sample_rate=16000,data_type=audio";
        AIUIMessage stopRecord = new AIUIMessage(AIUIConstant.CMD_STOP_RECORD, 0, 0, params, null);
        mAIUIAgent.sendMessage(stopRecord);
        stopSpeaking();
    }


    /**
     * 获取语音语义的返回结果
     */
    private AIUIListener mAIUIListener = new AIUIListener() {
        @Override
        public void onEvent(AIUIEvent event) {
            mAIUIEvents.setValue(event);
            switch (event.eventType) {
                case AIUIConstant.EVENT_CONNECTED_TO_SERVER:
                    Log.i(TAG, "on event: " + event.eventType + "<--已连接服务器");
                    if (!mIVWWakeup) {
                        SpeechManager.this.startVoice();
                    }
                    break;

                case AIUIConstant.EVENT_SERVER_DISCONNECTED:
                    Log.i(TAG, "on event: " + event.eventType + "<--与服务器断连");
                    //重连一次
                    createAgent();
                    break;

                case AIUIConstant.EVENT_WAKEUP:
                    Log.i(TAG, "on event: " + event.eventType + "<--进入识别状态");
                    break;

                //通过EVENT_RESULT解析AIUI返回的听写和语义结果
                case AIUIConstant.EVENT_RESULT:
                    Log.i(TAG, "on event: " + event.eventType + "<--解析AIUI返回的听写和语义结果");
                    processResult(event);
                    break;

                case AIUIConstant.EVENT_ERROR: {
                    int errorCode = event.arg1;
                    if (iErrorInfoCallback != null) {
                        iErrorInfoCallback.errorCallback(errorCode, event.info);
                    }
                    //没有录音权限
                    if (errorCode == ErrorCode.ERROR_AUDIO_RECORD) {
                        Log.e(TAG, "录音启动失败 :(，请检查是否有其他应用占用录音");
                    } else if (errorCode >= 10200 && errorCode <= 10215) {
                        Log.e(TAG, "AIUI Network Warning %d, Don't Panic" + errorCode);
                        return;
                    } else if (errorCode == 10120) {
                        Log.e(TAG, "结果等待超时,网络状态差或者服务器处理缓慢 errorCode::" + errorCode);
                        return;
                    } else {
                        Log.e(TAG, "event.eventType::" + event.eventType + "--errorCode::" + errorCode + "--event.info::" + event.info);
                    }
                }
                break;

                case AIUIConstant.EVENT_VAD: {
                    // TODO: 2019/3/13 暂时业务不需要处理
                }
                break;

                case AIUIConstant.EVENT_START_RECORD: {
                    Log.i(TAG, "on event: " + event.eventType + "<--已开始录音");
                }
                break;

                case AIUIConstant.EVENT_STOP_RECORD: {
                    Log.i(TAG, "on event: " + event.eventType + "<--已停止录音");
                }
                break;

                case AIUIConstant.EVENT_STATE: {
                    // 状态事件
                    mAIUIState = event.arg1;
                    if (AIUIConstant.STATE_IDLE == mAIUIState) {
                        Log.i(TAG, "on event STATE_IDLE: " + event.eventType + "<--AIUI闲置状态，AIUI未开启[0]");
                        AIUIMessage startMsg = new AIUIMessage(AIUIConstant.CMD_START, 0, 0, null, null);
                        mAIUIAgent.sendMessage(startMsg);
                    } else if (AIUIConstant.STATE_READY == mAIUIState) {
                        Log.i(TAG, "on event STATE_READY: " + event.eventType + "<--AIUI已就绪，等待唤醒[1]");
                        if (mAIUIState != AIUIConstant.STATE_WORKING) {
                            mAIUIAgent.sendMessage(new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null));
                        }
                    } else if (AIUIConstant.STATE_WORKING == mAIUIState) {
                        Log.i(TAG, "on event STATE_WORKING: " + event.eventType + "<--AIUI工作中，可进行交互[2]");
                    }
                }
                break;

                case AIUIConstant.EVENT_CMD_RETURN: {
                    //TODO 数据同步的返回，无需处理
                }
                break;

                default:
                    break;

            }
        }
    };

    /**
     * 处理AIUI结果事件（听写结果和语义结果）
     *
     * @param event 结果事件
     */
    public void processResult(AIUIEvent event) {
        try {
            JSONObject bizParamJson = new JSONObject(event.info);
            JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
            JSONObject params = data.getJSONObject("params");
            JSONObject content = data.getJSONArray("content").getJSONObject(0);
            long rspTime = event.data.getLong("eos_rslt", -1);
            String sub = params.optString("sub");
            if (content.has("cnt_id") && !sub.equals("tts")) {
                String cnt_id = content.getString("cnt_id");
                JSONObject cntJson = new JSONObject(new String(event.data.getByteArray(cnt_id), StandardCharsets.UTF_8));
                Log.i(TAG, "识别结果【1】-->" + cntJson.toString());
                if ("nlp".equals(sub)) {
                    String srcJson = cntJson.optString("intent");
                    Log.d(TAG, "SpeechWorker 语义结果：" + srcJson);
                    if (srcJson != null && srcJson.length() != 0) {
                        String speakText = ResponseJson.analysisResponse(srcJson);
                        if (speakText == null) {
                            return;
                        }
                        if (speakText.length() <= 1) {
                            return;
                        }
                        NLP_RESULT_CACHE = speakText.replace("[k3]", "").replace("[k0]", "");
                        Log.d(TAG, "SpeechWorker Result:NLP_RESULT_CACHE--->" + NLP_RESULT_CACHE + "----------------------------START");
                        speechCallback.nlpResult(NLP_RESULT_CACHE, srcJson);
                        Log.d(TAG, "Speak：" + speakText);
                    }
                } else if ("iat".equals(sub)) {
                    processIATResult(cntJson);
                }
            }
        } catch (Throwable e) {
            Log.i(TAG, "---->Throwable" + e.toString());
            e.printStackTrace();
        }
    }

    private String[] mIATPGSStack = new String[256];
    long allcount = 0;

    public void setBaseSpeechCallback(BaseSpeechCallback listener) {
        this.speechCallback = listener;
    }

    public static String NLP_RESULT_CACHE = "";

    /**
     * nlp 和 iat 基本返回结果基本同时回来,我们先把nlp的识别结果缓存下来，如果用户说的是业务话语，则走道维后台
     * 如果是闲聊，则把缓存的nlp语义结果取出来
     *
     * @return 返回缓存的语义结果
     */
    public static String getNlpResultCache() {
        return NLP_RESULT_CACHE;
    }

    public static void setNlpResultCacheEmpty() {
        NLP_RESULT_CACHE = "";
    }

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
            if (TextUtils.isEmpty(iatText)) {
                return;
            }
            String s = iatText.toString();
            Log.d(TAG, "SpeechWorker FINAL_RESULT---->" + s + "<----【" + ++allcount + "】" + "----------------------------START");
            // TODO: 2019/2/16 非PGS模式结果最终听写到的结果内容
            speechCallback.recognizeResult(s);
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
                if (TextUtils.isEmpty(mIATPGSStack[index])) {
                    continue;
                }
                PGSResult.append(mIATPGSStack[index]);
                //如果是最后一条听写结果，则清空stack便于下次使用
                if (lastResult) {
                    mIATPGSStack[index] = null;
                }
            }

            if (lastResult) {
                // TODO: 2019/2/16 pgs模式最终听写到的结果内容
                String ttsStr = PGSResult.toString();
                Log.d(TAG, "SpeechWorker FINAL_RESULT---->" + ttsStr + "<----【" + allcount + "】" + "----------------------------START");
                speechCallback.recognizeResult(ttsStr);
            }
        }
    }


    /**
     * 开始合成
     *
     * @param text       合成文本
     * @param onComplete 合成完成回调
     */
    public static void onSpeaking(String text, Runnable onComplete) {
        if (TextUtils.isEmpty(text)) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        mTTSCallback = onComplete;

        Log.i("start TTS %s", text);
        AIUIMessage startTts = new AIUIMessage(AIUIConstant.CMD_TTS, AIUIConstant.START, 0, initTtsParams(), text.getBytes());
        sendMessage(startTts);
    }


    /**
     * 开始合成
     *
     * @param text 待合成的文本
     */
    public static void onSpeaking(String text) {
        Log.i("start TTS %s", text);
        AIUIMessage startTts = new AIUIMessage(AIUIConstant.CMD_TTS, AIUIConstant.START, 0, initTtsParams(), text.getBytes());
        sendMessage(startTts);
    }

    /**
     * 配置语音合成所需的字符串
     *
     * @return 返回tts所需配置的参数串
     */
    private static String initTtsParams() {

        String tag = "@" + System.currentTimeMillis();
        //构建合成参数
        StringBuilder params = new StringBuilder();
        //合成发音人
        params.append("vcn=" + ttsLanguage);
        //合成速度
        params.append(",speed=50");
        //合成音调
        params.append(",pitch=50");
        //合成音量
        params.append(",volume=50");
        //合成音量
        params.append(",ent=x_tts");
        //合成tag，方便追踪合成结束，暂未实现
        params.append(",tag=").append(tag);
        return params.toString();
    }

    private static @ITTSLanguage String ttsLanguage;

    @ITTSLanguage
    private String getTTSLanguage() {
        return ttsLanguage;
    }

    /**
     * 设置合成发音人
     *
     * @param language TTSLanguage.vixqa
     */
    public void setTtsLanguage(@ITTSLanguage String language) {
        ttsLanguage = language;
    }

    /**
     * 暂停合成
     */
    public void pauseSpeaking() {
        Log.i(TAG, "Pause TTS");
        sendMessage(new AIUIMessage(AIUIConstant.CMD_TTS, AIUIConstant.PAUSE, 0, null, null));
    }

    /**
     * 停止合成播放
     */
    public static void stopSpeaking() {
        if (null == mAIUIAgent) {
            Log.i(TAG, "AIUIAgent 为空，请先创建");
            return;
        }
        Log.i(TAG, "Stop TTS");
        sendMessage(new AIUIMessage(AIUIConstant.CMD_TTS, AIUIConstant.CANCEL, 0, "", null));
    }

    /**
     * 发送AIUI消息
     *
     * @param message AIUI消息
     */
    public static void sendMessage(AIUIMessage message) {
        if (mAIUIAgent != null) {
            //确保AIUI处于唤醒状态
            if (mAIUIState != AIUIConstant.STATE_WORKING) {
                mAIUIAgent.sendMessage(new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null));
            }

            mAIUIAgent.sendMessage(message);
        }
    }

    /**
     * 语音唤醒参数
     *
     * @param context 上下文
     */
    private void initializeMSCIfExist(Context context, String appid) {
        Log.d(TAG, "initializeMSCIfExist::appid " + appid);
        SpeechUtility.createUtility(context, String.format("engine_start=ivw,delay_init=0,appid=%s", appid));
    }

    /**
     * 文本语义
     *
     * @param message 输入文本
     */
    public static void writeText(String message) {
        stopSpeaking();
        //pers_param用于启用动态实体和所见即可说功能
        String params = "data_type=text,pers_param={\"appid\":\"\",\"uid\":\"\"}";
        sendMessage(new AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0,
                params, message.getBytes()));
    }

    /**
     * 切换情景模式
     */
    public static void setScene(SCENE changeScene) {
        String setParams = "{\"global\":{\"scene\":\"" + changeScene + "\"}}";
        Log.d(TAG, "setParams::" + setParams);
        AIUIMessage setMsg = new AIUIMessage(AIUIConstant.CMD_SET_PARAMS, 0, 0, setParams, null);
        mAIUIAgent.sendMessage(setMsg);
    }

    public void onDestroy() {
        Log.i(TAG, "SpeechManager onDestroy");
        stopSpeaking();
        stopVoice();
        destroyAgent();
    }

    /**
     * 情景模式,中文，英文，不同的情景模式有不同的技能，问答
     * {@code https://aiui.iflyos.cn/app/5b614ca0/config?sceneName=trans}
     */
    public enum SCENE {
        /**
         * 情景模式trans:识别英文，英文的自定义问答请在AIUI官网添加
         */
        trans,
        /**
         * 情景模式main
         */
        main,
        /**
         * mainNoTuling
         */
        mainNoTuling
    }

    /**
     * 语音识别 中文类别下包括：普通话、四川话、粤语
     */
    public enum RecognizeLanguage {
        /**
         * 普通话
         */
        mandarin,
        /**
         * 四川话
         */
        lmz,
        /**
         * 粤语
         */
        cantonese
    }

    private MutableLiveData<AIUIEvent> mAIUIEvents = new MutableLiveData<>();

    public MutableLiveData<AIUIEvent> getmAIUIEvent() {
        return mAIUIEvents;
    }


    /**
     * 设置错误回调接口
     *
     * @param callback 传入错误信息回调接口
     */
    public void setErrorInfoCallback(IErrorInfoCallback callback) {
        iErrorInfoCallback = callback;
    }
}

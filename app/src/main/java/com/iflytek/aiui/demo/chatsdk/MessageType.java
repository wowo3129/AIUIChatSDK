package com.iflytek.aiui.demo.chatsdk;

/**
 * 消息类型定义。主调引擎服务接口消息、识别/合成 回调消息
 *
 * @author ydong
 */
public class MessageType {
    
    // -----------------识别主调消息类型定义---------------------//
    /**
     * 开始识别录音
     */
    public final static int CALL_START_RECOGNIZE = 1001;
    /**
     * 停止识别
     */
    public final static int CALL_STOP_RECOGNIZE = 1002;
    /**
     * 开始文本识别
     **/
    public final static int CALL_START_TEXT_RECOGNIZE = 1006;
    /**
     * 结束文本识别
     **/
    public final static int CALL_STOP_TEXT_RECOGNIZE = 1007;

    /**
     * 开始文本识别
     **/
    public final static int CALL_START_REEMAN_TEXT_RECOGNIZE = 1008;

    // ------------------合成主调消息类型定义------------------//
    /**
     * 开始合成
     */
    public final static int CALL_START_SPEAK = 3001;
    /**
     * 停止合成
     */
    public final static int CALL_STOP_SPEAK = 3002;

    // 语音识别插件初始化完成
    public final static int CALLBACK_Iat_INIT = 5001;
    // 语义理解插件初始化完成
    public final static int CALLBACK_TextUnderstander_INIT = 5002;
    // 合成插件初始化完成
    public final static int CALLBACK_SpeechSynthesizer_INIT = 5003;
    // 唤醒事件通知
    public final static int CALLBACK_WAKE_UP = 5004;
    public final static int CALLBACK_LAST_MOVTION = 5005;
    public final static int CALLBACK_SCRAMSTATE = 5006;

    public final static int CALLBACK_NETWORK = 5007;

    public final static int CALLBACK_REEMAN_AI = 5008;
    public final static int CALLBACK_REEMAN_AI_MOR = 5009;
    public final static int CALLBACK_REEMAN_AI_IFLYTEK = 5010;

    public final static int CALL_TULING_ANSWER = 1;
    public final static int CALLBACK_REEMAN_AI_LOCAL = 2;
}

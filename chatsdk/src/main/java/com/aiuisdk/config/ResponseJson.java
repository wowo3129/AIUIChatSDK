package com.aiuisdk.config;


import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Project Name：
 * Description ：寻找  "answer":{"text":""} 的递归
 * {"rc":4,"uuid":"cida114f2fb@dx005f0fd034ba010125","sid":"cida114f2fb@dx005f0fd034ba010125","text":"加一等于几"}
 *
 * @author ：update by ydong
 */
public class ResponseJson {

    /**
     * 正常情况下应答应该有一个key为answer的JSONObject
     */
    private static final String ANSWER = "answer";
    /**
     * 上述Object里应该包含一个text，string类型，这个应该就是我们要读出来的答案
     */
    private static final String ANSWER_TEXT = "text";
    /**
     * 用于标识用户请求响应的状态，它包含用户操作成功或异常等几个方面的状态编号。
     * 当存在多个候选的响应结果时，每个响应结果内都必须包含相应的rc码，便于客户端对每个响应包进行识别和操作。
     * 0	操作成功
     * 1	输入异常
     * 2	系统内部异常
     * 3	业务操作失败，没搜索到结果或信源异常
     * 4	文本没有匹配的技能场景，技能不理解或不能处理该文本
     */
    private static final String RC = "rc";

    private static final String JSON_HEAD = "{";
    private static final String JSON_END = "}";
    private static final String TAG = "ResponseJson";

    @Nullable
    public static String analysisResponse(String json) throws JSONException {
        if (checkJson(json)) {
            return null;
        }
        JSONObject object = new JSONObject(json);
        int rc = object.optInt(RC);
        if (rc == 0 || rc == 3) {
            if (object.has(ANSWER)) {
                return getAnswerText(object);
            } else {
                Iterator<String> keys = object.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONObject jo = object.optJSONObject(key);
                    if (jo != null) {
                        return analysisResponse(jo.toString());
                    }
                }
                return null;
            }
        } else if (rc == 1) {
            Log.e(TAG, "ResponseJson::analysisResponse rc == " + rc + "  输入异常");
            return "您可以换种方法问我";
        } else if (rc == 2) {
            Log.e(TAG, "ResponseJson::analysisResponse rc == " + rc + " 系统内部异常");
            return "系统内部异常";
        } else if (rc == 4) {
            Log.e(TAG, "ResponseJson::analysisResponse rc == " + rc + " 文本没有匹配的技能场景，技能不理解或不能处理该文本");
            return "不知道您在说什么，换个方式问我吧";
        } else {
            return null;
        }

    }

    private static String getAnswerText(JSONObject object) {
        String speakText = null;
        if (object != null) {
            JSONObject answer = object.optJSONObject(ANSWER);
            if (answer != null) {
                speakText = answer.optString(ANSWER_TEXT);
            }
        }
        Log.d(TAG, "SpeechWorker getAnswerText：" + speakText);
        return speakText;
    }

    public static String getQuestion(String json) throws JSONException {
        if (checkJson(json)) {
            return null;
        }
        JSONObject object = new JSONObject(json);
        return object.optString(ANSWER_TEXT);
    }

    /**
     * 检查返回返回的字符串是否是json串
     *
     * @param json 待检查json串
     * @return 返回对应的字符串是否是json串
     */
    private static boolean checkJson(String json) {
        return json == null || !json.trim().startsWith(JSON_HEAD) || !json.trim().endsWith(JSON_END);
    }
}

package com.huimin.baidulib.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;

import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.TtsMode;
import com.huimin.baidulib.control.InitConfig;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Created by fujiayi on 2017/12/28.
 */

/**
 * 自动排查工具，用于集成后发现错误。
 * <p>
 * 可以检测如下错误：
 * 1. PermissionCheck ： AndroidManifest,xml 需要的部分权限
 * 2. JniCheck： 检测so文件是否安装在指定目录
 * 3. AppInfoCheck: 联网情况下 , 检测appId appKey secretKey是否正确
 * 4. ApplicationIdCheck: 显示包名applicationId， 提示用户手动去官网检查
 * 5. ParamKeyExistCheck： 检查key是否存在，目前检查 SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE
 * 和PARAM_TTS_SPEECH_MODEL_FILE
 * 6.  OfflineResourceFileCheck 检查离线资源文件（需要从assets目录下复制），是否存在
 * <p>
 * <p>
 * 示例使用代码：
 * AutoCheck.getInstance(getApplicationContext()).check(initConfig, new Handler() {
 *
 * @Override public void handleMessage(Message msg) {
 * if (msg.what == 100) {
 * AutoCheck autoCheck = (AutoCheck) msg.obj;
 * synchronized (autoCheck) {
 * String message = autoCheck.obtainDebugMessage();
 * toPrint(message); // 可以用下面一行替代，在logcat中查看代码
 * //Log.w("AutoCheckMessage",message);
 * }
 * }
 * }
 * <p>
 * });
 */
public class AutoTtsCheck {

    private static AutoTtsCheck instance;

    private LinkedHashMap<String, Check> checks;

    private static Context context;

    private boolean hasError = false;

    volatile boolean isFinished = false;

    /**
     * 获取实例，非线程安全
     *
     * @return
     */
    public static AutoTtsCheck getInstance(Context context) {
        if (instance == null || AutoTtsCheck.context != context) {
            instance = new AutoTtsCheck(context);
        }
        return instance;
    }

    public void check(final InitConfig initConfig, final Handler handler) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                AutoTtsCheck obj = innerCheck(initConfig);
                isFinished = true;
                synchronized (obj) { // 偶发，同步线程信息
                    Message msg = handler.obtainMessage(100, obj);
                    handler.sendMessage(msg);
                }
            }
        });
        t.start();

    }

    private AutoTtsCheck innerCheck(InitConfig config) {
        checks.put("检查申请的Android权限", new PermissionCheck(context));
        checks.put("检查4个so文件是否存在", new JniCheck(context));
        checks.put("检查AppId AppKey SecretKey",
                new AppInfoCheck(config.getAppId(), config.getAppKey(), config.getSecretKey()));
        checks.put("检查包名", new ApplicationIdCheck(context, config.getAppId()));

        if (TtsMode.MIX.equals(config.getTtsMode())) {
            Map<String, String> params = config.getParams();
            String fileKey = SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE;
            checks.put("检查离线资TEXT文件参数", new ParamKeyExistCheck(params, fileKey,
                    "SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE未设置 ，"));
            checks.put("检查离线资源TEXT文件", new OfflineResourceFileCheck(params.get(fileKey)));
            fileKey = SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE;
            checks.put("检查离线资Speech文件参数", new ParamKeyExistCheck(params, fileKey,
                    "SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE未设置 ，"));
            checks.put("检查离线资源Speech文件", new OfflineResourceFileCheck(params.get(fileKey)));
        }

        for (Map.Entry<String, Check> e : checks.entrySet()) {
            Check check = e.getValue();
            check.check();
            if (check.hasError()) {
                break;
            }
        }
        return this;
    }

    public String obtainErrorMessage() {
        PrintConfig config = new PrintConfig();
        return formatString(config);
    }

    public String obtainDebugMessage() {
        PrintConfig config = new PrintConfig();
        config.withInfo = true;
        return formatString(config);
    }

    public String obtainAllMessage() {
        PrintConfig config = new PrintConfig();
        config.withLog = true;
        config.withInfo = true;
        return formatString(config);
    }

    public String formatString(PrintConfig config) {
        StringBuilder sb = new StringBuilder();
        hasError = false;

        for (HashMap.Entry<String, Check> entry : checks.entrySet()) {
            Check check = entry.getValue();
            String testName = entry.getKey();
            if (check.hasError()) {
                if (!hasError) {
                    hasError = true;
                }

                sb.append("【错误】【").append(testName).append(" 】  ").append(check.getErrorMessage()).append("\n");
                if (check.hasFix()) {
                    sb.append("【修复方法】【").append(testName).append(" 】  ").append(check.getFixMessage()).append("\n");
                }
            }
            if (config.withInfo && check.hasInfo()) {
                sb.append("【请手动检查】【").append(testName).append("】 ").append(check.getInfoMessage()).append("\n");
            }
            if (config.withLog && (config.withLogOnSuccess || hasError) && check.hasLog()) {
                sb.append("【log】:" + check.getLogMessage()).append("\n");
            }
        }
        if (!hasError) {
            sb.append("集成自动排查工具： 恭喜没有检测到任何问题\n");
        }
        return sb.toString();
    }

    public void clear() {
        checks.clear();
        hasError = false;
    }

    private AutoTtsCheck(Context context) {
        this.context = context;
        checks = new LinkedHashMap<String, Check>();
    }

    private static class PrintConfig {
        public boolean withFix = true;
        public boolean withInfo = false;
        public boolean withLog = false;
        public boolean withLogOnSuccess = false;
    }

    private static class PermissionCheck extends Check {
        private Context context;

        public PermissionCheck(Context context) {
            this.context = context;
        }

        @Override
        public void check() {
            String[] permissions = {
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    // Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    // Manifest.permission.WRITE_SETTINGS,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    // Manifest.permission.CHANGE_WIFI_STATE
            };

            ArrayList<String> toApplyList = new ArrayList<String>();

            for (String perm : permissions) {
                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(context, perm)) {
                    toApplyList.add(perm);
                    // 进入到这里代表没有权限.
                }
            }
            if (!toApplyList.isEmpty()) {
                errorMessage = "缺少权限：" + toApplyList;
                fixMessage = "请从AndroidManifest.xml复制相关权限";
            }
        }
    }

    private static class JniCheck extends Check {
        private Context context;

        private String[] soNames;

        public JniCheck(Context context) {
            this.context = context;
            soNames = new String[]{"libbd_etts.so", "libBDSpeechDecoder_V1.so", "libbdtts.so", "libgnustl_shared.so"};
        }

        @Override
        public void check() {
            String path = context.getApplicationInfo().nativeLibraryDir;
            appendLogMessage("Jni so文件目录 " + path);
            File[] files = new File(path).listFiles();
            TreeSet<String> set = new TreeSet<>();
            if (files != null) {
                for (File file : files) {
                    if (file.canRead()) {
                        set.add(file.getName());
                    }
                }
            }
            appendLogMessage("Jni目录内文件: " + set.toString());
            for (String name : soNames) {
                if (!set.contains(name)) {
                    errorMessage = "Jni目录" + path + " 缺少可读的so文件：" + name + "， 该目录文件列表: " + set.toString();
                    fixMessage = "如果您的app内没有其它so文件，请复制demo里的src/main/jniLibs至同名目录。"
                            + " 如果app内有so文件，请合并目录放一起(注意目录取交集，多余的目录删除)。";
                    break;
                }
            }
        }
    }

    private static class ParamKeyExistCheck extends Check {
        private Map<String, String> params;
        private String key;
        private String prefixErrorMessage;

        public ParamKeyExistCheck(Map<String, String> params, String key, String prefixErrorMessage) {
            this.params = params;
            this.key = key;
            this.prefixErrorMessage = prefixErrorMessage;
        }

        @Override
        public void check() {
            if (params == null || !params.containsKey(key)) {
                errorMessage = prefixErrorMessage + " 参数中没有设置：" + key;
                fixMessage = "请参照demo在设置 " + key + "参数";
            }
        }
    }

    private static class OfflineResourceFileCheck extends Check {
        private String filename;
        private String nullMessage;

        public OfflineResourceFileCheck(String filename) {
            this.filename = filename;
            this.nullMessage = nullMessage;
        }

        @Override
        public void check() {
            File file = new File(filename);
            boolean isSuccess = true;
            if (!file.exists()) {
                errorMessage = "资源文件不存在：" + filename;
                isSuccess = false;
            } else if (!file.canRead()) {
                errorMessage = "资源文件不可读：" + filename;
                isSuccess = false;
            }

            if (!isSuccess) {
                fixMessage = "请将demo中src/main/assets目录下同名文件复制到 " + filename;
            }
        }
    }

    private static class ApplicationIdCheck extends Check {

        private String appId;
        private Context context;

        public ApplicationIdCheck(Context context, String appId) {
            this.appId = appId;
            this.context = context;
        }

        @Override
        public void check() {
            infoMessage = "如果您集成过程中遇见离线合成初始化问题，请检查网页上appId：" + appId
                    + " 应用是否开通了合成服务，并且网页上的应用填写了Android包名："
                    + getApplicationId();
        }

        private String getApplicationId() {
            return context.getPackageName();
        }
    }


    private static class AppInfoCheck extends Check {
        private String appId;
        private String appKey;
        private String secretKey;

        public AppInfoCheck(String appId, String appKey, String secretKey) {
            this.appId = appId;
            this.appKey = appKey;
            this.secretKey = secretKey;
        }


        public void check() {
            do {
                appendLogMessage("try to check appId " + appId + " ,appKey=" + appKey + " ,secretKey" + secretKey);
                if (appId == null || appId.isEmpty()) {
                    errorMessage = "appId 为空";
                    fixMessage = "填写appID";
                    break;
                }
                if (appKey == null || appKey.isEmpty()) {
                    errorMessage = "appKey 为空";
                    fixMessage = "填写appID";
                    break;
                }
                if (secretKey == null || secretKey.isEmpty()) {
                    errorMessage = "secretKey 为空";
                    fixMessage = "secretKey";
                    break;
                }

            } while (false);
            try {
                checkOnline();
            } catch (UnknownHostException e) {
                infoMessage = "无网络或者网络不连通，忽略检测 : " + e.getMessage();
            } catch (Exception e) {
                errorMessage = e.getClass().getCanonicalName() + ":" + e.getMessage();
                fixMessage = " 重新检测appId， appKey， appSecret是否正确";
            }
        }

        public void checkOnline() throws Exception {
            String urlpath = "http://openapi.baidu.com/oauth/2.0/token?grant_type=client_credentials&client_id="
                    + appKey + "&client_secret=" + secretKey;
            URL url = new URL(urlpath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1000);
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder result = new StringBuilder();
            String line = "";
            do {
                line = reader.readLine();
                if (line != null) {
                    result.append(line);
                }
            } while (line != null);
            String res = result.toString();
            appendLogMessage("openapi return " + res);
            JSONObject jsonObject = new JSONObject(res);
            String error = jsonObject.optString("error");
            if (error != null && !error.isEmpty()) {
                throw new Exception("appkey secretKey 错误" + ", error:" + error + ", json is" + result);
            }
            String token = jsonObject.getString("access_token");
            if (token == null || !token.endsWith("-" + appId)) {
                throw new Exception("appId 与 appkey及 appSecret 不一致。appId = " + appId + " ,token = " + token);
            }
        }


    }

    private abstract static class Check {
        protected String errorMessage = null;

        protected String fixMessage = null;

        protected String infoMessage = null;

        protected StringBuilder logMessage;

        public Check() {
            logMessage = new StringBuilder();
        }

        public abstract void check();

        public boolean hasError() {
            return errorMessage != null;
        }

        public boolean hasFix() {
            return fixMessage != null;
        }

        public boolean hasInfo() {
            return infoMessage != null;
        }

        public boolean hasLog() {
            return !logMessage.toString().isEmpty();
        }

        public void appendLogMessage(String message) {
            logMessage.append(message + "\n");
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getFixMessage() {
            return fixMessage;
        }

        public String getInfoMessage() {
            return infoMessage;
        }

        public String getLogMessage() {
            return logMessage.toString();
        }


    }
}

# ChatSDK 全双工语音识别库
您可以下载本项目，然后将ChatSDK作为 Module 导入你的项目使用<br/>
## 介绍：ChatSDK 是基于讯飞的AIUI进行封装，让大家尽可能简洁的使用AIUI <br/>
## 重大亮点：全双工语音识别，不中断，不中断，不中断<br/>
## 包括：语音识别、语音合成、语音语义理解、文本语义理解<br/>

### 使用说明：
1：将chatsdk作为module引入自己的项目<br/>
2：替换自己的libaiui.so库和aiui_phone.cfg中的appid<br/>
3：安装成功后，确保权限打开，跟手机说话，会有结果返回<br/>

### 相关接口使用

#### step1 : 初始化语音合成、识别模块
```java 
SpeechManager.CreateInstance(getApplicationContext());
```
#### step2 : 设置语音识别和语义的回调
```java 
SpeechManager.getInstance().setBaseSpeechCallback(speechCallback); 
```
#### step3 : 语音识别和语义理解的回调
```java
BaseSpeechCallback speechCallback = new BaseSpeechCallback() {
    /**
     * 通过识别语音得到文字
     *
     * @param text 识别后的文本
     */
    @Override
    public void recognizeResult(String text) {
        //语音识别结果
    }

    /**
     * @param text 返回三方语义结果，可直接speak出来
     * @param json 返回三方语义结果json串，可根据需求自行解析
     */
    @Override
    public void nlpResult(String text, String json) {
        //语义理解结果
        SpeechManager.onSpeaking(text);
    }
};
```

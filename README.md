[![996.icu](https://img.shields.io/badge/link-996.icu-red.svg)](https://996.icu)
***
[ChatSDK](https://github.com/wowo3129/AIUIChatSDK/tree/master/chatsdk) :是对AIUI的语音SDK封装,套餐费用最低在6万/年</br>
[iflylib](https://github.com/wowo3129/MvpApp/tree/master/iflylib) :是对原始msc的语音SDK封装，相对AIUI便宜很多</br>
[baidulib](https://github.com/wowo3129/AIUIChatSDK/tree/master/baidulib) :是对百度语音SDK封装,百度号称永久免费</br>
<br/>
共同特点：实现了全双工语音识别iat、合成tts、和语义nlp部分接口的封装,经过稳定测试，通过简洁的调用方式来满足开发需求。两者核心类都是 SpeechManager</br>
参考文档：[玩转AIUI后处理](https://github.com/happyLiMing/AIUITPPServer)、[讯飞论坛](http://bbs.xfyun.cn/forum.php)</br>
参考项目：[电视语音助手](https://github.com/crjwgr/TvAssistant)
***
## [ChatSDK](https://github.com/wowo3129/AIUIChatSDK/tree/master/chatsdk) 全双工语音识别库
#### 重大亮点：全双工语音识别，不中断，不中断，不中断<br/>
ChatSDK 是基于讯飞的AIUI进行封装，使用简洁，您可以下载本项目，然后将[ChatSDK](https://github.com/wowo3129/AIUIChatSDK/tree/master/chatsdk)作为 Module 导入你的项目使用<br/>
[核心类:SpeechManager](https://github.com/wowo3129/AIUIChatSDK/blob/master/chatsdk/src/main/java/com/aiuisdk/SpeechManager.java)
<br/>
<br/>
#### 功能包括：语音识别、语音合成、语音语义理解、文本语义理解<br/>
#### 场景：(机器人、智能音箱、车载语音、家电语音、人机交互、在线教育机器人...)<br/>


#### 使用说明：
1：将chatsdk作为module引入自己的项目<br/>
2：替换自己的libaiui.so库和aiui_phone.cfg中的appid<br/>
3：安装成功后，确保权限打开，跟手机说话，会有结果返回<br/>

#### 相关接口使用

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
#### step4 : 错误码回调接口
```java
/**
 * 设置错误回调接口
 *
 * @param callback 传入错误信息回调接口
 */
public void setErrorInfoCallback(IErrorInfoCallback callback) {
    iErrorInfoCallback = callback;
}
```
***
#疑难问题：

1：讯飞服务有时候不稳定的情况：封装专门的测试工具<br/>
2：讯飞的服务器稳不稳定，对讯飞的HTTP和百度这种三方网站进行封装，实时的观察讯飞当前网络的链接UI动态情况<br/>
3：vad 前后置端点的处理, 解决跟interact_timeout两者之间互相影响的问题<br/>
4: 离线语音合成TTS不用讯飞语记<br/>
5：需要等2小时测试一下，但是热词并不是绝对的，只是增加热词的被识别出来的概率。<br/>
6：清除历史<br/>
  AIUI支持多轮对话，如在问合肥今天的天气怎么样之后，再询问明天的呢，AIUI会结合上一句询问合肥今天 天气的历史，就会回答合肥明天的天气。<br/>
  AIUI默认在休眠后唤醒会清除交互历史，在交互状态下唤醒，则不会清除交互历史。<br/>
  AIUI清除历史的方式是可配置的，默认为auto即是上面描述的模式。当配置成user值后， 用户可以通过发送CMD_CLEAN_DIALOG_HISTORY在任何时候手动清除交互的历史。即使在上面两种情况下，客户端没有主动清除交互历史，服务端保存用户交互历史的时间也是有限的，当用户交互超过5轮后，服务端也会将交互历史清空。<br/>
7：AIUI的SDK出现网络10120问题，实际是anzer5G网络来回切换导致网络不稳定，换了高功率的HRR-Test（anzer2017）后正常.<br/>
8:上传热词无法通过客户端SDK进行，单纯的热词仅可通过网页端进行上传，如果您逐步深入了解了所见即可说、动态实体等高级特性就会发现上传的实体等资源本身就是热词的一种。<br/>
9：AIUI主动在线合成有时会出现卡顿，建议使用离线合成<br/>
10：AIUI不支持识别语种的切换，只有通过情景模式来切换自己想要的语种<br/>
11：[关于 AIUI "continuous" 会识别到自身合成语音问题](http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=40844&highlight=%E5%90%88%E6%88%90)<br/>
11：(未完待续。。。）
***
```
联系方式： QQ群: 673450581   个人QQ：1094035520   昵称：肥兔子
```

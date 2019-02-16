AIUIChatDemo是一个集成了AIUI SDK的用于演示AIUI功能的产品Demo，同时开放源码供开发者集成时进行参考。
Demo支持通过语音或文本进行交互，并且会对听写和语义的结果进行处理展示。

Q1:Demo中AIUI SDK的调用在哪？
A:Demo对于AIUI SDK的调用位于源码中src/main/java/com/iflytek/aiui/demo/chat/repository/AIUIWrapper.java。
  同时Demo中也提供了一个完整调用AIUI SDK的简单入口，源码位于src/sample/java/com/iflytek/aiui/demo/chat/NlpDemo.java，
  你可以通过在Manifest中指定Demo入口Activity为NlpDemo进行体验。

Q2:Demo如何启用唤醒功能？
A:步骤如下：
  1. 下载唤醒资源，重命名为ivw.jet, 放入src/main/assets/ivw/下
  2. 将平台上下载的唤醒SDK中的Msc.jar放入libs/下，libmsc.so放入src/main/jniLibs/armeabi下

# AIUIChatSDK
AIUIChatSDK语音识别、合成、唤醒、SDK封装、上传热词、

用戶需要新的新的唤醒词，如何修改代码？
# todo




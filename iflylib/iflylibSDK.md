```
    SpeechManager 是唯一的对外接口类，通过该类管理识别 合成
    
    step1:     
    SpeechManager.getInstance().init(this);
    
    step2:
    private void initVoice() {
        SpeechManager.getInstance().initStart(this);
        SpeechManager.getInstance().setRecognizerListener(iRecognizerListener);
        SpeechManager.getInstance().setSpeakListener(iSpeakListener);
        SpeechManager.getInstance().startReco();
    }

    /**
     * 语音识别回调
     */
    IRecognizerListener iRecognizerListener = new IRecognizerListener() {
        @Override
        public void onVolumeChanged(int volume) {

        }

        @Override
        public void onResult(String result) {
            myViewModel.getResultLiveData().setValue(result);
            requestApiByRetrofitRxJava(result);
        }

        @Override
        public void onError(String msg) {

        }
    };
    /**
     * 语音合成回调
     */
    ISpeakListener iSpeakListener = new ISpeakListener() {
        @Override
        public void onSpeakBegin(String text) {

        }

        @Override
        public void onSpeakOver(String msg) {

        }

        @Override
        public void onInterrupted() {

        }
    };
    
```
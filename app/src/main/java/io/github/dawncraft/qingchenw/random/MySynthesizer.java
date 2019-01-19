package io.github.dawncraft.qingchenw.random;

import android.content.Context;
import android.util.Log;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

import java.util.Map;

/**
 * 对SpeechSynthesizer进行简单封装
 * 语音合成由百度强力支持
 * 感谢fujiayi
 * <p>
 * Created by QingChenW on 2018/6/26
 */
public class MySynthesizer
{
    private static boolean isInitial = false;

    private Context context;
    private SpeechSynthesizer speechSynthesizer;

    public MySynthesizer(Context context, Config config)
    {
        this(context);
        init(config);
    }

    public MySynthesizer(Context context)
    {
        if (isInitial)
        {
            // 不能连续调用SpeechSynthesizer.getInstance()
            throw new RuntimeException("SpeechSynthesizer hasn't been released, don't create a new object!");
        }
        this.context = context;
        isInitial = true;
    }

    /**
     * 该方法需要在新线程中调用, 且该线程不能结束
     *
     * @param config
     * @return
     */
    protected boolean init(Config config)
    {
        speechSynthesizer = SpeechSynthesizer.getInstance();
        speechSynthesizer.setContext(context);
        speechSynthesizer.setAppId(config.getAppId());
        speechSynthesizer.setApiKey(config.getAppKey(), config.getSecretKey());
        if (config.getParams() != null)
        {
            for (Map.Entry<String, String> e : config.getParams().entrySet())
            {
                speechSynthesizer.setParam(e.getKey(), e.getValue());
            }
        }
        speechSynthesizer.setSpeechSynthesizerListener(config.getListener());

        if (config.getTtsMode().equals(TtsMode.MIX))
        {
            AuthInfo authInfo = speechSynthesizer.auth(config.getTtsMode());
            if (!authInfo.isSuccess())
            {
                String errorMsg = authInfo.getTtsError().getDetailMessage();
                Log.e(context.getPackageName(),
                        String.format(context.getString(R.string.synthesize_auth_error), errorMsg));
            }
        }

        int result = speechSynthesizer.initTts(config.getTtsMode());
        if (result != 0)
        {
            Log.e(context.getPackageName(),
                    String.format(context.getString(R.string.synthesize_init_error), result));
            return false;
        }

        return true;
    }

    /**
     * 合成并播放
     *
     * @param text 小于1024 GBK字节, 即512个汉字或者字母数字
     * @return
     */
    public int speak(String text)
    {
        return speechSynthesizer.speak(text);
    }

    /**
     * 合成并播放
     *
     * @param text 小于1024 GBK字节，即512个汉字或者字母数字
     * @param utteranceId 用于listener的回调，默认"0"
     *
     * @return
     */
    public int speak(String text, String utteranceId)
    {
        return speechSynthesizer.speak(text, utteranceId);
    }

    public int pause()
    {
        return speechSynthesizer.pause();
    }

    public int resume()
    {
        return speechSynthesizer.resume();
    }

    public int stop()
    {
        return speechSynthesizer.stop();
    }

    public void release()
    {
        speechSynthesizer.stop();
        speechSynthesizer.release();
        speechSynthesizer = null;
        isInitial = false;
    }

    /**
     * 设置参数
     *
     * @return
     */
    public int setParam(String key, String value)
    {
        return speechSynthesizer.setParam(key, value);
    }

    /**
     * 注意 只有 TtsMode.MIX 才可以切换离线发音
     *
     * @return
     */
    public int loadModel(String modelFilename, String textFilename)
    {
        // 合成语音时不能调用该方法!
        stop();
        return speechSynthesizer.loadModel(modelFilename, textFilename);
    }

    /**
     * 设置播报音量，默认最大
     *
     * @param leftVolume 范围[0, 1], 默认1.0f
     * @param rightVolume 范围[0, 1], 默认1.0f
     */
    public void setStereoVolume(float leftVolume, float rightVolume)
    {
        speechSynthesizer.setStereoVolume(leftVolume, rightVolume);
    }

    public static class Config
    {
        /**
         * appId
         */
        private String appId;

        /**
         * appKey
         */
        private String appKey;

        /**
         * secretKey
         */
        private String secretKey;

        private TtsMode ttsMode;

        private Map<String, String> params;

        private SpeechSynthesizerListener listener;

        private Config() {}

        public Config(String appId, String appKey, String secretKey, TtsMode ttsMode,
                      Map<String, String> params, SpeechSynthesizerListener listener) {
            this(appId, appKey, secretKey);
            this.setTtsMode(ttsMode);
            this.setParams(params);
            this.setListener(listener);
        }

        public Config(String appId, String appKey, String secretKey) {
            this.appId = appId;
            this.appKey = appKey;
            this.secretKey = secretKey;
        }

        public String getAppId() {
            return this.appId;
        }

        public String getAppKey() {
            return this.appKey;
        }

        public String getSecretKey() {
            return this.secretKey;
        }

        public TtsMode getTtsMode() {
            return this.ttsMode;
        }
        public void setTtsMode(TtsMode ttsMode) { this.ttsMode = ttsMode; }

        public Map<String, String> getParams() {
            return params;
        }
        public void setParams(Map<String, String> params) { this.params = params; }

        public SpeechSynthesizerListener getListener() {
            return listener;
        }
        public void setListener(SpeechSynthesizerListener listener) { this.listener = listener; }
    }
}

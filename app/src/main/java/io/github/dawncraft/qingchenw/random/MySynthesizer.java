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
    private static boolean isInitied = false;

    private Context context;
    private SpeechSynthesizer speechSynthesizer;

    private boolean isCheckFile = true;

    public MySynthesizer(Context context, Config config)
    {
        this(context);
        init(config);
    }

    public MySynthesizer(Context context)
    {
        if (isInitied)
        {
            // SpeechSynthesizer.getInstance() 不要连续调用
            throw new RuntimeException("SpeechSynthesizer还未释放, 请勿新建一个新类");
        }
        this.context = context;
        isInitied = true;
    }

    /**
     * 该方法需要在新线程中调用, 且该线程不能结束.
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
                Log.e(context.getPackageName(), "语音合成引擎授权失败: " + errorMsg);
                return false;
            }
        }

        int result = speechSynthesizer.initTts(config.getTtsMode());
        if (result != 0)
        {
            Log.e(context.getPackageName(), "语音合成引擎初始化失败: " + result);
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
        isInitied = false;
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
     * 引擎在合成时该方法不能调用！！！
     * 注意 只有 TtsMode.MIX 才可以切换离线发音
     *
     * @return
     */
    public int loadModel(String modelFilename, String textFilename)
    {
        return speechSynthesizer.loadModel(modelFilename, textFilename);
    }

    /**
     * 设置播放音量，默认已经是最大声音
     * 0.0f为最小音量，1.0f为最大音量
     *
     * @param leftVolume  [0-1] 默认1.0f
     * @param rightVolume [0-1] 默认1.0f
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

        /**
         * 纯在线或者离在线融合, 如果需要离线合成功能, 请在您申请的应用中填写包名。
         */
        private TtsMode ttsMode;

        /**
         * 初始化的其它参数, 用于setParam
         */
        private Map<String, String> params;

        /**
         * 合成引擎的回调
         */
        private SpeechSynthesizerListener listener;

        private Config() {}

        public Config(String appId, String appKey, String secretKey, TtsMode ttsMode,
                      Map<String, String> params, SpeechSynthesizerListener listener) {
            this.appId = appId;
            this.appKey = appKey;
            this.secretKey = secretKey;
            this.ttsMode = ttsMode;
            this.params = params;
            this.listener = listener;
        }

        public String getAppId() {
            return appId;
        }

        public String getAppKey() {
            return appKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public TtsMode getTtsMode() {
            return ttsMode;
        }

        public Map<String, String> getParams() {
            return params;
        }

        public SpeechSynthesizerListener getListener() {
            return listener;
        }
    }
}

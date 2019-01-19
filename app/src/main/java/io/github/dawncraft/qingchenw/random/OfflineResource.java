package io.github.dawncraft.qingchenw.random;

import android.content.Context;
import android.content.res.AssetManager;

import java.util.HashMap;

/**
 * 百度语音合成引擎离线声学资源文件
 *
 * Created by fujiayi on 2017/5/19.
 */
public class OfflineResource
{
    // f7 离线女声
    public static final String VOICE_FEMALE = "0";
    // m15 离线男声
    public static final String VOICE_MALE = "1";
    // yyjw 度逍遥
    public static final String VOICE_DUXY = "3";
    // as 度丫丫
    public static final String VOICE_DUYY = "4";

    private static HashMap<String, Boolean> mapInitied = new HashMap<>();

    private AssetManager assets;
    private String destPath;
    private String textFilename;
    private String modelFilename;

    // TODO 移入MySynthesizer如果有必要的话
    public OfflineResource(Context context, String voiceType)
    {
        assets = context.getApplicationContext().getAssets();
        destPath = Utils.createDir(MainActivity.RES_PATH);
        setOfflineVoiceType(voiceType);
    }

    public String getTextFilename()
    {
        return textFilename;
    }

    public String getModelFilename()
    {
        return modelFilename;
    }

    public String getTextFilepath()
    {
        return destPath + "/" + getTextFilename();
    }

    public String getModelFilepath()
    {
        return destPath + "/" + getModelFilename();
    }

    public void setOfflineVoiceType(String voiceType)
    {
        String text = "bd_etts_text.dat";
        String model;
        if (VOICE_FEMALE.equals(voiceType)) {
            model = "bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat";
        } else if (VOICE_MALE.equals(voiceType)) {
            model = "bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat";
        } else if (VOICE_DUXY.equals(voiceType)) {
            model = "bd_etts_common_speech_yyjw_mand_eng_high_am-mix_v3.0.0_20170512.dat";
        } else if (VOICE_DUYY.equals(voiceType)) {
            model = "bd_etts_common_speech_as_mand_eng_high_am_v3.0.0_20170516.dat";
        } else {
            model = "bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat";
            // throw new RuntimeException("Voice type is not in the list");
        }

        textFilename = copyAssetsFile(text);
        modelFilename = copyAssetsFile(model);
    }

    private String copyAssetsFile(String sourceFilename)
    {
        // 原来有一个半成品缓存,被我移除了
        Utils.copyFromAssets(assets, sourceFilename, destPath + "/" + sourceFilename, false);
        return sourceFilename;
    }
}

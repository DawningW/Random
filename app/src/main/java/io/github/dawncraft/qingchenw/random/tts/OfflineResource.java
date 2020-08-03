package io.github.dawncraft.qingchenw.random.tts;

import android.content.Context;
import android.content.res.AssetManager;

import java.util.HashSet;

import io.github.dawncraft.qingchenw.random.MainActivity;
import io.github.dawncraft.qingchenw.random.utils.FileUtils;

/**
 * 百度语音合成引擎离线声学资源文件
 *
 * Created by fujiayi on 2017/5/19.
 */
public class OfflineResource implements IOfflineResourceConst
{
    private static HashSet<String> mapInitied = new HashSet<>();

    private AssetManager assets;
    private String destPath;
    private String textFileLocation;
    private String modelFileLocation;
    
    public OfflineResource(Context context, String voiceType)
    {
        assets = context.getApplicationContext().getAssets();
        destPath = FileUtils.createDir(MainActivity.RES_PATH);
        setOfflineVoiceType(voiceType);
    }

    public String getTextFileLocation()
    {
        return textFileLocation;
    }

    public String getModelFileLocation()
    {
        return modelFileLocation;
    }
    
    public void setOfflineVoiceType(String voiceType)
    {
        String text = TEXT_MODEL;
        String model;
        if (VOICE_MALE.equals(voiceType)) {
            model = VOICE_MALE_MODEL;
        } else if (VOICE_FEMALE.equals(voiceType)) {
            model = VOICE_FEMALE_MODEL;
        } else if (VOICE_DUXY.equals(voiceType)) {
            model = VOICE_DUXY_MODEL;
        } else if (VOICE_DUYY.equals(voiceType)) {
            model = VOICE_DUYY_MODEL;
        } else {
            model = VOICE_MALE_MODEL;
            System.out.println("The voice type is not in the list. Use default instead.");
            // throw new RuntimeException();
        }
        textFileLocation = copyAssetsFile(text);
        modelFileLocation = copyAssetsFile(model);
    }

    private String copyAssetsFile(String sourceFilename)
    {
        String destFilename = destPath + "/" + sourceFilename;
        boolean recover = false;
        if (!mapInitied.contains(sourceFilename))
        {
            // 启动时完全覆盖一次
            recover = true;
            mapInitied.add(sourceFilename);
        }
        FileUtils.copyFromAssets(assets, sourceFilename, destFilename, recover);
        return destFilename;
    }
}

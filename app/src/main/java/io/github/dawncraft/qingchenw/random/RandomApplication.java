package io.github.dawncraft.qingchenw.random;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.os.Environment;

import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;

import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallbackWithBeforeParam;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.dawncraft.qingchenw.random.tts.MySynthesizer;
import io.github.dawncraft.qingchenw.random.tts.OfflineResource;
import io.github.dawncraft.qingchenw.random.ui.MainActivity;
import io.github.dawncraft.qingchenw.random.utils.RandomEngine;
import io.github.dawncraft.qingchenw.random.utils.SystemUtils;

/**
 * 一个专为教师设计的叫号小应用
 * 感谢changer0的教程https://www.jianshu.com/p/bc5298651b30
 * <p>
 * Created by QingChenW on 2020/9/21
 */
public class RandomApplication extends Application
{
    // 更新地址
    public static final String UPDATE_URL = "https://raw.githubusercontent.com/DawningW/Random/master/update.json";
    // 资源文件路径
    public static final String RES_PATH = Environment.getExternalStorageDirectory() + "/" + "Random";

    private static RandomApplication instance;

    // 元素列表
    public Map<String, ArrayList<String>> elements = new LinkedHashMap<>();

    // 语音合成
    public static boolean voiceEnabled;
    public static MySynthesizer.Config synthesizerConfig = new MySynthesizer.Config();
    public static OfflineResource offlineResource;

    // 更新
    public static boolean updateEnabled;
    public static JSONObject update;
    public static boolean haveUpdate;

    // 应用信息
    static public PackageInfo packageInfo;
    // 配置
    public SharedPreferences sharedPreferences;

    // 百度语音合成引擎
    public MySynthesizer speechSynthesizer;
    // 随机数生成引擎
    public RandomEngine<String> randomEngine;

    @Override
    public void onCreate()
    {
        instance = this;
        super.onCreate();
        // 初始化配置
        packageInfo = SystemUtils.getPackageInfo(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.pref_voice, true);
        loadPreferences();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();
    }

    public static RandomApplication getInstance()
    {
        return instance;
    }
}

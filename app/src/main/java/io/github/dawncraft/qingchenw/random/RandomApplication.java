package io.github.dawncraft.qingchenw.random;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Environment;

import androidx.preference.PreferenceManager;

import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate.entity.UpdateEntity;
import com.xuexiang.xupdate.entity.UpdateError;
import com.xuexiang.xupdate.listener.IUpdateParseCallback;
import com.xuexiang.xupdate.listener.OnUpdateFailureListener;
import com.xuexiang.xupdate.proxy.IUpdateParser;

import org.json.JSONObject;

import io.github.dawncraft.qingchenw.random.utils.OKHttpUpdateHttpService;
import io.github.dawncraft.qingchenw.random.utils.SystemUtils;

import static com.xuexiang.xupdate.entity.UpdateError.ERROR.CHECK_NO_NEW_VERSION;

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

    // 实例
    private static RandomApplication instance;

    // 更新
    public static boolean updateEnabled;
    public static boolean haveUpdate;
    // 应用信息
    public static PackageInfo packageInfo;
    // 配置
    public static SharedPreferences sharedPreferences;

    @Override
    public void onCreate()
    {
        instance = this;
        super.onCreate();
        // 初始化配置
        packageInfo = SystemUtils.getPackageInfo(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.pref_voice, true);
        updateEnabled = sharedPreferences.getBoolean("update", true);
        // 初始化更新检查器
        XUpdate.get()
                .isWifiOnly(true)
                .isGet(true)
                .isAutoMode(false)
                .setOnUpdateFailureListener(new OnUpdateFailureListener()
                {
                    @Override
                    public void onFailure(UpdateError error)
                    {
                        if (error.getCode() != CHECK_NO_NEW_VERSION)
                        {
                            SystemUtils.toast(RandomApplication.this, error.toString());
                        }
                    }
                })
                .setIUpdateHttpService(new OKHttpUpdateHttpService())
                .setIUpdateParser(new IUpdateParser()
                {
                    @Override
                    public boolean isAsyncParser()
                    {
                        return true;
                    }

                    @Override
                    public UpdateEntity parseJson(String json) throws Exception
                    {
                        return null;
                    }

                    @Override
                    public void parseJson(String s, IUpdateParseCallback callback) throws Exception
                    {
                        JSONObject json = new JSONObject(s);
                        if (json.length() > 0 && json.getString("name").equals("Random"))
                        {
                            haveUpdate = json.getInt("versionCode") > packageInfo.versionCode;
                            callback.onParseResult(new UpdateEntity()
                                    .setHasUpdate(haveUpdate)
                                    .setForce(json.getBoolean("forceUpdate"))
                                    .setVersionCode(json.getInt("versionCode"))
                                    .setVersionName(json.getString("versionName"))
                                    .setDownloadUrl(json.getString("updateUrl"))
                                    .setMd5(json.getString("md5"))
                                    .setUpdateContent(json.getString("description")));
                        }
                    }
                })
                .init(this);
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

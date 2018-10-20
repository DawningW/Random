package io.github.dawncraft.qingchenw.random;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utils
{
    public static PackageInfo getPackageInfo(Context context)
    {
        PackageManager packageManager = context.getPackageManager();
        try
        {
            return packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @NonNull
    public static String join(CharSequence delimiter, String... elements)
    {
        if (elements.length > 0)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                return String.join(delimiter, elements);
            }
            else
            {
                StringBuilder stringBuilder = new StringBuilder();
                for(int i = 0; i < elements.length - 1; i++)
                {
                    stringBuilder.append(elements[i]).append(delimiter);
                }
                stringBuilder.append(elements[elements.length - 1]);
                return stringBuilder.toString();
            }
        }
        return "";
    }

    public static String readUrl(String urlString)
    {
        try
        {
            URL url = new URL(urlString);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            return read(new InputStreamReader(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String readFile(String path)
    {
        try
        {
            return read(new FileReader(new File(path)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String read(Reader reader)
    {
        try
        {
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null)
            {
                stringBuilder.append(line);
            }
            bufferedReader.close();
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static File downloadFile(String updateUrl, String path, ProgressDialog progressDialog)
    {
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            try
            {
                URL url = new URL(updateUrl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(8 * 1000);
                // 获取到文件的大小
                progressDialog.setMax(httpURLConnection.getContentLength());
                InputStream is = httpURLConnection.getInputStream();
                File file = new File(Environment.getExternalStorageDirectory() + "/" + path, "update.apk");
                FileOutputStream fos = new FileOutputStream(file);
                BufferedInputStream bis = new BufferedInputStream(is);
                byte[] buffer = new byte[1024];
                int len;
                int total = 0;
                while((len = bis.read(buffer)) != -1)
                {
                    fos.write(buffer, 0, len);
                    total += len;
                    // 获取当前下载量
                    progressDialog.setProgress(total);
                }
                fos.close();
                bis.close();
                is.close();
                return file;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String createDir(String dir)
    {
        String path = Environment.getExternalStorageDirectory().toString() + "/" + dir;
        File file = new File(path);
        if (!file.exists()) file.mkdirs();
        return path;
    }

    public static void writeFile(String path, String content)
    {
        try
        {
            File file = new File(path);
            file.createNewFile();
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, false));
            writer.append(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyFromAssets(AssetManager assets, String source, String path, boolean isCover)
    {
        File file = new File(path);
        if (isCover || (!isCover && !file.exists()))
        {
            InputStream is = null;
            FileOutputStream fos = null;
            try
            {
                is = assets.open(source);
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0)
                {
                    fos.write(buffer, 0, size);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null)
                {
                    try
                    {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try
                        {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static void toast(Context context, @StringRes int resId)
    {
        toast(context, context.getString(resId));
    }

    public static void toast(Context context, @StringRes int resId, int duration)
    {
        toast(context, context.getString(resId), duration);
    }

    public static void toast(Context context, String msg)
    {
        toast(context, msg, Toast.LENGTH_SHORT);
    }

    public static void toast(Context context, String msg, int duration)
    {
        Toast.makeText(context, msg, duration).show();
    }
}

package io.github.dawncraft.qingchenw.random.utils;

import android.app.ProgressDialog;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebUtils
{
    private WebUtils() {}
    
    public static boolean ping(String ip)
    {
        try
        {
            Process process = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);
            int status = process.waitFor();
            return status == 0;
        } catch (InterruptedException | IOException e) {
            return false;
        }
    }
    
    public static String readUrl(String urlString)
    {
        try
        {
            URL url = new URL(urlString);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            return FileUtils.read(new InputStreamReader(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    
    public static File downloadFile(String fileUrl, File file, ProgressDialog progressDialog)
    {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            try
            {
                URL url = new URL(fileUrl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(8 * 1000);
                // 获取到文件的大小
                progressDialog.setMax(httpURLConnection.getContentLength());
                InputStream is = httpURLConnection.getInputStream();
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
}

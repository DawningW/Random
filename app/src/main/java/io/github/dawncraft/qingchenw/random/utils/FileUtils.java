package io.github.dawncraft.qingchenw.random.utils;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.security.MessageDigest;

public class FileUtils
{
    private FileUtils() {}
    
    public static String createDir(String path)
    {
        File file = new File(path);
        if (!file.exists()) file.mkdirs();
        return path;
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
                stringBuilder.append(line).append('\n');
            }
            bufferedReader.close();
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
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
}

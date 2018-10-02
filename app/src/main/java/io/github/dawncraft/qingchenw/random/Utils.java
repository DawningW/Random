package io.github.dawncraft.qingchenw.random;

import android.content.Context;
import android.os.Build;
import android.support.annotation.StringRes;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Utils
{
    public static String join(CharSequence delimiter, String... elements)
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
            stringBuilder.append(elements.length - 1);
            return stringBuilder.toString();
        }
    }

    public static String readFile(String path)
    {
        try
        {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(path)));
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
        return null;
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

    public static void toast(Context context, @StringRes int resId)
    {
        toast(context, context.getString(resId));
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

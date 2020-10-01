package io.github.dawncraft.qingchenw.random.utils;

import android.os.Build;

import androidx.annotation.NonNull;

import com.baidu.tts.tools.StringTool;

public class Utils
{
    private Utils() {}
    
    public static int between(int min, int num, int max)
    {
        return Math.max(Math.min(num, max), min);
    }

    public static boolean isStrNullOrEmpty(String s)
    {
        return s == null || s.isEmpty();
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
}

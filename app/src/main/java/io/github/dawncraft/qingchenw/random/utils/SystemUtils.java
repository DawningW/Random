package io.github.dawncraft.qingchenw.random.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.util.TypedValue;
import android.widget.Toast;

import androidx.annotation.StringRes;

public class SystemUtils
{
    private SystemUtils() {}
    
    public static int dp2px(Context context, int value)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }

    public static PackageInfo getPackageInfo(Context context)
    {
        PackageManager packageManager = context.getPackageManager();
        try
        {
            return packageManager.getPackageInfo(context.getPackageName(), 0);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String getPath(Context context, Uri uri)
    {
        if ("content".equalsIgnoreCase(uri.getScheme()))
        {
            String[] projection = { "_data" };
            try
            {
                Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null)
                {
                    int column_index = cursor.getColumnIndexOrThrow("_data");
                    if (cursor.moveToFirst())
                    {
                        return cursor.getString(column_index);
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme()))
        {
            return uri.getPath();
        }
        return null;
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

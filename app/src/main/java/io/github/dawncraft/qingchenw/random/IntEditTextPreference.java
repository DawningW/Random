package io.github.dawncraft.qingchenw.random;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

public class IntEditTextPreference extends EditTextPreference
{
    private static final String androidns = "http://schemas.android.com/apk/res/android";

    private int mDefaultValue;

    public IntEditTextPreference(Context context)
    {
        super(context);
        mDefaultValue = -1;
    }

    public IntEditTextPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mDefaultValue = attrs.getAttributeIntValue(androidns, "defaultValue", -1);
    }

    public IntEditTextPreference(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mDefaultValue = attrs.getAttributeIntValue(androidns, "defaultValue", -1);
    }

    @Override
    protected String getPersistedString(String defaultReturnValue)
    {
        return String.valueOf(getPersistedInt(mDefaultValue));
    }

    @Override
    protected boolean persistString(String value)
    {
        return persistInt(Integer.valueOf(value));
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index)
    {
        return ta.getInt(index, mDefaultValue);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        if (restoreValue)
        {
            setText(String.valueOf(getPersistedInt(mDefaultValue)));
        }
        else
        {
            int value = (Integer) defaultValue;
            setText(String.valueOf(value));
            persistInt(value);
        }
    }
}

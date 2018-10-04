package io.github.dawncraft.qingchenw.random;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener
{
    private static final String androidns = "http://schemas.android.com/apk/res/android";
    private static final String appns = "http://schemas.android.com/apk/res-auto";

    private int mMaxValue;
    private int mMinValue;
    private int mDefaultValue;
    private int mCurrentValue;

    private SeekBar mSeekBar;
    private TextView mCurrentValueText;

    public SeekBarPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mMaxValue = attrs.getAttributeIntValue(appns, "maxValue", 100);
        mMinValue = attrs.getAttributeIntValue(appns, "minValue", 0);
        mCurrentValue = mDefaultValue = attrs.getAttributeIntValue(androidns, "defaultValue", 0);
        setWidgetLayoutResource(R.layout.preference_seekbar);
    }

    @Override
    public void onBindView(View view)
    {
        super.onBindView(view);

        mSeekBar = view.findViewById(R.id.seekBar);
        mSeekBar.setMax(mMaxValue - mMinValue);
        mSeekBar.setProgress(mDefaultValue - mMinValue);
        mSeekBar.setOnSeekBarChangeListener(this);

        mCurrentValueText = view.findViewById(R.id.currentValue);
        mCurrentValueText.setText(String.valueOf(mDefaultValue));

        TextView leftText = view.findViewById(R.id.maxValue);
        leftText.setText(String.valueOf(mMaxValue));

        TextView rightText = view.findViewById(R.id.minValue);
        rightText.setText(String.valueOf(mMinValue));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        int newValue = progress + mMinValue;
        if(newValue > mMaxValue) newValue = mMaxValue;
        else if(newValue < mMinValue) newValue = mMinValue;
        callChangeListener(newValue);
        mCurrentValue = newValue;
        seekBar.setProgress(mCurrentValue - mMinValue);
        mCurrentValueText.setText(String.valueOf(newValue));
        persistInt(newValue);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {
        notifyChanged();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index)
    {
        return ta.getInt(index, mDefaultValue);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {
        if(restoreValue)
        {
            mCurrentValue = getPersistedInt(mDefaultValue);
        }
        else
        {
            mCurrentValue = (Integer) defaultValue;
            persistInt(mCurrentValue);
        }
    }

    public int getMaxValue()
    {
        return mMaxValue;
    }

    public void setMaxValue(int value)
    {
        this.mMaxValue = value;
    }

    public int getMinValue()
    {
        return mMinValue;
    }

    public void setMinValue(int value)
    {
        this.mMinValue = value;
    }

    public int getDefaultValue()
    {
        return mDefaultValue;
    }

    public void setDefaultValue(int value)
    {
        this.mDefaultValue = value;
    }

    public int getCurrentValue()
    {
        return mCurrentValue;
    }

    public void setCurrentValue(int value)
    {
        this.mCurrentValue = value;
        mSeekBar.setProgress(mCurrentValue - mMinValue);
    }
}

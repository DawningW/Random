package io.github.dawncraft.qingchenw.random.ui.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.cardview.widget.CardView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.github.dawncraft.qingchenw.random.R;

public class FloatingMenuItem extends ViewGroup
{
    private String mText;
    private ColorStateList mTintList;
    private Drawable mImage;

    private CardView mCardView;
    private TextView mTagText;
    private FloatingActionButton mFloatingButton;

    public FloatingMenuItem(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        getAttributes(context, attrs);
        initViews(context);
    }

    private void getAttributes(Context context, AttributeSet attrs)
    {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FloatingMenuItem);
        mText = typedArray.getString(R.styleable.FloatingMenuItem_android_text);
        mTintList = typedArray.getColorStateList(R.styleable.FloatingMenuItem_backgroundTint);
        mImage = typedArray.getDrawable(R.styleable.FloatingMenuItem_srcCompat);
        typedArray.recycle();
    }

    private void initViews(Context context)
    {
        mCardView = new CardView(context);
        mTagText = new TextView(context);
        mTagText.setText(mText);
        mTagText.setSingleLine(true);
        mTagText.setPaddingRelative(dp2px(8), dp2px(8), dp2px(8), dp2px(8));
        mCardView.addView(mTagText);
        addView(mCardView);
        mFloatingButton = new FloatingActionButton(context);
        mFloatingButton.setFocusable(true);
        mFloatingButton.setSize(FloatingActionButton.SIZE_MINI);
        mFloatingButton.setSupportBackgroundTintList(mTintList);
        mFloatingButton.setImageDrawable(mImage);
        mFloatingButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                FloatingMenuItem.this.performClick();
            }
        });
        addView(mFloatingButton);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = 0;
        int height = 0;
        int count = getChildCount();
        for (int i = 0; i < count; i++)
        {
            View view = getChildAt(i);
            measureChild(view, widthMeasureSpec, heightMeasureSpec);
            width += view.getMeasuredWidth();
            height = Math.max(height, view.getMeasuredHeight());
        }
        width += dp2px(8 + 16 + 8);
        height += dp2px(4 + 4);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        int tagWidth = mCardView.getMeasuredWidth();
        int tagHeight = mCardView.getMeasuredHeight();
        int tl = dp2px(4);
        int tt = (getMeasuredHeight() - tagHeight) / 2;
        int tr = tl + tagWidth;
        int tb = tt + tagHeight;
        mCardView.layout(tl, tt, tr, tb);

        int fabWidth = mFloatingButton.getMeasuredWidth();
        int fabHeight = mFloatingButton.getMeasuredHeight();
        int fl = tr + dp2px(16);
        int ft = (getMeasuredHeight() - fabHeight) / 2;
        int fr = fl + fabWidth;
        int fb = ft + fabHeight;
        mFloatingButton.layout(fl, ft, fr, fb);
    }

    private int dp2px(int value)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}

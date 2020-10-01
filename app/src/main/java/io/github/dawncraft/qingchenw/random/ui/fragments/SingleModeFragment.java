package io.github.dawncraft.qingchenw.random.ui.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.baidu.tts.client.SpeechError;

import butterknife.BindAnim;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.dawncraft.qingchenw.random.R;
import io.github.dawncraft.qingchenw.random.RandomApplication;
import io.github.dawncraft.qingchenw.random.ui.ListActivity;
import io.github.dawncraft.qingchenw.random.ui.MainActivity;
import io.github.dawncraft.qingchenw.random.ui.VoiceActivity;
import io.github.dawncraft.qingchenw.random.utils.RandomEngine;
import io.github.dawncraft.qingchenw.random.utils.SystemUtils;

public class SingleModeFragment extends BaseModeFragment
{
    // 文本
    public static String showText;

    // 顶部布局
    @BindView(R.id.topLayout)
    public ConstraintLayout topLayout;
    @BindView(R.id.listText)
    public TextView listText;
    @BindView(R.id.voiceText)
    public TextView voiceText;
    @BindView(R.id.topDivider)
    public View topDivider;
    // 中部按钮
    @BindView(R.id.outputText)
    public TextView outputText;
    @BindView(R.id.imageButton)
    public ImageButton imageButton;
    // 底部布局
    @BindView(R.id.bottomLayout)
    public ConstraintLayout bottomLayout;
    @BindView(R.id.stateImage)
    public ImageView stateImage;
    @BindView(R.id.bottomDivider)
    public View bottomDivider;

    // 动画
    @BindAnim(R.anim.anim_go_up)
    public Animation goUpAnim;
    @BindAnim(R.anim.anim_go_down)
    public Animation goDownAnim;
    @BindAnim(R.anim.anim_back_up)
    public Animation backUpAnim;
    @BindAnim(R.anim.anim_back_down)
    public Animation backDownAnim;

    // 图片
    @BindDrawable(R.drawable.ic_circle_grey_24dp)
    public Drawable greyImage;
    @BindDrawable(R.drawable.ic_circle_green_24dp)
    public Drawable greenImage;
    @BindDrawable(R.drawable.ic_circle_red_24dp)
    public Drawable redImage;

    // 音效
    public int openAudio;
    public int closeAudio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        // 初始化布局
        View root = inflater.inflate(R.layout.fragment_single, container);
        // 初始化ButterKnife
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        // 读取配置
        showText = RandomApplication.sharedPreferences.getString("show_text", getString(R.string.voice_text_default));
        // 初始化控件
        listText.setText(String.format(getString(R.string.list_number), String.valueOf(getMainActivity().elements.size())));
        voiceText.setText(String.format(getString(R.string.voice_text), showText));
        // 加载音效
        if (getMainActivity().soundPool != null)
        {
            openAudio = getMainActivity().soundPool.load(getMainActivity(), R.raw.open,1);
            closeAudio = getMainActivity().soundPool.load(getMainActivity(), R.raw.close, 1);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        // 停止动画
        if (isShaking()) stop();
    }

    public void onClicked(View view)
    {
        switch(view.getId())
        {
            case R.id.imageButton:
            {
                if (!isShaking())
                    start();
                break;
            }
            case R.id.listButton:
            {
                startActivity(new Intent(getMainActivity(), ListActivity.class));
                break;
            }
            case R.id.voiceButton:
            {
                startActivity(new Intent(getMainActivity(), VoiceActivity.class));
                break;
            }
        }
    }

    @Override
    public void onSynthesizerError(String s, SpeechError speechError)
    {
        stateImage.setImageDrawable(redImage);
        super.onSynthesizerError(s, speechError);
    }

    @Override
    public void start()
    {
        if (!MainActivity.voiceEnabled || getMainActivity().speechSynthesizer.isInited())
        {
            if (getMainActivity().randomEngine.hasElement())
            {
                isShaking = true;
                // 生成文本
                Pair<String, String> element;
                try
                {
                    element = getMainActivity().randomEngine.generate();
                }
                catch (RandomEngine.InvalidCodeException e)
                {
                    e.printStackTrace();
                    SystemUtils.toast(getMainActivity(), R.string.code_invalid1);
                    element = getMainActivity().elements.get(e.getResult());
                }
                String text = String.format(showText, element);
                // 设置中心文本
                outputText.setText(text);
                // 隐藏中心按钮
                imageButton.setVisibility(View.GONE);
                // 显示这两条线
                topDivider.setVisibility(View.VISIBLE);
                bottomDivider.setVisibility(View.VISIBLE);
                // 播放动画
                topLayout.startAnimation(goUpAnim);
                bottomLayout.startAnimation(goDownAnim);
                // 发出振动
                if (MainActivity.vibratorEnabled)
                    getMainActivity().vibrator.vibrate(getMainActivity().vibrateTime);
                // 播放提示音
                if (MainActivity.soundEnabled)
                    getMainActivity().soundPool.play(openAudio, 1, 1, 0, 0, 1);
                // 播放语音
                if (MainActivity.voiceEnabled)
                    getMainActivity().speechSynthesizer.speak(text, "");
            }
            else
            {
                SystemUtils.toast(getMainActivity(), R.string.list_blank);
            }
        }
        else
        {
            SystemUtils.toast(getMainActivity(), R.string.synthesize_initializing);
        }
    }

    @Override
    public void stop()
    {
        isShaking = false;
        // 停止语音
        if (MainActivity.voiceEnabled)
            getMainActivity().speechSynthesizer.stop();
        // 播放动画
        topLayout.startAnimation(backUpAnim);
        bottomLayout.startAnimation(backDownAnim);
        // 播放提示音
        if (MainActivity.soundEnabled)
            getMainActivity().soundPool.play(closeAudio, 1, 1, 0, 0, 1);
        // 延时执行
        imageButton.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                // 显示中心按钮
                imageButton.setVisibility(View.VISIBLE);
                // 隐藏这两条线
                topDivider.setVisibility(View.GONE);
                bottomDivider.setVisibility(View.GONE);
            }
        }, 300);
    }
}

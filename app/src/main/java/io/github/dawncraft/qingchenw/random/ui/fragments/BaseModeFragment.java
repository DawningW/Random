package io.github.dawncraft.qingchenw.random.ui.fragments;

import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.baidu.tts.client.SpeechError;

import io.github.dawncraft.qingchenw.random.R;
import io.github.dawncraft.qingchenw.random.ui.MainActivity;
import io.github.dawncraft.qingchenw.random.utils.SystemUtils;

public abstract class BaseModeFragment extends Fragment
{
    // 宿主Activity
    private MainActivity mainActivity;
    // 正在摇晃判断
    protected boolean isShaking = false;

    public boolean isShaking()
    {
        return isShaking;
    }

    public abstract void start();

    public abstract void stop();

    public void onSynthesizerInit() {}

    public void onSpeechFinish(String s)
    {
        if (isShaking) stop();
    }

    public void onSynthesizerError(String s, final SpeechError speechError)
    {
        SystemUtils.toast(getMainActivity(),
                String.format(getString(R.string.synthesize_error), speechError.toString()),
                Toast.LENGTH_LONG);
    }

    public MainActivity getMainActivity()
    {
        if (mainActivity == null) mainActivity = (MainActivity) getActivity();
        return mainActivity;
    }
}

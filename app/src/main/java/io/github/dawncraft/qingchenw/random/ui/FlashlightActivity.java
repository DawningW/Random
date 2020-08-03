package io.github.dawncraft.qingchenw.random.ui;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import butterknife.ButterKnife;
import io.github.dawncraft.qingchenw.random.R;

public class FlashlightActivity extends AppCompatActivity
{
    // 是否开启手电筒
	private boolean islighting = false;
	// 相机
	public Camera camera;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flashlight);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 初始化ButterKnife
        ButterKnife.bind(this);
	}

    @Override
    protected void onStart()
    {
        super.onStart();
        // 初始化相机
        camera = Camera.open();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        // 释放相机
        camera.release();
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu)
    {
		getMenuInflater().inflate(R.menu.flashlight, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
    {
		int id = item.getItemId();
		if (id == R.id.action_switch)
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    public void onClicked(View v)
    {
        Parameters parameter = camera.getParameters();
        parameter.setFlashMode(islighting ? Parameters.FLASH_MODE_OFF : Parameters.FLASH_MODE_TORCH);
        camera.setParameters(parameter);
        islighting = !islighting;
    }
}

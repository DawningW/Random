package io.github.dawncraft.qingchenw.random;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class FlashlightActivity extends Activity
{
	boolean islight = false;
	Camera camera;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flashlight);

        camera = Camera.open();
		
		Button button = findViewById(R.id.toggleButton1);
		button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (!islight)
                {
                    Parameters parameter = camera.getParameters();
                    parameter.setFlashMode(Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(parameter);

                    islight = true;
                }
                else
                {
                    Parameters parameter = camera.getParameters();
                    parameter.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(parameter);

                    islight = false;
                }
		    }
        });
	}

    @Override
    protected void onPause()
    {
        super.onPause();
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
}

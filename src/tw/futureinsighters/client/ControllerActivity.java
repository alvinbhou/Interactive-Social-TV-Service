package tw.futureinsighters.client;

import java.util.Timer;
import java.util.TimerTask;

import org.allseenaliance.alljoyn.CafeApplication;

import com.technalt.serverlessCafe.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.style.RasterizerSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class ControllerActivity extends Activity {

	private final String CONTROLLER_CMD_UI_LEFT = "ISTVSgoleft";
	private final String CONTROLLER_CMD_UI_RIGHT = "ISTVSgoright";
	private final String CONTROLLER_CMD_UI_OK = "ISTVSok";

	private enum Direction {
		LEFT, RIGHT
	};

	private float a_x;
	private float a_y;
	private float a_z;
	private float g_x;
	private float g_y;
	private float g_z;

	private boolean is_up = false;
	private boolean is_up_long = false;
	private boolean is_down = false;
	private boolean is_down_long = false;
	private boolean is_controllable = true;

	private CafeApplication mChatApplication = null;

	private SensorManager sensorManager;
	private Sensor aSensor;
	private Sensor gSensor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_controller);

		/* OK Click */
		TextView ok_mid = (TextView) findViewById(R.id.oK_mid);
		ok_mid.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openTVController();
			}
		});

		mChatApplication = (CafeApplication) getApplication();

		// initialize sensor
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		aSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

		sensorManager.registerListener(aSensorListener, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(gSensorListener, gSensor, SensorManager.SENSOR_DELAY_NORMAL);

	}

	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(aSensorListener);
		sensorManager.unregisterListener(gSensorListener);
	}

	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(aSensorListener, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(gSensorListener, gSensor, SensorManager.SENSOR_DELAY_NORMAL);

	}

	private SensorEventListener aSensorListener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent event) {
			a_x = event.values[0];
			a_y = event.values[1];
			a_z = event.values[2];

			

			// motion handler

			if (a_x > 6 && a_y < 3 && a_y > -3 && a_z > 0) {
				// $('left_bg').style.opacity = (x - 4) / 8.0;
				moveAppList(Direction.LEFT);

			} else {
				// $('left_bg').style.opacity = 0;
			}
			if (a_x < -6 && a_y < 3 && a_y > -3 && a_z > 0) {
				// $('right_bg').style.opacity = (-x -4) / 8.0;
				moveAppList(Direction.RIGHT);

			} else {
				// $('right_bg').style.opacity = 0;
			}
			if (a_x < 3 && a_x > -3 && a_y > 7 && a_z > 0) {
				// $('down_bg').style.opacity = (y - 4) / 8.0;
				if (!is_up) {
					is_up = true;

					new android.os.Handler().postDelayed(new Runnable() {
						public void run() {
							if (is_up) { // 長時間
								is_up_long = true;
							} else { // 發現是短時間
								;
							}
						}
					}, 1000);
				}
				if (is_up_long) {
					// switchVolumn(false);
				}
			} else {

				// $('down_bg').style.opacity = 0;
				if (is_up) {
					is_up = false;
				}
				if (is_up_long) {
					is_up_long = false;
				}
			}
			if (a_x < 3 && a_x > -3 && a_y < -5 && a_z > 0) {
				// $('up_bg').style.opacity = (-y - 3) / 8.0;
				if (!is_down) {
					is_down = true;
					new android.os.Handler().postDelayed(new Runnable() {
						public void run() {
							if (is_down) { // 長時間
								is_down_long = true;
							} else { // 發現是短時間
										// ok_movement();
								runApp();
							}
						}
					}, 1000);
				}
				if (is_down_long) {
					// swichVolumn(....)
				}
			} else {

				// $('up_bg').style.opacity = 0;
				if (is_down) {
					is_down = false;
				}
				if (is_down_long) {
					is_down_long = false;
				}
			}

		}

		public void onAccuracyChanged(Sensor sensor) {

		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};
	private SensorEventListener gSensorListener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent event) {
			g_x = event.values[0];
			g_y = event.values[1];
			g_z = event.values[2];

		}

		public void onAccuracyChanged(Sensor sensor) {

		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};

	private void runApp() {
		if (is_controllable) {
			is_controllable = false;
			new android.os.Handler().postDelayed(new Runnable() {
				public void run() {
					is_controllable = true;
				}
			}, 500);
		} else {
			return;
		}
		mChatApplication.newLocalUserMessage(CONTROLLER_CMD_UI_OK);
	}

	private void moveAppList(Direction direction) {
		if (is_controllable) {
			is_controllable = false;
			new android.os.Handler().postDelayed(new Runnable() {
				public void run() {
					is_controllable = true;
				}
			}, 500);
		} else {
			return;
		}
		switch (direction) {
		case LEFT: {
			left_movement();
			mChatApplication.newLocalUserMessage(CONTROLLER_CMD_UI_LEFT);
			break;
		}
		case RIGHT: {
			right_movement();
			mChatApplication.newLocalUserMessage(CONTROLLER_CMD_UI_RIGHT);
			break;
		}
		}

	}

	private void openTVController() {
		Intent intent = new Intent(ControllerActivity.this, TvControllerActivity.class);
		startActivity(intent);
	}

	private void left_movement() {
		TextView left_arrow = (TextView) findViewById(R.id.left_arrow);
		left_arrow.setTextColor(Color.parseColor("#F44336"));
	}

	private void right_movement() {
		TextView right_arrow = (TextView) findViewById(R.id.right_arrow);
		right_arrow.setTextColor(Color.parseColor("#F44336"));
	}

	private void ok_movement() {
		TextView mid_ok = (TextView) findViewById(R.id.oK_mid);
		mid_ok.setTextColor(Color.parseColor("#F44336"));
	}

	private void reset_color() {
		TextView left_arrow = (TextView) findViewById(R.id.left_arrow);
		TextView right_arrow = (TextView) findViewById(R.id.right_arrow);
		TextView mid_ok = (TextView) findViewById(R.id.oK_mid);
		left_arrow.setTextColor(Color.parseColor("#FFF"));
		right_arrow.setTextColor(Color.parseColor("#FFF"));
		mid_ok.setTextColor(Color.parseColor("#FFF"));
	}

}
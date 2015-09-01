package tw.futureinsighters.client;

import org.allseenaliance.alljoyn.CafeApplication;

import com.technalt.serverlessCafe.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import tw.futureinsighters.defines.TVCONTROLLER_CMD;

public class ControllerActivity extends Activity {

	private enum Direction {
		LEFT, RIGHT
	};

	private float a_x, a_y, a_z, g_x, g_y, g_z;

	private boolean is_up = false;
	private boolean is_up_long = false;
	private boolean is_down = false;
	private boolean is_down_long = false;
	private boolean is_controllable = true;

	private CafeApplication mChatApplication = null;

	private SensorManager sensorManager;
	private Sensor aSensor;
	private Sensor gSensor;
	private float mAccel; // acceleration apart from gravity
	private float mAccelCurrent; // current acceleration including gravity
	private float mAccelLast; // last acceleration including gravity
	private AlertDialog.Builder tutorialDialog;
	private static boolean[] tutorial = new boolean[] {true,true,true};

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

		mAccel = 0.00f;
		mAccelCurrent = SensorManager.GRAVITY_EARTH;
		mAccelLast = SensorManager.GRAVITY_EARTH;

		if (tutorial[0]) {
			LayoutInflater inflater = getLayoutInflater();
			View view = inflater.inflate(R.layout.dialog_customize, null);
			TextView header, tutorialContent, footer;

			header = (TextView) view.findViewById(R.id.programName_dialog);
			tutorialContent = (TextView) view.findViewById(R.id.programDescription_dialog);
			footer = (TextView) view.findViewById(R.id.isAds_dialog);
			header.setText("TUTORIAL");
			header.setBackgroundColor(0xFF4CAF50);
			tutorialContent.setText(
					"Welcome to Gesture controll mode!    Let's try and tilt your phone left and right and see what happens!");
			footer.setText("Controll can be such easy and fashion!");

			tutorialDialog = new AlertDialog.Builder(ControllerActivity.this).setPositiveButton("DONE",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

						}

					});
			tutorialDialog.setCustomTitle(view);
			AlertDialog alert = tutorialDialog.create();
			alert.show();
			tutorial[0] = false;
		}

	}

	@Override
	public void onBackPressed() {
		mChatApplication.newLocalUserMessage(TVCONTROLLER_CMD.HOME);
		super.onBackPressed();
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

			mAccelLast = mAccelCurrent;
			mAccelCurrent = (float) Math.sqrt((double) (a_x * a_x + a_y * a_y + a_z * a_z));
			float delta = mAccelCurrent - mAccelLast;
			mAccel = mAccel * 0.9f + delta;

			if (mAccel > 12) {
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
				mChatApplication.newLocalUserMessage(TVCONTROLLER_CMD.CHANGE_QUOTE);
			}

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
		mChatApplication.newLocalUserMessage(TVCONTROLLER_CMD.UI_OK);
		ok_movement();
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
			if (tutorial[1]) {
				tutorial[1] = false;
				Toast.makeText(ControllerActivity.this, "Moves left!", Toast.LENGTH_SHORT).show();
			}
			left_movement();
			mChatApplication.newLocalUserMessage(TVCONTROLLER_CMD.UI_LEFT);
			break;
		}
		case RIGHT: {
			if (tutorial[2]) {
				tutorial[2] = false;
				Toast.makeText(ControllerActivity.this, "Moves right!", Toast.LENGTH_SHORT).show();
			}
			right_movement();
			mChatApplication.newLocalUserMessage(TVCONTROLLER_CMD.UI_RIGHT);
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

		new android.os.Handler().postDelayed(new Runnable() {
			public void run() {
				TextView left_arrow = (TextView) findViewById(R.id.left_arrow);
				left_arrow.setTextColor(Color.parseColor("#FFFFFF"));
			}

		}, 500);

	}

	private void right_movement() {
		TextView right_arrow = (TextView) findViewById(R.id.right_arrow);
		right_arrow.setTextColor(Color.parseColor("#F44336"));
		new android.os.Handler().postDelayed(new Runnable() {
			public void run() {
				TextView right_arrow = (TextView) findViewById(R.id.right_arrow);
				right_arrow.setTextColor(Color.parseColor("#FFFFFF"));
			}

		}, 500);

	}

	private void ok_movement() {
		TextView mid_ok = (TextView) findViewById(R.id.oK_mid);
		mid_ok.setTextColor(Color.parseColor("#F44336"));
		new android.os.Handler().postDelayed(new Runnable() {
			public void run() {
				TextView mid_ok = (TextView) findViewById(R.id.oK_mid);
				mid_ok.setTextColor(Color.parseColor("#FFFFFF"));
			}

		}, 500);
	}



}
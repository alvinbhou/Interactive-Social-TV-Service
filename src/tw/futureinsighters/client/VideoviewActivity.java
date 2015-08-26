package tw.futureinsighters.client;

import org.allseenaliance.alljoyn.CafeApplication;

import com.technalt.serverlessCafe.R;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VerticalSeekBar;

public class VideoviewActivity extends Activity {

	/* Alljoyn */
	private CafeApplication mChatApplication = null;
	private final String CONTROLLER_CMD_VL = "ISTVSvl -";

	/* Buttons */
	private Button playBtn, previousBtn, nextBtn, shuffleBtn, replayBtn, gestureBtn;
	private Boolean pause_clicked = false, shuffle_clicked = true;

	/* Sensor */
	private boolean is_appslist_on = false;
	private float a_x, a_y, a_z, g_x, g_y, g_z;
	private boolean is_up = false, is_up_long = false;
	private boolean is_down = false, is_down_long = false;
	private boolean is_controllable = true;
	private boolean sensor_on = false;
	private SensorManager sensorManager;
	private Sensor aSensor;
	private Sensor gSensor;

	/* Volume */
	private int volume = 50;
	private VerticalSeekBar volume_bar;
	private TextView volume_value = null;
	private boolean vl_gesture_controll = false;

	private enum Direction {
		LEFT, RIGHT
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_videoview);

		/* ------Start AllJoyn Service KEYWORD!!---- */
		mChatApplication = (CafeApplication) getApplication();

		playBtn = (Button) findViewById(R.id.playBtn);
		previousBtn = (Button) findViewById(R.id.previousBtn);
		nextBtn = (Button) findViewById(R.id.nextBtn);
		replayBtn = (Button) findViewById(R.id.replayBtn);
		shuffleBtn = (Button) findViewById(R.id.shuffleBtn);
		gestureBtn = (Button) findViewById(R.id.mediaGesture);
		playBtn.setBackgroundResource(R.drawable.ic_play_arrow_black_48dp);
		previousBtn.setBackgroundResource(R.drawable.ic_skip_previous_black_48dp);
		nextBtn.setBackgroundResource(R.drawable.ic_skip_next_black_48dp);
		replayBtn.setBackgroundResource(R.drawable.ic_replay_black_48dp);
		shuffleBtn.setBackgroundResource(R.drawable.ic_shuffle_white_48dp);

		playBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!pause_clicked) {
					playBtn.setBackgroundResource(R.drawable.ic_pause_black_48dp);
					pause_clicked = true;
				} else {
					playBtn.setBackgroundResource(R.drawable.ic_play_arrow_black_48dp);
					pause_clicked = false;
				}

			}
		});

		shuffleBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!shuffle_clicked) {
					shuffleBtn.setBackgroundResource(R.drawable.ic_shuffle_black_48dp);
					shuffle_clicked = true;
				} else {
					shuffleBtn.setBackgroundResource(R.drawable.ic_shuffle_white_48dp);
					shuffle_clicked = false;
				}

			}
		});

		gestureBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!sensor_on) {
					sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
					aSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
					gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

					sensorManager.registerListener(aSensorListener, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
					sensorManager.registerListener(gSensorListener, gSensor, SensorManager.SENSOR_DELAY_NORMAL);
					Toast.makeText(VideoviewActivity.this, "Sensor ON", Toast.LENGTH_SHORT).show();
				}
				sensor_on = true;

			}
		});

	}

	/* Sensor Event */
	private SensorEventListener aSensorListener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent event) {
			a_x = event.values[0];
			a_y = event.values[1];
			a_z = event.values[2];

			// motion handler

			if (a_x > 6 && a_y < 3 && a_y > -3 && a_z > 0) {
				// $('left_bg').style.opacity = (x - 4) / 8.0;
				moveMotion(Direction.LEFT);
			} else {
				// $('left_bg').style.opacity = 0;
			}
			if (a_x < -6 && a_y < 3 && a_y > -3 && a_z > 0) {
				// $('right_bg').style.opacity = (-x -4) / 8.0;
				moveMotion(Direction.RIGHT);
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
					switchVolume(false);
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
								// runApp();
							}
						}
					}, 1000);
				}
				if (is_down_long) {
					switchVolume(true);
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
		// mChatApplication.newLocalUserMessage(CONTROLLER_CMD_UI_OK);
	}

	private void moveMotion(Direction direction) {
		if (is_appslist_on) {
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
				// mChatApplication.newLocalUserMessage(CONTROLLER_CMD_UI_LEFT);
				break;
			}
			case RIGHT: {
				// mChatApplication.newLocalUserMessage(CONTROLLER_CMD_UI_RIGHT);
				break;
			}
			}
		} else {
			if (is_controllable) {
				is_controllable = false;
				new android.os.Handler().postDelayed(new Runnable() {
					public void run() {
						is_controllable = true;
					}
				}, 200); // Channel switches faster
			} else {
				return;
			}
			switch (direction) {
			case LEFT: {
				// NEED
				break;
			}
			case RIGHT: {
				// NEED
				break;
			}
			}
		}

	}

	private void switchVolume(boolean toggle) {

		if (is_controllable) {
			is_controllable = false;
			new android.os.Handler().postDelayed(new Runnable() {
				public void run() {
					is_controllable = true;
				}
			}, 20);
		} else {
			return;
		}

		if (toggle)
			volume++;
		else {
			volume--;
		}
		vl_gesture_controll = true;
		if (volume < 0)
			volume = 0;
		if (volume > 100)
			volume = 100;
		volumeCMD(volume);
	}

	private void volumeCMD(int volume) {
		if (!vl_gesture_controll) {
			if (is_controllable) {
				is_controllable = false;
				new android.os.Handler().postDelayed(new Runnable() {
					public void run() {
						is_controllable = true;
					}
				}, 10);
			} else {
				return;
			}
		}	
		String s, cmd;
		s = String.valueOf(volume);
		cmd = CONTROLLER_CMD_VL;
		cmd = cmd.concat(s);
		
		mChatApplication.newLocalUserMessage(cmd);
	}

}

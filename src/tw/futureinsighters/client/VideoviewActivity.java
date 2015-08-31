package tw.futureinsighters.client;

import java.io.File;

import org.allseenaliance.alljoyn.CafeApplication;

import com.technalt.serverlessCafe.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class VideoviewActivity extends Activity {

	/* Alljoyn */
	private CafeApplication mChatApplication = null;
	private final String CONTROLLER_VIDEOVIEWER_PLAY = "ISTVSVIDplay";
	private final String CONTROLLER_VIDEOVIEWER_PAUSE = "ISTVSVIDpause";
	private final String CONTROLLER_VIDEOVIEWER_RESTART = "ISTVSVIDrestart";
	private final String CONTROLLER_VIDEOVIEWER_NEXT = "ISTVSVIDnext";
	private final String CONTROLLER_VIDEOVIEWER_PRE = "ISTVSVIDpre";
	private final String CONTROLLER_VIDEOVIEWER_SHOW_MEDIACONTROLLER = "ISTVSVIDsm";
	private final String CONTROLLER_VIDEOVIEWER_REPEAT_ON = "ISTVSVIDrepon";
	private final String CONTROLLER_VIDEOVIEWER_REPEAT_OFF = "ISTVSVIDrepoff";
	private final String CONTROLLER_VIDEOVIEWER_SHUFFLE_ON = "ISTVSVIDshuon";
	private final String CONTROLLER_VIDEOVIEWER_SHUFFLE_OFF = "ISTVSVIDshuoff";
	private final String CONTROLLER_VIDEOVIEWER_VOLUME = "ISTVSVIDvl";
	private final String CONTROLLER_VIDEOVIEWER_SCREENSHOT = "ISTVSVIDss";
	private final String CONTROLLER_VIDEOVIEWER_CLOSE = "ISTVSVIDbye";

	/* Buttons */
	private Button playBtn, previousBtn, nextBtn, shuffleBtn, replayBtn, snapBtn, repeatBtn, shareBtn;
	private ToggleButton gestureToggle;
	private Boolean pause_clicked = false, shuffle_clicked = false, repeat_clicked = false;

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
	private SeekBar volume_bar;
	private TextView volume_value = null;
	private boolean vl_gesture_controll = false;

	private enum Direction {
		LEFT, RIGHT
	};

	/* SnapShot */
	private ImageView screenshotPreview;
	private String currentPath;
	private File screenshotImg;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_videoview);
		
		initUIComponent();

		/* recieve screenshot info from MainActivity */
		BroadcastReceiver channelInfoBroadcastReciever = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				currentPath = intent.getStringExtra("screenshotPath");
				updateImagePreview();
			}
		};
		IntentFilter channelInfoFilter = new IntentFilter("screenshotPath");
		registerReceiver(channelInfoBroadcastReciever, channelInfoFilter);

		/* ------Start AllJoyn Service KEYWORD!!---- */
		mChatApplication = (CafeApplication) getApplication();

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		aSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

		/* Buttons */

		playBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!pause_clicked) {
					playBtn.setBackgroundResource(R.drawable.ic_play_arrow_black_48dp);
					pause_clicked = true;
					mChatApplication.newLocalUserMessage(CONTROLLER_VIDEOVIEWER_PAUSE);
				} else {
					playBtn.setBackgroundResource(R.drawable.ic_pause_black_48dp);
					pause_clicked = false;
					mChatApplication.newLocalUserMessage(CONTROLLER_VIDEOVIEWER_PLAY);
				}

			}
		});

		previousBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mChatApplication.newLocalUserMessage(CONTROLLER_VIDEOVIEWER_PRE);

			}
		});

		nextBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mChatApplication.newLocalUserMessage(CONTROLLER_VIDEOVIEWER_NEXT);

			}
		});

		replayBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mChatApplication.newLocalUserMessage(CONTROLLER_VIDEOVIEWER_RESTART);

			}
		});

		shuffleBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!shuffle_clicked) {
					shuffleBtn.setBackgroundResource(R.drawable.ic_shuffle_black_48dp);
					shuffle_clicked = true;
					mChatApplication.newLocalUserMessage(CONTROLLER_VIDEOVIEWER_SHUFFLE_ON);
				} else {
					shuffleBtn.setBackgroundResource(R.drawable.ic_shuffle_white_48dp);
					shuffle_clicked = false;
					mChatApplication.newLocalUserMessage(CONTROLLER_VIDEOVIEWER_SHUFFLE_OFF);
				}

			}
		});

		repeatBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!repeat_clicked) {
					repeatBtn.setBackgroundResource(R.drawable.ic_repeat_black_48dp);
					repeat_clicked = true;
					mChatApplication.newLocalUserMessage(CONTROLLER_VIDEOVIEWER_REPEAT_ON);
				} else {
					repeatBtn.setBackgroundResource(R.drawable.ic_repeat_white_48dp);
					repeat_clicked = false;
					mChatApplication.newLocalUserMessage(CONTROLLER_VIDEOVIEWER_REPEAT_OFF);
				}

			}
		});

		gestureToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					sensor_on = true;
					sensorManager.registerListener(aSensorListener, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
					sensorManager.registerListener(gSensorListener, gSensor, SensorManager.SENSOR_DELAY_NORMAL);
					Toast.makeText(VideoviewActivity.this, "Sensor ON", Toast.LENGTH_SHORT).show();
				} else {
					if (sensor_on) {
						sensorManager.unregisterListener(aSensorListener);
						sensorManager.unregisterListener(gSensorListener);
						Toast.makeText(VideoviewActivity.this, "Sensor OFF", Toast.LENGTH_SHORT).show();
					}
					sensor_on = false;
				}
			}
		});

		snapBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mChatApplication.newLocalUserMessage(CONTROLLER_VIDEOVIEWER_SCREENSHOT);

			}
		});

		shareBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (currentPath == null) {
					Toast.makeText(getApplicationContext(), "You have nothing to share!", Toast.LENGTH_SHORT).show();
					return;
				}
				Toast.makeText(VideoviewActivity.this, "OPEN AND SHARE!", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				Uri uri = Uri.fromFile(screenshotImg);
				intent.setDataAndType(uri, "image/*");
				startActivity(intent);
			}
		});

		/* Volume seekbar */

		volume_bar = (SeekBar) findViewById(R.id.seekbar);
		volume_bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				volume = progress;
				vl_gesture_controll = false;
				volumeCMD(volume);
			}
		});

	}

	@Override
	public void onBackPressed() {

		if (sensor_on) {
			sensorManager.unregisterListener(aSensorListener);
			sensorManager.unregisterListener(gSensorListener);
		}
		mChatApplication.newLocalUserMessage(CONTROLLER_VIDEOVIEWER_CLOSE);
		super.onBackPressed();
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
			if (a_x < 3 && a_x > -3 && a_y < -3 && a_z > 0) {
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
			mChatApplication.newLocalUserMessage(CONTROLLER_VIDEOVIEWER_PRE);
			break;
		}
		case RIGHT: {
			mChatApplication.newLocalUserMessage(CONTROLLER_VIDEOVIEWER_NEXT);
			break;
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
		cmd = CONTROLLER_VIDEOVIEWER_VOLUME;
		cmd = cmd.concat(s);
		volume_value.setText(Integer.toString(volume));
		volume_bar.setProgress(volume);
		mChatApplication.newLocalUserMessage(cmd);
	}

	private void updateImagePreview() {
		LinearLayout videoLayout = (LinearLayout)findViewById(R.id.videoLayoutMain);
		LinearLayout mediaBtnLayout = (LinearLayout)findViewById(R.id.mediaBtn);
		screenshotImg = new File(currentPath);
		if (screenshotImg.exists()) {
			Bitmap myBitmap = BitmapFactory.decodeFile(screenshotImg.getAbsolutePath());
			screenshotPreview.setImageBitmap(myBitmap);
			 Palette palette = Palette.generate(myBitmap); 
			 if (palette.getLightVibrantColor() != null) {
				 videoLayout.setBackgroundColor(palette .getDarkVibrantColor().getRgb());
			 }
			 if (palette.getLightVibrantColor() != null) {
				 mediaBtnLayout.setBackgroundColor(palette .getLightVibrantColor().getRgb());
			 }		
		}
	}
	
	
	private void initUIComponent(){
		playBtn = (Button) findViewById(R.id.playBtn);
		previousBtn = (Button) findViewById(R.id.previousBtn);
		nextBtn = (Button) findViewById(R.id.nextBtn);
		replayBtn = (Button) findViewById(R.id.replayBtn);
		shuffleBtn = (Button) findViewById(R.id.shuffleBtn);
		snapBtn = (Button) findViewById(R.id.snapBtn);
		repeatBtn = (Button) findViewById(R.id.repeatBtn);
		shareBtn = (Button) findViewById(R.id.shareBtn);

		playBtn.setBackgroundResource(R.drawable.ic_play_arrow_black_48dp);
		previousBtn.setBackgroundResource(R.drawable.ic_skip_previous_black_48dp);
		nextBtn.setBackgroundResource(R.drawable.ic_skip_next_black_48dp);
		replayBtn.setBackgroundResource(R.drawable.ic_replay_black_48dp);
		shuffleBtn.setBackgroundResource(R.drawable.ic_shuffle_white_48dp);
		repeatBtn.setBackgroundResource(R.drawable.ic_repeat_white_48dp);

		volume_value = (TextView) findViewById(R.id.volumeValue);
		gestureToggle = (ToggleButton) findViewById(R.id.mediaGesture);
		screenshotPreview = (ImageView) findViewById(R.id.screenshotPreview);
		gestureToggle.setChecked(false);
	}
}

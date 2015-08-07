package com.technalt.serverlessCafe;

import com.technalt.serverless.CafeApplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.Sampler.Value;
import android.widget.TextView;
import android.widget.VerticalSeekBar;
import android.os.Bundle;
import android.app.Activity;
import android.media.Image;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class TvControllerActivity extends Activity {

	private final String CONTROLLER_CMD_UI_LEFT = "ISTVSgoleft";
	private final String CONTROLLER_CMD_UI_RIGHT = "ISTVSgoright";
	private final String CONTROLLER_CMD_UI_OK = "ISTVSok";
	private final String CONTTOLLER_CMD_VL = "ISTVvolume";

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

	/*------------- UI --------*/

	DrawerLayout drawerLayout;
	View leftDrawerView, rightDrawerView;
	TextView textPrompt, textPrompt2;
	ListView bookmarkDrawerList, historyDrawerList;
	TextView textSelection, channel_information;
	Button share_btn, snap_btn;

	private String[] bookmark_Channels = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday",
			"sad", "BBC", "CNN", "ESPN", "lor", "asd", "asdas" };
	private String[] history_Channels = { "HBO", "Annimax", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday",
			"sad", "BBC", "CNN", "ESPN", "lor", "asd", "asdas" };

	ArrayAdapter<String> arrayAdapter1;
	ArrayAdapter<String> arrayAdapter2;

	SeekBar ChannelBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tvcontroller);

		/*
		 * Create Bitmap Failed FrameLayout view = (FrameLayout)
		 * findViewById(R.id.root_layout); view.setDrawingCacheEnabled(true);
		 * view.buildDrawingCache(); Bitmap bm = view.getDrawingCache(); Blut
		 * bitmap Bitmap blur_bm = Bitmap.createScaledBitmap(bm, bm.getWidth(),
		 * bm.getHeight(), true);
		 * 
		 * 
		 * /* BLur btn
		 */

		/* Share Btn */
		share_btn = (Button) findViewById(R.id.share_btn);
		share_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_TEXT, "Here is some channel information!!");
				sendIntent.setType("text/plain");
				startActivity(sendIntent);

			}
		});

		/* Volume */
		VerticalSeekBar volume_bar = (VerticalSeekBar) findViewById(R.id.volume_bar);
		final TextView volume_value = (TextView) findViewById(R.id.volume_value);
		volume_bar.setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// TODO Auto-generated method stub
				volume_value.setText(String.valueOf(progress));
				mChatApplication.newLocalUserMessage(CONTTOLLER_CMD_VL);

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Log.d("TESsT", "IS THIS A CAKE");
			}
		});

		// Handler seekBarHandler = new Handler();

		/* Channel Seekbar */
		ChannelBar = (SeekBar) findViewById(R.id.customSeekBar);
		ChannelBar.setProgress(50);
		final TextView channel_value = (TextView) findViewById(R.id.channel_value);
		final TextView shift_Value = (TextView)findViewById(R.id.channel_shift);
		ChannelBar.setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// TODO Auto-generated method stub
				channel_value.setText(String.valueOf(progress));
				
				// CHannel Processing
				int shift = 0;
				shift = progress - 50;
				shift_Value.setText(String.valueOf(shift));
				
				// Channel Increasing
				if(shift > 20 && shift < 100){
					
				}
				
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				ChannelBar.setProgress(50);
				Log.d("TEST", "IS THIS A CAKE");
			}
		});

	
		
		

		/* For TextView Scroll */
		channel_information = (TextView) findViewById(R.id.channel_infor);
		channel_information.setMovementMethod(new ScrollingMovementMethod());

		// textPrompt = (TextView) findViewById(R.id.prompt);
		// textPrompt2 = (TextView) findViewById(R.id.prompt2);

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		leftDrawerView = (View) findViewById(R.id.leftdrawer);
		rightDrawerView = (View) findViewById(R.id.rightdrawer);

		// drawerLayout.setDrawerListener(myDrawerListener);

		/*
		 * In my trial experiment: Without dummy OnTouchListener for the
		 * drawView to consume the onTouch event, touching/clicking on
		 * un-handled view on drawView will pass to the view under it! -
		 * Touching on the Android icon will trigger the
		 * TextView("http://android-er.blogspot.com/") to open the web.
		 */

		/* ------Bookmark Drawer List---- */

		leftDrawerView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return true;
			}
		});

		textSelection = (TextView) findViewById(R.id.selection);
		bookmarkDrawerList = (ListView) findViewById(R.id.bookmarklist);
		arrayAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, bookmark_Channels) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);

				TextView textView = (TextView) view.findViewById(android.R.id.text1);

				/* YOUR CHOICE OF COLOR */
				textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				textView.setGravity(Gravity.CENTER);

				return view;
			}
		};
		bookmarkDrawerList.setAdapter(arrayAdapter1);

		/*
		 * Select which channel bookmarkDrawerList.setOnItemClickListener(new
		 * OnItemClickListener() {
		 * 
		 * @Override public void onItemClick(AdapterView<?> parent, View view,
		 * int position, long id) { String sel = (String)
		 * parent.getItemAtPosition(position); textSelection.setText(sel); } });
		 */

		/* ------History Drawer List---- */
		rightDrawerView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return true;
			}
		});

		textSelection = (TextView) findViewById(R.id.selection);
		historyDrawerList = (ListView) findViewById(R.id.historylist);
		arrayAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, history_Channels) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);

				TextView textView = (TextView) view.findViewById(android.R.id.text1);

				/* YOUR CHOICE OF COLOR */
				textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				textView.setGravity(Gravity.CENTER);

				return view;
			}
		};
		historyDrawerList.setAdapter(arrayAdapter2);

		/*
		 * Select which channel historyDrawerList.setOnItemClickListener(new
		 * OnItemClickListener() {
		 * 
		 * @Override public void onItemClick(AdapterView<?> parent, View view,
		 * int position, long id) { String sel = (String)
		 * parent.getItemAtPosition(position); textSelection.setText(sel); } });
		 */

		mChatApplication = (CafeApplication) getApplication();
		
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
			mChatApplication.newLocalUserMessage(CONTROLLER_CMD_UI_LEFT);
			break;
		}
		case RIGHT: {
			mChatApplication.newLocalUserMessage(CONTROLLER_CMD_UI_RIGHT);
			break;
		}
		}

	}

}
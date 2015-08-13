package tw.futureinsighters.client;

import org.allseenaliance.alljoyn.CafeApplication;

import com.technalt.serverlessCafe.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VerticalSeekBar;

public class TvControllerActivity extends Activity {

	/* AllJoyn Controll */
	private final String CONTROLLER_CMD_UI_LEFT = "ISTVSgoleft";
	private final String CONTROLLER_CMD_UI_RIGHT = "ISTVSgoright";
	private final String CONTROLLER_CMD_UI_OK = "ISTVSok";
	private final String CONTROLLER_CMD_VL = "ISTVSvl-";
	private final String CONTROLLER_CMD_CN = "ISTVScn-";
	private final String CONTROLLER_CMD_UI_BM_OP = "ISTVSsb";
	private final String CONTROLLER_CMD_UI_BM_CL = "ISTVShb";
	private final String CONTROLLER_CMD_UI_HT_OP = "ISTVSsh";
	private final String CONTROLLER_CMD_UI_HT_CL = "ISTVShh";
	private final String CONTROLLER_CMD_GET_CUR_CHANNEL_INFO = "ISTVScurchannelinfo";

	private enum Direction {
		LEFT, RIGHT
	};

	private float a_x, a_y, a_z, g_x, g_y, g_z;
	private boolean is_up = false, is_up_long = false;
	private boolean is_down = false, is_down_long = false;
	private boolean is_controllable = true;

	private CafeApplication mChatApplication = null;

	private SensorManager sensorManager;
	private Sensor aSensor;
	private Sensor gSensor;

	/* UI */
	private DrawerLayout drawerLayout;
	private View leftDrawerView, rightDrawerView;
	private ListView bookmarkDrawerList, historyDrawerList;
	private TextView textSelection, channel_information;
	private Button share_btn, snap_btn, gesture_btn;
	private ArrayAdapter<String> arrayAdapter1;
	private ArrayAdapter<String> arrayAdapter2;
	boolean left_open = false, right_open = false;

	/* Channel */
	private SeekBar ChannelBar;
	private int shift = 0, delta = 0;
	private boolean channelBarOnTouched = false;
	private EditText channel_edit;
	private Button channel_submit;
	private ImageView bookmark_img;

	/* Channel Info */
	private ChannelInfo channelInfo = new ChannelInfo();

	private class ChannelInfo {
		public int number = 0;
		public String name = "defaule_name";
		public String intro = "Sorry! Something went wrong.";
		public Boolean isAds = false; 
	}
	
	/* String arrays */
	private String[] bookmark_Channels = { "sad", "BBC", "CNN", "ESPN", "lor", "asd", "asdas" };
	private String[] history_Channels = { "HBO", "STAR", "TVBS", "sad", "BBC", "CNN", "ESPN" };

	/* ---- */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tvcontroller);

		/* ------Start AllJoyn Service KEYWORD!!---- */
		mChatApplication = (CafeApplication) getApplication();

		/* channel info reciever */

		BroadcastReceiver channelInfoBroadcastReciever = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// NOT FINISHED YET!!! ERROR NOT　HANDLED
				Toast.makeText(context, "MSG Got!", Toast.LENGTH_SHORT).show();
				
				String name = intent.getStringExtra("name");
				int number = Integer.parseInt(intent.getStringExtra("number"));
				String intro = intent.getStringExtra("intro");
				Boolean isAds = Boolean.valueOf(intent.getStringExtra("isAds"));
				Toast.makeText(context, "name: " + name + "  number: " + Integer.toString(number) + "  intro" + intro,
						Toast.LENGTH_LONG).show();
				channelInfo.name = name;
				channelInfo.number = number;
				channelInfo.intro = intro;
				channelInfo.isAds = isAds;
				// updateChannelInfoUI();
			}
		};
		IntentFilter channelInfoFilter = new IntentFilter("channelInfo");
		registerReceiver(channelInfoBroadcastReciever, channelInfoFilter);

		// FOR TEST ONLY !!!!!
		requestCurChannelInfo();

		/******* UI *******/

		/* Keyboard */
		/* Avoid auto appear */
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		channel_edit = (EditText) findViewById(R.id.editText_cn);
		channel_submit = (Button) findViewById(R.id.submit_cn);
		channel_edit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
						|| (actionId == EditorInfo.IME_ACTION_DONE)) {
					channel_submit.performClick();
				}
				return false;

			}
		});

		/* Gesture Btn */
		gesture_btn = (Button) findViewById(R.id.gesture_btn);
		gesture_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				LinearLayout vl_layout = (LinearLayout) findViewById(R.id.vl_layout);
				LinearLayout cn_layout = (LinearLayout) findViewById(R.id.cn_layout);
				LinearLayout gt_layout = (LinearLayout) findViewById(R.id.gesture_layout);
				LinearLayout bottom_container = (LinearLayout) findViewById(R.id.bottom_container);
				vl_layout.setVisibility(View.GONE);
				cn_layout.setVisibility(View.GONE);

				gt_layout.setVisibility(View.VISIBLE);

				// bottom_container.getBackground().setColorFilter(Color.parseColor("#333333"),
				// PorterDuff.Mode.MULTIPLY);
			}
		});

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
				String s, cmd;
				s = String.valueOf(progress);
				cmd = CONTROLLER_CMD_VL;
				cmd = cmd.concat(s);
				volume_value.setText(s);
				volumeCMD(cmd);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Log.d("TESsT", "IS THIS A CAKE");
			}
		});

		/* Channel Seekbar */
		ChannelBar = (SeekBar) findViewById(R.id.customSeekBar);
		ChannelBar.setProgress(50);
		final TextView channel_value = (TextView) findViewById(R.id.channel_value);
		final TextView shift_Value = (TextView) findViewById(R.id.channel_shift);
		ChannelBar.setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				int x = ChannelBar.getProgress();
				channel_value.setText(String.valueOf(x));

				// CHannel Processing
				Log.d("ChannelBar", "Touched");
				shift = progress - 50;
				shift_Value.setText(String.valueOf(shift));

				channelBarOnTouched = true;
				if (shift > 0) {
					new android.os.Handler().postDelayed(new Runnable() {
						public void run() {
							if (channelBarOnTouched) {
								channelFastCheck_pos();
							}
						}
					}, 500);
				}
				if (shift < 0) {
					new android.os.Handler().postDelayed(new Runnable() {
						public void run() {
							if (channelBarOnTouched) {
								channelFastCheck_neg();
							}
						}
					}, 500);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				Log.d("Tracking", "msg");
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if (shift > 0) {
					channelCMD(++delta);
				}
				if (shift < 0) {
					channelCMD(--delta);
				}
				ChannelBar.setProgress(50);
				channelBarOnTouched = false;
				TextView delta_value = (TextView) findViewById(R.id.channel_delta);
				delta_value.setText(String.valueOf(delta));

				Log.d("TEST", "IS THIS A CAKE");
			}
		});

		/* For TextView Scroll */
		channel_information = (TextView) findViewById(R.id.channel_infor);
		channel_information.setMovementMethod(new ScrollingMovementMethod());
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		leftDrawerView = (View) findViewById(R.id.leftdrawer);
		rightDrawerView = (View) findViewById(R.id.rightdrawer);
		drawerLayout.setDrawerListener(myDrawerListener);

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
		arrayAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				bookmark_Channels) {

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

		bookmark_img = (ImageView) findViewById(R.id.bookmark_img);
		bookmark_img.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				bookmark_img.setImageResource(R.drawable.bookmark_added);
			}
		});

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
		arrayAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				history_Channels) {

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

	private void volumeCMD(String cmd) {
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

		mChatApplication.newLocalUserMessage(cmd);
	}

	private void channelCMD(int delta) {
		String cmd = CONTROLLER_CMD_CN;
		cmd = cmd.concat(String.valueOf(delta));
		mChatApplication.newLocalUserMessage(cmd);
	}

	DrawerListener myDrawerListener = new DrawerListener() {

		@Override
		public void onDrawerClosed(View drawerView) {
			if (left_open) {
				// Toast.makeText(TvControllerActivity.this,"Left is
				// open",Toast.LENGTH_SHORT).show();
				mChatApplication.newLocalUserMessage(CONTROLLER_CMD_UI_BM_CL);
				left_open = false;
				bookmark_img.setImageResource(R.drawable.bookmark_add3);
				// Toast.makeText(TvControllerActivity.this,"Left is
				// closed",Toast.LENGTH_SHORT).show();
			}
			if (right_open) {
				// Toast.makeText(TvControllerActivity.this,"Right is
				// open",Toast.LENGTH_SHORT).show();
				mChatApplication.newLocalUserMessage(CONTROLLER_CMD_UI_HT_CL);
				right_open = false;
				// Toast.makeText(TvControllerActivity.this,"Rght is
				// closed",Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onDrawerOpened(View drawerView) {
			if (!left_open) {
				if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
					left_open = true;
					right_open = false;
					mChatApplication.newLocalUserMessage(CONTROLLER_CMD_UI_BM_OP);
				}
			}
			if (!right_open) {
				if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
					right_open = true;
					left_open = false;
					mChatApplication.newLocalUserMessage(CONTROLLER_CMD_UI_HT_OP);
				}
			}
		}

		@Override
		public void onDrawerSlide(View drawerView, float slideOffset) {

		}

		@Override
		public void onDrawerStateChanged(int newState) {

		}
	};

	private void channelFastCheck_pos() {
		if (is_controllable) {
			is_controllable = false;
			new android.os.Handler().postDelayed(new Runnable() {
				public void run() {
					is_controllable = true;
				}
			}, 200);
		} else {
			return;
		}
		if (channelBarOnTouched) {
			channelCMD(++delta);
			TextView delta_value = (TextView) findViewById(R.id.channel_delta);
			delta_value.setText(String.valueOf(delta));
			new android.os.Handler().postDelayed(new Runnable() {
				public void run() {
					channelFastCheck_pos();
				}
			}, 250);
		}
	}

	private void channelFastCheck_neg() {
		if (is_controllable) {
			is_controllable = false;
			new android.os.Handler().postDelayed(new Runnable() {
				public void run() {
					is_controllable = true;
				}
			}, 200);
		} else {
			return;
		}
		if (channelBarOnTouched) {
			channelCMD(--delta);
			TextView delta_value = (TextView) findViewById(R.id.channel_delta);
			delta_value.setText(String.valueOf(delta));
			new android.os.Handler().postDelayed(new Runnable() {
				public void run() {
					channelFastCheck_neg();
				}
			}, 250);
		}
	}
	
	/* get channel info */
	private void requestCurChannelInfo() {
		ChannelInfo newChannelInfo = new ChannelInfo();
		mChatApplication.newLocalUserMessage(CONTROLLER_CMD_GET_CUR_CHANNEL_INFO);
		
	}

}
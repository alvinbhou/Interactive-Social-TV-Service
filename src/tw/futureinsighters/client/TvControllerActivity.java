package tw.futureinsighters.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.allseenaliance.alljoyn.CafeApplication;

import com.technalt.serverlessCafe.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRouter.UserRouteInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VerticalSeekBar;
import tw.futureinsighters.defines.TVCONTROLLER_CMD;

public class TvControllerActivity extends Activity {

	private enum Direction {
		LEFT, RIGHT
	};

	private boolean is_appslist_on = true;
	private float a_x, a_y, a_z, g_x, g_y, g_z;
	private boolean is_up = false, is_up_long = false;
	private boolean is_down = false, is_down_long = false;
	private boolean is_controllable = true;
	private boolean sensor_on = false;

	private CafeApplication mChatApplication = null;

	private SensorManager sensorManager;
	private Sensor aSensor;
	private Sensor gSensor;

	/* UI */
	private DrawerLayout drawerLayout;
	private View leftDrawerView, rightDrawerView;
	private ListView bookmarkDrawerList, historyDrawerList;
	private TextView channelValue, channel_information, program_name;
	private Button share_btn, fb_btn, voice_btn;
	private ArrayAdapter<String> arrayAdapter1;
	private ArrayAdapter<String> arrayAdapter2;
	private boolean left_open = false, right_open = false;
	private LinearLayout vl_layout;
	private LinearLayout cn_layout;
	private LinearLayout gt_layout;

	/* Gesture */
	private ImageView gesture_img;
	private Button gesture_return, google_btn;

	/* Volume */
	private int volume = 50;
	private VerticalSeekBar volume_bar;
	private TextView volume_value = null;
	private boolean vl_gesture_controll = false;

	/* Channel */
	private SeekBar ChannelBar;
	private int shift = 0;
	private boolean channelBarOnTouched = false;
	private EditText channel_edit;
	private Button channel_submit;
	private ImageView bookmark_img;
	private int channelTmp = 0;
	private ArrayList<String> bookmarkChannels;

	/* Channel Info */
	private ChannelInfo curChannelInfo = new ChannelInfo();
	private ChannelInfo channelInfo = new ChannelInfo();
	private Boolean requesting_infor = true;
	/* 1 for current, 2 for search */

	private class ChannelInfo {
		public int number = 66;
		public String channelName = "No Channel Name.";
		public String programName = "No Program.";
		public String programDescription = "Requesting data ...	";
		public Boolean isAds = false;
	}

	/* Dialog elements */
	private TextView programName_dialog, programDescription_dialog, isAds_dialog;
	private AlertDialog.Builder channelDialog;

	/* ---- */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tvcontroller);

		/* close the sensor */
		sensor_on = false;

		/* ------Start AllJoyn Service KEYWORD!!---- */
		mChatApplication = (CafeApplication) getApplication();
		initializeChannel();

		/* channel info reciever */
		BroadcastReceiver curChannelInfoBroadcastReciever = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// NOT FINISHED YET!!! ERROR NOT HANDLED
				// Toast.makeText(context, "MSG Got!",
				// Toast.LENGTH_SHORT).show();

				String channelName = intent.getStringExtra("channelName");
				String programName = intent.getStringExtra("programName");
				int number = Integer.parseInt(intent.getStringExtra("number"));
				String programDescription = intent.getStringExtra("programDescription");
				Boolean isAds = Boolean.valueOf(intent.getStringExtra("isAds"));

				curChannelInfo.channelName = channelName;
				curChannelInfo.programName = programName;
				curChannelInfo.number = number;
				curChannelInfo.programDescription = programDescription;
				curChannelInfo.isAds = isAds;
				updateChannelInfoUI();

			}
		};
		IntentFilter curChannelInfoFilter = new IntentFilter("curChannelInfo");
		registerReceiver(curChannelInfoBroadcastReciever, curChannelInfoFilter);

		BroadcastReceiver channelInfoBroadcastReciever = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// NOT FINISHED YET!!! ERROR NOT HANDLED
				Toast.makeText(context, "MSG Got!", Toast.LENGTH_SHORT).show();

				String channelName = intent.getStringExtra("channelName");
				String programName = intent.getStringExtra("programName");
				int number = Integer.parseInt(intent.getStringExtra("number"));
				String programDescription = intent.getStringExtra("programDescription");
				Boolean isAds = Boolean.valueOf(intent.getStringExtra("isAds"));

				channelInfo.channelName = channelName;
				channelInfo.programName = programName;
				channelInfo.number = number;
				channelInfo.programDescription = programDescription;
				channelInfo.isAds = isAds;

				dialogRefresh();
			}
		};
		IntentFilter channelInfoFilter = new IntentFilter("channelInfo");
		registerReceiver(channelInfoBroadcastReciever, channelInfoFilter);

		BroadcastReceiver otherBroadcastReciever = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// NOT FINISHED YET!!! ERROR NOT HANDLED

				String name = intent.getStringExtra("name");
				if (name.equals("AppsListIsOn")) {
					is_appslist_on = true;

				} else if (name.equals("AppsListIsOff")) {
					is_appslist_on = false;
				}
			}
		};
		IntentFilter otherBroadcastFilter = new IntentFilter("other");
		registerReceiver(otherBroadcastReciever, otherBroadcastFilter);

		/******* UI *******/

		/* Keyboard */
		/* Avoid auto appear */
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		channel_edit = (EditText) findViewById(R.id.editText_cn);
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

		/* channel submit button */
		channel_submit = (Button) findViewById(R.id.submit_cn);
		channel_submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String value = channel_edit.getText().toString();
				if (!value.isEmpty() && Integer.parseInt(value) < 500) {
					Toast.makeText(TvControllerActivity.this, value, Toast.LENGTH_SHORT).show();
					curChannelInfo.number = Integer.parseInt(value);
					channelCMD(curChannelInfo.number);
				}
			}
		});

		/* Google search */
		google_btn = (Button) findViewById(R.id.google_btn);
		google_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String query = "Something went wrong!";
				try {
					query = URLEncoder.encode(curChannelInfo.programName, "utf-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String url = "http://www.google.com/search?q=" + query;
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(url));
				startActivity(intent);
			}
		});

		/* FB share */
		fb_btn = (Button) findViewById(R.id.fb_btn);
		fb_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String inforToShare = "http://www.getfresh.org.tw/";
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				// intent.putExtra(Intent.EXTRA_SUBJECT, "Foo bar"); // NB: has
				// no effect!
				intent.putExtra(Intent.EXTRA_TEXT, inforToShare);

				// See if official Facebook app is found
				boolean facebookAppFound = false;
				List<ResolveInfo> matches = getPackageManager().queryIntentActivities(intent, 0);
				for (ResolveInfo info : matches) {
					if (info.activityInfo.packageName.toLowerCase().startsWith("com.facebook.katana")) {
						intent.setPackage(info.activityInfo.packageName);
						facebookAppFound = true;
						break;
					}
				}

				// As fallback, launch sharer.php in a browser
				if (!facebookAppFound) {
					String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=" + inforToShare;
					intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
				}

				startActivity(intent);

			}
		});

		/* Voice Btn */
		voice_btn = (Button) findViewById(R.id.voice_btn);
		voice_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "zh");

				try {
					startActivityForResult(intent, 1);
				} catch (ActivityNotFoundException a) {
					Toast.makeText(TvControllerActivity.this, "Oops! Your device doesn't support Speech to Text",
							Toast.LENGTH_SHORT).show();
				}

			}
		});

		/* Gesture Btn */
		gesture_img = (ImageView) findViewById(R.id.gesture_btn);
		int imagesToShow[] = { R.drawable.gesture2, R.drawable.gesture2 };
		animate(gesture_img, imagesToShow, 0, true);

		ImageView clickable_gesture = (ImageView) findViewById(R.id.gesture_btn2);

		clickable_gesture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				vl_layout = (LinearLayout) findViewById(R.id.vl_layout);
				cn_layout = (LinearLayout) findViewById(R.id.cn_layout);
				gt_layout = (LinearLayout) findViewById(R.id.gesture_layout);

				vl_layout.setVisibility(View.GONE);
				cn_layout.setVisibility(View.GONE);
				gt_layout.setVisibility(View.VISIBLE);

				// initialize sensor
				sensor_on = true;
				sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
				aSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

				sensorManager.registerListener(aSensorListener, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
				sensorManager.registerListener(gSensorListener, gSensor, SensorManager.SENSOR_DELAY_NORMAL);

			}
		});

		/* Gesture Return Btn */
		gesture_return = (Button) findViewById(R.id.end_gesture);
		gesture_return.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				vl_layout = (LinearLayout) findViewById(R.id.vl_layout);
				cn_layout = (LinearLayout) findViewById(R.id.cn_layout);
				gt_layout = (LinearLayout) findViewById(R.id.gesture_layout);

				vl_layout.setVisibility(View.VISIBLE);
				cn_layout.setVisibility(View.VISIBLE);
				gt_layout.setVisibility(View.GONE);
				sensorManager.unregisterListener(aSensorListener);
				sensorManager.unregisterListener(gSensorListener);
				sensor_on = false;
				mChatApplication.newLocalUserMessage(TVCONTROLLER_CMD.APPLIST_OFF);

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
		volume_bar = (VerticalSeekBar) findViewById(R.id.volume_bar);
		volume_value = (TextView) findViewById(R.id.volume_value);
		volume_bar.setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				volume = progress;
				vl_gesture_controll = false;
				volumeCMD(volume);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});

		/* Channel Seekbar */
		ChannelBar = (SeekBar) findViewById(R.id.customSeekBar);
		ChannelBar.setProgress(50);
		ChannelBar.setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

				Log.d("ChannelBar", "Touched");
				shift = progress - 50;

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
					channelCMD(++curChannelInfo.number);
				} else if (shift < 0) {
					channelCMD(--curChannelInfo.number);
				}
				ChannelBar.setProgress(50);
				channelBarOnTouched = false;
				arrayAdapter2.notifyDataSetChanged();
			}
		});

		/* For TextView Scroll and drawer */
		channel_information = (TextView) findViewById(R.id.channel_infor);
		channel_information.setMovementMethod(new ScrollingMovementMethod());
		program_name = (TextView) findViewById(R.id.channel_txt);
		program_name.setMovementMethod(new ScrollingMovementMethod());
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

		// textSelection = (TextView) findViewById(R.id.selection);
		bookmarkDrawerList = (ListView) findViewById(R.id.bookmarklist);
		ArrayList<Integer> result1 = getBookmark(); // get bookmarks
		bookmarkChannels = new ArrayList<String>();
		for (int i = 0; i < result1.size(); i++) {
			bookmarkChannels.add(Integer.toString(result1.get(i)));
		}
		arrayAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, bookmarkChannels) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);

				TextView textView = (TextView) view.findViewById(android.R.id.text1);

				textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				textView.setGravity(Gravity.CENTER);

				return view;
			}
		};
		bookmarkDrawerList.setAdapter(arrayAdapter1);
		/* short click */
		bookmarkDrawerList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String select = (String) parent.getItemAtPosition(position);
				channelCMD(Integer.parseInt(select));
			}
		});
		/* long click */
		bookmarkDrawerList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				channelAlert(arg0, arg2);
				return true;
			}
		});

		bookmark_img = (ImageView) findViewById(R.id.bookmark_img);
		bookmark_img.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				bookmark_img.setImageResource(R.drawable.bookmark_added);
				addBookmark(curChannelInfo.number);
				drawerUpdate();
				arrayAdapter1.notifyDataSetChanged();
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
		// textSelection = (TextView) findViewById(R.id.selection);
		historyDrawerList = (ListView) findViewById(R.id.historylist);
		String[] historyChannels = new String[10];
		int[] result2 = getHistory(); // get history
		for (int i = 0; i < 10; i++) { // casting
			historyChannels[i] = Integer.toString(result2[i]);
		}
		arrayAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, historyChannels) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);

				TextView textView = (TextView) view.findViewById(android.R.id.text1);

				textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				textView.setGravity(Gravity.CENTER);

				return view;
			}
		};
		historyDrawerList.setAdapter(arrayAdapter2);

		/* short click */
		historyDrawerList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String select = (String) parent.getItemAtPosition(position);
				channelCMD(Integer.parseInt(select));
			}
		});

	}



	/* voice to text search */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case 1: {
			if (resultCode == Activity.RESULT_OK && null != data) {
				String voiceResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);

				if (voiceResult.contains("turn to channel ")) {
					String tmp = voiceResult.substring(16);
					Toast.makeText(TvControllerActivity.this, tmp, Toast.LENGTH_SHORT).show();
					if (tmp.matches("^-?\\d+$"))
						channelCMD(Integer.parseInt(tmp));
				} else if (voiceResult.contains("turn to ")) {
					String tmp = voiceResult.substring(8);
					Toast.makeText(TvControllerActivity.this, tmp, Toast.LENGTH_SHORT).show();
					if (tmp.matches("^-?\\d+$"))
						channelCMD(Integer.parseInt(tmp));
				}
			}
			break;
		}
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		recordShareReference();
		if (sensor_on) {
			sensorManager.unregisterListener(aSensorListener);
			sensorManager.unregisterListener(gSensorListener);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();	
		if(sensor_on)
			gesture_return.performClick();
	}

	/* Send backToHome message to Tv */
	@Override
	public void onBackPressed() {
		if (sensor_on)
			gesture_return.performClick();
		else {
			recordShareReference();
			
			mChatApplication.newLocalUserMessage(TVCONTROLLER_CMD.HOME);
			super.onBackPressed();
		}
	}

	@Override
	public void onDestroy() {
		recordShareReference();
		sensor_on = false;
		mChatApplication.newLocalUserMessage(TVCONTROLLER_CMD.HOME);
		super.onDestroy();
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
								runApp();
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
		mChatApplication.newLocalUserMessage(TVCONTROLLER_CMD.UI_OK);
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
				mChatApplication.newLocalUserMessage(TVCONTROLLER_CMD.UI_LEFT);
				break;
			}
			case RIGHT: {
				mChatApplication.newLocalUserMessage(TVCONTROLLER_CMD.UI_RIGHT);
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
				channelCMD(--curChannelInfo.number);
				break;
			}
			case RIGHT: {
				channelCMD(++curChannelInfo.number);
				break;
			}
			}
		}

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
		volume_bar.setProgress(volume);
		String s, cmd;
		s = String.valueOf(volume);
		cmd = TVCONTROLLER_CMD.VL;
		cmd = cmd.concat(s);
		volume_value.setText(s);
		mChatApplication.newLocalUserMessage(cmd);
	}

	/* Delta v.s. Channel number */
	private void channelCMD(int number) {
		addHistory(number); // optional
		String cmd = TVCONTROLLER_CMD.CN;
		cmd = cmd.concat(String.valueOf(number));
		mChatApplication.newLocalUserMessage(cmd);
		requestCurChannelInfo();
		channelValue = (TextView) findViewById(R.id.channelValue);
		channelValue.setText(String.valueOf(number));

	}

	DrawerListener myDrawerListener = new DrawerListener() {

		@Override
		public void onDrawerClosed(View drawerView) {
			if (left_open) {
				mChatApplication.newLocalUserMessage(TVCONTROLLER_CMD.UI_BM_CL);
				left_open = false;
				bookmark_img.setImageResource(R.drawable.bookmark_add3);
			}
			if (right_open) {
				mChatApplication.newLocalUserMessage(TVCONTROLLER_CMD.UI_HT_CL);
				right_open = false;
			}
			drawerUpdate();
		}

		@Override
		public void onDrawerOpened(View drawerView) {
			invalidateOptionsMenu();
			arrayAdapter1.notifyDataSetInvalidated();
			arrayAdapter2.notifyDataSetInvalidated();
			drawerUpdate();
			if (!left_open) {
				if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
					left_open = true;
					right_open = false;
					mChatApplication.newLocalUserMessage(TVCONTROLLER_CMD.UI_BM_OP);
				}
			}
			if (!right_open) {
				if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
					right_open = true;
					left_open = false;
					mChatApplication.newLocalUserMessage(TVCONTROLLER_CMD.UI_HT_OP);
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
			channelCMD(++curChannelInfo.number);
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
			channelCMD(--curChannelInfo.number);
			new android.os.Handler().postDelayed(new Runnable() {
				public void run() {
					channelFastCheck_neg();
				}
			}, 250);
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

	/* get current channel info */
	private void requestCurChannelInfo() {
		mChatApplication.newLocalUserMessage(TVCONTROLLER_CMD.GET_CUR_CHANNEL_INFO);
	}

	/* get channel info */
	private void checkChannelInfo(String number) {
		String cmd = TVCONTROLLER_CMD.GET_CHANNEL_INFO;
		cmd = cmd.concat(number);
		mChatApplication.newLocalUserMessage(cmd);
	}

	/* Bookmarks and History management */

	/*
	 * return true if succeed return false if the channel already exists in
	 * Bookmark
	 */
	private Boolean addBookmark(int channel) {
		// check if the channel already exists
		SharedPreferences bookmarkRecord = getSharedPreferences("bookmark", 0);
		if (bookmarkRecord.contains(Integer.toString(channel))) {
			return false;
		}

		SharedPreferences.Editor editor = bookmarkRecord.edit();
		editor.putBoolean(Integer.toString(channel), true);
		editor.commit();
		return true;
	}

	/*
	 * return true if succeed return false if not found in record
	 */
	private Boolean removeBookmark(int channel) {
		SharedPreferences bookmarkRecord = getSharedPreferences("bookmark", 0);
		if (bookmarkRecord.contains(Integer.toString(channel))) {
			bookmarkRecord.edit().remove(Integer.toString(channel)).commit();
			return true;
		} else {
			return false;
		}
	}

	/*
	 * return all bookmarks in the form of ArrayList of Integer
	 */
	private ArrayList<Integer> getBookmark() {
		SharedPreferences bookmarkRecord = getSharedPreferences("bookmark", 0);
		Map<String, ?> allBookmarks = bookmarkRecord.getAll();
		ArrayList<Integer> bookmarks = new ArrayList<Integer>();
		for (Map.Entry<String, ?> bookmark : allBookmarks.entrySet()) {
			bookmarks.add(Integer.parseInt(bookmark.getKey()));
		}
		return bookmarks;
	}

	/*
	 * return true if succeed return false if the new one is the same with the
	 * last one
	 */
	private Boolean addHistory(int channel) {
		// check if the channel already exists
		SharedPreferences historyRecord = getSharedPreferences("history", 0);
		int head = historyRecord.getInt("head", 0);
		if (channel == historyRecord.getInt(Integer.toString(head), -1)) {
			return false;
		}
		head = (head + 1) % 10;

		SharedPreferences.Editor editor = historyRecord.edit();
		editor.putInt(Integer.toString(head), channel);
		editor.putInt("head", head);
		editor.commit();
		return true;
	}

	/*
	 * return all bookmarks in the form of ArrayList of Integer
	 */
	private int[] getHistory() {
		SharedPreferences historyRecord = getSharedPreferences("history", 0);
		int[] result = new int[10];
		int index = historyRecord.getInt("head", 0);
		for (int i = 0; i < 10; i++) {
			result[i] = historyRecord.getInt(Integer.toString(index), 0);
			if (--index < 0)
				index += 10;
		}
		return result;
	}

	/* Animation for gesture */
	private void animate(final ImageView imageView, final int images[], final int imageIndex, final boolean forever) {

		// imageView <-- The View which displays the images
		// images[] <-- Holds R references to the images to display
		// imageIndex <-- index of the first image to show in images[]

		int fadeInDuration = 800; // Configure time values here
		int timeBetween = 1000;
		int fadeOutDuration = 800;

		imageView.setVisibility(View.INVISIBLE);
		imageView.setImageResource(images[imageIndex]);

		Animation fadeIn = new AlphaAnimation(0, 1);
		fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
		fadeIn.setDuration(fadeInDuration);

		Animation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setInterpolator(new AccelerateInterpolator()); // and this
		fadeOut.setStartOffset(fadeInDuration + timeBetween);
		fadeOut.setDuration(fadeOutDuration);

		AnimationSet animation = new AnimationSet(false); // change to false
		animation.addAnimation(fadeIn);
		animation.addAnimation(fadeOut);
		animation.setRepeatCount(1);
		imageView.setAnimation(animation);

		animation.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation animation) {
				if (images.length - 1 > imageIndex) {
					animate(imageView, images, imageIndex + 1, forever);
				} else {
					if (forever == true) {
						animate(imageView, images, 0, forever);
					}
				}
			}

			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
			}

			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
			}
		});
	}

	/* update channel info UI after request channel info */
	private void updateChannelInfoUI() {
		final TextView channel_txt = (TextView) findViewById(R.id.channel_txt);
		final TextView channel_infor = (TextView) findViewById(R.id.channel_infor);
		channel_txt.setText(curChannelInfo.programName);
		// NOT DONE
		// search channel name for icon

		channel_infor.setText(curChannelInfo.programDescription);
		// there's no room for is_ads info
	}

	private void initializeChannel() {
		new android.os.Handler().postDelayed(new Runnable() {
			public void run() {
				SharedPreferences channelRecord = getSharedPreferences("lastChannel", 0);
				channelCMD(channelRecord.getInt("lastChannel", 13));
				// requestCurChannelInfo();
			}
		}, 7000);

		// channelCMD(74);
		// requestCurChannelInfo();
	}

	private void drawerUpdate() {
		bookmarkDrawerList = (ListView) findViewById(R.id.bookmarklist);
		String[] bookmarkChannels;
		ArrayList<Integer> result1 = getBookmark(); // get bookmarks
		bookmarkChannels = new String[result1.size()]; // casting
		for (int i = 0; i < result1.size(); i++) {
			bookmarkChannels[i] = Integer.toString(result1.get(i));
		}
		arrayAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, bookmarkChannels) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);

				TextView textView = (TextView) view.findViewById(android.R.id.text1);

				textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				textView.setGravity(Gravity.CENTER);

				return view;
			}
		};
		bookmarkDrawerList.setAdapter(arrayAdapter1);

		historyDrawerList = (ListView) findViewById(R.id.historylist);
		String[] historyChannels = new String[10];
		int[] result2 = getHistory(); // get history
		for (int i = 0; i < 10; i++) { // casting
			historyChannels[i] = Integer.toString(result2[i]);
		}
		arrayAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, historyChannels) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);

				TextView textView = (TextView) view.findViewById(android.R.id.text1);

				textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				textView.setGravity(Gravity.CENTER);

				return view;
			}
		};
		historyDrawerList.setAdapter(arrayAdapter2);
	}

	private void channelAlert(AdapterView<?> arg0, int pos) {

		String select = (String) arg0.getItemAtPosition(pos);
		checkChannelInfo(select);

		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_customize, null);

		programName_dialog = (TextView) view.findViewById(R.id.programName_dialog);
		programDescription_dialog = (TextView) view.findViewById(R.id.programDescription_dialog);
		isAds_dialog = (TextView) view.findViewById(R.id.isAds_dialog);
		programName_dialog.setText(" Program not found yet");
		programName_dialog.setBackgroundColor(0xFFF44336);
		programDescription_dialog.setText("Requesting data ...");
		programDescription_dialog.setMovementMethod(new ScrollingMovementMethod());
		isAds_dialog.setText((channelInfo.isAds) ? "Ads" : "Currently No Ads");

		channelDialog = new AlertDialog.Builder(TvControllerActivity.this)
				.setPositiveButton("DELETE", new bookmarkOnClickListener(arg0, pos))
				.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		channelDialog.setCustomTitle(view);
		AlertDialog alert = channelDialog.create();
		alert.show();
	}

	private void dialogRefresh() {
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_customize, null);
		programName_dialog.setText(channelInfo.programName);
		programName_dialog.setBackgroundColor((channelInfo.isAds) ? 0xFFFF6D00 : 0xFF4CAF50);
		programDescription_dialog.setText(channelInfo.programDescription);
		programDescription_dialog.setMovementMethod(new ScrollingMovementMethod());
		isAds_dialog.setText((channelInfo.isAds) ? "Ads" : "Currently No Ads");
		channelDialog.setCustomTitle(view);

	}

	private class bookmarkOnClickListener implements DialogInterface.OnClickListener {

		private int position;
		AdapterView<?> parent;

		public bookmarkOnClickListener(AdapterView<?> parent, int position) {
			this.position = position;
			this.parent = parent;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			String select = (String) parent.getItemAtPosition(position);
			removeBookmark(Integer.parseInt(select));
			drawerUpdate();
		}
	};
	
	private void recordShareReference(){
	
		SharedPreferences lastWatchedChannel = getSharedPreferences("lastChannel", 13);
		SharedPreferences.Editor editor = lastWatchedChannel.edit();
		editor.putInt("lastChannel", curChannelInfo.number);
		editor.commit();
	}
}
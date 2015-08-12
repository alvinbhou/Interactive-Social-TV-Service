package com.technalt.serverless;

import java.util.List;

import android.R.string;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.technalt.serverlessCafe.ControllerActivity;
import com.technalt.serverlessCafe.R;

public class MainActivity extends Activity implements Observer {

	// main menu
	private double screenWidth, screenHeight;

	// alljoyn

	private Button join;
	private Button stop;
	private Button start;
	private Button leave;
	private Button sendjson;

	private CafeApplication mChatApplication = null;
	private TextView preview;
	private EditText edit;

	private static final int HANDLE_APPLICATION_QUIT_EVENT = 0;
	private static final int HANDLE_CHANNEL_STATE_CHANGED_EVENT = 1;
	private static final int HANDLE_ALLJOYN_ERROR_EVENT = 2;
	private int retry_count = 0;

	// icons
	ImageView connect_img;
	Boolean flag_connect = true;
	Boolean controller_clicked = false;
	Button connect_success, connect_failure;
	// boolean found = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.host);

		// main menu

		getScreenSize();
		uiInit();
		connection_image();

		final LinearLayout controllerLayout = (LinearLayout) findViewById(R.id.controllerLayout);
		// click listeners
		controllerLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				controller_clicked = true;
				Intent intent = new Intent(MainActivity.this, ControllerActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.fadein, R.anim.fadeout);

			}
		});
		// final LinearLayout settingLayout = (LinearLayout)
		// findViewById(R.id.helpLayout);
		// // click listeners
		// settingLayout.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// // controller_clicked = true;
		// Intent intent = new Intent(MainActivity.this,
		// SettingsActivity.class);
		// startActivity(intent);
		// }
		// });

		// alljoyn
		stop = new Button(this);
		join = new Button(this);
		preview = (TextView) findViewById(R.id.textpreview);
		edit = new EditText(this);
		sendjson = new Button(this);
		leave = new Button(this);
		stop.setEnabled(false);
		leave.setEnabled(false);

		join.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final Dialog dialog = new Dialog(MainActivity.this);
				dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
				// dialog.setContentView(R.layout.usejoindialog);

				ArrayAdapter<String> channelListAdapter = new ArrayAdapter<String>(MainActivity.this,
						android.R.layout.test_list_item);
				final ListView channelList = new ListView(getApplicationContext());
				channelList.setAdapter(channelListAdapter);

				List<String> channels = mChatApplication.getFoundChannels();

				for (String channel : channels) {
					int lastDot = channel.lastIndexOf('.');
					if (lastDot < 0) {
						continue;
					}
					channelListAdapter.add(channel.substring(lastDot + 1));
				}
				channelListAdapter.notifyDataSetChanged();

				int length = channelList.getCount();
				TextView size = (TextView) findViewById(R.id.size);
				String str = Integer.toString(length);
				size.setText(str);
				String name = "";
				boolean found = false;

				for (int i = 0; i < length; i++) {
					name = channelList.getItemAtPosition(i).toString();
					if (name.equals("FutureInsighters")) {
						found = true;
						connect_success.performClick();

					}
				}
				if (!found) {
					new android.os.Handler().postDelayed(new Runnable() {
						public void run() {							
							retry_count++;
							if (retry_count > 20) {
								connect_failure.performClick();
								return;
							}
							join.performClick();
						}
					}, 500);
					return;
				}
				mChatApplication.useSetChannelName(name);
				mChatApplication.useJoinChannel();

				start.setEnabled(false);
				stop.setEnabled(false);
				join.setEnabled(false);
				sendjson.setEnabled(true);
				leave.setEnabled(true);

			}
		});

		stop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mChatApplication.hostStopChannel();
				stop.setEnabled(false);
				start.setEnabled(true);
				leave.setEnabled(false);
				sendjson.setEnabled(false);

			}
		});

		leave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mChatApplication.useLeaveChannel();
				mChatApplication.useSetChannelName("Not set");
				leave.setEnabled(false);
				sendjson.setEnabled(false);

				// start.setEnabled(true);
				stop.setEnabled(true);
				join.setEnabled(true);

			}
		});

		mChatApplication = (CafeApplication) getApplication();
		mChatApplication.checkin();

		// updateChannelState();

		mChatApplication.addObserver(this);

		// mChatApplication.hostSetChannelName("ServerlessCafe");
		// mChatApplication.hostInitChannel();
		// mChatApplication.hostStartChannel();
		//
		// start.setEnabled(false);
		//
		// stop.setEnabled(true);

		updateChannelState();

		// connect image

	}

	public void onDestroy() {

		mChatApplication = (CafeApplication) getApplication();
		mChatApplication.deleteObserver(this);

		mChatApplication.quit();

		super.onDestroy();
	}

	// alljoyn

	private void updateChannelState() {
		AllJoynService.HostChannelState channelState = mChatApplication.hostGetChannelState();
		String name = mChatApplication.hostGetChannelName();
		// boolean haveName = true;
		if (name == null) {
			// haveName = false;
			name = "Not set";
		}

		Toast.makeText(MainActivity.this, "Session Name " + name, Toast.LENGTH_SHORT).show();

		switch (channelState) {
		case IDLE:

			Toast.makeText(MainActivity.this, "Session Status idle", Toast.LENGTH_SHORT).show();

			break;
		case NAMED:

			Toast.makeText(MainActivity.this, "Session status named" + name, Toast.LENGTH_SHORT).show();
			break;
		case BOUND:

			Toast.makeText(MainActivity.this, "Session status bound" + name, Toast.LENGTH_SHORT).show();

			break;
		case ADVERTISED:

			Toast.makeText(MainActivity.this, "Session status advertised" + name, Toast.LENGTH_SHORT).show();
			break;
		case CONNECTED:

			Toast.makeText(MainActivity.this, "Session status connected", Toast.LENGTH_SHORT).show();

			break;
		default:

			Toast.makeText(MainActivity.this, "Session status unknown", Toast.LENGTH_SHORT).show();

			break;
		}

		if (channelState == AllJoynService.HostChannelState.IDLE) {

		}

	}

	private void updateHistory() {

		String messager = mChatApplication.getHistoryMessage();

		preview.setText(messager);

		/*
		 * List<String> messages = mChatApplication.getHistory(); for (String
		 * message : messages) { Toast.makeText(MainActivity.this,
		 * "History changed!!" + message, Toast.LENGTH_SHORT).show(); }
		 */

	}

	private static final int HANDLE_HISTORY_CHANGED_EVENT = 3;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case HANDLE_HISTORY_CHANGED_EVENT: {
				Log.i("", "mHandler.handleMessage(): HANDLE_HISTORY_CHANGED_EVENT");
				updateHistory();
				break;
			}

			case HANDLE_APPLICATION_QUIT_EVENT: {

				finish();
			}
				break;
			case HANDLE_CHANNEL_STATE_CHANGED_EVENT: {

				updateChannelState();
			}
				break;
			case HANDLE_ALLJOYN_ERROR_EVENT: {

			}
				break;
			default:
				break;
			}
		}
	};

	public synchronized void update(Observable o, Object arg) {

		String qualifier = (String) arg;

		if (qualifier.equals(CafeApplication.APPLICATION_QUIT_EVENT)) {
			Message message = mHandler.obtainMessage(HANDLE_APPLICATION_QUIT_EVENT);
			mHandler.sendMessage(message);
		}

		if (qualifier.equals(CafeApplication.HISTORY_CHANGED_EVENT)) {
			Message message = mHandler.obtainMessage(HANDLE_HISTORY_CHANGED_EVENT);
			mHandler.sendMessage(message);
		}

		if (qualifier.equals(CafeApplication.HOST_CHANNEL_STATE_CHANGED_EVENT)) {
			Message message = mHandler.obtainMessage(HANDLE_CHANNEL_STATE_CHANGED_EVENT);
			mHandler.sendMessage(message);
		}

		if (qualifier.equals(CafeApplication.ALLJOYN_ERROR_EVENT)) {
			Message message = mHandler.obtainMessage(HANDLE_ALLJOYN_ERROR_EVENT);
			mHandler.sendMessage(message);
		}
	}

	// main menu

	private void getScreenSize() {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		screenWidth = size.x;
		screenHeight = size.y;
		;
	}

	private void uiInit() {
		final LinearLayout logoLayout = (LinearLayout) findViewById(R.id.logoLayout);
		final LinearLayout connectLayout = (LinearLayout) findViewById(R.id.connectLayout);
		final LinearLayout bottomLayout = (LinearLayout) findViewById(R.id.bottomLayout);
		final LinearLayout controllerLayout = (LinearLayout) findViewById(R.id.controllerLayout);
		final LinearLayout helpLayout = (LinearLayout) findViewById(R.id.helpLayout);
		final ImageView connect_logo = (ImageView) findViewById(R.id.connect_logo);

		LayoutParams params = logoLayout.getLayoutParams();
		params.width = (int) (screenWidth);
		params.height = (int) (screenHeight * 0.2);
		logoLayout.setLayoutParams(params);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);

		// set connect logo size
		params = connect_logo.getLayoutParams();
		params.width = (int) (screenWidth / 2);
		params.height = (int) (screenWidth / 2);
		connect_logo.setLayoutParams(params);

		params = connectLayout.getLayoutParams();
		params.width = (int) (screenWidth);
		params.height = (int) (screenHeight * 0.45);
		connectLayout.setLayoutParams(params);

		params = bottomLayout.getLayoutParams();
		params.width = (int) screenWidth;
		params.height = (int) (screenWidth / 2);
	}

	// connection image (rotate and stuff)
	private void connection_image() {
		connect_img = (ImageView) findViewById(R.id.connect_logo);
		final Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate_picture);
		connect_img.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				// if (flag_connect) {
				// flash();
				// flag_connect = true;
				// }
				connect_img.startAnimation(rotate);
				connect_img.setEnabled(false);

				new android.os.Handler().postDelayed(new Runnable() {
					public void run() {
						join.performClick();
					}
				}, 2000);
			}
		});

		connect_success = (Button) findViewById(R.id.connect_success);
		connect_failure = (Button) findViewById(R.id.connect_fail);
		connect_success.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				connect_img.setImageResource(R.drawable.icon_connect_success);
				connect_img.clearAnimation();
			}
		});
		connect_failure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				connect_img.setImageResource(R.drawable.icon_connect_fail);
				connect_img.clearAnimation();			
			}
		});
	}

	// color flashes
	private void flash() {
		ColorDrawable[] color = { new ColorDrawable(0xFFFFEB3B), new ColorDrawable(0xFFF57F17) };
		TransitionDrawable trans = new TransitionDrawable(color);
		LinearLayout layout = (LinearLayout) findViewById(R.id.controllerLayout);
		layout.setBackground(trans);
		trans.startTransition(1000);

		new android.os.Handler().postDelayed(new Runnable() {
			public void run() {
				LinearLayout layout = (LinearLayout) findViewById(R.id.controllerLayout);
				ColorDrawable[] color2 = { new ColorDrawable(0xFFF57F17), new ColorDrawable(0xFFFFEB3B) };
				TransitionDrawable trans2 = new TransitionDrawable(color2);
				layout.setBackground(trans2);
				trans2.startTransition(1000);
			}
		}, 1000);

		if (!controller_clicked) {
			new android.os.Handler().postDelayed(new Runnable() {
				public void run() {
					flash();
				}
			}, 2000);
		}

	}

}

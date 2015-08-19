package tw.futureinsighters.client;

import java.util.List;

import org.allseenaliance.alljoyn.AllJoynService;
import org.allseenaliance.alljoyn.CafeApplication;
import org.allseenaliance.alljoyn.Observable;
import org.allseenaliance.alljoyn.Observer;

import com.technalt.serverlessCafe.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Animation.AnimationListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements Observer {

	// main menu
	private double screenWidth, screenHeight;

	// alljoyn

	private Button join;
	private Button stop;
	private Button leave;
	private Button sendjson;

	private CafeApplication mChatApplication = null;
	private TextView preview;
	private EditText edit;

	private static final int HANDLE_APPLICATION_QUIT_EVENT = 0;
	private static final int HANDLE_CHANNEL_STATE_CHANGED_EVENT = 1;
	private static final int HANDLE_ALLJOYN_ERROR_EVENT = 2;
	private int retry_count = 0;



	/* client to TV CMD */
	private final String CONTROLLER_CMD_CONN_CONN = "ISTVSconn";
	private final String CONTROLLER_CMD_CONN_SETNAME = "ISTVSsetname";
	private final String CONTROLLER_CMD_CONN_FINISHCONN = "ISTVSfinishconn";
	private final String CONTROLLER_CMD_CONN_DISCONN = "ISTVSdisconn";
	final private String CONTROLLER_NOTIFICATION_SYSNOTI = "ISTVSsysnoti";

	/* TV to client CMD */
	private final String TV_RESPONSE_CHANNEL = "SVTSIcurchannel";
	private final String TV_RESPONSE_CHANNEL_INFO = "SVTSIcurchannelinfo";
	private final String TV_RESPONSE_APPSLIST_ON = "SVTSIappsliston";
	private final String TV_RESPONSE_APPSLIST_OFF = "SVTSIappslistoff";

	// icons
	ImageView controllerImage, connectImage, settingsImage, helpImage;
	Boolean flag_connect = true;
	Boolean controller_connected_clicked = false;
	Button connect_success, connect_failure;
	int imagesToShow[],imageCount = 0;
	// boolean found = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		

		// main menu
		connection_image();

		// controller image
		controllerImage = (ImageView) findViewById(R.id.controllerImage);
		controllerImage.setEnabled(false);
		
		// click listeners
		controllerImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				controller_connected_clicked = true;
				Intent intent = new Intent(MainActivity.this, ControllerActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.fadein, R.anim.fadeout);

			}
		});

		settingsImage = (ImageView) findViewById(R.id.settingsImage);
		// click listeners
		settingsImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// controller_clicked = true;
				Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
				startActivity(intent);
			}
		});

		/* notification catcher listener register */
		LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("SystemNotifications"));

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
							if (retry_count > 21) {
								retry_count = 0;
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
		userDisconn();
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

		if (messager.contains(TV_RESPONSE_CHANNEL_INFO)) {
			Intent intent = new Intent("channelInfo");
			intent.putExtra("number", messager.substring(messager.indexOf(" --") + 3, messager.indexOf(" ---")));
			intent.putExtra("name", messager.substring(messager.indexOf(" ---") + 4, messager.indexOf(" ----")));
			intent.putExtra("intro", messager.substring(messager.indexOf(" ----") + 5, messager.indexOf(" -----")));
			intent.putExtra("isAds", messager.substring(messager.indexOf(" -----") + 6));
			this.sendBroadcast(intent);
		} else if (messager.contains(TV_RESPONSE_APPSLIST_ON)) {
			Intent intent = new Intent("other");
			intent.putExtra("name", "AppsListIsOn");
			this.sendBroadcast(intent);
		} else if (messager.contains(TV_RESPONSE_APPSLIST_OFF)) {
			Intent intent = new Intent("other");
			intent.putExtra("name", "AppsListIsOff");
			this.sendBroadcast(intent);
		}

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
//		final LinearLayout logoLayout = (LinearLayout) findViewById(R.id.logoLayout);
//		final LinearLayout connectLayout = (LinearLayout) findViewById(R.id.connectLayout);
//		final LinearLayout bottomLayout = (LinearLayout) findViewById(R.id.bottomLayout);
//		final LinearLayout controllerLayout = (LinearLayout) findViewById(R.id.controllerLayout);
//		final LinearLayout helpLayout = (LinearLayout) findViewById(R.id.helpLayout);
//		final ImageView connect_logo = (ImageView) findViewById(R.id.connect_logo);
//
//		LayoutParams params = logoLayout.getLayoutParams();
//		params.width = (int) (screenWidth);
//		params.height = (int) (screenHeight * 0.2);
//		logoLayout.setLayoutParams(params);
//		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//				LinearLayout.LayoutParams.MATCH_PARENT);
//
//		// set connect logo size
//		params = connect_logo.getLayoutParams();
//		params.width = (int) (screenWidth / 2);
//		params.height = (int) (screenWidth / 2);
//		connect_logo.setLayoutParams(params);
//
//		params = connectLayout.getLayoutParams();
//		params.width = (int) (screenWidth);
//		params.height = (int) (screenHeight * 0.45);
//		connectLayout.setLayoutParams(params);
//
//		params = bottomLayout.getLayoutParams();
//		params.width = (int) screenWidth;
//		params.height = (int) (screenWidth / 2);
	}

	// connection image (rotate and stuff)
	private void connection_image() {
		connectImage = (ImageView) findViewById(R.id.connect_logo);
		final Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate_picture);
		connectImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				connectImage.setImageResource(R.drawable.icon_connecting);
				connectImage.startAnimation(rotate);

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
				connectImage.setImageResource(R.drawable.icon_connect_success);
				connectImage.clearAnimation();
				connectImage.setEnabled(false);
				controllerImage.setEnabled(true);
				flash();				
				userConnectTV();			
			}
		});
		connect_failure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				connectImage.setImageResource(R.drawable.icon_connect_fail);
				connectImage.clearAnimation();
			}
		});
	}

	/* color flashes */

	private void flash() {

		new android.os.Handler().postDelayed(new Runnable() {
			public void run() {							
				int imagesToShow[] = { R.drawable.ic_speaker_phone_white_48dp_2, R.drawable.ic_speaker_phone_white_48dp_3,R.drawable.ic_speaker_phone_white_48dp_4,R.drawable.ic_speaker_phone_white_48dp};
				controllerImage.setImageResource(imagesToShow[imageCount]);
				imageCount = (imageCount + 1) % 4;
				flash();
			}
		}, 800);	
	}
	
	/* Connect TV and Set */
	private void userConnectTV() {
		//mChatApplication.newLocalUserMessage(CONTROLLER_CMD_CONN_CONN); /* temporary unused */
		mChatApplication.newLocalUserMessage(new SettingsManager(getApplicationContext()).getCMD());
		
		//mChatApplication.newLocalUserMessage(CONTROLLER_CMD_CONN_FINISHCONN); /* temporary unused */
	}

	private void userDisconn() {
		mChatApplication.newLocalUserMessage(CONTROLLER_CMD_CONN_DISCONN);
	}

	/* notification catcher listener */
	private BroadcastReceiver onNotice = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String pack = intent.getStringExtra("package");
			String title = intent.getStringExtra("title");
			String text = intent.getStringExtra("text");
			String msg = CONTROLLER_NOTIFICATION_SYSNOTI + " -" + pack + " --" + title + " ---" + text;
			mChatApplication.newLocalUserMessage(msg);
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
		}
	};

}

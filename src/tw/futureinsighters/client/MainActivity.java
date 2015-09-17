package tw.futureinsighters.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import tw.futureinsighters.defines.TvcontrollerCMD;
import tw.futureinsighters.defines.TVResponse;

public class MainActivity extends Activity implements Observer {

	// main menu
	private double screenWidth, screenHeight;

	// alljoyn
	private Button join;
	private Button stop;
	private Button leave;	

	private CafeApplication mChatApplication = null;
	private TextView preview, controller_text;
	
	private static final int HANDLE_APPLICATION_QUIT_EVENT = 0;
	private static final int HANDLE_CHANNEL_STATE_CHANGED_EVENT = 1;
	private static final int HANDLE_ALLJOYN_ERROR_EVENT = 2;
	private int retry_count = 0;

	// Transfer image
	private int imageTransitCount = 0;
	String encodedImage = null;
	
	// icons
	ImageView controllerImage, connectImage, settingsImage, helpImage;
	Boolean flag_connect = true;
	Boolean controller_connected_clicked = false;
	Button connect_success, connect_failure;
	int imagesToShow[], imageCount = 0;


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

		// click listeners
		helpImage = (ImageView) findViewById(R.id.helpImage);
		helpImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, ImageviewActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.fadein, R.anim.fadeout);

			}
		});

		// click listeners
		settingsImage = (ImageView) findViewById(R.id.settingsImage);		
		settingsImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {				
				Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.fadein, R.anim.fadeout);
			}
		});

		/* notification catcher listener register */
		LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("SystemNotifications"));

		// alljoyn
		stop = new Button(this);
		join = new Button(this);
		preview = (TextView) findViewById(R.id.textpreview);
		leave = new Button(this);

		join.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final Dialog dialog = new Dialog(MainActivity.this);
				dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);

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

				new android.os.Handler().postDelayed(new Runnable() {
					public void run() {
						connect_success.performClick();
					}
				}, 1000);

			}
		});

		stop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mChatApplication.hostStopChannel();
			}
		});

		leave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mChatApplication.useLeaveChannel();
				mChatApplication.useSetChannelName("Not set");
			}
		});

		mChatApplication = (CafeApplication) getApplication();
		mChatApplication.checkin();
		mChatApplication.addObserver(this);

		updateChannelState();
	}

	public void onDestroy() {
		userDisconn();
		mChatApplication = (CafeApplication) getApplication();
		mChatApplication.deleteObserver(this);
		mChatApplication.hostStopChannel();
		mChatApplication.useLeaveChannel();
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

		if (messager.contains(TVResponse.CUR_CHANNEL_INFO)) {
			Intent intent = new Intent("curChannelInfo");
			intent.putExtra("number", messager.substring(messager.indexOf(" *") + 2, messager.indexOf(" **")));
			intent.putExtra("channelName", messager.substring(messager.indexOf(" **") + 3, messager.indexOf(" ***")));
			intent.putExtra("programName", messager.substring(messager.indexOf(" ***") + 4, messager.indexOf(" ****")));
			intent.putExtra("programDescription",
					messager.substring(messager.indexOf(" ****") + 5, messager.indexOf(" *****")));
			intent.putExtra("isAds", messager.substring(messager.indexOf(" *****") + 6, messager.indexOf(" ******")));
			this.sendBroadcast(intent);
		} else if (messager.contains(TVResponse.CHANNEL_INFO)) {
			Intent intent = new Intent("channelInfo");
			intent.putExtra("number", messager.substring(messager.indexOf(" *") + 2, messager.indexOf(" **")));
			intent.putExtra("channelName", messager.substring(messager.indexOf(" **") + 3, messager.indexOf(" ***")));
			intent.putExtra("programName", messager.substring(messager.indexOf(" ***") + 4, messager.indexOf(" ****")));
			intent.putExtra("programDescription",
					messager.substring(messager.indexOf(" ****") + 5, messager.indexOf(" *****")));
			intent.putExtra("isAds", messager.substring(messager.indexOf(" *****") + 6, messager.indexOf(" ******")));
			this.sendBroadcast(intent);
		} else if (messager.contains(TVResponse.APPSLIST_ON)) {
			Intent intent = new Intent("other");
			intent.putExtra("name", "AppsListIsOn");
			this.sendBroadcast(intent);
		} else if (messager.contains(TVResponse.APPSLIST_OFF)) {
			Intent intent = new Intent("other");
			intent.putExtra("name", "AppsListIsOff");
			this.sendBroadcast(intent);
		} else if (messager.contains(TVResponse.IMAGE_TRANSFER_START)) {
			// begin the transport of image
			encodedImage = "";
			imageTransitCount = Integer
					.parseInt(messager.substring(messager.indexOf(TVResponse.IMAGE_TRANSFER_START) + 12));
		} else if (messager.length() > 100 && messager.substring(0, 90).contains(TVResponse.IMAGE_TRANSFER)) {
			// get image encoded packaages

			encodedImage = encodedImage + (messager.substring(messager.indexOf(TVResponse.IMAGE_TRANSFER) +1+TVResponse.IMAGE_TRANSFER.length()));
			if (--imageTransitCount != 0)
				return;
			// transport finished. start load image.
			try {
				Log.d("Hi", "Gonna decode");
				byte[] decodedByte = Base64.decode(encodedImage, 0);
				encodedImage = ""; // clean memory
				Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
				decodedByte = null; // clean memory				
				saveBitmap(decodedImage);
			} catch (Exception e) {
				Toast.makeText(MainActivity.this, "Cannot get picture", Toast.LENGTH_SHORT).show();
			}
		}else if(messager.contains(TVResponse.TV_OPENED)) {
			controller_connected_clicked = true;
			Intent intent = new Intent(MainActivity.this, TvControllerActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.fadein, R.anim.fadeout);
			
		}
		else if (messager.contains(TVResponse. VIDEOVIEWER_OPENED)){
			Toast.makeText(getApplicationContext(), "VIdeo open", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(MainActivity.this,VideoviewActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		}
		else if (messager.contains(TVResponse. IMAGEVIEWER_OPENED)){
			Intent intent = new Intent(MainActivity.this, ImageviewActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.fadein, R.anim.fadeout);
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
		controller_text  = (TextView) findViewById(R.id.controllerText);
		connect_success.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				connectImage.setImageResource(R.drawable.icon_connect_success);
				connectImage.clearAnimation();
				connectImage.setEnabled(false);
				controllerImage.setEnabled(true);
				controller_text.setText("Let's get started!");
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
				int imagesToShow[] = { R.drawable.ic_speaker_phone_white_48dp_2,
						R.drawable.ic_speaker_phone_white_48dp_3, R.drawable.ic_speaker_phone_white_48dp_4,
						R.drawable.ic_speaker_phone_white_48dp };
				controllerImage.setImageResource(imagesToShow[imageCount]);
				imageCount = (imageCount + 1) % 4;
				flash();
			}
		}, 800);
	}

	/* Connect TV and Set */
	private void userConnectTV() {		
		mChatApplication.newLocalUserMessage(new SettingsManager(getApplicationContext()).getCMD());
	}

	private void userDisconn() {
		mChatApplication.newLocalUserMessage(TvcontrollerCMD.CONN_DISCONN);
	}

	/* notification catcher listener */
	private BroadcastReceiver onNotice = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String pack = intent.getStringExtra("package");
			String title = intent.getStringExtra("title");
			String text = intent.getStringExtra("text");
			String msg = TvcontrollerCMD.SYSNOTI + " -" + pack + " --" + title + " ---" + text;
			mChatApplication.newLocalUserMessage(msg);
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
		}
	};
	
	private void saveBitmap(Bitmap bmp){		
		Date now = new Date();
	    android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
	    String mPath = Environment.getExternalStorageDirectory().toString() + "/image/" + now + ".jpg";
	    Log.d("Pathhhhhh" , mPath);
		FileOutputStream out = null;
		try {
		    out = new FileOutputStream(mPath);
		    bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
		} catch (Exception e) {
		    e.printStackTrace();
		} finally {
		    try {
		        if (out != null) {
		            out.close();
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
		// notify VideoviewActivity
		Intent intent = new Intent("screenshotPath");
		intent.putExtra("screenshotPath", mPath);
		this.sendBroadcast(intent);		
	}
}

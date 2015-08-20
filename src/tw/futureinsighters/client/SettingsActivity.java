package tw.futureinsighters.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.allseenaliance.alljoyn.CafeApplication;

import com.technalt.serverlessCafe.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class SettingsActivity extends Activity implements AbsListView.OnScrollListener {

	/* AllJoyn Controll */
	private CafeApplication mChatApplication = null;


	/* Swipe container */
	SwipeRefreshLayout swipeContainer;

	/* List information */
	public ListView listView;
	public SimpleAdapter adapter;
	private int[] image = { R.drawable.ic_account_circle_black_48dp, R.drawable.ic_wc_black_24dp,
			R.drawable.ic_cake_black_24dp, R.drawable.ic_directions_run_black_48dp, R.drawable.ic_textsms_black_48dp,
			R.drawable.ic_school_black_48dp, R.drawable.ic_account_circle_black_48dp,
			R.drawable.ic_account_circle_black_48dp, R.drawable.ic_account_circle_black_48dp,
			R.drawable.ic_account_circle_black_48dp };
	private String[] settingText = { "Username", "Gender", "Age", "Pace of Life", "Notification", "Interest", "Privacy",
			"Security", "Help", "About us" };
	private String[] settingHint = { "Set your account name", "What's your gender?", "Set your age for channel filter",
			"Customize controller mode based on your life pace!",
			"You can recieve notifications while watching TV! Stay connected with your friends!", "Select the field you are interset in",
			"Privacy settings", "Securtiy??", "Getting confused? Click Here!", "Contact FutureInsighters" };

	private int lastTopValue = 0;
	private ImageView backgroundImage;
	
	private SettingsManager settingsManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		/* initialize SettingsManager */
		settingsManager = new SettingsManager(this);
		/* ------Start AllJoyn Service KEYWORD!!---- */
		mChatApplication = (CafeApplication) getApplication();
		
		swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
		swipeContainer.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						swipeContainer.setRefreshing(false);
						Toast.makeText(SettingsActivity.this, "All your settings have been saved", Toast.LENGTH_SHORT)
								.show();
						settingsBroadcast();
						mChatApplication.newLocalUserMessage(new SettingsManager(getApplicationContext()).getCMD());
					}
				}, 3000);
			}

		});
		// Configure the refreshing colors
		swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
				android.R.color.holo_orange_light, android.R.color.holo_red_light);

		/* ListView */
		listView = (ListView) findViewById(R.id.settingListView);
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < settingText.length; i++) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("image", image[i]);
			item.put("text", settingText[i]);
			item.put("hint", settingHint[i]);
			items.add(item);
		}
		adapter = new SimpleAdapter(this, items, R.layout.activity_settings_listrow,
				new String[] { "image", "text", "hint" },
				new int[] { R.id.settingImage, R.id.settingText, R.id.settingHint });
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
				case 1:
					final EditText input = new EditText(SettingsActivity.this);
					input.setSingleLine(true);
					AlertDialog.Builder userDialog = new AlertDialog.Builder(SettingsActivity.this).setTitle("USERNAME")
							.setMessage("Set your username:").setIcon(R.drawable.ic_face_black_24dp).setView(input)
							.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							settingsManager.setName( input.getText().toString() );
						}
					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Toast.makeText(SettingsActivity.this, "neg", Toast.LENGTH_SHORT).show();
							dialog.cancel();
						}
					});
					input.setHint(settingsManager.getName());
					userDialog.show();
					break;

				case 2:
					AlertDialog.Builder genderDialog = new AlertDialog.Builder(SettingsActivity.this).setTitle("GENDER")
							.setIcon(R.drawable.ic_wc_black_24dp)
							.setPositiveButton("MALE", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							settingsManager.setGender(1);
						}
					}).setNegativeButton("FEMALE", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							settingsManager.setGender(2);
						}
					}).setNeutralButton("OTHER", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							settingsManager.setGender(3);
						}
					});
					genderDialog.show();
					break;

				case 3:
					String[] items = { "Below 12", "12~18", "Above 18" };
					AlertDialog.Builder ageDialog = new AlertDialog.Builder(SettingsActivity.this).setTitle("AGE")
							.setIcon(R.drawable.ic_cake_black_24dp).setSingleChoiceItems(items, settingsManager.getAge(), null)
							.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
							if (position == 1)
								settingsManager.setAge(1);
							else if (position == 2) {
								settingsManager.setAge(2);
							} else
								settingsManager.setAge(3);
						}
					});
					ageDialog.show();
					break;

				case 4:
					String[] pace = { "Slow", "Downshifting", "Average", "Fast" };
					AlertDialog.Builder paceDialog = new AlertDialog.Builder(SettingsActivity.this)
							.setTitle("LIFE OF PACE").setIcon(R.drawable.ic_directions_walk_black_24dp)
							.setSingleChoiceItems(pace, settingsManager.getPace(), null)
							.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
							if (position == 1)
								settingsManager.setPace(1);
							else if (position == 2) {
								settingsManager.setPace( 2);
							} else if (position == 3)
								settingsManager.setPace(3);
							else {
								settingsManager.setPace(4);
							}
						}
					});
					paceDialog.show();
					break;

				case 5:
					String[] notification = { "ON", "OFF" };
					AlertDialog.Builder notifDialog = new AlertDialog.Builder(SettingsActivity.this)
							.setTitle("NOTIFICATION").setIcon(R.drawable.ic_sms_failed_black_24dp)
							.setSingleChoiceItems(notification, (settingsManager.getNotification())?0:1, null)
							.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
							if (position == 0)
								settingsManager.setNotification(true);
							else
								settingsManager.setNotification(false);
						}
					});
					notifDialog.show();
					break;

				case 6:
					String[] preferField = { "Art", "Literature", "Science", "Technology", "Random" };
					AlertDialog.Builder preferDialog = new AlertDialog.Builder(SettingsActivity.this)
							.setTitle("NOTIFICATION").setIcon(R.drawable.ic_school_black_48dp)
							.setSingleChoiceItems(preferField, settingsManager.getField(), null)
							.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
							settingsManager.setField(position);
						}
					});
					preferDialog.show();
					break;

				}

			}

		});

		// inflate custom header and attach it to the list
		LayoutInflater inflater = getLayoutInflater();
		ViewGroup header = (ViewGroup) inflater.inflate(R.layout.activity_settings_header, listView, false);
		listView.addHeaderView(header, null, false);

		// we take the background image and button reference from the header
		backgroundImage = (ImageView) header.findViewById(R.id.listHeaderImage);
		listView.setOnScrollListener(this);

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		Rect rect = new Rect();
		backgroundImage.getLocalVisibleRect(rect);
		if (lastTopValue != rect.top) {
			lastTopValue = rect.top;
			backgroundImage.setY((float) (rect.top / 2.0));
		}
	}

	public void settingsBroadcast() { /* deprecated */
		String settingsCMD, gender, age, pace, notification, preferField;
		gender = Integer.toString(settingsManager.getGender());
		age = Integer.toString(settingsManager.getAge());
		pace = Integer.toString(settingsManager.getPace());
		notification = settingsManager.getNotification()?"1":"2";
		preferField = Integer.toString(settingsManager.getField());
//		settingsCMD = SETTINGS_CMD_INFO + " -" + settingsManager.getName() + " --" + gender + age + pace + notification + preferField;
		Toast.makeText(SettingsActivity.this, settingsManager.getCMD(), Toast.LENGTH_SHORT).show();

	}

}

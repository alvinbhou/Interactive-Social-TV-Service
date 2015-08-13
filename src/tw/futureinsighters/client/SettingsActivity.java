package tw.futureinsighters.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.technalt.serverlessCafe.R;

import android.R.string;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class SettingsActivity extends Activity implements AbsListView.OnScrollListener {
	public ListView listView;
	public SimpleAdapter adapter;
	private int[] image = { R.drawable.user, R.drawable.gender, R.drawable.age, R.drawable.pace,
			R.drawable.notification, R.drawable.user, R.drawable.user, R.drawable.user, R.drawable.user,
			R.drawable.user };
	private String[] settingText = { "Username", "Gender", "Age", "Pace of Life", "Notification", "Parents", "Privacy",
			"Security", "Help", "About us" };
	private String[] settingHint = { "Set your account name", "What's your gender?", "Set your age for channel filter",
			"Customize controller mode based on your life pace!", "Stay connected with friends!", "Parent Mode",
			"Privacy settings", "Securtiy??", "Getting confused? Click Here!", "Contact FutureInsighters" };

	private static final int MAX_ROWS = 50;
	private int lastTopValue = 0;
	private ImageView backgroundImage;
	
	private class UserInfo{
		public String name;
		public int gender = 0;  
		/* male 1, female 2, other 3 */
		public int age = 0;
		/* Below 12 , 12 - 18, 18+ */
		public int pace = 0;
		/* 1,2,3,4 */
		public boolean notification = true; 
	}
	
	private UserInfo userInfo = new UserInfo();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
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
					AlertDialog.Builder userDialog = new AlertDialog.Builder(SettingsActivity.this)
					.setTitle("USERNAME")
					.setMessage("Set your username:")
					.setIcon(R.drawable.ic_face_black_24dp)											
					.setView(input)
					.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {							 
							userInfo.name = input.getText().toString();
							Toast.makeText(SettingsActivity.this, userInfo.name, Toast.LENGTH_SHORT).show();
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Toast.makeText(SettingsActivity.this, "neg", Toast.LENGTH_SHORT).show();
							dialog.cancel();
						}
					});					
					userDialog.show();
					break;
					
				case 2:
					AlertDialog.Builder genderDialog = new AlertDialog.Builder(SettingsActivity.this)
					.setTitle("GENDER")					
					.setIcon(R.drawable.ic_wc_black_24dp)					
					.setPositiveButton("MALE", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {							 
							userInfo.gender = 1;
						}
					})					
					.setNegativeButton("FEMALE", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							userInfo.gender = 2;							
						}
					})	
					.setNeutralButton("OTHER", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							userInfo.gender = 3;							
						}
					});	
					genderDialog.show();
					break;
					
				case 3:
					String[] items = {"Below 12","12~18","Above 18"};
					AlertDialog.Builder ageDialog = new AlertDialog.Builder(SettingsActivity.this)
					.setTitle("AGE")
					.setMessage("There may be a channel filter.")
					.setIcon(R.drawable.ic_cake_black_24dp)				
					.setSingleChoiceItems(items, 0, null)
					.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {						
			                int position = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
			                if(position == 1)
			                	userInfo.age = 1;
			                else if(position == 2){			                	
			                	userInfo.age = 2;				                	
			                }			                	
			                else
			                	userInfo.age = 3;	               	
						}
					});
					ageDialog.show();					
					break;
					
				case 4:
					String[] pace = {"Slow","Downshifting","Average","Fast"};
					AlertDialog.Builder paceDialog = new AlertDialog.Builder(SettingsActivity.this)
					.setTitle("LIFE OF PACE")	
					.setMessage("Different settings may differ the control delay gap.")
					.setIcon(R.drawable.ic_directions_walk_black_24dp)				
					.setSingleChoiceItems(pace, 0, null)
					.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {						
			                int position = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
			                if(position == 1)
			                	userInfo.pace = 1;
			                else if(position == 2){			                	
			                	userInfo.pace = 2;				                	
			                }			                	
			                else if(position == 3)
			                	userInfo.pace = 3;
			                else {
			                	userInfo.pace = 4;
							}
						}
					});
					paceDialog.show();					
					break;
					
				case 5:
					String[] notification = {"ON","OFF"};
					AlertDialog.Builder notifDialog = new AlertDialog.Builder(SettingsActivity.this)
					.setTitle("NOTIFICATION")				
					.setIcon(R.drawable.ic_sms_failed_black_24dp)	
					.setMessage("You can recieve notifications while watching TV!")
					.setSingleChoiceItems(notification, 0, null)
					.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {						
			                int position = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
			                if(position == 1)
			                	userInfo.notification= true;
			                else
			                	userInfo.notification= false;
						}
					});
					notifDialog.show();					
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

}

package com.technalt.serverlessCafe;

import com.technalt.serverless.CafeApplication;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsActivity extends Activity {
	public ListView listView;
	public SimpleAdapter adapter;
	private int[] image = { R.drawable.user, R.drawable.gender, R.drawable.age, R.drawable.pace,
			R.drawable.notification };
	private String[] settingText = { "Username", "Gender", "Age", "Life Pace", "Notification" };
	private String[] settingHint = { "Set your account name", "What's your gender?", "Set your age for channel filter",
			"Customize controller mode based on your life pace!", "Stay connected with friends!" };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		listView = (ListView) findViewById(R.id.settingListView);
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < image.length; i++) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("image", image[i]);
			item.put("text", settingText[i]);
			item.put("hint", settingHint[i]);
			items.add(item);
		}
		adapter = new SimpleAdapter(this, items, R.layout.activity_settings_listrow, new String[]{"image","text","hint"},
				new int[]{R.id.settingImage,R.id.settingText,R.id.settingHint});
		listView.setAdapter(adapter);
		
		
	}
	
}

package tw.futureinsighters.client;

import com.technalt.serverlessCafe.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.allseenaliance.alljoyn.CafeApplication;

public class SettingsActivity extends Activity implements AbsListView.OnScrollListener {
	public ListView listView;
	public SimpleAdapter adapter;
	private int[] image = { R.drawable.user, R.drawable.gender, R.drawable.age, R.drawable.pace,
			R.drawable.notification, R.drawable.user, R.drawable.user, R.drawable.user, R.drawable.user,
			R.drawable.user};
	private String[] settingText = { "Username", "Gender", "Age", "Life Pace", "Notification", "Parents", "Privacy",
			"Security", "Help", "About us" };
	private String[] settingHint = { "Set your account name", "What's your gender?", "Set your age for channel filter",
			"Customize controller mode based on your life pace!", "Stay connected with friends!", "Parent Mode",
			"Privacy settings", "Securtiy??", "Getting confused? Click Here!", "Contact FutureInsighters" };

	private static final int MAX_ROWS = 50;
	private int lastTopValue = 0;
	private ImageView backgroundImage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
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

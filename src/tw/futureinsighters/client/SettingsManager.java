package tw.futureinsighters.client;

import java.util.ArrayList;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
	private final String SETTINGS_CMD_INFO = "ISTVSsetting";

	private Context mContext;
	private SharedPreferences userInfo;

	public SettingsManager(Context context) {
		mContext = context;
		userInfo = mContext.getSharedPreferences("userInfo", 0);
	}

	/* setters */

	public void setName(String name) {
		SharedPreferences.Editor editor = userInfo.edit();
		editor.putString("name", name);
		editor.commit();
	}

	public void setAge(int age) {
		SharedPreferences.Editor editor = userInfo.edit();
		editor.putInt("age", age);
		editor.commit();
	}

	public void setAge(String age) {
		SharedPreferences.Editor editor = userInfo.edit();
		try {
			editor.putInt("age", Integer.parseInt(age));
		} catch (Exception e) {
		}
		;
		editor.commit();
	}

	/* gender (1 for male, 2 for female, 3 for others) */
	public void setGender(int gender) {
		SharedPreferences.Editor editor = userInfo.edit();
		editor.putInt("gender", gender);
		editor.commit();
	}

	public void setGender(String gender) {
		SharedPreferences.Editor editor = userInfo.edit();
		try {
			editor.putInt("gender", Integer.parseInt(gender));
		} catch (Exception x) {
		}
		;
		editor.commit();
	}

	public void setPace(int pace) {
		SharedPreferences.Editor editor = userInfo.edit();
		editor.putInt("pace", pace);
		editor.commit();
	}

	/* set whether notification should be turned on */
	public void setNotification(Boolean notification) {
		SharedPreferences.Editor editor = userInfo.edit();
		editor.putBoolean("notification", notification);
		editor.commit();
	}

	public void setField(int field) {
		SharedPreferences.Editor editor = userInfo.edit();
		editor.putInt("field", field);
		editor.commit();
	}

	/* getters */

	public String getName() {
		return this.userInfo.getString("name", "No name");
	}

	public int getAge() {
		return this.userInfo.getInt("age", 0);
	}

	public int getGender() {
		return this.userInfo.getInt("gender", 0);
	}

	public int getPace() {
		return this.userInfo.getInt("pace", 0);
	}

	public Boolean getNotification() {
		return this.userInfo.getBoolean("notification", true);
	}

	public int getField() {
		return this.userInfo.getInt("field", 0);
	}

	/* command generator */
	public String getCMD() {
		return SETTINGS_CMD_INFO + " -" + this.getName() + " --" + Integer.toString(this.getAge())
				+ Integer.toString(this.getGender()) + Integer.toString(this.getPace())
				+ (this.getNotification() ? "1" : "2") + Integer.toString(this.getField());
	}
}

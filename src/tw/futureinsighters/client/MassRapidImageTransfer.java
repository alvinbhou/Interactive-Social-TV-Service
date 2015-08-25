package tw.futureinsighters.client;

import org.allseenaliance.alljoyn.CafeApplication;

import android.widget.Toast;

public class MassRapidImageTransfer {

	/* AllJoyn Controll */
	private final String IMAGEVIEWER_SHOW = "ISTVSIMGst";

	private CafeApplication mChatApplication = null;
	private final int PACKAGE_SIZE = 100000;
	private String encode, cmd;
	private int packageNum, codeLength, packageCount = 0, i;

	public MassRapidImageTransfer(CafeApplication cafeIn, String encode) {
		this.encode = encode;
		this.mChatApplication = cafeIn;
	}

	public void exe() {
		codeLength = encode.length();

		packageNum = codeLength / PACKAGE_SIZE;

		cmd = IMAGEVIEWER_SHOW + Integer.toString(packageNum + 1);
		mChatApplication.newLocalUserMessage(cmd);

		for (i = 0; i < packageNum; ++i) {
			new android.os.Handler().postDelayed(new Runnable() {
				public void run() {
					cmd = encode.substring(PACKAGE_SIZE * packageCount, PACKAGE_SIZE * (packageCount + 1));
					// Toast.makeText(ImageviewActivity.this,
					// Integer.toString(i) + "**" +
					// Integer.toString(tmp.length()),
					// Toast.LENGTH_SHORT).show();
					mChatApplication.newLocalUserMessage("ISTVSIMGs " + cmd);
					packageCount++;
				}
			}, 300 * i + 10);
		}

		new android.os.Handler().postDelayed(new Runnable() {
			public void run() {

				cmd = encode.substring(PACKAGE_SIZE * packageCount);
				// Toast.makeText(ImageviewActivity.this, Integer.toString(i) +
				// "**" + Integer.toString(tmp.length()),
				// Toast.LENGTH_SHORT).show();
				mChatApplication.newLocalUserMessage("ISTVSIMGs " + cmd);
			}
		}, 400 * (i + 1) + 1000);

	}

}

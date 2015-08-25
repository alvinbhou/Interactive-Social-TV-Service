package tw.futureinsighters.client;

import java.io.ByteArrayOutputStream;

import org.allseenaliance.alljoyn.CafeApplication;

import com.technalt.serverlessCafe.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ImageviewActivity extends Activity {

	private static int RESULT_LOAD_IMAGE = 1;
	private CafeApplication mChatApplication = null;
	int i, package_num;
	private String tmp, encode, cmd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_imageview);

		/* ------Start AllJoyn Service KEYWORD!!---- */
		mChatApplication = (CafeApplication) getApplication();

		Button buttonLoadImage = (Button) findViewById(R.id.buttonLoadPicture);
		buttonLoadImage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Intent i = new Intent(Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

				startActivityForResult(i, RESULT_LOAD_IMAGE);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();

			ImageView imageView = (ImageView) findViewById(R.id.imgView);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			Bitmap bitmap = BitmapFactory.decodeFile(picturePath, options);
			// imageView.setImageBitmap(bitmap);

			encode = encodeTobase64(bitmap);
			TextView text = (TextView) findViewById(R.id.text);

			new MassRapidImageTransfer(mChatApplication, encode).exe();
			
			text.setText(Integer.toString(encode.length()));
			// mChatApplication.newLocalUserMessage("222");

			// mChatApplication.newLocalUserMessage(cmd);
			// mChatApplication.newLocalUserMessage("5");
			// text.setText(Integer.toString(encode.length()));
			// Toast.makeText(ImageviewActivity.this, code,
			// Toast.LENGTH_LONG).show;
			// imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

		}

	}

	public static String encodeTobase64(Bitmap image) {
		Bitmap immagex = image;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] b = baos.toByteArray();
		String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

		// Log.e("LOOK", imageEncoded);
		return imageEncoded;
	}

	public static Bitmap decodeBase64(String input) {
		byte[] decodedByte = Base64.decode(input, 0);
		return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
	}

}

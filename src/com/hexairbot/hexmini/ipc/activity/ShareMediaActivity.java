package com.hexairbot.hexmini.ipc.activity;

import java.io.File;

import com.hexairbot.hexmini.FeedbackActivity;
import com.hexairbot.hexmini.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;

public class ShareMediaActivity extends Activity {

	private String media_path = null;
	private String media_type = null;
	private boolean actionInitFirst = false;
	private ImageView selectimg = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_media);
		initActionBar();
		Intent intent = getIntent();
		media_path = intent.getStringExtra("media_path");
		media_type = intent.getStringExtra("media_type");

		selectimg = (ImageView) findViewById(R.id.selectimg);
		File file = new File(media_path);
		if (file.exists()) {
			Bitmap bm = BitmapFactory.decodeFile(media_path);
			selectimg.setImageBitmap(bm);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.sharemenu, menu);
		return true;

	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.sharebtn:
			SharePhoto(media_path, media_type, ShareMediaActivity.this);
			break;
		case android.R.id.home:
			ShareMediaActivity.this.finish();
			break;
		case R.id.delbtn:
			new AlertDialog.Builder(ShareMediaActivity.this)
				.setTitle(R.string.delbtnname)
//				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage(R.string.del_text)
				.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						File file = new File(media_path);
						deleteFile(file);
						dialog.dismiss();
						ShareMediaActivity.this.finish();
					}
				})
				.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
					}
				}).show();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initActionBar() {
		ActionBar actionBar = this.getActionBar();
		actionBar.setBackgroundDrawable(this.getResources().getDrawable(
				R.drawable.bar_top));
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		if (!actionInitFirst) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			int flags = ActionBar.DISPLAY_HOME_AS_UP
					& ActionBar.DISPLAY_SHOW_HOME;
			int change = actionBar.getDisplayOptions() ^ flags;
			actionBar.setDisplayOptions(change, flags);
		}
		actionBar.setTitle("");
	}

	public void SharePhoto(String mediaUri, String type, final Activity activity) {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		File file = new File(mediaUri);
		shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		shareIntent.setType(type);

		startActivity(Intent.createChooser(shareIntent, activity.getTitle()));
	}

	public void deleteFile(File file) {
		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
			} else if (file.isDirectory()) {
				File files[] = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					deleteFile(files[i]);
				}
			}
			file.delete();
		}
	}

}

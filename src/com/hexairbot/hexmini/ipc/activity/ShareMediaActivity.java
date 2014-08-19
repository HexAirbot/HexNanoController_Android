package com.hexairbot.hexmini.ipc.activity;

import java.io.File;

import com.hexairbot.hexmini.R;
import com.vmc.ipc.util.MediaUtil;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

public class ShareMediaActivity extends Activity {

	private Button sharebtn = null;
	private boolean actionInitFirst = false;
	private ImageView selectimg = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_media);
		initActionBar();
		Intent intent = getIntent();
		final String media_path = intent.getStringExtra("media_path");
		final String media_type = intent.getStringExtra("media_type");
		
		sharebtn = (Button)findViewById(R.id.sharebtn);
		sharebtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SharePhoto(media_path, media_type, ShareMediaActivity.this);
			}
		});
		
		selectimg = (ImageView)findViewById(R.id.selectimg);
		File file = new File(media_path);
		if(file.exists()){
			Bitmap bm = BitmapFactory.decodeFile(media_path);
			selectimg.setImageBitmap(bm);
		}
	} 
	
	public void SharePhoto(String mediaUri,String type,final Activity activity) { 
	    Intent shareIntent = new Intent(Intent.ACTION_SEND); 
	    File file = new File(mediaUri);
	    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		shareIntent.setType(type);
	    
	    startActivity(Intent.createChooser(shareIntent, activity.getTitle())); 
	}
	
	private void initActionBar() {
		ActionBar actionBar = this.getActionBar();
		actionBar.setBackgroundDrawable(this.getResources().getDrawable(
				R.drawable.barre_haut));
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		if (!actionInitFirst) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			int flags = ActionBar.DISPLAY_HOME_AS_UP
					& ActionBar.DISPLAY_SHOW_HOME;
			int change = actionBar.getDisplayOptions() ^ flags;
			actionBar.setDisplayOptions(change, flags);
		}
//		actionBar.setTitle(R.string.gallery_photo_title);

	}
	
}

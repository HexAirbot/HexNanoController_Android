//package com.hexairbot.hexmini.ipc.activity;
//
//import java.io.File;
//import com.hexairbot.hexmini.R;
//import com.vmc.ipc.util.MediaUtil;
//
//import android.annotation.SuppressLint;
//import android.app.ActionBar;
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Matrix;
//import android.graphics.PointF;
//import android.media.ThumbnailUtils;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.util.DisplayMetrics;
//import android.util.FloatMath;
//import android.util.Log;
//import android.util.TypedValue;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.View.OnTouchListener;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.ImageView;
//
//public class ShareMediaActivity extends Activity implements OnTouchListener{
//
//	private String media_path = null;
//	private String media_type = null;
//	private boolean actionInitFirst = false;
//	private ImageView selectimg = null;
//	private int media_type_int;
//	private long file_id;
//	private Button btnplay = null;
//
//	private Bitmap bitmap;
//	private Matrix matrix = new Matrix();
//	private Matrix savedMatrix = new Matrix();
//    static final int NONE = 0; 	
//    static final int DRAG = 1; 	
//    static final int ZOOM = 2; 	
//    int mode = NONE; 
//    PointF start = new PointF(); 
//    PointF mid = new PointF(); 
//    float oldDist = 1f; 
//	private DisplayMetrics dm;
//	
//	private float max_scale_times = 5;
//	private float min_scale_times = 0.3f;	
//    
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.share_media);
//				
//		Intent intent = getIntent();
//		media_path = intent.getStringExtra("media_path");
//		media_type = intent.getStringExtra("media_type");
//		media_type_int = intent.getIntExtra("media_type_int", -1);
//		file_id = intent.getLongExtra("file_id", -1);
//
//		selectimg = (ImageView) findViewById(R.id.selectimg);
//		btnplay = (Button)findViewById(R.id.btnplay);
//		
//		initActionBar();
//		
//		dm = new DisplayMetrics();
//		getWindowManager().getDefaultDisplay().getMetrics(dm);
//		
//		if (media_type_int == MediaUtil.MEDIA_TYPE_VIDEO) {			
//			selectimg.setImageBitmap(getVideoThumbnail(media_path, dm.widthPixels, dm.heightPixels,  
//	                MediaStore.Images.Thumbnails.MICRO_KIND));
//			
//			btnplay.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					Intent intent = new Intent();
//					intent.setAction(android.content.Intent.ACTION_VIEW);
//					intent.setDataAndType(Uri.parse(media_path), media_type);
//					ShareMediaActivity.this.startActivity(intent);
//				}
//			});
//		}else {
//			File file = new File(media_path);
//			if (file.exists()) {
//				bitmap = BitmapFactory.decodeFile(media_path);
//				int width = bitmap.getWidth();
//				int height = bitmap.getHeight();
//
//				int newWidth = dm.widthPixels;			
//				int newHeight = dm.heightPixels - getActionBarHeight();				
//				float scaleWidth = ((float) newWidth) / width;		
//				float scaleHeight = ((float) newHeight) / height;	
//	
//				Matrix tmp_matrix = new Matrix();
//			
//				tmp_matrix.postScale(scaleWidth, scaleHeight);
//				bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, tmp_matrix, true);
//
//				selectimg.setImageBitmap(bitmap);//selectimg.seta
//				selectimg.setOnTouchListener((OnTouchListener) this);			
//				selectimg.setLongClickable(true);
//				
//				btnplay.setVisibility(View.GONE);
//			}
//		}
//	
//	}
//	
//	private int getActionBarHeight() {
//	    int actionBarHeight = getActionBar().getHeight();
//	    if (actionBarHeight != 0)
//	        return actionBarHeight;
//	    final TypedValue tv = new TypedValue();
//	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//	        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
//	            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
//	    } else if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
//	        actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
//	    return actionBarHeight;
//	}
//	
//	private Bitmap getVideoThumbnail(String videoPath, int width, int height,  
//	            int kind) {  
//	        Bitmap bitmap = null;
//	        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
//	        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,  
//	                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);  
//	        return bitmap;  
//	    }  
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.sharemenu, menu);
//		return true;
//
//	}
//
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.sharebtn:
//			SharePhoto(media_path, media_type, ShareMediaActivity.this);
//			break;
//		case android.R.id.home:
//			ShareMediaActivity.this.finish();
//			break;
//		case R.id.delbtn:
//			String textFormat = getResources().getString(R.string.del_text); 
//			new AlertDialog.Builder(ShareMediaActivity.this)
//					.setTitle(R.string.delbtnname)					
//					.setMessage(String.format(textFormat, 1, ""))
//					.setNegativeButton(R.string.dialog_cancel,
//							new DialogInterface.OnClickListener() {
//								public void onClick(DialogInterface dialog,
//										int whichButton) {
//									dialog.dismiss();
//								}
//							})
//					.setPositiveButton(R.string.dialog_ok,
//							new DialogInterface.OnClickListener() {
//								public void onClick(DialogInterface dialog,
//										int whichButton) {
//									File file = new File(media_path);
//									deleteFile(file);
//									dialog.dismiss();
//									ShareMediaActivity.this.finish();
//								}
//							}).show();					
//			break;
//		default:
//			break;
//		}
//		return super.onOptionsItemSelected(item);
//	}
//
//	private void initActionBar() {
//		ActionBar actionBar = this.getActionBar();
//		actionBar.setBackgroundDrawable(this.getResources().getDrawable(
//				R.drawable.bar_top));
//		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//				WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		if (!actionInitFirst) {
//			actionBar.setDisplayHomeAsUpEnabled(true);
//			int flags = ActionBar.DISPLAY_HOME_AS_UP
//					& ActionBar.DISPLAY_SHOW_HOME;
//			int change = actionBar.getDisplayOptions() ^ flags;
//			actionBar.setDisplayOptions(change, flags);
//		}
//		if (media_type_int == MediaUtil.MEDIA_TYPE_VIDEO) {
//			actionBar.setTitle(R.string.gallery_video_title);
//		} else {
//			actionBar.setTitle(R.string.gallery_photo_title);
//		}
//	}
//
//	public void SharePhoto(String mediaUri, String type, final Activity activity) {
//		Intent shareIntent = new Intent(Intent.ACTION_SEND);
//		File file = new File(mediaUri);
//		shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
//		shareIntent.setType(type);
//
//		startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.app_name)));
//	}
//
//	public void deleteFile(File file) {
//		// if (file.exists()) {
//		// if (file.isFile()) {
//		// file.delete();
//		// } else if (file.isDirectory()) {
//		// File files[] = file.listFiles();
//		// for (int i = 0; i < files.length; i++) {
//		// deleteFile(files[i]);
//		// }
//		// }
//		// file.delete();
//		// }
//		MediaUtil.deleteLocalMedia(ShareMediaActivity.this, media_type_int, file_id);
//	}
//
//	private float spacing(MotionEvent event) { 		
//		float x = event.getX(0) - event.getX(1); 		
//		float y = event.getY(0) - event.getY(1); 
//		
//		return FloatMath.sqrt(x * x + y * y); 
//	} 
//
//	private void midPoint(PointF point, MotionEvent event) { 
//        float x = event.getX(0) + event.getX(1); 
//        float y = event.getY(0) + event.getY(1); 
//        point.set(x / 2, y / 2); 		
//	}
//	
//	@SuppressLint("ClickableViewAccessibility")
//	@Override
//	public boolean onTouch(View v, MotionEvent event) {
//		// TODO Auto-generated method stub
//		selectimg.setScaleType(ImageView.ScaleType.MATRIX);
//		
//		switch(event.getActionMasked()){
//			case MotionEvent.ACTION_DOWN:				
//				matrix.set(selectimg.getImageMatrix());
//				savedMatrix.set(matrix);
//				start.set(event.getX(),event.getY());				
//				mode = DRAG;				
//				break;
//		
//		    case MotionEvent.ACTION_POINTER_DOWN:  //¶àµã´¥¿Ø
//		        oldDist = this.spacing(event);
//		        if (oldDist > 10f) {
//		             savedMatrix.set(matrix);
//		             midPoint(mid,event);
//		             mode = ZOOM;
//		        }
//		        break;
//		
//		    case MotionEvent.ACTION_POINTER_UP:
//		        mode = NONE;
//		        break;
//		
//		    case MotionEvent.ACTION_MOVE:
//		
//	            if (mode == DRAG) {         	
//	                matrix.set(savedMatrix);
//	                matrix.postTranslate(event.getX()-start.x, event.getY()-start.y);	
//	            } else if(mode == ZOOM){ 
//		             float newDist = spacing(event);
//		             if (newDist > 10) {
//		                 matrix.set(savedMatrix);
//		                 float scale = newDist/oldDist;
//		                 //matrix.postScale(scale, scale, mid.x, mid.y);              
//		                 		
//		                 matrix.postScale(scale, scale, mid.x, mid.y);
//		                 float[] values = new float[9];
//		                 matrix.getValues(values);
//		                 if (values[0] > max_scale_times) {
//		                	 matrix.setScale(max_scale_times, max_scale_times, mid.x, mid.y);
//		                 } else if (values[0] < min_scale_times) {
//		                	 matrix.setScale(min_scale_times, min_scale_times, mid.x, mid.y);
//		                 }  
//		             }
//		
//		         }
//		
//		         break;
//		
//		}
//	
//		selectimg.setImageMatrix(matrix);
//		
//		return false;
//	}
//}

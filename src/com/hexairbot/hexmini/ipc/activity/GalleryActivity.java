package com.hexairbot.hexmini.ipc.activity;

import java.io.File;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import com.hexairbot.hexmini.R;
import com.hexairbot.hexmini.ipc.view.OnGalleryItemClick;
import com.hexairbot.hexmini.ipc.view.SquareRelativeLayout;
import com.hexairbot.hexmini.ipc.view.adapter.RemoteMediaAdapter;
import com.vmc.ipc.util.DebugHandler;
import com.vmc.ipc.util.MediaUtil;

public class GalleryActivity extends Activity implements
		LoaderCallbacks<Cursor>, OnItemClickListener {

	private final static String TAG = "GalleryActivity";

	public final static int LOADER_ID_IMAGE = 0;
	public final static int LOADER_ID_VIEDO = 1;
	public final static int LOADER_ID_ALL = 2;

	public final static int BROWSER_TYPE_LOCAL = 100;
	public final static int BROWSER_TYPE_REMOTE = 101;

	private Uri queryUri;
	private String[] mediaColumns;
	private ListAdapter adapter;

	private GridView gridView = null;
	private int mediaType;
	private boolean actionInitFirst = false;
	private int browserType = -1;
	private ArrayList<Long> selectIds = new ArrayList<Long>();

	View actionBarCustomView;

	private int type;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);		
		
		this.setContentView(R.layout.gallery_layout);
		
		Intent intent = this.getIntent();
		type = intent.getIntExtra("type", MediaUtil.MEDIA_TYPE_IMAGE);
		browserType = intent.getIntExtra("browser_type", BROWSER_TYPE_LOCAL);

		gridView = (GridView) this.findViewById(R.id.gridView);
		gridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
		gridView.setMultiChoiceModeListener(new MultiChoiceModeListener());
		//loadData(type);
		   
//		actionBarCustomView = new View(GalleryActivity.this);
//		actionBarCustomView.setBackgroundResource(R.drawable.bar_top);
//		actionBarCustomView.setLayoutParams(new LayoutParams(
//				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		
		initActionBar();
	}

	public class MultiChoiceModeListener implements
			GridView.MultiChoiceModeListener {
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			//mode.setCustomView(actionBarCustomView);
			
			MenuItem item = menu.add("delete");
			item.setIcon(R.drawable.btn_delete);
			selectIds.clear();
			return true;
		}

		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return true;
		}

		public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
			if (item.getTitle().equals("delete")) {
				String textFormat = getResources().getString(R.string.del_text); 
				String language = getResources().getConfiguration().locale.getCountry();
				
				if (language.equals("CN")) {
					new AlertDialog.Builder(GalleryActivity.this)
					.setTitle(R.string.delbtnname)					
					.setMessage(String.format(textFormat, selectIds.size()))					
					.setNegativeButton(R.string.dialog_cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									dialog.dismiss();
								}
							})
					.setPositiveButton(R.string.dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									((OnGalleryItemClick) adapter).delete(selectIds);						
									mode.finish();
									loadData(type);
								}
							}).show();
				} else {
					new AlertDialog.Builder(GalleryActivity.this)
						.setTitle(R.string.delbtnname)					
						.setMessage(String.format(textFormat, selectIds.size(), selectIds.size() > 1 ? "s" : ""))	
						.setNegativeButton(R.string.dialog_cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										dialog.dismiss();
									}
								})
						.setPositiveButton(R.string.dialog_ok,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										((OnGalleryItemClick) adapter).delete(selectIds);						
										mode.finish();
										loadData(type);
									}
								}).show();
				}	
			}
			return true;
		}
		
		public void onDestroyActionMode(ActionMode mode) {
		}

		public void onItemCheckedStateChanged(ActionMode mode, int position,
				long id, boolean checked) {
			int selectCount = gridView.getCheckedItemCount();
			mode.setTitle(GalleryActivity.this.getResources().getString(
					R.string.media_multiChoice_count, selectCount));
			if (checked) {
				selectIds.add(id);
			} else {
				selectIds.remove(id);
			}
		}

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
		if (mediaType == MediaUtil.MEDIA_TYPE_IMAGE) {
			actionBar.setTitle(R.string.gallery_photo_title);
		} else if (mediaType == MediaUtil.MEDIA_TYPE_VIDEO) {
			actionBar.setTitle(R.string.gallery_video_title);
		} else {
			actionBar.setTitle(R.string.gallery_title);
		}
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// Intent intent = new Intent(this, MainActivity.class);
			// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			// startActivity(intent);
			// NavUtils.navigateUpFromSameTask(this);
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void loadData(int type) {
		int id = LOADER_ID_IMAGE;
		if (type == MediaUtil.MEDIA_TYPE_IMAGE) {
			id = LOADER_ID_IMAGE;
			queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			mediaColumns = new String[] { MediaStore.Images.Media.DATA,
					MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE,
					MediaStore.Images.Media.MIME_TYPE, };
		} else if (type == MediaUtil.MEDIA_TYPE_VIDEO) {
			id = LOADER_ID_VIEDO;
			queryUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
			mediaColumns = new String[] { MediaStore.Video.Media.DATA,
					MediaStore.Video.Media._ID, MediaStore.Video.Media.TITLE,
					MediaStore.Video.Media.MIME_TYPE, };
		} else {
			id = LOADER_ID_ALL;
			queryUri= Uri.parse("content://media/external");
			mediaColumns = new String[] { MediaStore.Video.Media.DATA,
					MediaStore.Video.Media._ID, MediaStore.Video.Media.TITLE,
					MediaStore.Video.Media.MIME_TYPE, };
		}
		mediaType = type;
		if (browserType == BROWSER_TYPE_LOCAL) {
			adapter = new MySimpleCursorAdapter(this,
					R.layout.media_item_layout, null, mediaColumns,
					new int[] { R.id.media_thumb }, 0);
			//((MySimpleCursorAdapter) adapter).setViewBinder(new MyViewBinder());
			this.getLoaderManager().initLoader(id, null, this);
		} else if (browserType == BROWSER_TYPE_REMOTE) {
			adapter = new RemoteMediaAdapter(this, mediaType);
		}
		gridView.setAdapter(adapter);
		if (adapter instanceof RemoteMediaAdapter) {
			((RemoteMediaAdapter) adapter).loadData(mediaType);
		}
		gridView.setOnItemClickListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (adapter != null) {
			((OnGalleryItemClick) adapter).destroy();
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		loadData(type);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	private class MyViewBinder implements ViewBinder {

		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			// TODO Auto-generated method stub
			if (columnIndex == 0) {
//				String path = cursor.getString(columnIndex);
//				int id = cursor.getInt(columnIndex + 1);
//				DebugHandler.logd(TAG, "load image:" + id);
//				Options options = new BitmapFactory.Options();
//				options.inDither = false;
//				options.inPreferredConfig = Bitmap.Config.RGB_565;
//				Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(
//						GalleryActivity.this.getContentResolver(), id,
//						MediaStore.Images.Thumbnails.MICRO_KIND, options);
//				if (bitmap == null) {
//					bitmap = MediaUtil.getLocalMediaThumbnail(mediaType, path);
//				}
//				if (bitmap != null) {
//					DebugHandler.logd(TAG, String.format("bitmap (%d,%d)",
//							bitmap.getWidth(), bitmap.getHeight()));
//					((ImageView) view).setImageBitmap(bitmap);
//					bitmap.recycle();
//				} else {
//					((ImageView) view).setBackgroundColor(Color.GRAY);
//				}
//				// bitmap.recycle();
				return true;
			}
			return false;
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle ext) {
		// TODO Auto-generated method stub
		// DebugHandler.logd(TAG, "onCreateLoader----------");

		String selection = MediaStore.Images.Media.DATA + " like ?";
		// String[] bindArgs = new String[]{"%DCIM%"};
		String[] bindArgs = new String[] { "%" + MediaUtil.IPC_DIR + "%" };
		CursorLoader cursorLoader = new CursorLoader(this, queryUri,
				mediaColumns, selection, bindArgs, null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		// TODO Auto-generated method stub
		// DebugHandler.logd(TAG, "onLoadFinished----------");
		((MySimpleCursorAdapter) adapter).swapCursor(arg1);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		// DebugHandler.logd(TAG, "onLoaderReset----------");
		((MySimpleCursorAdapter) adapter).swapCursor(null);
	}

	private class MySimpleCursorAdapter extends SimpleCursorAdapter implements
			OnGalleryItemClick {

		public MySimpleCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			return new SquareRelativeLayout(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			DebugHandler
					.logd(TAG, "MySimpleCursorAdapter.getView: " + position);
			View view = super.getView(position, convertView, parent);
			return view;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			// return super.getItem(position);
			String path = this.getCursor().getString(0);
			return path;
		}

		@Override
		public void onClick(int position, Context context) {
			Intent intent = new Intent();
			String path = (String) getItem(position);
			String intentType = null;
			if (mediaType == MediaUtil.MEDIA_TYPE_IMAGE) {
				intentType = "image/*";
			} else if (mediaType == MediaUtil.MEDIA_TYPE_VIDEO) {
				intentType = "video/*";
			}
			intent.putExtra("media_path", path);
			intent.putExtra("media_type", intentType);
			intent.setClass(GalleryActivity.this, ShareMediaActivity.class);
			GalleryActivity.this.startActivity(intent);
		}

		@Override
		public void delete(ArrayList<Long> selects) {
			// TODO Auto-generated method stub
			if (selects.size() == 0)
				return;
			for (int i = 0; i < selectIds.size(); i++) {
				MediaUtil.deleteLocalMedia(GalleryActivity.this, mediaType,
						selectIds.get(i));
			}
		}

		@Override
		public void destroy() {
			// TODO Auto-generated method stub

		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View arg1, int position,
			long arg3) {
		// TODO Auto-generated method stub
		DebugHandler.logd(TAG, String.format("position=%d", position));
		DebugHandler.logd(TAG, String.format("id=%d", arg3));
		((OnGalleryItemClick) gridView.getAdapter()).onClick(position, this);
	}
}

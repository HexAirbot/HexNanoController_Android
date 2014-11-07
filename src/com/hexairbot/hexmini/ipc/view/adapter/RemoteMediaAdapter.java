package com.hexairbot.hexmini.ipc.view.adapter;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hexairbot.hexmini.R;
import com.hexairbot.hexmini.ipc.activity.GalleryActivity;
import com.hexairbot.hexmini.ipc.activity.ShareMediaActivity;
import com.hexairbot.hexmini.ipc.view.MediaFile;
import com.hexairbot.hexmini.ipc.view.OnGalleryItemClick;
import com.hexairbot.hexmini.ipc.view.SquareRelativeLayout;
import com.vmc.ipc.ftp.FtpCallbackListener;
import com.vmc.ipc.ftp.FtpConnectedListener;
import com.vmc.ipc.ftp.FtpManager;
import com.vmc.ipc.util.DebugHandler;
import com.vmc.ipc.util.MediaUtil;

public class RemoteMediaAdapter extends BaseAdapter implements
		OnGalleryItemClick, FtpConnectedListener {

	private final static String TAG = "RemoteMediaAdapter";

	private final static Uri URI_LOCAL_IMAGE = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	private final static Uri URI_LOCAL_VIDEO = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	private final static String[] PROJECTION_LOCAL_IMAGE = new String[] {
			MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID,
			MediaStore.Images.Media.TITLE, MediaStore.Images.Media.MIME_TYPE,
			MediaStore.Images.Media.DATE_MODIFIED, MediaStore.Images.Media.SIZE };
	private final static String[] PROJECTION_LOCAL_VIDEO = new String[] {
			MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID,
			MediaStore.Video.Media.TITLE, MediaStore.Video.Media.MIME_TYPE,
			MediaStore.Video.Media.DATE_MODIFIED, MediaStore.Video.Media.SIZE };
	private String selection = MediaStore.Images.Media.DATA + " like ?";
	private String[] bindArgs = new String[] { "%" + MediaUtil.IPC_DIR + "%" };
	private final static String REMOTE_FILE_PREFIX = "ftp_";

	private final static String MEDIA_NAME = "media_name";
	private Context mContext = null;
	private List<MediaFile> mData = new ArrayList<MediaFile>();
	private List<MediaFile> remoteData = new ArrayList<MediaFile>();
	private List<MediaFile> localData = new ArrayList<MediaFile>();
	private int preRemoteDataSize = 0;
	private List<MediaFile> preLocalData = new ArrayList<MediaFile>();
	private HashMap<String, MediaFile> dataMap = new HashMap<String, MediaFile>();
	private HashMap<String, MediaFile> localDataMap = new HashMap<String, MediaFile>();
	private HashMap<String, MediaFile> remoteDataMap = new HashMap<String, MediaFile>();
	private HashMap<String, SoftReference<Bitmap>> bitmapCache = new HashMap<String, SoftReference<Bitmap>>();
	private FtpManager mFtpManager;
	int mType = -1;

	private Cursor mCursor = null;
	private ChangeObserver mChangeObserver = null;
	private ThumbChangeObserver mThumbChangeObserver = null;
	private boolean isFirstLoad = true;
	private boolean ftpDataLoaded = false;
	Uri queryUri = null;
	Uri thumbUri = null;

	LoadDataTask mLoadDataTask;
	LoadDataTask mLoadDataTask2;

	List<MediaFile> localMedias = new ArrayList<MediaFile>();
	
	public RemoteMediaAdapter(Context context, int type) {
		// TODO Auto-generated constructor stub
		mContext = context;
		mType = type;
		mFtpManager = FtpManager.getInstance();
		mFtpManager.setFtpConnectedListener(this);

		mChangeObserver = new ChangeObserver();
		mThumbChangeObserver = new ThumbChangeObserver();

		if (type != MediaUtil.MEDIA_TYPE_ALL) {
			if (type == MediaUtil.MEDIA_TYPE_IMAGE) {
				queryUri = URI_LOCAL_IMAGE;
				thumbUri = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
			} else {
				queryUri = URI_LOCAL_VIDEO;
				thumbUri = MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI;
			}
			mContext.getContentResolver().registerContentObserver(queryUri, true,
					mChangeObserver);
			mContext.getContentResolver().registerContentObserver(thumbUri, true,
					mThumbChangeObserver);
		} else {
			Uri queryUri1 = URI_LOCAL_IMAGE;
			Uri thumbUri1 = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
			
			Uri queryUri2 = URI_LOCAL_VIDEO;
			Uri thumbUri2 = MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI;
			
			mContext.getContentResolver().registerContentObserver(queryUri1, true,
					mChangeObserver);
			mContext.getContentResolver().registerContentObserver(thumbUri1, true,
					mThumbChangeObserver);
			
			mContext.getContentResolver().registerContentObserver(queryUri2, true,
					mChangeObserver);
			mContext.getContentResolver().registerContentObserver(thumbUri2, true,
					mThumbChangeObserver);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		DebugHandler.logd(TAG, "RemoteMediaAdapter.getView: " + position
				+ "---convertView=" + convertView);
		return createViewFromResource(position, convertView, parent);
	}

	class ItemViewHolder {
		ImageView thumbView;
		TextView nameView;
		ImageView ftpMark;
		View waitingInd;
		View downloadind;
		ImageView playIcon;
	}

	private View createViewFromResource(int position, View convertView,
			ViewGroup parent) {
		View v;
		ItemViewHolder holder;
		if (convertView == null) {
			v = new SquareRelativeLayout(mContext);
			holder = new ItemViewHolder();
			holder.downloadind = v.findViewById(R.id.media_ftp_downloading);
			holder.thumbView = (ImageView) v.findViewById(R.id.media_thumb);
			holder.ftpMark = (ImageView) v.findViewById(R.id.media_ftp_marked);
			holder.nameView = (TextView) v.findViewById(R.id.media_name);
			holder.waitingInd = v.findViewById(R.id.media_ftp_download_wait);
			holder.playIcon = (ImageView)v.findViewById(R.id.play_icon);
			v.setTag(holder);
		} else {
			v = convertView;
			holder = (ItemViewHolder) v.getTag();
		}
		if (mData.size() > 0 || position >= mData.size()) {
			MediaFile mediaFile = mData.get(position);
			DebugHandler.logd(TAG, "load file:" + mediaFile.localPath);

			if (mediaFile.isRemote) {
				holder.ftpMark.setVisibility(View.VISIBLE);
			} else {
				holder.ftpMark.setVisibility(View.GONE);
			}

			if (mediaFile != null) {
				String name = mediaFile.name;
				holder.nameView.setText(name);
			}

			if (mediaFile.type == MediaUtil.MEDIA_TYPE_VIDEO) {
				holder.playIcon.setVisibility(View.VISIBLE);
			} else {
				holder.playIcon.setVisibility(View.GONE);
			}
			
			DisplayThumbTask displayTask = new DisplayThumbTask(mediaFile,
					holder.downloadind, holder.waitingInd, holder.thumbView);
			displayTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
		}
		return v;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mData.get(position);
	}

	public void loadData(int type) {
		if (type != MediaUtil.MEDIA_TYPE_ALL) {
			String[] remotePath = new String[2];
			if (type == MediaUtil.MEDIA_TYPE_IMAGE) {
				remotePath[0] = FtpManager.FTP_IMAGES_DIR;
			} else if (type == MediaUtil.MEDIA_TYPE_VIDEO) {
				remotePath[0] = FtpManager.FTP_VIDEOS_DIR;
			}
			remotePath[1] = type + "";
			// if (mLoadDataTask != null) {
			// mLoadDataTask.cancel(false);
			// mLoadDataTask = null;
			// }
			mLoadDataTask = new LoadDataTask();
			// mLoadDataTask.execute(remotePath);
			mLoadDataTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, remotePath);
		} else {
			String[] remotePath1 = new String[2];
			String[] remotePath2 = new String[2];
			
			remotePath1[0] = FtpManager.FTP_IMAGES_DIR;
			remotePath2[0] = FtpManager.FTP_VIDEOS_DIR;
			remotePath1[1] = MediaUtil.MEDIA_TYPE_IMAGE + "";
			remotePath2[1] = MediaUtil.MEDIA_TYPE_VIDEO + "";
			
			mLoadDataTask = new LoadDataTask();
			mLoadDataTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, remotePath1);
			
			mLoadDataTask2 = new LoadDataTask();
			mLoadDataTask2.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, remotePath2);
		}
		
	}

	private List<MediaFile> getLocalData(int type) {
		if (type == MediaUtil.MEDIA_TYPE_ALL) {
			localMedias = null;
			getLocalData(MediaUtil.MEDIA_TYPE_IMAGE);
			getLocalData(MediaUtil.MEDIA_TYPE_VIDEO);
			return localMedias;
		}
		
		Uri queryUri = null;
		String[] projection = null;
		if (type == MediaUtil.MEDIA_TYPE_IMAGE) {
			queryUri = URI_LOCAL_IMAGE;
			projection = PROJECTION_LOCAL_IMAGE;
		} else if (type == MediaUtil.MEDIA_TYPE_VIDEO) {
			queryUri = URI_LOCAL_VIDEO;
			projection = PROJECTION_LOCAL_VIDEO;
		}
		mCursor = mContext.getContentResolver().query(queryUri, projection,
				selection, bindArgs, null);

		if (mCursor == null)
			return localMedias;
		while (mCursor.moveToNext()) {
			String _data = mCursor.getString(0);
			long _id = mCursor.getLong(1);
			String _title = _data.substring(_data.lastIndexOf("/") + 1);
			String _type = mCursor.getString(3);
			long _modifyDate = mCursor.getLong(4);
			long _size = mCursor.getLong(5);
			// int id,int type,String name,String path,String modifyDate,boolean
			// isRemote
			boolean isRemote = _title.indexOf(REMOTE_FILE_PREFIX) != -1;
			if (isRemote) {
				_title = _title.substring(_title
						.lastIndexOf(REMOTE_FILE_PREFIX)
						+ REMOTE_FILE_PREFIX.length());
			}
			MediaFile mediaFile = new MediaFile(_id, type, _title, _data,
					_size, isRemote);
			if (isRemote) {
				mediaFile.isDownloaded = true;
				StringBuffer remotePath = new StringBuffer();
				if (type == MediaUtil.MEDIA_TYPE_IMAGE) {
					remotePath.append(FtpManager.FTP_IMAGES_DIR);
				} else if (type == MediaUtil.MEDIA_TYPE_VIDEO) {
					remotePath.append(FtpManager.FTP_VIDEOS_DIR);
				}
				remotePath.append(mediaFile.name);
				mediaFile.remotePath = remotePath.toString();
			}
			localMedias.add(mediaFile);
		}
		mCursor.close();
		return localMedias;
	}

	private boolean mergerRemoteData(List<MediaFile> remotedata) {
		boolean isDifferent = false;
		if (remoteDataMap.size() != remotedata.size()) {
			remoteDataMap.clear();
			for (MediaFile file : remotedata) {
				String name = file.name;
				remoteDataMap.put(name, file);
			}
			isDifferent = true;
		} else {
			for (MediaFile file : remotedata) {
				String name = file.name;
				MediaFile cache = remoteDataMap.get(name);
				if (cache == null) {
					remoteDataMap.put(name, file);
					isDifferent = true;
				} else {
					if (file.size != cache.size) {
						remoteDataMap.put(name, file);
						isDifferent = true;
					}
				}
			}
		}
		return isDifferent;
	}

	private boolean mergerLocalData(List<MediaFile> localdata) {
		boolean isDifferent = false;
		if (localDataMap.size() != localdata.size()) {
			localDataMap.clear();
			for (MediaFile file : localdata) {
				String name = file.name;
				localDataMap.put(name, file);
			}
			isDifferent = true;
		} else {
			for (MediaFile file : localdata) {
				String name = file.name;
				MediaFile cache = localDataMap.get(name);
				if (cache == null || file.differentTo(cache)) {
					localDataMap.put(name, file);
					isDifferent = true;
				}
			}
		}
		return isDifferent;
	}

	private void mergeData() {
		dataMap.clear();
		dataMap.putAll(localDataMap);
		Collection<MediaFile> remoteFiles = remoteDataMap.values();
		Iterator<MediaFile> it = remoteFiles.iterator();
		while (it.hasNext()) {
			MediaFile file = it.next();
			String name = file.name;
			MediaFile cache = dataMap.get(name);
			if (cache == null) {
				dataMap.put(name, file);
			} else {
				if (file.size != cache.size) {
					dataMap.put(name, file);
				}
			}
		}
	}

	private class LoadDataTask extends AsyncTask<String, Integer, Integer> {
		int type = -1;
		ArrayList<MediaFile> newData = new ArrayList<MediaFile>();

		protected Integer doInBackground(String... params) {
			String remotePath = params[0];
			type = Integer.parseInt(params[1]);
			newData.clear();
			boolean isRemotedif = false;
			boolean isLocaledif = false;
			if (!ftpDataLoaded) {
				remoteData = mFtpManager.getMeidaData(remotePath);
			}
			localData = null;
			localData = getLocalData(type);
			if (localData != null)
				isLocaledif = mergerLocalData(localData);
			if (remoteData != null
					&& (!ftpDataLoaded || (remoteData.size() != preRemoteDataSize))) {
				isRemotedif = mergerRemoteData(remoteData);
				preRemoteDataSize = remoteData.size();
				ftpDataLoaded = true;
			}
			mergeData();
			newData.clear();
			newData.addAll(dataMap.values());
			return (isFirstLoad || (isLocaledif || isRemotedif)) ? 1 : 0;
		}

		protected void onPostExecute(Integer result) {
			if (result > 0 && !isCancelled()) {
				DebugHandler.logd(TAG, "get remote media has changed.");
				setData(newData);
				RemoteMediaAdapter.this.notifyDataSetChanged();
			}
			if (isFirstLoad)
				isFirstLoad = false;
		}
	}

	public void downloadRemoteFile(MediaFile file, FtpCallbackListener listener) {
		StringBuffer dst = new StringBuffer();
		dst.append(MediaUtil.getMediaDir(file.type));
		dst.append(REMOTE_FILE_PREFIX + file.name);
		mFtpManager.setDownloadCallbackListener(listener);
		mFtpManager.downloadFile(dst.toString(), file.remotePath);
	}

	private class DisplayThumbTask extends AsyncTask<Void, Integer, Bitmap>
			implements FtpCallbackListener {

		MediaFile mMediaFile = null;
		View downloadingIndictor = null;
		View waitingIndictor = null;
		ImageView imageView = null;

		public DisplayThumbTask(MediaFile file, View dInd, View wInd,
				ImageView view) {
			mMediaFile = file;
			downloadingIndictor = dInd;
			waitingIndictor = wInd;
			imageView = view;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			imageView.setBackgroundColor(Color.GRAY);
			Bitmap thumb = null;
			StringBuffer cacheKey = new StringBuffer();
			cacheKey.append(mMediaFile.localPath).append(mMediaFile.remotePath);
			SoftReference<Bitmap> sCache = bitmapCache.get(cacheKey.toString());
			if (sCache != null)
				thumb = sCache.get();
			if (thumb != null) {
				imageView.setImageBitmap(thumb);
				this.cancel(false);
				return;
			}

			if (waitingIndictor != null) {
				waitingIndictor.setVisibility(View.GONE);
			}
			if (downloadingIndictor != null)
				downloadingIndictor.setVisibility(View.VISIBLE);
		}

		protected Bitmap doInBackground(Void... params) {
			// DebugHandler.logd(TAG, "start download file:"
			// + mMediaFile.remotePath);
			// downloadRemoteFile(mMediaFile, this);
			// DebugHandler
			// .logd(TAG, "end download file:" + mMediaFile.remotePath);
			if (this.isCancelled())
				return null;
			Bitmap bitmap = null;

			if (mMediaFile.isRemote && !mMediaFile.isDownloaded
					&& mMediaFile.type == MediaUtil.MEDIA_TYPE_IMAGE) {
				DebugHandler.logd(TAG, "start download file:"
						+ mMediaFile.remotePath);
				downloadRemoteFile(mMediaFile, this);
				StringBuffer dst = new StringBuffer();
				dst.append(MediaUtil.getMediaDir(mMediaFile.type));
				dst.append("ftp_" + mMediaFile.name);
				mMediaFile.isDownloaded = true;
				MediaUtil.scanIpcMediaFile(mContext, dst.toString());
				DebugHandler.logd(TAG, "end download file:"
						+ mMediaFile.remotePath);
			}
			bitmap = MediaUtil.getMediaThumbnail(mContext, mMediaFile,
					bitmapCache);
			return bitmap;
		}

		protected void onProgressUpdate(Integer... progress) {
			DebugHandler.logd(TAG,
					String.format("downloaded %d bytes ", progress[0]));
		}

		protected void onPostExecute(Bitmap bitmap) {
			if (this.isCancelled())
				return;
			if (downloadingIndictor != null)
				downloadingIndictor.setVisibility(View.GONE);

			if (bitmap != null) {
				// DebugHandler.logd(TAG, String.format("bitmap (%d,%d)",
				// bitmap.getWidth(), bitmap.getHeight()));
				imageView.setImageBitmap(bitmap);
				// bitmap.recycle();
			} else {
				imageView.setImageBitmap(null);
			}
		}

		@Override
		public void onProgress(int progress, int totle) {
			// TODO Auto-generated method stub
			this.publishProgress(progress);
		}

		@Override
		public void onError(String err) {
			// TODO Auto-generated method stub

		}
	}

	public void setData(ArrayList<MediaFile> newData) {
		mData.clear();
		mData.addAll(newData);
		Collections.sort(mData);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public void onClick(int position, Context context) {
		// TODO Auto-generated method stub
		synchronized (mData) {
			MediaFile file = mData.get(position);
			/*
			if (file.isRemote == false || file.isDownloaded == true) {
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				String path = file.localPath;
				String intentType = null;
				if (file.type == MediaUtil.MEDIA_TYPE_IMAGE) {
					intentType = "image/*";
				} else if (file.type == MediaUtil.MEDIA_TYPE_VIDEO) {
					intentType = "video/*";
				}
				intent.setDataAndType(Uri.fromFile(new File(path)), intentType);
				mContext.startActivity(intent);
			} else {
				if (file.type == MediaUtil.MEDIA_TYPE_IMAGE) {
					DebugHandler.logWithToast(mContext, mContext.getResources()
							.getString(R.string.remote_image_is_undownloaded),
							1500);
				} else if (file.type == MediaUtil.MEDIA_TYPE_VIDEO) {
					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW);
					StringBuffer url = new StringBuffer();
					url.append("http://").append(FtpManager.HOST)
							.append(file.remotePath);
					// String path = file.remotePath;
					String intentType = "video/*";
					intent.setDataAndType(Uri.parse(url.toString()), intentType);
					mContext.startActivity(intent);
				}
			}*/
			if (file.isRemote == false || file.isDownloaded == true) {
				String path = file.localPath;
				String intentType = null;
				if (file.type == MediaUtil.MEDIA_TYPE_IMAGE) {
					intentType = "image/*";
				} else if (file.type == MediaUtil.MEDIA_TYPE_VIDEO) {
					intentType = "video/*";
				}
				Intent intent = new Intent();
				intent.putExtra("media_path", path);
				intent.putExtra("media_type", intentType);
				intent.putExtra("media_type_int", file.type);
				
				intent.putExtra("file_id", file.id);
				intent.setClass(mContext, ShareMediaActivity.class);
				mContext.startActivity(intent);
			} else {
				if (file.type == MediaUtil.MEDIA_TYPE_IMAGE) {
					DebugHandler.logWithToast(mContext, mContext.getResources()
							.getString(R.string.remote_image_is_undownloaded),
							1500);
				} else if (file.type == MediaUtil.MEDIA_TYPE_VIDEO) {
					Intent intent = new Intent();					
					StringBuffer url = new StringBuffer();
					String path = url.append("http://").append(FtpManager.HOST)
							.append(file.remotePath).toString();
					
					String intentType = "video/*";
					intent.putExtra("media_path", path);
					intent.putExtra("media_type", intentType);
					intent.putExtra("media_type_int", file.type);
					intent.putExtra("file_id", file.id);
					intent.setClass(mContext, ShareMediaActivity.class);
					mContext.startActivity(intent);
				}
			}
		}

	}

	protected void onContentChanged() {
		DebugHandler.logd(TAG, "Auto requerying " + mCursor + " due to update");
		loadData(mType);
		// mCursor.requery();
	}

	private class ChangeObserver extends ContentObserver {
		public ChangeObserver() {
			super(new Handler());
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

		@Override
		public void onChange(boolean selfChange) {
			// DebugHandler.logd(TAG,
			// "Content has changed--"+selfChange+"--"+this);
			// onContentChanged();
		}
	}

	private class ThumbChangeObserver extends ContentObserver {
		public ThumbChangeObserver() {
			super(new Handler());
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

		@Override
		public void onChange(boolean selfChange) {
			DebugHandler.logd(TAG, "ThumbChangeObserver onchanged--"
					+ selfChange);
			onContentChanged();
		}
	}

	private class MyDataSetObserver extends DataSetObserver {
		@Override
		public void onChanged() {
			notifyDataSetChanged();
		}

		@Override
		public void onInvalidated() {
			notifyDataSetInvalidated();
		}
	}

	@Override
	public void delete(ArrayList<Long> selects) {
		// TODO Auto-generated method stub
		if (selects == null)
			return;
		ArrayList<MediaFile> temp = (ArrayList<MediaFile>) ((ArrayList<MediaFile>) mData)
				.clone();
		for (long position : selects) {
			MediaFile file = temp.get((int) position);
			if (!file.isRemote || file.isDownloaded || file.id != -1) {
				MediaUtil.deleteLocalMedia(mContext, file.type, file.id);				
			}
			if (file.isRemote) {
				MediaUtil.deleteRemoteMedia(mFtpManager, file.remotePath);
				deleteRemoteFromCache(file);
				if (file.type == MediaUtil.MEDIA_TYPE_VIDEO) {
					onContentChanged();
				}
			}		
		}
		notifyDataSetChanged();
	}

	private void deleteRemoteFromCache(MediaFile file) {
		if (remoteData == null)
			return;
		int index = -1;
		for (int i = 0; i < remoteData.size(); i++) {
			MediaFile mFile = remoteData.get(i);
			if (mFile.name.equals(file.name)) {
				index = i;
				break;
			}
		}
		if (index >= 0)
			remoteData.remove(index);
	}

	@Override
	public void destroy() {
		mContext.getContentResolver()
				.unregisterContentObserver(mChangeObserver);
		mContext.getContentResolver().unregisterContentObserver(
				mThumbChangeObserver);
	}

	@Override
	public void onFtpConnect() {
		// TODO Auto-generated method stub
		loadData(mType);
	}

	@Override
	public void onFtpDisconnect() {
		// TODO Auto-generated method stub
		
	}
}

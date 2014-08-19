package com.vmc.ipc.util;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;

import com.hexairbot.hexmini.ipc.view.MediaFile;
import com.vmc.ipc.ftp.FtpManager;

public class MediaUtil {

	private final static String TAG = "MediaUtil";
	public final static int MEDIA_TYPE_IMAGE = 100;
	public final static int MEDIA_TYPE_VIDEO = 200;
	public final static int MEDIA_TYPE_ALL = 300;
	public final static String IPC_DIR = "/ipc/";
	public final static String IPC_IMAGE_DIR = "/ipc/images/";
	public final static String IPC_VIDEO_DIR = "/ipc/videos/";

	public static boolean hasIpcMediaFile(int type) {
		String externalStore = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		String mediaDir;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaDir = externalStore + IPC_IMAGE_DIR;
		} else {
			mediaDir = externalStore + IPC_VIDEO_DIR;
		}
		File file = new File(mediaDir);
		if (!file.exists())
			file.mkdir();
		if (file.listFiles().length > 0)
			return true;
		else
			return false;
	}

	public static boolean createIPCDir() {
		String externalStore = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		// DebugHandler.logd(TAG, "externalStore="+externalStore);
		String imagedir = externalStore + IPC_IMAGE_DIR;
		File ifile = new File(imagedir);
		if (!ifile.exists()) {
			return ifile.mkdirs();
		}

		String videoDir = externalStore + IPC_VIDEO_DIR;
		File vfile = new File(videoDir);
		if (!vfile.exists()) {
			return vfile.mkdirs();
		}
		return false;
	}

	public static void scanIpcMediaFile(Context context, String path) {
		File file = new File(path);
		if (!file.exists())
			return;
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		intent.setData(Uri.fromFile(new File(path)));
		context.sendBroadcast(intent);
	}

	public static Bitmap getLocalMediaThumbnail(int type, String path) {
		Bitmap thumb = null;

		if (path != null) {
			if (type == MEDIA_TYPE_VIDEO) {
				thumb = ThumbnailUtils.createVideoThumbnail(path,
						Video.Thumbnails.MICRO_KIND);
			} else if (type == MEDIA_TYPE_IMAGE) {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 10;
				Bitmap bitmap = BitmapFactory.decodeFile(path, options);
				thumb = ThumbnailUtils.extractThumbnail(bitmap, 96, 96);
				if (bitmap != null)
					bitmap.recycle();
			}
			if (thumb == null) {
				DebugHandler.logd(TAG, "Can't create mini thumbnail for "
						+ path);
				return null;
			}
		}
		return thumb;
	}

	public static Bitmap getMediaThumbnail(Context context, MediaFile file,
			HashMap<String, SoftReference<Bitmap>> bitmapCache) {
		if (file == null)
			return null;
		Bitmap thumb = null;
		int type = file.type;
		String path = file.localPath;
		StringBuffer cacheKey = new StringBuffer();
		cacheKey.append(file.localPath).append(file.remotePath);

		SoftReference<Bitmap> sCache = bitmapCache.get(cacheKey.toString());
		if (sCache != null)
			thumb = sCache.get();
		if (thumb != null)
			return thumb;

		if (file.id != -1) {
			Options options = new BitmapFactory.Options();
			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.RGB_565;
			if (type == MEDIA_TYPE_IMAGE) {
				thumb = MediaStore.Images.Thumbnails.getThumbnail(
						context.getContentResolver(), file.id,
						MediaStore.Images.Thumbnails.MICRO_KIND, options);
			} else if (type == MEDIA_TYPE_VIDEO) {
				thumb = MediaStore.Video.Thumbnails.getThumbnail(
						context.getContentResolver(), file.id,
						MediaStore.Images.Thumbnails.MICRO_KIND, options);
			}
		}
		if (thumb == null) {
			if ((!file.isRemote || type == MEDIA_TYPE_IMAGE) && path != null) {
				if (type == MEDIA_TYPE_VIDEO) {
					thumb = ThumbnailUtils.createVideoThumbnail(path,
							Video.Thumbnails.MICRO_KIND);
				} else if (type == MEDIA_TYPE_IMAGE) {
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inSampleSize = 10;
					Bitmap bitmap = BitmapFactory.decodeFile(path, options);
					thumb = ThumbnailUtils.extractThumbnail(bitmap, 96, 96);
					if (bitmap != null)
						bitmap.recycle();
				}
			} else if (file.isRemote && type == MEDIA_TYPE_VIDEO
					&& !file.isDownloaded) {
				StringBuffer url = new StringBuffer();
				url.append("http://").append(FtpManager.HOST)
						.append(file.remotePath);
				thumb = MediaUtil.createVideoThumbnailForNetVideo(url
						.toString());
			}
		}
		if (thumb == null) {
			DebugHandler.logd(TAG, "Can't create mini thumbnail for " + path);
			return null;
		} else {
			bitmapCache.put(cacheKey.toString(), new SoftReference<Bitmap>(
					thumb));
		}
		return thumb;
	}

	public static Bitmap createVideoThumbnailForNetVideo(String url) {
		Bitmap bitmap = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		int kind = MediaStore.Video.Thumbnails.MINI_KIND;
		try {
			if (Build.VERSION.SDK_INT >= 14) {
				retriever.setDataSource(url, new HashMap<String, String>());
			} else {
				retriever.setDataSource(url);
			}
			bitmap = retriever.getFrameAtTime(500);
		} catch (IllegalArgumentException ex) {
			// Assume this is a corrupt video file
		} catch (RuntimeException ex) {
			// Assume this is a corrupt video file.
		} finally {
			try {
				retriever.release();
			} catch (RuntimeException ex) {
				// Ignore failures while cleaning up.
			}
		}
		if (kind == Images.Thumbnails.MICRO_KIND && bitmap != null) {
			bitmap = ThumbnailUtils.extractThumbnail(bitmap, 96, 96,
					ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		}
		return bitmap;
	}

	public static String getMediaDir(int type) {
		StringBuffer path = new StringBuffer();
		path.append(Environment.getExternalStorageDirectory().getAbsolutePath());
		if (type == MEDIA_TYPE_VIDEO) {
			path.append(IPC_VIDEO_DIR);
		} else if (type == MEDIA_TYPE_IMAGE) {
			path.append(IPC_IMAGE_DIR);
		}
		return path.toString();
	}

	public static String getAppDir() {
		StringBuffer path = new StringBuffer();
		path.append(Environment.getExternalStorageDirectory().getAbsolutePath());
		path.append(IPC_DIR);
		return path.toString();
	}

	public static String getAppConfigDir() {
		StringBuffer path = new StringBuffer();
		path.append(Environment.getExternalStorageDirectory().getAbsolutePath());
		path.append(IPC_DIR);
		path.append("config/");
		return path.toString();
	}

	public static boolean compareMediaData(List<MediaFile> src,
			List<MediaFile> pre) {
		if ((src == null && pre != null) || (src != null && pre == null))
			return false;
		if (src.size() != pre.size())
			return false;
		for (int i = 0; i < src.size(); i++) {
			if (src.get(i).differentTo(pre.get(i))) {
				return false;
			}
		}
		return true;
	}

	public static void deleteLocalMedia(Context context, int type, long id) {
		Uri baseUri;
		if (type == MediaUtil.MEDIA_TYPE_IMAGE) {
			baseUri = Images.Media.EXTERNAL_CONTENT_URI;
		} else {
			baseUri = Video.Media.EXTERNAL_CONTENT_URI;
		}
		context.getContentResolver().delete(baseUri, "_id=?",
				new String[] { String.valueOf(id) });
	}

	public static void deleteRemoteMedia(FtpManager ftp, String dst) {
		ftp.deleteFile(dst);
	}
}

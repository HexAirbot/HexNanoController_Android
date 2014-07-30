package com.vmc.ipc.ftp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;


import com.hexairbot.hexmini.ipc.view.MediaFile;
import com.vmc.ipc.util.DebugHandler;
import com.vmc.ipc.util.MediaUtil;

public class FtpManager implements Runnable{
    
    public final static String TAG = "FtpManager";
    
    private FTPClient ftp;
    
    public final static String FTP_IMAGES_DIR = "/ipc/images/";
    public final static String FTP_VIDEOS_DIR = "/ipc/videos/";
    
    private static FtpManager instance = null;
//    private static final String HOST = "10.0.13.125";
//    private static final String USER = "administrator";
//    private static final String PASSWORD = "0";
    public static final String HOST = "192.168.1.1";
    private static final String USER = "anonymous";
    private static final String PASSWORD = "";
    private static final String TEMP_FILE_PATH = MediaUtil.getAppDir()+"ftpdir.tmp";
    public final static int STATE_CONNETED = 1;
    public final static int STATE_DISCONNETED = 0;
    public final static int STATE_CONNETING = 2;
    
    private File tempFile = null;
    private FtpConnectedListener mFtpConnectedListener;
    private FtpCallbackListener mFtpCallbackListener;
    private Object stateLock = new Object();
    private int state = 0;
    
    private FtpManager() {
	ftp = new FTPClient();
    }
    
    public static FtpManager getInstance() {
	if(instance == null)
	    instance = new FtpManager();
	return instance;
    }
    
    public void connect() {
	if(getState() == STATE_CONNETING || getState() == STATE_CONNETED)
	    return;
	Thread cThread = new Thread(this);
	cThread.start();
    }
    
    public int getState() {
	synchronized (stateLock) {
	    return state;
	}
    }
    
    public List<MediaFile> getMeidaData(String path){
	if(getState() != STATE_CONNETED) return null;
	ftp.FtpDir(TEMP_FILE_PATH, path);
	File file = new File(TEMP_FILE_PATH);
	List<MediaFile> mData = new ArrayList<MediaFile>();
	if(!file.exists()) {
	    Log.d(TAG, "there is no tempfile to be accessed.");
	    return null;
	}
	try {
	    BufferedReader input = new BufferedReader(new FileReader(file));
	    String line = null;
	    int type = -1;
	    if(path.indexOf(MediaUtil.IPC_IMAGE_DIR) != -1) {
		type = MediaUtil.MEDIA_TYPE_IMAGE;
	    }
	    else if(path.indexOf(MediaUtil.IPC_VIDEO_DIR) != -1) {
		type = MediaUtil.MEDIA_TYPE_VIDEO;
	    }
	    while((line = input.readLine()) != null) {
		Log.d(TAG, line);
//		String infos = "-rwxrwxrwx   1 owner    group          599707 Nov 22  2013 1.jpg";
		String[] infos = line.trim().replaceAll("\\s+", " ").split(" ");
		String modifyDate = infos[infos.length - 4]+" "+infos[infos.length - 3]+" "+infos[infos.length - 2];
		int size = Integer.parseInt(infos[infos.length - 5]);
		MediaFile mediaFile = new MediaFile(type,infos[infos.length - 1],size);
		mediaFile.isRemote = true;
		mediaFile.remotePath = path + infos[infos.length - 1];
		mData.add(mediaFile);
	    }
	    input.close();
	    return mData;
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return null;
    }
    
    
    public void destroy() {
	synchronized (stateLock) {
        	if(ftp != null) {
        	    ftp.FtpQuit();
        	    if(mFtpConnectedListener != null) mFtpConnectedListener.disconnect();
        	    state = STATE_DISCONNETED;
        	}
	}
    }

    public void setFtpConnectedListener(FtpConnectedListener listener) {
	mFtpConnectedListener = listener;
    }
    
    @Override
    public void run() {
	// TODO Auto-generated method stub
	synchronized (stateLock) {
	    state = STATE_CONNETING;
	    DebugHandler.logd(TAG, "connect ftp host:"+HOST);
	    if(ftp.FtpConnect(HOST) == 1) {
		ftp.FtpSetConnectionMode(FTPClient.FTP_CONNECT_MODE_PASV);
    	    	if(ftp.FtpLogin(USER, PASSWORD) == 1) {
        	    	if(mFtpConnectedListener != null) {
        	    	    mFtpConnectedListener.connect();
        	    	}
        	    	state = STATE_CONNETED;
        	    	return;
    	    	}
    	    	else {
    	    	    DebugHandler.logd(TAG, "ftp log fail");
    	    	}
	    }
	    else {
		DebugHandler.logd(TAG, "ftp connect fail");
	    }
	    state = STATE_DISCONNETED;
	}
    }
    
    public void setDownloadCallbackListener(FtpCallbackListener listener) {
	mFtpCallbackListener = listener;
    }
    
    public void downloadFile(String dst,String src) {
	synchronized (stateLock) {
        	if(state != STATE_CONNETED) {
        	    DebugHandler.logd(TAG, "we have not been connected to FTP SERVER");
        	    return;
        	}
        	ftp.FtpClearCallback();
        	if(mFtpCallbackListener != null) {
        	    ftp.FtpSetCallback(mFtpCallbackListener);
        	}
        //	ftp.FtpGet("/sdcard/qw/images/11.jpg", "/ipc/images/1.jpg", FTPClient.FTP_TRANSFER_MODE_IMAGE);
        	ftp.FtpGet(dst, src, FTPClient.FTP_TRANSFER_MODE_IMAGE);
	}
    }
    
    public void deleteFile(String dst) {
        synchronized (stateLock) {
        	if(state != STATE_CONNETED) {
        	    DebugHandler.logd(TAG, "we have not been connected to FTP SERVER");
        	    return;
        	}
        	ftp.FtpDelete(dst);
	}
    }
}

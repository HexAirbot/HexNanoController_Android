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

    public final static String FTP_DIR = "/ipc/";
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
    private static final String CHECK_FILE_PATH = MediaUtil.getAppDir()+"checkftp.tmp";
    public final static int STATE_CONNETED = 1;
    public final static int STATE_DISCONNETED = 0;
    public final static int STATE_CONNETING = 2;
    
    private File tempFile = null;
    private FtpConnectedListener mFtpConnectedListener;
    private FtpCallbackListener mFtpCallbackListener;
    private Object stateLock = new Object();
    private int state = STATE_DISCONNETED;
    private boolean stopFtp = true;
    
    private FtpManager() {
	ftp = new FTPClient();
    }
    
    public static FtpManager getInstance() {
	if(instance == null)
	    instance = new FtpManager();
	return instance;
    }
    
    public void init() {
	if (stopFtp) {
	    Thread cThread = new Thread(this);
	    cThread.start();
	}
	stopFtp = false;
    }
    
    private void connect(){
	state = STATE_CONNETING;
	DebugHandler.logd(TAG, "connect ftp host:" + HOST);
	try {
	    if (ftp.FtpConnect(HOST) == 1) {
	        ftp.FtpSetConnectionMode(FTPClient.FTP_CONNECT_MODE_PASV);
	        if (ftp.FtpLogin(USER, PASSWORD) == 1) {
	    	if (mFtpConnectedListener != null) {
	    	    mFtpConnectedListener.onFtpConnect();
	    	}
	    	state = STATE_CONNETED;
	    	return;
	        } else {
	    	DebugHandler.logd(TAG, "ftp log fail");
	        }
	    } else {
	        DebugHandler.logd(TAG, "ftp connect fail");
	    }
	} catch (FtpException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    
    public int getState() {
	return state;
    }
    
    public String lastResponse() {
	try {
	    return ftp.FtpLastResponse();
	} catch (FtpException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return "lastresponse was fail";
	}
    }
    
    public List<MediaFile> getMeidaData(String path){
	if(state != STATE_CONNETED) return null;
	try {
	    ftp.FtpDir(TEMP_FILE_PATH, path);
	} catch (FtpException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	    return null;
	}
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
	stopFtp = true;
    }

    public void setFtpConnectedListener(FtpConnectedListener listener) {
	mFtpConnectedListener = listener;
    }
    
    @Override
    public void run() {
	connect();
	while (!stopFtp) {
	    int checkConn = 0;
	    try {
		checkConn = ftp.FtpNlst(CHECK_FILE_PATH, FTP_DIR);
	    } catch (FtpException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    }
	    if (checkConn == 0) {
		state = STATE_DISCONNETED;
		if (mFtpConnectedListener != null) {
		    mFtpConnectedListener.onFtpDisconnect();
		}
		connect();
	    }
	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	    DebugHandler.logd(TAG, "checkConn = "+checkConn);
	}
	if (ftp != null) {
	    try {
		ftp.FtpQuit();
	    } catch (FtpException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    if (mFtpConnectedListener != null)
		mFtpConnectedListener.onFtpDisconnect();
	    state = STATE_DISCONNETED;
	}
	
	state = STATE_DISCONNETED;
    }
    
    public void setDownloadCallbackListener(FtpCallbackListener listener) {
	mFtpCallbackListener = listener;
    }
    
    public void downloadFile(String dst,String src) {
	if (state != STATE_CONNETED) {
	    DebugHandler.logd(TAG, "we have not been connected to FTP SERVER");
	    return;
	}
	try {
	    ftp.FtpClearCallback();
	    if (mFtpCallbackListener != null) {
		ftp.FtpSetCallback(mFtpCallbackListener);
	    }
	    ftp.FtpGet(dst, src, FTPClient.FTP_TRANSFER_MODE_IMAGE);
	} catch (FtpException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    
    public void deleteFile(String dst) {
	if (state != STATE_CONNETED) {
	    DebugHandler.logd(TAG, "we have not been connected to FTP SERVER");
	    return;
	}
	ftp.FtpDelete(dst);
	
    }
}

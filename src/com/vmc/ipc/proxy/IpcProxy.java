/**
 * @author Administrator
 *
 */
package com.vmc.ipc.proxy;

import java.util.ArrayList;

import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;


import com.vmc.ipc.config.VmcConfig;
import com.vmc.ipc.service.ConnectStateManager;
import com.vmc.ipc.util.DebugHandler;

public class IpcProxy {

    private final static String TAG = "IpcProxy";

    private Application appContext = null;
    private String[] navData = null;
    private VIConfig viConfig = null;
    private LocalBroadcastManager mLocalBroadcastManager = null;
    private Intent connectStateIntent = null;
    private ArrayList<OnRecordCompleteListener> mOnRecordCompleteListeners = new ArrayList<OnRecordCompleteListener>();

    private Handler mHandler = null;
    private int mDecMode = 0;

    public final static String ACTION_DECODEMODE_CHANGED = "action_decodeMode_changed";
    public final static String ACTION_CONNECT_QUALITY_CHANGED = "action_connect_quality_changed";
    public final static String ACTION_RESOLUTION_CHANGED = "action_resolution_changed";
    public final static String ACTION_BITRATE_CHANGED = "action_bitrate_changed";
    public final static String ACTION_REFRESH_DEBUG = "action_refresh_debug";
    public final static String EXTRA_DECODE_MODE = "decode_mode";
    public final static String EXTRA_CONNECT_QUALITY = "connect_quality";
    public final static String EXTRA_RESOLUTION = "resolution";
    public final static String EXTRA_DEBUG_INFO = "debug_info";

    public final static int MESSAGE_DECODEMODE_CHANGED = 5656;
    public final static int MESSAGE_RECORD_COMPLETED = 1001;
    public final static int MESSAGE_CONNECT_QUALITY_CHANGEBAD = 1002;
    public final static int MESSAGE_CONNECT_QUALITY_CHANGEGOOD = 1003;
    public final static int MESSAGE_RESOLUTION_CHANGED = 1004;
    public final static int MESSAGE_REFRESH_DEBUG = 1005;
    
    public final static int DEFAULT_DECODE_MODE = 1; //default mode is soft
    public final static int DEFAULT_PREVIEW_RESOLUTION_WIDTH = 1280; 
    public final static int DEFAULT_PREVIEW_RESOLUTION_HEIGHT = 720; 
    public final static int DEFAULT_PREVIEW_BITRATECONTROL = 1; //1 fps 2 quality
    
    public IpcProxy(Application app) {
	// navData = new NavData();
	viConfig = new VIConfig();
	mLocalBroadcastManager = LocalBroadcastManager.getInstance(app);
	connectStateIntent = new Intent(
		ConnectStateManager.ACTION_CONNECT_STATE_CHANGED);
	mHandler = new CallbackHandler();
    }

    private void setAppContext(Application app) {
	appContext = app;
    }

    public void doConnect() {
	connect(null);
    }

    public void doPause() {
	pause();
    }

    public void doResume() {
	resume();
    }

    public void doDisconnect() {
	disconnect();
    }

    public void doTriggerTakeOff() {
	triggerTakeOff();
    }

    public void doStartPreview() {
	startPreview();
    }

    public void doStopPreview() {
	stopPreview();
    }

    public void doOnSizeChanged(int width, int height) {
	onSizeChange(width, height);
    }

    public void doTakePhoto(String destDir, String name, boolean containGPS) {
	takePhoto(destDir, name, containGPS);
    }

    public void doStartRecord(String destDir, String cacheDir, String name,
	    boolean containGPS) {
	startRecord(destDir, cacheDir, name, containGPS);
    }

    public void doStopRecord() {
	stopRecord();
    }

    public String[] getNavData() {
	return navData;
    }

    public void doUpdateNavData() {
	navData = takeNavDataSnapshot(null);
    }

    public void doSendMessage2Server(String[] key, String[] value) {
	sendMessage2Server(key, value);
    }

    public void doUpdateVIConfig() {
	viConfig = takeConfigSnapshot(viConfig);
    }

    public VIConfig getVIConfig() {
	return viConfig;
    }

    public String doGetConfigItem(String configName) {
	return getConfigItem(configName);
    }

    public void doSetConfigItem(String configName, String value) {
	setConfigItem(configName, value);
    }

    public void doResetConfig() {
	resetConfig();
    }

    public void setIpcDecMode(int decmode) {
	setDecodeStrategy(decmode);
    }

    public void onRecordComplete(boolean isSuccess) {
	if (mOnRecordCompleteListeners == null
		|| mOnRecordCompleteListeners.size() == 0)
	    return;
	for (int i = 0; i < mOnRecordCompleteListeners.size(); i++) {
	    mOnRecordCompleteListeners.get(i).onRecordComplete(isSuccess);
	}
    }

    public static interface OnRecordCompleteListener {
	public void onRecordComplete(boolean isSuccess);
    }

    public void addOnRecordCompleteListener(OnRecordCompleteListener lis) {
	mOnRecordCompleteListeners.add(lis);
    }

    public void removeOnRecordCompleteListener(OnRecordCompleteListener lis) {
	mOnRecordCompleteListeners.remove(lis);
    }

    /**
     * ------------------------------------------------------------------------
     * -- native api function
     */
    // api for connect to device
    public native void connect(String address);

    public native void setDecodeStrategy(int strategy);

    public native void pause();

    public native void resume();

    public native void disconnect();

    public native void triggerTakeOff();

    public void ipcConnectStateChanged(int state, String info) {
	connectStateIntent.putExtra(ConnectStateManager.EXTRA_STATE, state);
	connectStateIntent.putExtra(ConnectStateManager.EXTRA_INFO, info);
	mLocalBroadcastManager.sendBroadcast(connectStateIntent);
    }

    // api for media
    public native void startPreview();

    public native void stopPreview();
    
    public native void setPreviewResolution(int width,int height);

    public native void setBitrateControlType(int type);

    public void ipcPreviewStateChanged(int state, String info) {

    }

    public native void onSizeChange(int width, int height);

    public native void takePhoto(String destDir, String name, boolean containGPS);

    public native void startRecord(String destDir, String cacheDir,
	    String name, boolean containGPS);

    public native void stopRecord();

    public native void takePhotoRemote(boolean containGPS);

    public native void startRecordRemote(boolean containGPS);

    public native void stopRecordRemote();

    public void ipcRecordVideoStateChanged(int state, String info) {

    }

    // api for getting IPCDevice function state
    public native String[] takeNavDataSnapshot(NavData navData);

    public native void sendMessage2Server(String[] keys, String[] value);

    public void ipcFunctionStateChanged(NavData newNavData) {

    }

    // api for getting/setting IPCDevice configuration
    public native VIConfig takeConfigSnapshot(VIConfig config);

    public native String getConfigItem(String configName);

    public native void setConfigItem(String configName, String value);

    public native void resetConfig();

    public void ipcConfigChanged(String configName, String value, String info) {

    }

    public void sendMsgByJni(int cmd, String msg) {
	DebugHandler.logd(TAG, "sendMsgByJni: " + cmd);
	Message msg2 = new Message();
	msg2.what = cmd;
	msg2.obj = msg;
	mHandler.sendMessage(msg2);
    }

    private class CallbackHandler extends Handler {
	@Override
	public void handleMessage(Message msg) {
	    switch (msg.what) {
	    case MESSAGE_DECODEMODE_CHANGED: {
		Intent intent = new Intent(ACTION_DECODEMODE_CHANGED);
		intent.putExtra(EXTRA_DECODE_MODE, (String) msg.obj);
		mLocalBroadcastManager.sendBroadcast(intent);
		DebugHandler.logd(TAG, "send broadcast: " + MESSAGE_DECODEMODE_CHANGED);
		break;
	    }
	    case MESSAGE_RECORD_COMPLETED: {
		int success = (Integer) msg.obj;
		onRecordComplete(success == 0 ? false : true);
		break;
	    }
	    case MESSAGE_CONNECT_QUALITY_CHANGEBAD:{
		Intent intent = new Intent(ACTION_CONNECT_QUALITY_CHANGED);
		intent.putExtra(EXTRA_CONNECT_QUALITY, -1);
		mLocalBroadcastManager.sendBroadcast(intent);
		break;
	    }
	    case MESSAGE_CONNECT_QUALITY_CHANGEGOOD:{
		Intent intent = new Intent(ACTION_CONNECT_QUALITY_CHANGED);
		intent.putExtra(EXTRA_CONNECT_QUALITY, 1);
		mLocalBroadcastManager.sendBroadcast(intent);
		break;
	    }
	    case MESSAGE_RESOLUTION_CHANGED:{
		VmcConfig.getInstance().setResolution((String) msg.obj);
		Intent intent = new Intent(ACTION_RESOLUTION_CHANGED);
		intent.putExtra(EXTRA_RESOLUTION, (String) msg.obj);
		mLocalBroadcastManager.sendBroadcast(intent);
		break;
	    }
	    case MESSAGE_REFRESH_DEBUG:{
		Intent intent = new Intent(ACTION_REFRESH_DEBUG);
		intent.putExtra(EXTRA_DEBUG_INFO, (String) msg.obj);
		mLocalBroadcastManager.sendBroadcast(intent);
		break;
	    }
	    }

	}

    };

}
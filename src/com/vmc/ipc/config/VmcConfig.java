package com.vmc.ipc.config;


import com.vmc.ipc.util.DebugHandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class VmcConfig {
    public final static String TAG = "VmcConfig";
    
    public final static boolean DEFAULT_ISSTOREREMOTE = false;
    public final static String DEFAULT_RESOLUTION = "1280,720";
    public final static int DEFAULT_DECODEMODE = 1;
    public final static boolean DEFAULT_AUTOCONNECT2AVAILABLEAP = false;
    public final static String DEFAULT_LASTAVAILABLEIPCAP = "";
    
    private String isStoreRemote_key = "isStoreRemote";
    private String resolution_key = "resolution";
    private String decodeMode_key = "decodeMode";
    private String autoConnect2AvailableAp_key = "autoConnect2AvailableAp"; 
    private String lastAvailableIpcAp_key = "lastAvailableIpcAp";
    
    private boolean isStoreRemote = false;
    private String resolution = null;
    private int decodeMode = -1;
    private boolean autoConnect2AvailableAp = false; 
    private String lastAvailableIpcAp = null;
    
    ConfigStoreHandler mConfigStoreHandler;
    
    public void setConfigStoreHandler(ConfigStoreHandler handler) {
	mConfigStoreHandler = handler;
    }
    
    public void resetConfig() {
	if(mConfigStoreHandler != null) {
	    mConfigStoreHandler.resetConfig();
	}
	else {
	    DebugHandler.logd(TAG, "you have no mConfigStoreHandler");
	}
    }
    
    public void resetPreviewConfig() {
	if(mConfigStoreHandler != null) {
	    setDecodeMode(DEFAULT_DECODEMODE);
	}
	else {
	    DebugHandler.logd(TAG, "you have no mConfigStoreHandler");
	}
    }
    
    public boolean isAutoConnect2AvailableAp() {
	boolean value = mConfigStoreHandler.getConfig(autoConnect2AvailableAp_key, DEFAULT_AUTOCONNECT2AVAILABLEAP);
        return value;
    }

    public void setAutoConnect2AvailableAp(boolean autoConnect2AvailableAp) {
        this.autoConnect2AvailableAp = autoConnect2AvailableAp;
        mConfigStoreHandler.putConfigBoolean(autoConnect2AvailableAp_key,autoConnect2AvailableAp);
    }

    public String getLastAvailableIpcAp() {
	String value = mConfigStoreHandler.getConfig(lastAvailableIpcAp_key, DEFAULT_LASTAVAILABLEIPCAP);
        return value;
    }

    public void setLastAvailableIpcAp(String lastAvailableIpcAp) {
        this.lastAvailableIpcAp = lastAvailableIpcAp;
        mConfigStoreHandler.putConfigString(lastAvailableIpcAp_key,lastAvailableIpcAp);
    }
    
//    public boolean isStoreRemote() {
//	boolean value = mConfigStoreHandler.getConfig(isStoreRemote_key, DEFAULT_ISSTOREREMOTE);
//        return value;
//    }
//
//    public void setStoreRemote(boolean isStoreRemote) {
//        this.isStoreRemote = isStoreRemote;
//        mConfigStoreHandler.putConfigBoolean(isStoreRemote_key,isStoreRemote);
//    }

//    public String getResolution() {
//	String value = mConfigStoreHandler.getConfig(resolution_key, DEFAULT_RESOLUTION);
//        return value;
//    }
//
//    public void setResolution(String resolution) {
//	if(resolution == null || resolution.indexOf(",") == -1) {
//	    resolution = null;
//	}
//        this.resolution = resolution;
//        mConfigStoreHandler.putConfigString(resolution_key, resolution);
//    }

//    public int getDecodeMode() {
//	int value = mConfigStoreHandler.getConfig(decodeMode_key, DEFAULT_DECODEMODE);
//        return value;
//    }
//
//    public void setDecodeMode(int decodeMode) {
//        this.decodeMode = decodeMode;
//        mConfigStoreHandler.putConfigInt(decodeMode_key,decodeMode);
//    }
    
    public native boolean isStoreRemote();

    public native void setStoreRemote(boolean isStoreRemote);

    public native String getResolution();

    public native void setResolution(String resolution);

    public native int getDecodeMode();

    public native void setDecodeMode(int decodeMode);

    public native int getBitrateControl();

    public native void setBitrateControl(int mode);
    
    public native void initNativeConfig(String dir);

    private static VmcConfig instance = null;
    
    private VmcConfig() {
	isStoreRemote = false;
	resolution = "1280,720";
	decodeMode = 1;
    }
    
    public static VmcConfig getInstance() {
	if(instance == null) {
	    instance = new VmcConfig();
	}
	return instance;
    }
}

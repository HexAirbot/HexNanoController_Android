package com.vmc.ipc.service;

import java.util.ArrayList;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import com.hexairbot.hexmini.R;
import com.vmc.ipc.proxy.IpcProxy;
import com.vmc.ipc.util.DebugHandler;
import com.vmc.ipc.ftp.FtpManager;

public class ConnectStateManager {

    private final static String TAG = "ConnectStateManager";

    public final static int CONNECTED = 1;
    public final static int DISCONNECTED = 0;
    public final static int PAUSED = 2;
    public final static int CONNECTING = 3;

    public final static String ACTION_CONNECT_STATE_CHANGED = "com.vmc.ipc.service.ConnectStateManager.stateChanged";
    public final static String EXTRA_STATE = "connect_state";
    public final static String EXTRA_INFO = "connect_changed_info";

    private Application appContext = null;
    private int currentState = 0;
    private ConnectStateChangedReceiver mConnectStateChangedReceiver = new ConnectStateChangedReceiver();
    private IpcProxy proxy = null;
    private int failCnt = 0;
    private boolean selectApSetting = false;

    private ArrayList<OnIpcConnectChangedListener> connectChangedListeners = new ArrayList<OnIpcConnectChangedListener>();
    private LocalBroadcastManager mLocalBroadcastManager;
    /**
     * 杩欓噷浣跨敤鍗曚緥妯″紡锛屽彧鍏佽锟�锟斤拷杩炴帴绠＄悊鍣ㄥ瓨锟�     */
    private static ConnectStateManager instance = null;

    private ConnectStateManager(Application app) {
	appContext = app;
	proxy = new IpcProxy(app);
	mLocalBroadcastManager = LocalBroadcastManager.getInstance(app);
    }

    public static ConnectStateManager getInstance(Application app) {
	if (instance == null) {
	    instance = new ConnectStateManager(app);
	}
	return instance;
    }

    public void init() {
	IntentFilter filter = new IntentFilter();
	filter.addAction(ACTION_CONNECT_STATE_CHANGED);
	mLocalBroadcastManager.registerReceiver(mConnectStateChangedReceiver,
		filter);
	// proxy.connect();
    }

    public void destroy() {
	proxy.disconnect();
	mLocalBroadcastManager.unregisterReceiver(mConnectStateChangedReceiver);
	FtpManager.getInstance().destroy();
    }

    public IpcProxy getIpcProxy() {
	return proxy;
    }

    private class ConnectStateChangedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
	    // TODO Auto-generated method stub
	    int newState = intent.getIntExtra(EXTRA_STATE, 0);
	    String info = intent.getStringExtra(EXTRA_INFO);
	    setState(newState, info);
	}
    }

    public void addConnectChangedListener(OnIpcConnectChangedListener lis) {
	synchronized (connectChangedListeners) {
	    connectChangedListeners.add(lis);
	}
    }

    public void removeConnectChangedListener(OnIpcConnectChangedListener lis) {
	synchronized (connectChangedListeners) {
	    connectChangedListeners.remove(lis);
	}
    }

    public int getState() {
	return currentState;
    }

    public void connect(String address) {
	if (currentState == CONNECTING || currentState == CONNECTED) {
	    DebugHandler.logd(TAG, "you are connect repeatedly.");
	}
	proxy.connect(address);
	currentState = CONNECTING;
    }

    public void pause() {
	setState(PAUSED, "paused by user");
	proxy.pause();
    }

    public void resume() {
	setState(CONNECTED, "resumed by user");
	proxy.resume();
    }

    public void disconnect() {
	proxy.disconnect();
    }

    private void setState(int newState, String info) {
	DebugHandler.logInsist(TAG, String.format(
		"connect state changed from %1$d to %2$d,because %3$s",
		currentState, newState, info));
	if (currentState == newState){
	    if(currentState == DISCONNECTED) {
		onDisConnected();
	    }else {
		return;
	    }
	}
	else {
	    if (newState == CONNECTED) {
		if (currentState == PAUSED) {
		    onResumed();
		} else {
		    onConnected();
		}
	    } else if (newState == DISCONNECTED) {
		onDisConnected();
	    } else if (newState == PAUSED) {
		onPaused();
	    }
	    currentState = newState;
	}
    }

    private void onConnected() {
	synchronized (connectChangedListeners) {
	    if (connectChangedListeners.size() == 0)
		return;
	    for (OnIpcConnectChangedListener lis : connectChangedListeners) {
		lis.OnIpcConnected();
	    }
	    failCnt = 0;
	}
	if(FtpManager.getInstance().getState() == FtpManager.STATE_DISCONNETED) {
	    FtpManager.getInstance().init();
	}
    }

    private void onDisConnected() {
	synchronized (connectChangedListeners) {
	    if (connectChangedListeners.size() == 0)
		return;
	    for (OnIpcConnectChangedListener lis : connectChangedListeners) {
		lis.OnIpcDisConnected();
	    }
	    
	    if(failCnt > 5 && !selectApSetting) {
		/*
	    DebugHandler.logWithToast(appContext, appContext.getResources().getString(R.string.select_ap_to_connect), 3000);
		Intent apintent = new Intent();
		apintent.setAction(ApConnectService.ACTION_CONNECT_MANUALLY);
		apintent.setClass(appContext, ApConnectService.class);
		appContext.startService(apintent);
		*/
		selectApSetting = true;
		failCnt = 0;
	    }
	    else{
	    	if(!selectApSetting)
	    		failCnt++;
	    }
	}
    }

    private void onPaused() {
	synchronized (connectChangedListeners) {
	    if (connectChangedListeners.size() == 0)
		return;
	    for (OnIpcConnectChangedListener lis : connectChangedListeners) {
		lis.onIpcPaused();
	    }
	}
    }

    private void onResumed() {
	synchronized (connectChangedListeners) {
	    if (connectChangedListeners.size() == 0)
		return;
	    for (OnIpcConnectChangedListener lis : connectChangedListeners) {
		lis.onIpcResumed();
	    }
	}
    }
}

package com.hexairbot.hexmini.services;

import java.util.ArrayList;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.hexairbot.hexmini.services.IpcProxy;
import com.hexairbot.hexmini.util.DebugHandler;

public class ConnectStateManager {

    static {
	System.loadLibrary("vmcipc");
    }

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

    private ArrayList<OnIpcConnectChangedListener> connectChangedListeners = new ArrayList<OnIpcConnectChangedListener>();
    private LocalBroadcastManager mLocalBroadcastManager;
    /**
     * è¿??ä½¿ç????æ¨¡å?ï¼?????ä¸?¸ªè¿??ç®¡ç??¨å???     */
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
	if (currentState == newState)
	    return;
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
	}
    }

    private void onDisConnected() {
	synchronized (connectChangedListeners) {
	    if (connectChangedListeners.size() == 0)
		return;
	    for (OnIpcConnectChangedListener lis : connectChangedListeners) {
		lis.OnIpcDisConnected();
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

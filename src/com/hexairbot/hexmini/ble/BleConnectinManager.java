/**
 * 
 */
package com.hexairbot.hexmini.ble;

import com.hexairbot.hexmini.R.string;

import android.R.integer;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * @author koupoo
 *
 */
public class BleConnectinManager implements BleConnectionDelegate {
	private static final String TAG = BleConnectionDelegate.class.getSimpleName();

	private BleConnection currentConnection;
	private boolean isTryingConnect;
	private Context context;
	private BleConnectinManagerDelegate delegate;
	
	public BleConnectinManager(){
		super();
	}
	
	public BleConnection getCurrentConnection() {
		return currentConnection;
	}
	
	public void connect(BleConnection connection){
		/*
		if (currentConnection == connection) {
			if (currentConnection != null) {
				if (isConnected()) {
					return;
				}
				if (isTryingConnect) {
					return;
				}
				isTryingConnect = true;
				currentConnection.connect();
			}
		}
		else{
			if (currentConnection != null) {
				currentConnection.disconnect();
				currentConnection.releaseSource();
			}
			currentConnection = connection;
			isTryingConnect = true;
			currentConnection.setDelegate(this);
			currentConnection.connect();
		}
		*/
		if (currentConnection != null) {
			currentConnection.disconnect();
			currentConnection.releaseSource();
		}
		currentConnection = connection;
		isTryingConnect = true;
		currentConnection.setDelegate(this);
		currentConnection.connect();
	}
	
	public void disconnect(){
		if (currentConnection != null) {
			//currentConnection.disconnect();
			currentConnection.releaseSource();
			currentConnection = null;
			isTryingConnect = false;
		}
	}
	
	public void sendData(String data){
		if ((currentConnection != null) && currentConnection.isConnected()) {
			currentConnection.sendData(data);
		}
	}
	

	public void sendData(byte[] data){
		if ((currentConnection != null) && currentConnection.isConnected()) {
			currentConnection.sendData(data);
		}
	}
	
	public boolean isConnected(){
		if (currentConnection != null) {
			return currentConnection.isConnected();
		}
		return false;
	}

	public BleConnectinManagerDelegate getDelegate() {
		return delegate;
	}

	public void setDelegate(BleConnectinManagerDelegate delegate) {
		this.delegate = delegate;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	@Override
	public void didConnect(BleConnection connection) {
		if (currentConnection != connection) {
			Log.d(TAG, "didConnect:old connection, just ignore");
		}
		
		if (delegate != null) {
			delegate.didConnect(this, connection);
		}
	}

	@Override
	public void didDisconnect(BleConnection connection) {
		if (currentConnection != connection) {
			Log.d(TAG, "didDisconnect: connection, just ignore");
		}
		
		if (delegate != null) {
			delegate.didDisconnect(this, connection);
		}
	}

	@Override
	public void didReceiveData(BleConnection connection, String data) {
		if (currentConnection != connection) {
			Log.d(TAG, "didReceiveData: connection, just ignore");
		}
		
		if (delegate != null) {
			delegate.didReceiveData(this, connection, data);
		}
	}

	@Override
	public void didFailToConnect(BleConnection connection) {
		if (currentConnection != connection) {
			Log.d(TAG, "didFailToConnect: connection, just ignore");
		}
		
		if (delegate != null) {
			delegate.didFailToConnect(this, connection);
		}
	}
}
